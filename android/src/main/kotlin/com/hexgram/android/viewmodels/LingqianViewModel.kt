package com.hexgram.android.viewmodels

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hexgram.android.models.AIService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

private const val TAG = "LingqianVM"

data class LingqianCategory(val key: String, val label: String)

val LINGQIAN_CATEGORIES = listOf(
    LingqianCategory("综合", "综合运势"),
    LingqianCategory("家宅", "家宅运势"),
    LingqianCategory("生意", "生意经营"),
    LingqianCategory("谋望", "谋望求财"),
    LingqianCategory("婚姻", "婚姻感情"),
    LingqianCategory("功名", "学艺功名"),
    LingqianCategory("出外", "出行外出"),
    LingqianCategory("官讼", "官讼是非"),
    LingqianCategory("占病", "健康疾病"),
    LingqianCategory("失物", "失物寻找"),
    LingqianCategory("行人", "行人音讯"),
)

data class LingqianResult(
    val qianNum: Int,
    val qianName: String,
    val qianType: String,
    val guaXiang: String,
    val shengXiao: String,
    val xiWen: String,
    val shiYue: String,
    val neiZhao: String,
    val suijunZongShi: String = "",
    val suijunAgeRange: String = "",
    val suijunFortune: String = "",
)

class LingqianViewModel(application: Application) : AndroidViewModel(application) {

    enum class Phase { INPUT, SHAKING, RESULT }

    var phase by mutableStateOf(Phase.INPUT)
    var question by mutableStateOf("")
    var selectedCategoryIndex by mutableIntStateOf(0)
    var ageText by mutableStateOf("")
    var selectedGender by mutableIntStateOf(0)  // 0=男, 1=女
    var qianResult by mutableStateOf<LingqianResult?>(null)
    var resultText by mutableStateOf("")
    var detailText by mutableStateOf("")
    var isShaking by mutableStateOf(false)
    var shakeProgress by mutableStateOf(0f)

    // AI state
    var aiText by mutableStateOf("")
    var aiLoading by mutableStateOf(false)
    var aiError by mutableStateOf("")

    private val selectedCategoryKey: String
        get() = LINGQIAN_CATEGORIES.getOrNull(selectedCategoryIndex)?.key ?: "综合"

    // MARK: - 摇签

    fun shake() {
        if (phase != Phase.INPUT) return
        isShaking = true
        phase = Phase.SHAKING
        shakeProgress = 0f

        viewModelScope.launch {
            // 模拟摇签动画
            for (i in 1..15) {
                delay(100)
                shakeProgress = i / 15f
            }
            val num = (1..51).random()
            isShaking = false
            phase = Phase.RESULT
            fetchQian(num)
        }
    }

    // MARK: - 查询签文

    private fun fetchQian(num: Int) {
        val endpoint = AIService.getEndpoint(getApplication())
        if (endpoint.isBlank()) {
            qianResult = LingqianResult(num, "第${num}签", "", "", "", "", "", "")
            resultText = "## 第${num}签\n\n⚠ 未配置服务器，无法获取签文"
            return
        }

        viewModelScope.launch {
            try {
                val baseUrl = endpoint.trimEnd('/')
                val classicsUrl = "$baseUrl/api/lingqian"

                val response = withContext(Dispatchers.IO) {
                    val conn = URL(classicsUrl).openConnection() as HttpURLConnection
                    conn.requestMethod = "POST"
                    conn.setRequestProperty("Content-Type", "application/json")
                    conn.connectTimeout = 15_000
                    conn.readTimeout = 30_000
                    conn.doOutput = true

                    val body = JSONObject().apply {
                        put("qianNum", num)
                        put("category", selectedCategoryKey)
                        put("question", question)
                        put("gender", if (selectedGender == 0) "男" else "女")
                        val age = ageText.toIntOrNull()
                        if (age != null && age > 0) put("age", age)
                    }
                    OutputStreamWriter(conn.outputStream, "UTF-8").use { it.write(body.toString()) }

                    val code = conn.responseCode
                    if (code in 200..299) {
                        conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                    } else {
                        throw Exception("服务器错误 ($code)")
                    }
                }

                val json = JSONObject(response)
                Log.d(TAG, "Response keys: ${json.keys().asSequence().toList()}")
                val qian = json.optJSONObject("qian")
                if (qian != null) {
                    val suijun = json.optJSONObject("suijun")
                    Log.d(TAG, "suijun object: $suijun")
                    Log.d(TAG, "suijun zongShi: ${suijun?.optString("zongShi", "EMPTY")}")
                    qianResult = LingqianResult(
                        qianNum = qian.optInt("qianNum", num),
                        qianName = qian.optString("qianName", ""),
                        qianType = qian.optString("qianType", ""),
                        guaXiang = qian.optString("guaXiang", ""),
                        shengXiao = qian.optString("shengXiao", ""),
                        xiWen = qian.optString("xiWen", ""),
                        shiYue = qian.optString("shiYue", ""),
                        neiZhao = qian.optString("neiZhao", ""),
                        suijunZongShi = suijun?.optString("zongShi", "") ?: "",
                        suijunAgeRange = suijun?.optString("ageRange", "") ?: "",
                        suijunFortune = suijun?.optString("fortune", "") ?: "",
                    )
                    resultText = formatResult(qianResult!!)
                    Log.d(TAG, "resultText contains 岁君: ${resultText.contains("岁君")}")
                    detailText = formatDetail(json.optJSONObject("detail"))
                } else {
                    resultText = "## 第${num}签\n\n获取签文失败"
                }
            } catch (e: Exception) {
                resultText = "## 第${num}签\n\n查询失败：${e.message}"
            }
        }
    }

