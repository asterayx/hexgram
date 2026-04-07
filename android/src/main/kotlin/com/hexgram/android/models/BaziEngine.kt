package com.hexgram.android.models

data class BaziPillar(
    val label: String,
    val gan: String,
    val zhi: String,
    val shiShen: String,
    val wuxing: String,
    val cangGan: List<CangGanItem>
)

data class CangGanItem(
    val gan: String,
    val shiShen: String,
    val wuxing: String
)

data class DaYunItem(
    val gan: String,
    val zhi: String,
    val age: Int,
    val year: Int,
    val wuxing: String
)

data class ShenShaItem(
    val name: String,
    val pillar: String,
    val description: String
)

data class DiZhiRelation(
    val type: String,
    val branches: String,
    val detail: String
)

data class BaziResult(
    val name: String,
    val sex: String,
    val birthYear: Int,
    val birthMonth: Int,
    val birthDay: Int,
    val birthHour: Int,
    val pillars: List<BaziPillar>,
    val riGan: String,
    val riGanWuxing: String,
    val isStrong: Boolean,
    val wuxingCounts: Map<String, Double>,
    val daYun: List<DaYunItem>,
    val isShunPai: Boolean,
    val liuNianGan: String,
    val liuNianZhi: String,
    val liuNianShiShen: String,
    val currentYear: Int,
    val shengXiao: String,
    val naYinPillars: List<String>,
    val shenSha: List<ShenShaItem>,
    val diZhiRelations: List<DiZhiRelation>
)

object BaziEngine {

    fun calculate(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        sex: String,
        name: String = "",
        currentYear: Int = 2026
    ): BaziResult {
        val ng = CalendarCalc.nianGanZhi(year, month, day)
        val yz = CalendarCalc.yueZhi(year, month, day)
        val yg = CalendarCalc.yueGan(ng.first, yz)
        val ri = CalendarCalc.riGanZhi(year, month, day)
        val si = CalendarCalc.shiGanZhi(ri.first, hour)

        val riGan = ri.first

        // 四柱原始数据: (label, gan, zhi)
        val pillarData = listOf(
            Triple("年柱", ng.first, ng.second),
            Triple("月柱", yg, yz),
            Triple("日柱", ri.first, ri.second),
            Triple("时柱", si.first, si.second)
        )

        val pillars = pillarData.mapIndexed { i, p ->
            val ss = if (i == 2) "日主" else CalendarCalc.shiShen(riGan, p.second)
            val wx = GanZhi.wuxingTianGan[p.second] ?: ""
            val cg = (GanZhi.cangGan[p.third] ?: emptyList()).map { g ->
                CangGanItem(
                    gan = g,
                    shiShen = CalendarCalc.shiShen(riGan, g),
                    wuxing = GanZhi.wuxingTianGan[g] ?: ""
                )
            }
            BaziPillar(label = p.first, gan = p.second, zhi = p.third, shiShen = ss, wuxing = wx, cangGan = cg)
        }

        // 五行力量
        val wxC = mutableMapOf("木" to 0.0, "火" to 0.0, "土" to 0.0, "金" to 0.0, "水" to 0.0)
        for (p in pillars) {
            wxC[p.wuxing] = (wxC[p.wuxing] ?: 0.0) + 1.0
            for (cg in p.cangGan) {
                wxC[cg.wuxing] = (wxC[cg.wuxing] ?: 0.0) + 0.5
            }
        }

        val meWx = GanZhi.wuxingTianGan[riGan] ?: ""
        val shengMe = GanZhi.wxSheng.entries.firstOrNull { it.value == meWx }?.key ?: ""
        val helpS = (wxC[meWx] ?: 0.0) + (wxC[shengMe] ?: 0.0)
        val drainS = wxC.filter { it.key != meWx && it.key != shengMe }.values.sum()
        val isStrong = helpS > drainS

        // 大运
        val nianYin = GanZhi.ganIndex(ng.first) % 2
        val isShun = (nianYin == 0 && sex == "M") || (nianYin == 1 && sex == "F")
        val yueGIdx = GanZhi.ganIndex(yg)
        val yueZIdx = GanZhi.zhiIndex(yz)
        val daYun = mutableListOf<DaYunItem>()
        for (i in 1..8) {
            val off = if (isShun) i else -i
            val dg = GanZhi.tianGan[((yueGIdx + off) % 10 + 10) % 10]
            val dz = GanZhi.diZhi[((yueZIdx + off) % 12 + 12) % 12]
            val age = i * 10 - 6
            daYun.add(
                DaYunItem(
                    gan = dg,
                    zhi = dz,
                    age = age,
                    year = year + age,
                    wuxing = GanZhi.wuxingTianGan[dg] ?: ""
                )
            )
        }

        // 流年
        val lnGZ = CalendarCalc.nianGanZhi(currentYear, 6, 1)

        val zhiIdx = GanZhi.zhiIndex(ng.second)

        // 纳音
        val naYinPillars = pillarData.map { GanZhi.naYin(it.second, it.third) }

        // 神煞
        val shenSha = calculateShenSha(riGan, pillarData)

        // 地支关系
        val allZhi = pillarData.map { it.third }
        val diZhiRelations = analyzeDiZhiRelations(allZhi, pillarData.map { it.first })

        return BaziResult(
            name = name,
            sex = sex,
            birthYear = year,
            birthMonth = month,
            birthDay = day,
            birthHour = hour,
            pillars = pillars,
            riGan = riGan,
            riGanWuxing = meWx,
            isStrong = isStrong,
            wuxingCounts = wxC.toMap(),
            daYun = daYun,
            isShunPai = isShun,
            liuNianGan = lnGZ.first,
            liuNianZhi = lnGZ.second,
            liuNianShiShen = CalendarCalc.shiShen(riGan, lnGZ.first),
            currentYear = currentYear,
            shengXiao = GanZhi.shengXiao[zhiIdx],
            naYinPillars = naYinPillars,
            shenSha = shenSha,
            diZhiRelations = diZhiRelations
        )
    }

