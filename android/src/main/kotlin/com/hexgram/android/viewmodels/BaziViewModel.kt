package com.hexgram.android.viewmodels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hexgram.android.models.AIService
import com.hexgram.android.models.BaziEngine
import com.hexgram.android.models.BaziResult
import kotlinx.coroutines.launch
import java.util.Calendar

class BaziViewModel(application: Application) : AndroidViewModel(application) {

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
        if (resultText.isBlank()) return
        val endpoint = AIService.getEndpoint(getApplication())
        if (endpoint.isBlank()) {
            aiText = "未配置Worker地址。请在设置中配置后端地址。"
            return
        }
        aiLoading = true
        aiText = ""
        viewModelScope.launch {
            try {
                aiText = AIService.callWorker(
                    endpoint = endpoint,
                    type = "bazi",
                    data = resultText
                )
            } catch (e: Exception) {
                aiText = "AI解读失败：${e.message}"
            } finally {
                aiLoading = false
            }
        }
    }
}
