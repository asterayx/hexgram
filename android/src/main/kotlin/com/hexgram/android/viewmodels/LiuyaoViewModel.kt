package com.hexgram.android.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.hexgram.shared.Classics
import com.hexgram.shared.GuaResult
import com.hexgram.shared.NajiaEngine
import com.hexgram.shared.CalendarCalc
import com.hexgram.shared.YAO_NAMES
import java.util.Calendar

class LiuyaoViewModel : ViewModel() {

    var question by mutableStateOf("")
    var selectedYear by mutableStateOf(Calendar.getInstance().get(Calendar.YEAR))
    var selectedMonth by mutableStateOf(Calendar.getInstance().get(Calendar.MONTH) + 1)
    var selectedDay by mutableStateOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
    var selectedHourIndex by mutableStateOf(currentChineseHourIndex())

    val lines = mutableStateListOf<Int>()
    var isTossing by mutableStateOf(false)
    var phase by mutableStateOf(LiuyaoPhase.INPUT)

    var guaResult by mutableStateOf<GuaResult?>(null)
    var resultText by mutableStateOf("")
    var aiText by mutableStateOf("")
    var aiLoading by mutableStateOf(false)

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
            aiText = ""
        }
    }

    fun reset() {
        lines.clear()
        phase = LiuyaoPhase.INPUT
        guaResult = null
        resultText = ""
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

        // Append classics
        val gaodao = Classics.GAODAO[result.guaKey]
        if (gaodao != null) {
            resultText += "\n## 高岛易断\n\n"
            resultText += "**【卦断】** ${gaodao.judgment}\n\n"
            if (gaodao.yao.isNotEmpty()) {
                resultText += "**【爻断】**\n"
                for ((i, line) in lines.withIndex()) {
                    val marker = if (line == 6 || line == 9) " ★" else ""
                    if (i < gaodao.yao.size) {
                        resultText += "${YAO_NAMES[i]}爻${marker}：${gaodao.yao[i]}\n"
                    }
                }
            }
        }

        // 黄金策
        var hjKeys = mutableListOf<String>()
        val q = question
        if (q.any { "财钱利润业绩收入投资".contains(it) }) hjKeys.add("求财")
        if (q.any { "工作事业升职官".contains(it) }) hjKeys.add("事业")
        if (q.any { "婚恋感情对象桃花".contains(it) }) hjKeys.add("婚姻")
        if (q.any { "病健康身体医".contains(it) }) hjKeys.add("疾病")
        if (q.any { "出行旅行程出差".contains(it) }) hjKeys.add("出行")
        if (q.any { "诉官司纠纷法".contains(it) }) hjKeys.add("诉讼")
        if (hjKeys.isEmpty()) hjKeys.add("求财")

        resultText += "\n## 黄金策断语\n\n"
        for (key in hjKeys) {
            Classics.HUANGJINCE[key]?.let { text ->
                resultText += "**【${key}】**\n${text}\n\n"
            }
        }
    }

    fun requestAI() {
        aiText = "请在设置中配置API密钥后使用AI解读功能。"
    }
}

enum class LiuyaoPhase {
    INPUT, TOSSING, DONE
}
