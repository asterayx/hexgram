package com.hexgram.android.models

/**
 * 农历↔公历转换
 * 基于寿星万年历数据，覆盖 1900-2100 年
 */
object LunarCalendar {

    data class LunarDate(
        val year: Int,
        val month: Int,
        val day: Int,
        val isLeapMonth: Boolean = false
    ) {
        val displayString: String
            get() {
                val monthNames = arrayOf("正","二","三","四","五","六","七","八","九","十","冬","腊")
                val dayNames = arrayOf(
                    "初一","初二","初三","初四","初五","初六","初七","初八","初九","初十",
                    "十一","十二","十三","十四","十五","十六","十七","十八","十九","二十",
                    "廿一","廿二","廿三","廿四","廿五","廿六","廿七","廿八","廿九","三十"
                )
                val mStr = (if (isLeapMonth) "闰" else "") + monthNames[month - 1] + "月"
                val dStr = dayNames[day - 1]
                return "农历${year}年${mStr}${dStr}"
            }
    }

    private val lunarInfo = intArrayOf(
        0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2,
        0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0, 0x14977,
        0x04970, 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970,
        0x06566, 0x0d4a0, 0x0ea50, 0x06e95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950,
        0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557,
        0x06ca0, 0x0b550, 0x15355, 0x04da0, 0x0a5b0, 0x14573, 0x052b0, 0x0a9a8, 0x0e950, 0x06aa0,
        0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0,
        0x096d0, 0x04dd5, 0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b6a0, 0x195a6,
        0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50, 0x06d40, 0x0af46, 0x0ab60, 0x09570,
        0x04af5, 0x04970, 0x064b0, 0x074a3, 0x0ea50, 0x06b58, 0x05ac0, 0x0ab60, 0x096d5, 0x092e0,
        0x0c960, 0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0, 0x092d0, 0x0cab5,
        0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9, 0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930,
        0x07954, 0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260, 0x0ea65, 0x0d530,
        0x05aa0, 0x076a3, 0x096d0, 0x04afb, 0x04ad0, 0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45,
        0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0,
        0x14b63, 0x09370, 0x049f8, 0x04970, 0x064b0, 0x168a6, 0x0ea50, 0x06b20, 0x1a6c4, 0x0aae0,
        0x092e0, 0x0d2e3, 0x0c960, 0x0d557, 0x0d4a0, 0x0da50, 0x05d55, 0x056a0, 0x0a6d0, 0x055d4,
        0x052d0, 0x0a9b8, 0x0a950, 0x0b4a0, 0x0b6a6, 0x0ad50, 0x055a0, 0x0aba4, 0x0a5b0, 0x052b0,
        0x0b273, 0x06930, 0x07337, 0x06aa0, 0x0ad50, 0x14b55, 0x04b60, 0x0a570, 0x054e4, 0x0d160,
        0x0e968, 0x0d520, 0x0daa0, 0x16aa6, 0x056d0, 0x04ae0, 0x0a9d4, 0x0a4d0, 0x0d150, 0x0f252,
        0x0d520,
    )

    private const val BASE_YEAR = 1900
    private val BASE_JD = julianDay(1900, 1, 31) // 农历1900正月初一

    private fun julianDay(year: Int, month: Int, day: Int): Int {
        val a = (14 - month) / 12
        val y = year + 4800 - a
        val m = month + 12 * a - 3
        return day + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045
    }

    private fun fromJulianDay(jd: Int): Triple<Int, Int, Int> {
        val a = jd + 32044
        val b = (4 * a + 3) / 146097
        val c = a - (146097 * b) / 4
        val d = (4 * c + 3) / 1461
        val e = c - (1461 * d) / 4
        val m = (5 * e + 2) / 153
        val day = e - (153 * m + 2) / 5 + 1
        val month = m + 3 - 12 * (m / 10)
        val year = 100 * b + d - 4800 + m / 10
        return Triple(year, month, day)
    }

    fun leapMonth(year: Int): Int {
        if (year < BASE_YEAR || year - BASE_YEAR >= lunarInfo.size) return 0
        return (lunarInfo[year - BASE_YEAR] shr 12) and 0xF
    }

    private fun daysInMonth(year: Int, month: Int, isLeap: Boolean): Int {
        if (year < BASE_YEAR || year - BASE_YEAR >= lunarInfo.size) return 29
        if (isLeap) {
            return if ((lunarInfo[year - BASE_YEAR] shr 16) and 1 == 1) 30 else 29
        }
        return if ((lunarInfo[year - BASE_YEAR] shr (month - 1)) and 1 == 1) 30 else 29
    }

    private fun daysInYear(year: Int): Int {
        if (year < BASE_YEAR || year - BASE_YEAR >= lunarInfo.size) return 354
        var total = 0
        val leap = leapMonth(year)
        for (m in 1..12) {
            total += daysInMonth(year, m, false)
            if (m == leap) {
                total += daysInMonth(year, m, true)
            }
        }
        return total
    }

    // 农历→公历
    fun lunarToSolar(year: Int, month: Int, day: Int, isLeapMonth: Boolean = false): Triple<Int, Int, Int>? {
        if (year < BASE_YEAR || year - BASE_YEAR >= lunarInfo.size) return null
        if (month < 1 || month > 12 || day < 1 || day > 30) return null

        val leap = leapMonth(year)
        if (isLeapMonth && leap != month) return null

        val maxDay = daysInMonth(year, month, isLeapMonth)
        if (day > maxDay) return null

        var offset = 0
        for (y in BASE_YEAR until year) {
            offset += daysInYear(y)
        }

        val yearLeap = leapMonth(year)
        for (m in 1 until month) {
            offset += daysInMonth(year, m, false)
            if (m == yearLeap) {
                offset += daysInMonth(year, m, true)
            }
        }

        if (isLeapMonth) {
            offset += daysInMonth(year, month, false)
        }

        offset += day - 1
        return fromJulianDay(BASE_JD + offset)
    }

    // 公历→农历
    fun solarToLunar(year: Int, month: Int, day: Int): LunarDate? {
        val targetJD = julianDay(year, month, day)
        var offset = targetJD - BASE_JD
        if (offset < 0) return null

        var lunarYear = BASE_YEAR
        while (lunarYear - BASE_YEAR < lunarInfo.size) {
            val daysThisYear = daysInYear(lunarYear)
            if (offset < daysThisYear) break
            offset -= daysThisYear
            lunarYear++
        }
        if (lunarYear - BASE_YEAR >= lunarInfo.size) return null

        val yearLeap = leapMonth(lunarYear)
        var lunarMonth = 1
        var isLeap = false

        for (m in 1..12) {
            val days = daysInMonth(lunarYear, m, false)
            if (offset < days) {
                lunarMonth = m
                isLeap = false
                break
            }
            offset -= days

            if (m == yearLeap) {
                val leapDays = daysInMonth(lunarYear, m, true)
                if (offset < leapDays) {
                    lunarMonth = m
                    isLeap = true
                    break
                }
                offset -= leapDays
            }

            if (m == 12) lunarMonth = 12
        }

        return LunarDate(lunarYear, lunarMonth, offset + 1, isLeap)
    }

    fun daysInLunarMonth(year: Int, month: Int, isLeapMonth: Boolean = false): Int {
        return daysInMonth(year, month, isLeapMonth)
    }
}
