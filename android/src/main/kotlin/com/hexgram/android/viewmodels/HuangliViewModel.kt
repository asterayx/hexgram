package com.hexgram.android.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.hexgram.android.models.HuangliEngine
import com.hexgram.android.models.HuangliResult
import java.util.Calendar

class HuangliViewModel : ViewModel() {

    // Input state
    var selectedYear by mutableStateOf(Calendar.getInstance().get(Calendar.YEAR))
    var selectedMonth by mutableStateOf(Calendar.getInstance().get(Calendar.MONTH) + 1)
    var selectedDay by mutableStateOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH))

    // Result state
    var result by mutableStateOf<HuangliResult?>(null)
    var resultText by mutableStateOf("")

    // AI state
    var aiText by mutableStateOf("")
    var aiLoading by mutableStateOf(false)

    init {
        // Auto-calculate for today
        calculate()
    }

    fun calculate() {
        val huangliResult = HuangliEngine.calculate(selectedYear, selectedMonth, selectedDay)
        result = huangliResult
        resultText = HuangliEngine.formatPlainText(huangliResult)
    }

    fun previousDay() {
        val cal = Calendar.getInstance()
        cal.set(selectedYear, selectedMonth - 1, selectedDay)
        cal.add(Calendar.DAY_OF_MONTH, -1)
        selectedYear = cal.get(Calendar.YEAR)
        selectedMonth = cal.get(Calendar.MONTH) + 1
        selectedDay = cal.get(Calendar.DAY_OF_MONTH)
        calculate()
    }

    fun nextDay() {
        val cal = Calendar.getInstance()
        cal.set(selectedYear, selectedMonth - 1, selectedDay)
        cal.add(Calendar.DAY_OF_MONTH, 1)
        selectedYear = cal.get(Calendar.YEAR)
        selectedMonth = cal.get(Calendar.MONTH) + 1
        selectedDay = cal.get(Calendar.DAY_OF_MONTH)
        calculate()
    }

    fun goToToday() {
        val now = Calendar.getInstance()
        selectedYear = now.get(Calendar.YEAR)
        selectedMonth = now.get(Calendar.MONTH) + 1
        selectedDay = now.get(Calendar.DAY_OF_MONTH)
        calculate()
    }

    fun requestAI() {
        aiText = "请在设置中配置API密钥后使用AI解读功能。"
    }
}