    // 神煞计算
    fun calculateShenSha(riGan: String, pillars: List<Triple<String, String, String>>): List<ShenShaItem> {
        val result = mutableListOf<ShenShaItem>()

        // 天乙贵人：以日干查
        val tianYi = mapOf(
            "甲" to listOf("丑", "未"), "乙" to listOf("子", "申"),
            "丙" to listOf("亥", "酉"), "丁" to listOf("亥", "酉"),
            "戊" to listOf("丑", "未"), "己" to listOf("子", "申"),
            "庚" to listOf("丑", "未"), "辛" to listOf("寅", "午"),
            "壬" to listOf("卯", "巳"), "癸" to listOf("卯", "巳")
        )
        tianYi[riGan]?.let { guiRen ->
            for (p in pillars) {
                if (p.third in guiRen) {
                    result.add(ShenShaItem("天乙贵人", p.first, "逢凶化吉、遇难呈祥之神"))
                }
            }
        }

        // 文昌：以日干查
        val wenChang = mapOf(
            "甲" to "巳", "乙" to "午", "丙" to "申", "丁" to "酉", "戊" to "申",
            "己" to "酉", "庚" to "亥", "辛" to "子", "壬" to "寅", "癸" to "卯"
        )
        wenChang[riGan]?.let { wc ->
            for (p in pillars) {
                if (p.third == wc) {
                    result.add(ShenShaItem("文昌", p.first, "主聪明好学、利考试文书"))
                }
            }
        }

        // 驿马：以日支查
        val yiMa = mapOf(
            "寅" to "申", "申" to "寅", "巳" to "亥", "亥" to "巳",
            "子" to "寅", "午" to "申", "卯" to "巳", "酉" to "亥",
            "辰" to "寅", "戌" to "申", "丑" to "亥", "未" to "巳"
        )
        val riZhi = pillars[2].third
        yiMa[riZhi]?.let { ym ->
            for (p in pillars) {
                if (p.first != "日柱" && p.third == ym) {
                    result.add(ShenShaItem("驿马", p.first, "主奔波走动、迁移变动"))
                }
            }
        }

        // 桃花（咸池）：以日支查
        val taoHua = mapOf(
            "寅" to "卯", "午" to "卯", "戌" to "卯",
            "申" to "酉", "子" to "酉", "辰" to "酉",
            "巳" to "午", "酉" to "午", "丑" to "午",
            "亥" to "子", "卯" to "子", "未" to "子"
        )
        taoHua[riZhi]?.let { th ->
            for (p in pillars) {
                if (p.first != "日柱" && p.third == th) {
                    result.add(ShenShaItem("桃花", p.first, "主人缘佳、异性缘旺"))
                }
            }
        }

        // 华盖：以日支查
        val huaGai = mapOf(
            "寅" to "戌", "午" to "戌", "戌" to "戌",
            "申" to "辰", "子" to "辰", "辰" to "辰",
            "巳" to "丑", "酉" to "丑", "丑" to "丑",
            "亥" to "未", "卯" to "未", "未" to "未"
        )
        huaGai[riZhi]?.let { hg ->
            for (p in pillars) {
                if (p.first != "日柱" && p.third == hg) {
                    result.add(ShenShaItem("华盖", p.first, "主孤高、艺术才华、宗教缘"))
                }
            }
        }

        // 羊刃：以日干查
        val yangRen = mapOf(
            "甲" to "卯", "乙" to "寅", "丙" to "午", "丁" to "巳", "戊" to "午",
            "己" to "巳", "庚" to "酉", "辛" to "申", "壬" to "子", "癸" to "亥"
        )
        yangRen[riGan]?.let { yr ->
            for (p in pillars) {
                if (p.third == yr) {
                    result.add(ShenShaItem("羊刃", p.first, "刚烈之星、主性格刚强、防灾伤"))
                }
            }
        }

        return result
    }

