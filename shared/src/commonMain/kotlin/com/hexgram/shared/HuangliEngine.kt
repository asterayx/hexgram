package com.hexgram.shared

data class HuangliResult(
    val year: Int,
    val month: Int,
    val day: Int,
    val weekDay: String,
    val riGan: String,
    val riZhi: String,
    val nianGan: String,
    val nianZhi: String,
    val yueZhi: String,
    val lunarMonth: String,
    val shengXiao: String,
    val jianChu: String,
    val erShiBaXiu: String,
    val yi: String,
    val ji: String,
    val xiShen: String,
    val caiShen: String,
    val fuShen: String,
    val chongSha: String,
    val pengZuGan: String,
    val pengZuZhi: String
)

object HuangliEngine {

    val jianChuNames = listOf("建", "除", "满", "平", "定", "执", "破", "危", "成", "收", "开", "闭")

    val erShiBaXiuNames = listOf(
        "角", "亢", "氐", "房", "心", "尾", "箕",
        "斗", "牛", "女", "虚", "危", "室", "壁",
        "奎", "娄", "胃", "昴", "毕", "觜", "参",
        "井", "鬼", "柳", "星", "张", "翼", "轸"
    )

    val xiShenMap = mapOf(
        "甲" to "东北", "乙" to "西北", "丙" to "西南", "丁" to "正南", "戊" to "东南",
        "己" to "东北", "庚" to "西南", "辛" to "正南", "壬" to "正南", "癸" to "东南"
    )

    val caiShenMap = mapOf(
        "甲" to "东北", "乙" to "东方", "丙" to "西南", "丁" to "西方", "戊" to "北方",
        "己" to "南方", "庚" to "东方", "辛" to "南方", "壬" to "南方", "癸" to "东方"
    )

    val fuShenMap = mapOf(
        "甲" to "东南", "乙" to "东北", "丙" to "正西", "丁" to "西北", "戊" to "北方",
        "己" to "西南", "庚" to "西南", "辛" to "正东", "壬" to "东南", "癸" to "正北"
    )

    val yiMap = mapOf(
        "建" to "出行 上任 动土 开市",
        "除" to "治病 祭祀 解除 扫舍",
        "满" to "祈福 嫁娶 立券 入宅",
        "平" to "修饰 涂泥 安机 会亲",
        "定" to "冠带 嫁娶 开市 修造",
        "执" to "祭祀 捕捉 栽种 修造",
        "破" to "治病 求医",
        "危" to "祭祀 安床 纳畜 经络",
        "成" to "嫁娶 开市 立券 入学 上任",
        "收" to "纳财 求嗣 祭祀 安葬",
        "开" to "开市 立券 求医 动土 嫁娶",
        "闭" to "安葬 收藏 筑堤"
    )

    val jiMap = mapOf(
        "建" to "嫁娶 动土 开仓",
        "除" to "嫁娶 远行",
        "满" to "栽种 动土 服药",
        "平" to "祈福 出行",
        "定" to "出行 诉讼 纳畜",
        "执" to "开市 出行 搬迁",
        "破" to "诸事不宜",
        "危" to "出行 登高 动土",
        "成" to "诉讼 出行",
        "收" to "安葬 开市",
        "开" to "安葬",
        "闭" to "开市 出行 嫁娶 远行"
    )

    val pengZuGanMap = mapOf(
        "甲" to "甲不开仓财物耗散", "乙" to "乙不栽植千株不长",
        "丙" to "丙不修灶必见灾殃", "丁" to "丁不剃头头必生疮",
        "戊" to "戊不受田田主不祥", "己" to "己不破券二比并亡",
        "庚" to "庚不经络织机虚张", "辛" to "辛不合酱主人不尝",
        "壬" to "壬不汲水更难提防", "癸" to "癸不词讼理弱敌强"
    )

    val pengZuZhiMap = mapOf(
        "子" to "子不问卜自惹祸殃", "丑" to "丑不冠带主不还乡",
        "寅" to "寅不祭祀神鬼不尝", "卯" to "卯不穿井水泉不香",
        "辰" to "辰不哭泣必主重丧", "巳" to "巳不远行财物伏藏",
        "午" to "午不苫盖屋主更张", "未" to "未不服药毒气入肠",
        "申" to "申不安床鬼祟入房", "酉" to "酉不宴客醉坐颠狂",
        "戌" to "戌不吃犬作怪上床", "亥" to "亥不嫁娶不利新郎"
    )

