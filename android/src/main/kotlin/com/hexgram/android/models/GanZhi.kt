package com.hexgram.android.models

object GanZhi {
    val tianGan = listOf("甲","乙","丙","丁","戊","己","庚","辛","壬","癸")
    val diZhi = listOf("子","丑","寅","卯","辰","巳","午","未","申","酉","戌","亥")
    val shengXiao = listOf("鼠","牛","虎","兔","龙","蛇","马","羊","猴","鸡","狗","猪")

    val wuxingTianGan = mapOf(
        "甲" to "木","乙" to "木","丙" to "火","丁" to "火","戊" to "土",
        "己" to "土","庚" to "金","辛" to "金","壬" to "水","癸" to "水"
    )
    val wuxingDiZhi = mapOf(
        "子" to "水","丑" to "土","寅" to "木","卯" to "木","辰" to "土","巳" to "火",
        "午" to "火","未" to "土","申" to "金","酉" to "金","戌" to "土","亥" to "水"
    )

    // 五行生克
    val wxSheng = mapOf("木" to "火","火" to "土","土" to "金","金" to "水","水" to "木")
    val wxKe = mapOf("木" to "土","土" to "水","水" to "火","火" to "金","金" to "木")

    val wuxingAll = listOf("木","火","土","金","水")

    // 藏干
    val cangGan = mapOf(
        "子" to listOf("癸"), "丑" to listOf("己","癸","辛"), "寅" to listOf("甲","丙","戊"),
        "卯" to listOf("乙"), "辰" to listOf("戊","乙","癸"), "巳" to listOf("丙","庚","戊"),
        "午" to listOf("丁","己"), "未" to listOf("己","丁","乙"), "申" to listOf("庚","壬","戊"),
        "酉" to listOf("辛"), "戌" to listOf("戊","辛","丁"), "亥" to listOf("壬","甲")
    )

    // 六十甲子纳音表
    val naYinTable = listOf(
        "海中金","海中金","炉中火","炉中火","大林木","大林木",
        "路旁土","路旁土","剑锋金","剑锋金","山头火","山头火",
        "涧下水","涧下水","城头土","城头土","白蜡金","白蜡金",
        "杨柳木","杨柳木","泉中水","泉中水","屋上土","屋上土",
        "霹雳火","霹雳火","松柏木","松柏木","长流水","长流水",
        "沙中金","沙中金","山下火","山下火","平地木","平地木",
        "壁上土","壁上土","金箔金","金箔金","覆灯火","覆灯火",
        "天河水","天河水","大驿土","大驿土","钗钏金","钗钏金",
        "桑柘木","桑柘木","大溪水","大溪水","沙中土","沙中土",
        "天上火","天上火","石榴木","石榴木","大海水","大海水"
    )

    /// 获取纳音：根据天干地支序号
    fun naYin(gan: String, zhi: String): String {
        val gIdx = ganIndex(gan)
        val zIdx = zhiIndex(zhi)
        val jiazi = (gIdx * 6 + zIdx * 5) % 60
        return naYinTable[jiazi]
    }

    // 地支六合
    data class LiuHeEntry(val a: String, val b: String, val wx: String)
    val liuHe = listOf(
        LiuHeEntry("子","丑","土"), LiuHeEntry("寅","亥","木"), LiuHeEntry("卯","戌","火"),
        LiuHeEntry("辰","酉","金"), LiuHeEntry("巳","申","水"), LiuHeEntry("午","未","火")
    )

    // 地支三合局
    data class SanHeEntry(val a: String, val b: String, val c: String, val wx: String)
    val sanHe = listOf(
        SanHeEntry("申","子","辰","水"), SanHeEntry("寅","午","戌","火"),
        SanHeEntry("亥","卯","未","木"), SanHeEntry("巳","酉","丑","金")
    )

    // 地支三会方
    val sanHui = listOf(
        SanHeEntry("寅","卯","辰","木"), SanHeEntry("巳","午","未","火"),
        SanHeEntry("申","酉","戌","金"), SanHeEntry("亥","子","丑","水")
    )

    // 地支六冲
    val liuChong = listOf(
        "子" to "午", "丑" to "未", "寅" to "申",
        "卯" to "酉", "辰" to "戌", "巳" to "亥"
    )

    // 地支三刑
    val sanXing = listOf(
        "寅" to "巳", "巳" to "申", "申" to "寅",     // 无恩之刑
        "丑" to "戌", "戌" to "未", "未" to "丑",     // 恃势之刑
        "子" to "卯", "卯" to "子",                    // 无礼之刑
        "辰" to "辰", "午" to "午", "酉" to "酉", "亥" to "亥"  // 自刑
    )

    // 地支相害（六害/穿）
    val xiangHai = listOf(
        "子" to "未", "丑" to "午", "寅" to "巳",
        "卯" to "辰", "申" to "亥", "酉" to "戌"
    )

    fun ganIndex(g: String): Int = tianGan.indexOf(g).coerceAtLeast(0)
    fun zhiIndex(z: String): Int = diZhi.indexOf(z).coerceAtLeast(0)
}