    // 地支关系分析
    fun analyzeDiZhiRelations(allZhi: List<String>, pillarLabels: List<String>): List<DiZhiRelation> {
        val result = mutableListOf<DiZhiRelation>()

        // 六合
        for (he in GanZhi.liuHe) {
            val has0 = allZhi.contains(he.a)
            val has1 = allZhi.contains(he.b)
            if (has0 && has1) {
                result.add(
                    DiZhiRelation(
                        type = "六合",
                        branches = "${he.a}${he.b}合${he.wx}",
                        detail = "${he.a}${he.b}六合化${he.wx}，主和合、亲近"
                    )
                )
            }
        }

        // 三合局
        for (sh in GanZhi.sanHe) {
            val members = listOf(sh.a, sh.b, sh.c)
            val has = members.filter { z -> allZhi.contains(z) }
            if (has.size == 3) {
                result.add(
                    DiZhiRelation(
                        type = "三合局",
                        branches = "${sh.a}${sh.b}${sh.c}合${sh.wx}局",
                        detail = "三合${sh.wx}局成化，${sh.wx}五行力量大增"
                    )
                )
            } else if (has.size == 2) {
                val missing = members.first { !allZhi.contains(it) }
                result.add(
                    DiZhiRelation(
                        type = "半合",
                        branches = "${has.joinToString("")}半合${sh.wx}局",
                        detail = "缺${missing}，半合${sh.wx}局，力量稍弱"
                    )
                )
            }
        }

        // 六冲
        for (ch in GanZhi.liuChong) {
            val has0 = allZhi.contains(ch.first)
            val has1 = allZhi.contains(ch.second)
            if (has0 && has1) {
                val idx0 = allZhi.indexOf(ch.first)
                val idx1 = allZhi.indexOf(ch.second)
                result.add(
                    DiZhiRelation(
                        type = "六冲",
                        branches = "${ch.first}${ch.second}冲",
                        detail = "${pillarLabels[idx0]}${ch.first}冲${pillarLabels[idx1]}${ch.second}，主动荡变化"
                    )
                )
            }
        }

        // 三刑（非自刑）
        val xingPairs = listOf(
            Triple("寅", "巳", "无恩之刑"), Triple("巳", "申", "无恩之刑"), Triple("申", "寅", "无恩之刑"),
            Triple("丑", "戌", "恃势之刑"), Triple("戌", "未", "恃势之刑"), Triple("未", "丑", "恃势之刑"),
            Triple("子", "卯", "无礼之刑"), Triple("卯", "子", "无礼之刑")
        )
        for (xp in xingPairs) {
            if (allZhi.contains(xp.first) && allZhi.contains(xp.second)) {
                val key = listOf(xp.first, xp.second).sorted().joinToString("")
                if (result.none { it.type == "三刑" && it.branches.contains(key) }) {
                    result.add(
                        DiZhiRelation(
                            type = "三刑",
                            branches = "${xp.first}${xp.second}刑",
                            detail = "${xp.third}：${xp.first}刑${xp.second}，主是非口舌、刑伤"
                        )
                    )
                }
            }
        }

        // 自刑
        for (zhi in listOf("辰", "午", "酉", "亥")) {
            if (allZhi.count { it == zhi } >= 2) {
                result.add(
                    DiZhiRelation(
                        type = "自刑",
                        branches = "${zhi}${zhi}自刑",
                        detail = "${zhi}见${zhi}为自刑，主自我困扰"
                    )
                )
            }
        }

        // 相害
        for (hai in GanZhi.xiangHai) {
            if (allZhi.contains(hai.first) && allZhi.contains(hai.second)) {
                result.add(
                    DiZhiRelation(
                        type = "相害",
                        branches = "${hai.first}${hai.second}害",
                        detail = "${hai.first}${hai.second}相害，主暗中损耗、不顺"
                    )
                )
            }
        }

        return result
    }

