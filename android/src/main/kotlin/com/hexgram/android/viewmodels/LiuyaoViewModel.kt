package com.hexgram.android.viewmodels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hexgram.android.models.AIService
import com.hexgram.android.models.ClassicsService
import com.hexgram.android.models.DEFAULT_CATEGORIES
import com.hexgram.android.models.QuestionCategory
import com.hexgram.android.models.GuaResult
import com.hexgram.android.models.NajiaEngine
import com.hexgram.android.models.CalendarCalc
import com.hexgram.android.models.YAO_NAMES
import kotlinx.coroutines.launch
import java.util.Calendar

class LiuyaoViewModel(application: Application) : AndroidViewModel(application) {

    var question by mutableStateOf("")
    var selectedCategoryIndex by mutableStateOf(0) // 综合
    var selectedYear by mutableStateOf(Calendar.getInstance().get(Calendar.YEAR))
    var selectedMonth by mutableStateOf(Calendar.getInstance().get(Calendar.MONTH) + 1)
    var selectedDay by mutableStateOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
    var selectedHourIndex by mutableStateOf(currentChineseHourIndex())

    val lines = mutableStateListOf<Int>()
    var isTossing by mutableStateOf(false)
    var phase by mutableStateOf(LiuyaoPhase.INPUT)

    var guaResult by mutableStateOf<GuaResult?>(null)
    var resultText by mutableStateOf("")
    var classicsText by mutableStateOf("")
    var classicsLoading by mutableStateOf(false)
    var aiText by mutableStateOf("")
    var aiLoading by mutableStateOf(false)

    // 事类列表（从 Worker 获取，有默认值兜底）
    var categories by mutableStateOf(DEFAULT_CATEGORIES)

    companion object {
        val CHINESE_HOURS = listOf(
            "子时 (23-01)", "丑时 (01-03)", "寅时 (03-05)", "卯时 (05-07)",
            "辰时 (07-09)", "巳时 (09-11)", "午时 (11-13)", "未时 (13-15)",
            "申时 (15-17)", "酉时 (17-19)", "戌时 (19-21)", "亥时 (21-23)"
        )
        val HOUR_VALUES = listOf(23, 1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21)

        fun currentChineseHourIndex(): Int {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            return when {
                hour == 23 || hour == 0 -> 0
                else -> ((hour + 1) / 2) % 12
            }
        }
    }

    fun toss() {
        if (lines.size >= 6 || isTossing) return
        isTossing = true
        phase = LiuyaoPhase.TOSSING

        val coin1 = if (Math.random() < 0.5) 2 else 3
        val coin2 = if (Math.random() < 0.5) 2 else 3
        val coin3 = if (Math.random() < 0.5) 2 else 3
        lines.add(coin1 + coin2 + coin3)
        isTossing = false

        if (lines.size >= 6) phase = LiuyaoPhase.DONE
    }

    fun undo() {
        if (lines.isNotEmpty()) {
            lines.removeAt(lines.size - 1)
            phase = if (lines.isEmpty()) LiuyaoPhase.INPUT else LiuyaoPhase.TOSSING
            guaResult = null
            resultText = ""
            classicsText = ""
            aiText = ""
        }
    }

    fun reset() {
        lines.clear()
        phase = LiuyaoPhase.INPUT
        guaResult = null
        resultText = ""
        classicsText = ""
        aiText = ""
        isTossing = false
    }

    fun doReading() {
        if (lines.size < 6) return

        val riGZ = CalendarCalc.riGanZhi(selectedYear, selectedMonth, selectedDay)
        val yueZhi = CalendarCalc.yueZhi(selectedYear, selectedMonth, selectedDay)

        val result = NajiaEngine.zhuangGua(lines.toList(), riGZ.first, riGZ.second, yueZhi)
        guaResult = result
        resultText = NajiaEngine.formatGuaText(result)

        // 异步查询经典文献
        fetchClassics(result)
    }

    private fun fetchClassics(result: GuaResult) {
        classicsLoading = true
        classicsText = ""
        val catKey = categories.getOrNull(selectedCategoryIndex)?.key ?: "_总论"

        viewModelScope.launch {
            try {
                val classics = ClassicsService.query(
                    context = getApplication(),
                    guaKey = result.guaKey,
                    guaName = result.guaName,
                    changedGuaKey = result.changedGuaKey,
                    changedGuaName = result.changedGuaName,
                    category = catKey
                )

                // 更新类目列表
                if (classics.categories.isNotEmpty()) {
                    categories = classics.categories
                }

                // 拼装经典文本
                val sb = StringBuilder()

                classics.gaodao?.let { g ->
                    sb.append("## 高岛易断 · ${g.name}卦\n\n")
                    sb.append("**【卦断】** ${g.judgment}\n\n")
                    if (g.yao.isNotEmpty()) {
                        sb.append("**【爻断】**\n")
                        for ((i, line) in lines.withIndex()) {
                            val marker = if (line == 6 || line == 9) " ★" else ""
                            if (i < g.yao.size && g.yao[i].isNotBlank()) {
                                sb.append("${YAO_NAMES[i]}爻${marker}：${g.yao[i]}\n")
                            }
                        }
                        sb.append("\n")
                    }
                }

                classics.huangjince?.let { h ->
                    sb.append("## 黄金策 · ${h.label}\n\n")
                    sb.append("${h.text}\n\n")
                }

                classics.jiaoshi?.let { j ->
                    sb.append("## 焦氏易林\n\n")
                    sb.append("**${result.guaName}之${result.changedGuaName ?: ""}**：${j}\n\n")
                }

                classicsText = sb.toString()
            } catch (e: Exception) {
                classicsText = "经典查询失败：${e.message}"
            } finally {
                classicsLoading = false
            }
        }
    }

    fun requestAI() {
        if (resultText.isBlank()) return
        val endpoint = AIService.getEndpoint(getApplication())
        if (endpoint.isBlank()) {
            aiText = "未配置Worker地址。请在设置中配置后端地址。"
            return
        }
        aiLoading = true
        aiText = ""
        // AI 解读时把经典文本也一并发送
        val fullData = resultText + (if (classicsText.isNotBlank()) "\n\n$classicsText" else "")
        viewModelScope.launch {
            try {
                aiText = AIService.callWorker(
                    endpoint = endpoint,
                    type = "liuyao",
                    data = fullData,
                    question = question
                )
            } catch (e: Exception) {
                aiText = "AI解读失败：${e.message}"
            } finally {
                aiLoading = false
            }
        }
    }
}

enum class LiuyaoPhase {
    INPUT, TOSSING, DONE
}
