package com.hexgram.android.models

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

// 数据类
data class ClassicEntry(
    val name: String,
    val judgment: String,
    val yao: List<String>
)

data class HuangjinceEntry(
    val category: String,
    val label: String,
    val text: String
)

data class QuestionCategory(
    val key: String,
    val label: String
)

data class ClassicsResult(
    val gaodao: ClassicEntry?,
    val huangjince: HuangjinceEntry?,
    val jiaoshi: String?,
    val categories: List<QuestionCategory>
)

// 默认事类（在网络不可用时使用）
val DEFAULT_CATEGORIES = listOf(
    QuestionCategory("_总论", "综合"),
    QuestionCategory("求财", "求财"),
    QuestionCategory("事业", "事业"),
    QuestionCategory("感情", "感情"),
    QuestionCategory("婚姻", "婚姻"),
    QuestionCategory("考试", "考试"),
    QuestionCategory("家宅", "家宅"),
    QuestionCategory("疾病", "疾病"),
    QuestionCategory("出行", "出行"),
    QuestionCategory("诉讼", "诉讼"),
    QuestionCategory("失物", "失物"),
    QuestionCategory("天气", "天气"),
    QuestionCategory("怀孕", "怀孕"),
    QuestionCategory("投资", "投资"),
    QuestionCategory("求职", "求职"),
    QuestionCategory("生意", "生意"),
)

/**
 * 经典文献服务 - 通过 Worker /api/classics 查询
 * Worker 先查 D1 缓存，缓存未命中则调用 LLM 生成并存入 D1
 */
object ClassicsService {

    suspend fun query(
        context: Context,
        guaKey: String,
        guaName: String = "",
        changedGuaKey: String? = null,
        changedGuaName: String? = null,
        category: String? = null
    ): ClassicsResult = withContext(Dispatchers.IO) {
        val endpoint = AIService.getEndpoint(context)
        if (endpoint.isBlank()) {
            return@withContext ClassicsResult(null, null, null, DEFAULT_CATEGORIES)
        }

        // 构建 /api/classics URL
        val baseUrl = endpoint.trimEnd('/')
        val classicsUrl = if (baseUrl.endsWith("/api/classics")) baseUrl
            else baseUrl.replace(Regex("/+$"), "").let {
                // 如果 endpoint 是 "https://xxx.workers.dev" 或 "https://xxx.workers.dev/"
                val base = it.removeSuffix("/")
                "$base/api/classics"
            }

        val conn = URL(classicsUrl).openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.connectTimeout = 15_000
            conn.readTimeout = 60_000
            conn.doOutput = true

            val body = JSONObject().apply {
                put("guaKey", guaKey)
                put("guaName", guaName)
                if (changedGuaKey != null) put("changedGuaKey", changedGuaKey)
                if (changedGuaName != null) put("changedGuaName", changedGuaName)
                if (category != null) put("category", category)
            }

            OutputStreamWriter(conn.outputStream, "UTF-8").use { it.write(body.toString()) }

            val code = conn.responseCode
            val text = if (code in 200..299) {
                conn.inputStream.bufferedReader().use { it.readText() }
            } else {
                return@withContext ClassicsResult(null, null, null, DEFAULT_CATEGORIES)
            }

            parseClassicsResponse(JSONObject(text))
        } catch (e: Exception) {
            ClassicsResult(null, null, null, DEFAULT_CATEGORIES)
        } finally {
            conn.disconnect()
        }
    }

    private fun parseClassicsResponse(json: JSONObject): ClassicsResult {
        // gaodao
        val gaodao = json.optJSONObject("gaodao")?.let { g ->
            val yaoArr = g.optJSONArray("yao")
            val yao = if (yaoArr != null) {
                (0 until yaoArr.length()).map { yaoArr.optString(it, "") }
            } else emptyList()
            ClassicEntry(
                name = g.optString("name", ""),
                judgment = g.optString("judgment", ""),
                yao = yao
            )
        }

        // huangjince
        val huangjince = json.optJSONObject("huangjince")?.let { h ->
            HuangjinceEntry(
                category = h.optString("category", ""),
                label = h.optString("label", ""),
                text = h.optString("text", "")
            )
        }

        // jiaoshi
        val jiaoshi = json.optString("jiaoshi", "").ifBlank { null }

        // categories
        val catArr = json.optJSONArray("categories")
        val categories = if (catArr != null && catArr.length() > 0) {
            (0 until catArr.length()).map { i ->
                val c = catArr.getJSONObject(i)
                QuestionCategory(c.optString("key"), c.optString("label"))
            }
        } else DEFAULT_CATEGORIES

        return ClassicsResult(gaodao, huangjince, jiaoshi, categories)
    }
}