    // MARK: - 格式化

    private fun formatResult(r: LingqianResult): String {
        val sb = StringBuilder()
        sb.append("## 第${r.qianNum}签 · ${r.qianName}\n\n")
        if (r.guaXiang.isNotEmpty()) sb.append("**${r.guaXiang}** · ${r.qianType}\n\n")
        if (r.suijunZongShi.isNotEmpty()) sb.append("### 岁君总诗\n${r.suijunZongShi}\n\n")
        if (r.suijunFortune.isNotEmpty()) {
            val genderLabel = if (selectedGender == 0) "男" else "女"
            sb.append("### 流年运势（${genderLabel}·${r.suijunAgeRange}）\n${r.suijunFortune}\n\n")
        }
        if (r.shiYue.isNotEmpty()) sb.append("### 诗曰\n${r.shiYue}\n\n")
        if (r.neiZhao.isNotEmpty()) sb.append("**内兆**：${r.neiZhao}\n\n")
        if (r.xiWen.isNotEmpty()) sb.append("### 典故\n${r.xiWen}\n\n")
        return sb.toString()
    }

    private fun formatDetail(detail: JSONObject?): String {
        if (detail == null || detail.length() == 0) return ""
        val sb = StringBuilder("---\n### 分类详解\n\n")
        for (key in detail.keys()) {
            val value = detail.get(key)
            if (value is JSONObject) {
                sb.append("**$key**\n")
                for (k in value.keys()) {
                    val v = value.optString(k, "")
                    if (v.isNotEmpty()) sb.append("· $k：$v\n")
                }
                sb.append("\n")
            } else {
                val str = value.toString()
                if (str.isNotEmpty()) sb.append("**$key**：$str\n\n")
            }
        }
        return sb.toString()
    }

    // MARK: - AI 解读

    fun requestAI() {
        if (resultText.isBlank()) return
        val endpoint = AIService.getEndpoint(getApplication())
        if (endpoint.isBlank()) {
            aiError = "未配置Worker地址"
            return
        }
        aiLoading = true
        aiText = ""
        aiError = ""

        val fullData = resultText + detailText +
            "\n\n所求事类：${LINGQIAN_CATEGORIES[selectedCategoryIndex].label}" +
            if (question.isNotEmpty()) "\n用户所问：$question" else ""

        viewModelScope.launch {
            try {
                aiText = AIService.callWorker(
                    endpoint = endpoint,
                    type = "lingqian",
                    data = fullData,
                    question = question
                )
            } catch (e: Exception) {
                aiError = "解签失败：${e.message}"
            } finally {
                aiLoading = false
            }
        }
    }

    // MARK: - 重置

    fun reset() {
        phase = Phase.INPUT
        qianResult = null
        resultText = ""
        detailText = ""
        shakeProgress = 0f
        isShaking = false
        aiText = ""
        aiError = ""
        aiLoading = false
    }
}
