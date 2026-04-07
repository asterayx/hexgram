package com.hexgram.android.models

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * AI服务 - 统一通过 Cloudflare Worker 代理调用 LLM
 * Worker负责存储提示词和API Key，App仅发送排盘数据
 */
object AIService {

    private const val PREFS_NAME = "hexgram_settings"
    private const val KEY_ENDPOINT = "worker_endpoint"

    fun getEndpoint(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_ENDPOINT, "https://yijing-api.asternos.workers.dev") ?: "https://yijing-api.asternos.workers.dev"
    }

    /**
     * 调用 Worker AI 代理
     * @param endpoint Worker URL
     * @param type "liuyao", "bazi", "huangli"
     * @param data 排盘结果文本
     * @param question 用户问题（可选）
     * @return AI 解读文本
     */
    suspend fun callWorker(
        endpoint: String,
        type: String,
        data: String,
        question: String = ""
    ): String = withContext(Dispatchers.IO) {
        if (endpoint.isBlank()) {
            throw Exception("未配置Worker地址。请在设置中配置后端地址。")
        }

        val url = URL(endpoint)
        val conn = url.openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.connectTimeout = 30_000
            conn.readTimeout = 120_000
            conn.doOutput = true

            val body = JSONObject().apply {
                put("type", type)
                put("data", data)
                put("question", question)
            }

            OutputStreamWriter(conn.outputStream, "UTF-8").use { writer ->
                writer.write(body.toString())
            }

            val responseCode = conn.responseCode
            val responseText = if (responseCode in 200..299) {
                conn.inputStream.bufferedReader().use { it.readText() }
            } else {
                val errorText = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                throw Exception("服务器错误 ($responseCode): ${errorText.take(200)}")
            }

            val json = JSONObject(responseText)
            if (json.has("error")) {
                throw Exception(json.getString("error"))
            }
            json.optString("reading", "解析失败")
        } finally {
            conn.disconnect()
        }
    }
}
