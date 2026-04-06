package com.hexgram.shared

import kotlin.math.PI
import kotlin.math.sin

object CalendarCalc {
    /// 公历转儒略日数
    fun jdn(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) { y -= 1; m += 12 }
        val a = y / 100
        val b = 2 - a + a / 4
        return (365.25 * (y + 4716)).toInt().toDouble() +
               (30.6001 * (m + 1)).toInt().toDouble() +
               day.toDouble() + b.toDouble() - 1524.5
    }

    /// 日柱干支
    fun riGanZhi(year: Int, month: Int, day: Int): Pair<String, String> {
        val j = (jdn(year, month, day) + 0.5).toInt()
        val gIdx = ((j - 1) % 10 + 10) % 10
        val zIdx = ((j + 1) % 12 + 12) % 12
        return GanZhi.tianGan[gIdx] to GanZhi.diZhi[zIdx]
    }

    // MARK: - 精确节气计算 (Jean Meeus 天文算法)

    /// 计算指定年份某节气的精确日期 (返回JDE)
    /// solarTermIndex: 0=小寒, 1=大寒, 2=立春, 3=雨水, ... 23=大寒
    private fun solarTermJDE(year: Int, termIndex: Int): Double {
        val y = year.toDouble() + (termIndex.toDouble() * 15.0 / 360.0)
        val jdY2000 = 2451545.0
        val t = (y - 2000.0) / 1000.0

        val l0 = 280.46646 + 36000.76983 * (y - 2000.0) / 100.0
        val m = 357.52911 + 35999.05029 * (y - 2000.0) / 100.0
        val mRad = m * PI / 180.0

        val c = (1.9146 - 0.004817 * t) * sin(mRad) +
                0.019993 * sin(2 * mRad) +
                0.00029 * sin(3 * mRad)

        val sunLng = (l0 + c) % 360.0

        val target = (termIndex.toDouble() * 15.0 + 285.0) % 360.0
        var diff = target - sunLng
        if (diff > 180) diff -= 360.0
        if (diff < -180) diff += 360.0

        val jd0 = jdY2000 + (y - 2000.0) * 365.25
        return jd0 + diff / 360.0 * 365.25
    }

    /// 将 JDE 转换为公历日期 (年, 月, 日)
    private fun jdeToDate(jd: Double): Triple<Int, Int, Int> {
        val z = (jd + 0.5).toInt()
        val a: Int = if (z < 2299161) {
            z
        } else {
            val alpha = ((z.toDouble() - 1867216.25) / 36524.25).toInt()
            z + 1 + alpha - alpha / 4
        }
        val b = a + 1524
        val c = ((b.toDouble() - 122.1) / 365.25).toInt()
        val d = (365.25 * c.toDouble()).toInt()
        val e = ((b - d).toDouble() / 30.6001).toInt()

        val day = b - d - (30.6001 * e.toDouble()).toInt()
        val month = if (e < 14) e - 1 else e - 13
        val year = if (month > 2) c - 4716 else c - 4715
        return Triple(year, month, day)
    }

    /// 十二节(月首节气)对应的节气序号和地支
    private data class JieTerm(val termIndex: Int, val zhi: String)
    private val jieTerms = listOf(
        JieTerm(2, "寅"),   // 立春
        JieTerm(4, "卯"),   // 惊蛰
        JieTerm(6, "辰"),   // 清明
        JieTerm(8, "巳"),   // 立夏
        JieTerm(10, "午"),  // 芒种
        JieTerm(12, "未"),  // 小暑
        JieTerm(14, "申"),  // 立秋
        JieTerm(16, "酉"),  // 白露
        JieTerm(18, "戌"),  // 寒露
        JieTerm(20, "亥"),  // 立冬
        JieTerm(22, "子"),  // 大雪
        JieTerm(0, "丑")    // 小寒 (属下一年计算)
    )

    /// 查表缓存：存储年份→节气日期
    private val solarTermCache = mutableMapOf<Int, List<Triple<Int, Int, String>>>()

    /// 获取指定年份的12个节(月首)的精确日期
    private fun getJieDates(year: Int): List<Triple<Int, Int, String>> {
        solarTermCache[year]?.let { return it }

        val dates = mutableListOf<Triple<Int, Int, String>>()
        for (jt in jieTerms) {
            val jde = solarTermJDE(year, jt.termIndex)
            val (_, m, d) = jdeToDate(jde)
            dates.add(Triple(m, d, jt.zhi))
        }
        dates.sortWith(compareBy({ it.first }, { it.second }))

        solarTermCache[year] = dates
        return dates
    }

    /// 月支 (精确节气算法)
    fun yueZhi(year: Int, month: Int, day: Int): String {
        val dates = getJieDates(year)
        // 从后往前找第一个不超过当前日期的节
        for (i in dates.indices.reversed()) {
            val d = dates[i]
            if (month > d.first || (month == d.first && day >= d.second)) {
                return d.third
            }
        }
        // 在第一个节之前，取上一年最后一个节
        val prevDates = getJieDates(year - 1)
        return prevDates.lastOrNull()?.third ?: "丑"
    }

    /// 获取立春精确日期
    fun liChunDate(year: Int): Pair<Int, Int> {
        val jde = solarTermJDE(year, 2) // 立春 = term index 2
        val (_, m, d) = jdeToDate(jde)
        return m to d
    }

    /// 年柱干支 (以精确立春为界)
    fun nianGanZhi(year: Int, month: Int, day: Int): Pair<String, String> {
        var y = year
        val lc = liChunDate(year)
        if (month < lc.first || (month == lc.first && day < lc.second)) y -= 1
        val gIdx = ((y - 4) % 10 + 600) % 10
        val zIdx = ((y - 4) % 12 + 600) % 12
        return GanZhi.tianGan[gIdx] to GanZhi.diZhi[zIdx]
    }

    /// 纳音查询
    fun naYin(gan: String, zhi: String): String = GanZhi.naYin(gan, zhi)

    /// 月干 (年上起月法)
    fun yueGan(nianGan: String, yueZhi: String): String {
        val base = mapOf("甲" to 2,"己" to 2,"乙" to 4,"庚" to 4,"丙" to 6,"辛" to 6,"丁" to 8,"壬" to 8,"戊" to 0,"癸" to 0)
        val off = (GanZhi.zhiIndex(yueZhi) - 2 + 12) % 12
        return GanZhi.tianGan[((base[nianGan] ?: 0) + off) % 10]
    }

    /// 时柱干支 (日上起时法)
    fun shiGanZhi(riGan: String, hour: Int): Pair<String, String> {
        val idx = ((hour + 1) % 24) / 2
        val base = mapOf("甲" to 0,"己" to 0,"乙" to 2,"庚" to 2,"丙" to 4,"辛" to 4,"丁" to 6,"壬" to 6,"戊" to 8,"癸" to 8)
        return GanZhi.tianGan[((base[riGan] ?: 0) + idx) % 10] to GanZhi.diZhi[idx]
    }

    /// 十神
    fun shiShen(riGan: String, otherGan: String): String {
        val me = GanZhi.wuxingTianGan[riGan]!!
        val other = GanZhi.wuxingTianGan[otherGan]!!
        val sameYinYang = GanZhi.ganIndex(riGan) % 2 == GanZhi.ganIndex(otherGan) % 2
        if (me == other) return if (sameYinYang) "比肩" else "劫财"
        if (GanZhi.wxSheng[me] == other) return if (sameYinYang) "食神" else "伤官"
        if (GanZhi.wxKe[me] == other) return if (sameYinYang) "偏财" else "正财"
        if (GanZhi.wxKe[other] == me) return if (sameYinYang) "七杀" else "正官"
        if (GanZhi.wxSheng[other] == me) return if (sameYinYang) "偏印" else "正印"
        return "?"
    }

    /// 空亡计算
    fun kongWang(riGan: String, riZhi: String): List<String> {
        val tgIdx = GanZhi.ganIndex(riGan)
        val dzIdx = GanZhi.zhiIndex(riZhi)
        var diff = dzIdx - tgIdx
        if (diff < 0) diff += 12
        val xunStart = (dzIdx - diff + 120) % 12
        return listOf(GanZhi.diZhi[(xunStart + 10) % 12], GanZhi.diZhi[(xunStart + 11) % 12])
    }
}