    val chongMap = mapOf(
        "子" to "马(南)", "丑" to "羊(西南)", "寅" to "猴(西)", "卯" to "鸡(西)",
        "辰" to "狗(西北)", "巳" to "猪(北)", "午" to "鼠(北)", "未" to "牛(东北)",
        "申" to "虎(东)", "酉" to "兔(东)", "戌" to "龙(东南)", "亥" to "蛇(南)"
    )

    fun calculate(year: Int, month: Int, day: Int): HuangliResult {
        val ri = CalendarCalc.riGanZhi(year, month, day)
        val yz = CalendarCalc.yueZhi(year, month, day)
        val ng = CalendarCalc.nianGanZhi(year, month, day)

        // 星期计算：基于JDN
        val weekDays = listOf("日", "一", "二", "三", "四", "五", "六")
        val j = CalendarCalc.jdn(year, month, day)
        val dow = ((j + 1.5).toInt() % 7 + 7) % 7

        // 建除十二神
        val yzIdx = GanZhi.zhiIndex(yz)
        val riZIdx = GanZhi.zhiIndex(ri.second)
        val jcIdx = ((riZIdx - yzIdx) % 12 + 12) % 12
        val jianChu = jianChuNames[jcIdx]

        // 二十八宿
        val refJDN = CalendarCalc.jdn(2000, 1, 1)
        val curJDN = CalendarCalc.jdn(year, month, day)
        val xiuIdx = (((curJDN - refJDN).toInt() + 10) % 28 + 28) % 28
        val xiu = erShiBaXiuNames[xiuIdx]

        // 农历月（以月支近似推算）
        val lunarMonths = listOf("正", "二", "三", "四", "五", "六", "七", "八", "九", "十", "冬", "腊")
        val mzIdx = GanZhi.zhiIndex(yz)
        val lm = lunarMonths[(mzIdx - 2 + 12) % 12]

        val yi = (yiMap[jianChu] ?: "").trim()
        val ji = (jiMap[jianChu] ?: "诸事不宜").trim()

        val zhiIdx = GanZhi.zhiIndex(ng.second)

        return HuangliResult(
            year = year,
            month = month,
            day = day,
            weekDay = weekDays[dow],
            riGan = ri.first,
            riZhi = ri.second,
            nianGan = ng.first,
            nianZhi = ng.second,
            yueZhi = yz,
            lunarMonth = lm,
            shengXiao = GanZhi.shengXiao[zhiIdx],
            jianChu = jianChu,
            erShiBaXiu = xiu,
            yi = yi,
            ji = ji,
            xiShen = xiShenMap[ri.first] ?: "?",
            caiShen = caiShenMap[ri.first] ?: "?",
            fuShen = fuShenMap[ri.first] ?: "?",
            chongSha = chongMap[ri.second] ?: "?",
            pengZuGan = pengZuGanMap[ri.first] ?: "",
            pengZuZhi = pengZuZhiMap[ri.second] ?: ""
        )
    }

    fun formatPlainText(r: HuangliResult): String {
        val sb = StringBuilder()
        sb.append("${r.year}年${r.month}月${r.day}日 星期${r.weekDay}\n")
        sb.append("日柱：${r.riGan}${r.riZhi} 年柱：${r.nianGan}${r.nianZhi} ${r.shengXiao}年\n")
        sb.append("月建：${r.yueZhi}月 十二建星：${r.jianChu}日 二十八宿：${r.erShiBaXiu}宿\n")
        sb.append("宜：${if (r.yi.isEmpty()) "无" else r.yi}\n忌：${r.ji}\n")
        sb.append("喜神方位：${r.xiShen} 财神方位：${r.caiShen} 福神方位：${r.fuShen}\n")
        sb.append("冲煞：冲${r.chongSha}\n")
        sb.append("彭祖百忌：${r.pengZuGan}；${r.pengZuZhi}")
        return sb.toString()
    }
}