    fun formatPlainText(result: BaziResult): String {
        val sb = StringBuilder()
        if (result.name.isNotEmpty()) sb.append("${result.name}，")
        sb.append(if (result.sex == "M") "男" else "女")
        sb.append("命，")
        val hourIdx = ((result.birthHour + 1) % 24) / 2
        sb.append("${result.birthYear}年${result.birthMonth}月${result.birthDay}日${GanZhi.diZhi[hourIdx]}时生\n")
        sb.append("四柱：${result.pillars.joinToString(" ") { "${it.gan}${it.zhi}" }}\n")
        sb.append("十神：${result.pillars.joinToString(" ") { it.shiShen }}\n")
        sb.append("藏干：${result.pillars.joinToString(" ") { "${it.zhi}(${it.cangGan.joinToString(",") { cg -> cg.gan }})" }}\n")
        sb.append("日主${result.riGan}（${result.riGanWuxing}），${if (result.isStrong) "身旺" else "身弱"}\n")

        val wxStr = GanZhi.wuxingAll.joinToString(" ") { wx ->
            val v = result.wuxingCounts[wx] ?: 0.0
            val formatted = if (v == v.toLong().toDouble()) "${v.toLong().toInt()}.0" else v.toString()
            "${wx}:${formatted}"
        }
        sb.append("五行：${wxStr}\n")

        sb.append("大运（${if (result.isShunPai) "顺" else "逆"}排）：${result.daYun.joinToString(" ") { "${it.gan}${it.zhi}(${it.age}-${it.age + 9}岁,${it.year}-${it.year + 9}年)" }}\n")
        val curDy = result.daYun.firstOrNull { result.currentYear >= it.year && result.currentYear < it.year + 10 }
        if (curDy != null) {
            sb.append("当前大运：${curDy.gan}${curDy.zhi}\n")
        }
        sb.append("${result.currentYear}年流年：${result.liuNianGan}${result.liuNianZhi}（对日主为${result.liuNianShiShen}）\n")
        sb.append("纳音：${result.naYinPillars.joinToString(" ")}\n")
        if (result.shenSha.isNotEmpty()) {
            sb.append("神煞：${result.shenSha.joinToString("、") { "${it.name}(${it.pillar})" }}\n")
        }
        if (result.diZhiRelations.isNotEmpty()) {
            sb.append("地支关系：${result.diZhiRelations.joinToString("、") { it.branches }}")
        }
        return sb.toString()
    }
}
