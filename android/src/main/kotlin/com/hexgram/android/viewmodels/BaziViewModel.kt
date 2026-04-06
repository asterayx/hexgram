package com.hexgram.android.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.hexgram.shared.BaziEngine
import com.hexgram.shared.BaziResult
import java.util.Calendar

class BaziViewModel : ViewModel() {

    // Input state
    var selectedYear by mutableStateOf(1990)
    var selectedMonth by mutableStateOf(1)
    var selectedDay by mutableStateOf(1)
    var selectedHourIndex by mutableStateOf(0)
    var sex by mutableStateOf("M") // "M" or "F"
    var name by mutableStateOf("")

    // Result state
    var result by mutableStateOf<BaziResult?>(null)
    var resultText by mutableStateOf("")

    // AI state
    var aiText by mutableStateOf("")
    var aiLoading by mutableStateOf(false)

    companion object {
        val CHINESE_HOURS = listOf(
            "子时 (23-01)", "丑时 (01-03)", "寅时 (03-05)", "卯时 (05-07)",
            "辰时 (07-09)", "巳时 (09-11)", "午时 (11-13)", "未时 (13-15)",
            "申时 (15-17)", "酉时 (17-19)", "戌时 (19-21)", "亥时 (21-23)"
        )
        val HOUR_VALUES = listOf(23, 1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21)
    }

    fun calculate() {
        val hourValue = HOUR_VALUES[selectedHourIndex]
        val baziResult = BaziEngine.calculate(
            selectedYear, selectedMonth, selectedDay,
            hourValue, sex, name
        )
        result = baziResult
        resultText = BaziEngine.formatPlainText(baziResult)
    }

    fun reset() {
        result = null
        resultText = ""
        aiText = ""
        selectedYear = 1990
        selectedMonth = 1
        selectedDay = 1
        selectedHourIndex = 0
        sex = "M"
        name = ""
    }

    fun requestAI() {
        aiText = "请在设置中配置API密钥后使用AI解读功能。"
    }
}
