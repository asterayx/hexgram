package com.hexgram.android.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.hexgram.shared.Classics
import com.hexgram.shared.GuaResult
import com.hexgram.shared.NajiaEngine
import java.util.Calendar

class LiuyaoViewModel : ViewModel() {

    // Input state
    var question by mutableStateOf("")
    var selectedYear by mutableStateOf(Calendar.getInstance().get(Calendar.YEAR))
    var selectedMonth by mutableStateOf(Calendar.getInstance().get(Calendar.MONTH) + 1)
    var selectedDay by mutableStateOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
    var selectedHourIndex by mutableStateOf(currentChineseHourIndex())

    // Tossing state
    val lines = mutableStateListOf<Int>()
    var isTossing by mutableStateOf(false)
    var phase by mutableStateOf(LiuyaoPhase.INPUT) // INPUT, TOSSING, DONE

    // Result state
    var guaResult by mutableStateOf<GuaResult?>(null)
    var resultText by mutableStateOf("")
    var classicsText by mutableStateOf("")

    // AI state
    var aiText by mutableStateOf("")
    var aiLoading by mutableStateOf(false)

    companion object {
        val CHINESE_HOURS = listOf(
            "子时 (23-01)", "丑时 (01-03)", "寅时 (03-05)", "卯时 (05-07)",
            "辰时 (07-09)", "巳时 (09-11)", "午时 (11-13)", "未时 (13-15)",
            "申时 (15-17)", "酉时 (17-19)", "戌时 (19-21)", "亥时 (21-23)"
        )

        val HOUR_VALUES = listOf(0, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22)

        fun currentChineseHourIndex(): Int {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            return when {
                hour == 23 || hour == 0 -> 0
                else -> ((hour + 1) / 2) % 12
            }
        }
    }

    fun toss() {
        if (lines.size >= 6) return
        isTossing = true
        phase = LiuyaoPhase.TOSSING

        // Simulate three coins: each coin is 2 (yin) or 3 (yang)
        val coin1 = if (Math.random() < 0.5) 2 else 3
        val coin2 = if (Math.random() < 0.5) 2 else 3
        val coin3 = if (Math.random() < 0.5) 2 else 3
        val value = coin1 + coin2 + coin3 // 6, 7, 8, or 9

        lines.add(value)
        isTossing = false

        if (lines.size >= 6) {
            phase = LiuyaoPhase.DONE
        }
    }

    fun undo() {
        if (lines.isNotEmpty()) {
            lines.removeAt(lines.size - 1)
            if (lines.isEmpty()) {
                phase = LiuyaoPhase.INPUT
            } else {
                phase = LiuyaoPhase.TOSSING
            }
            // Clear result if we undo
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

        val linesArray = lines.toIntArray()

        // Determine riGan, riZhi, yueZhi from the selected date
        val riGZ = com.hexgram.shared.CalendarCalc.riGanZhi(selectedYear, selectedMonth, selectedDay)
        val riGan = riGZ.first
        val riZhi = riGZ.second
        val yueZhi = com.hexgram.shared.CalendarCalc.yueZhi(selectedYear, selectedMonth, selectedDay)

        val result = NajiaEngine.zhuangGua(linesArray, riGan, riZhi, yueZhi)
        guaResult = result
        resultText = NajiaEngine.formatGuaText(result)

        // Look up classics
        classicsText = buildClassicsText(result)
    }

    private fun buildClassicsText(result: GuaResult): String {
        val sb = StringBuilder()

        // Gua code for lookup (binary string from bottom to top)
        val benGuaCode = result.yaos.joinToString("") { if (it.isYang) "1" else "0" }

        // Look up Gaodao (高岛易断)
        val gaodao = Classics.GAODAO[benGuaCode]
        if (gaodao != null) {
            sb.appendLine("【高岛易断】${gaodao.name}")
            sb.appendLine()
            if (gaodao.judgment.isNotBlank()) {
                sb.appendLine("卦断：${gaodao.judgment}")
                sb.appendLine()
            }
            // Find moving yaos and show their yao text
            result.yaos.forEachIndexed { index, yao ->
                if (yao.isChanging && index < gaodao.yao.size) {
                    val yaoText = gaodao.yao[index]
                    if (yaoText.isNotBlank()) {
                        val yaoName = when (index) {
                            0 -> "初爻"
                            1 -> "二爻"
                            2 -> "三爻"
                            3 -> "四爻"
                            4 -> "五爻"
                            5 -> "上爻"
                            else -> "第${index + 1}爻"
                        }
                        sb.appendLine("${yaoName}动：$yaoText")
                        sb.appendLine()
                    }
                }
            }
        }

        // Look up Jiaoshi Yilin (焦氏易林)
        if (result.bianGuaCode.isNotBlank()) {
            val jiaoshi = Classics.JIAOSHI[benGuaCode]?.get(result.bianGuaCode)
            if (jiaoshi != null && jiaoshi.isNotBlank()) {
                sb.appendLine("【焦氏易林】")
                sb.appendLine(jiaoshi)
                sb.appendLine()
            }
        }

        // Look up Huangjince (黄金策) based on question keywords
        val hjc = matchHuangjince(question)
        if (hjc.isNotBlank()) {
            sb.appendLine("【黄金策】")
            sb.appendLine(hjc)
        }

        return sb.toString()
    }

    private fun matchHuangjince(question: String): String {
        if (question.isBlank()) return ""

        val categoryMap = mapOf(
            "求财" to listOf("财", "钱", "收入", "投资", "生意", "买卖", "利润"),
            "事业" to listOf("事业", "工作", "升职", "跳槽", "公司", "职位", "考试"),
            "婚姻" to listOf("婚", "恋", "爱", "感情", "对象", "夫妻", "姻缘"),
            "疾病" to listOf("病", "健康", "身体", "医", "治疗"),
            "出行" to listOf("出行", "旅行", "出差", "远行", "出国"),
            "诉讼" to listOf("诉", "官司", "纠纷", "法", "打官司")
        )

        for ((category, keywords) in categoryMap) {
            for (kw in keywords) {
                if (question.contains(kw)) {
                    return Classics.HUANGJINCE[category] ?: ""
                }
            }
        }

        return Classics.HUANGJINCE["_总论"] ?: ""
    }

    fun requestAI() {
        // Placeholder - in a real implementation, this would call the AI API
        aiText = "请在设置中配置API密钥后使用AI解读功能。"
    }
}

enum class LiuyaoPhase {
    INPUT, TOSSING, DONE
}
