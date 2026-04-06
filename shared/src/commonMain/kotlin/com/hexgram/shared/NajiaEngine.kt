package com.hexgram.shared

// MARK: - Data classes
data class BaGuaInfo(val name: String, val wuxing: String, val najiaYang: String, val najiaYin: String)
data class GongInfo(val gong: String, val shi: Int, val ying: Int, val youHun: Boolean, val guiHun: Boolean)

data class YaoData(
    val pos: Int, val posName: String, val yinYang: String,
    val value: Int, val valueLabel: String, val isDong: Boolean,
    val tianGan: String, val diZhi: String, val wuxing: String,
    val liuqin: String, val isShi: Boolean, val isYing: Boolean,
    val liuShen: String, val isKong: Boolean,
    val yueEffect: String, val riWangShuai: String
)

data class FuShenData(val liuqin: String, val tianGan: String, val diZhi: String, val wuxing: String, val fuUnder: Int)
data class DongBianData(val yaoPos: String, val benLiuqin: String, val benGanZhi: String, val benWuxing: String, val bianLiuqin: String, val bianGanZhi: String, val bianWuxing: String, val relation: String, val teXing: String)
data class AnDongData(val yaoPos: String, val liuqin: String, val ganZhi: String, val wuxing: String, val reason: String)
data class SanHePanData(val branches: String, val wuxing: String, val detail: String)

data class GuaResult(
    val guaName: String, val guaKey: String,
    val innerGua: String, val outerGua: String, val innerWx: String, val outerWx: String,
    val gong: String, val gongWx: String, val youHun: Boolean, val guiHun: Boolean,
    val yaos: List<YaoData>, val hasChanging: Boolean, val changingIdx: List<Int>,
    val changedGuaName: String?, val changedGuaKey: String?, val changedYaos: List<YaoData>?,
    val dongBianAnalysis: List<DongBianData>, val fuShen: List<FuShenData>,
    val isLiuChongGua: Boolean, val kongWang: List<String>,
    val riGan: String, val riZhi: String, val yueZhi: String,
    val isLiuHeGua: Boolean, val anDong: List<AnDongData>, val yuePo: List<Int>,
    val isFanYin: Boolean, val isFuYin: Boolean, val sanHe: List<SanHePanData>
)

// MARK: - Constants
val BAGUA_TABLE = mapOf(
    "111" to BaGuaInfo("乾","金","甲","壬"), "000" to BaGuaInfo("坤","土","乙","癸"),
    "010" to BaGuaInfo("坎","水","戊","戊"), "101" to BaGuaInfo("离","火","己","己"),
    "100" to BaGuaInfo("震","木","庚","庚"), "011" to BaGuaInfo("巽","木","辛","辛"),
    "001" to BaGuaInfo("艮","土","丙","丙"), "110" to BaGuaInfo("兑","金","丁","丁")
)

val NAJIA_DIZHI = mapOf(
    "乾" to Pair(listOf("子","寅","辰"), listOf("午","申","戌")),
    "坤" to Pair(listOf("未","巳","卯"), listOf("丑","亥","酉")),
    "坎" to Pair(listOf("寅","辰","午"), listOf("申","戌","子")),
    "离" to Pair(listOf("卯","丑","亥"), listOf("酉","未","巳")),
    "震" to Pair(listOf("子","寅","辰"), listOf("午","申","戌")),
    "巽" to Pair(listOf("丑","亥","酉"), listOf("未","巳","卯")),
    "艮" to Pair(listOf("辰","午","申"), listOf("戌","子","寅")),
    "兑" to Pair(listOf("巳","卯","丑"), listOf("亥","酉","未"))
)

val HEXAGRAM_NAMES = mapOf(
    "111111" to "乾","111110" to "夬","111101" to "大有","111100" to "大壮","111011" to "小畜","111010" to "需","111001" to "大畜","111000" to "泰",
    "110111" to "履","110110" to "兑","110101" to "睽","110100" to "归妹","110011" to "中孚","110010" to "节","110001" to "损","110000" to "临",
    "101111" to "同人","101110" to "革","101101" to "离","101100" to "丰","101011" to "家人","101010" to "既济","101001" to "贲","101000" to "明夷",
    "100111" to "无妄","100110" to "随","100101" to "噬嗑","100100" to "震","100011" to "益","100010" to "屯","100001" to "颐","100000" to "复",
    "011111" to "姤","011110" to "大过","011101" to "鼎","011100" to "恒","011011" to "巽","011010" to "井","011001" to "蛊","011000" to "升",
    "010111" to "讼","010110" to "困","010101" to "未济","010100" to "解","010011" to "涣","010010" to "坎","010001" to "蒙","010000" to "师",
    "001111" to "遁","001110" to "咸","001101" to "旅","001100" to "小过","001011" to "渐","001010" to "蹇","001001" to "艮","001000" to "谦",
    "000111" to "否","000110" to "萃","000101" to "晋","000100" to "豫","000011" to "观","000010" to "比","000001" to "剥","000000" to "坤"
)

val LIUSHEN_ORDER = listOf("青龙","朱雀","勾陈","螣蛇","白虎","玄武")
val LIUSHEN_START = mapOf("甲" to 0,"乙" to 0,"丙" to 1,"丁" to 1,"戊" to 2,"己" to 3,"庚" to 4,"辛" to 4,"壬" to 5,"癸" to 5)
val LIUCHONG_MAP = mapOf("子" to "午","丑" to "未","寅" to "申","卯" to "酉","辰" to "戌","巳" to "亥","午" to "子","未" to "丑","申" to "寅","酉" to "卯","戌" to "辰","亥" to "巳")
val YAO_NAMES = listOf("初","二","三","四","五","上")
val YAO_LABELS = mapOf(6 to "老阴", 7 to "少阳", 8 to "少阴", 9 to "老阳")

val CHANGSHENG = mapOf(
    "木" to mapOf("亥" to "长生","子" to "沐浴","丑" to "冠带","寅" to "临官","卯" to "帝旺","辰" to "衰","巳" to "病","午" to "死","未" to "墓","申" to "绝","酉" to "胎","戌" to "养"),
    "火" to mapOf("寅" to "长生","卯" to "沐浴","辰" to "冠带","巳" to "临官","午" to "帝旺","未" to "衰","申" to "病","酉" to "死","戌" to "墓","亥" to "绝","子" to "胎","丑" to "养"),
    "土" to mapOf("寅" to "长生","卯" to "沐浴","辰" to "冠带","巳" to "临官","午" to "帝旺","未" to "衰","申" to "病","酉" to "死","戌" to "墓","亥" to "绝","子" to "胎","丑" to "养"),
    "金" to mapOf("巳" to "长生","午" to "沐浴","未" to "冠带","申" to "临官","酉" to "帝旺","戌" to "衰","亥" to "病","子" to "死","丑" to "墓","寅" to "绝","卯" to "胎","辰" to "养"),
    "水" to mapOf("申" to "长生","酉" to "沐浴","戌" to "冠带","亥" to "临官","子" to "帝旺","丑" to "衰","寅" to "病","卯" to "死","辰" to "墓","巳" to "绝","午" to "胎","未" to "养")
)

// MARK: - NajiaEngine
object NajiaEngine {
    private var _baGongTable: Map<String, GongInfo>? = null

    val baGongTable: Map<String, GongInfo>
        get() {
            _baGongTable?.let { return it }
            val t = buildBaGong()
            _baGongTable = t
            return t
        }

    private fun buildBaGong(): Map<String, GongInfo> {
        val pureGua = listOf("111","000","010","101","100","011","001","110")
        val result = mutableMapOf<String, GongInfo>()
        for (pg in pureGua) {
            val gua = BAGUA_TABLE[pg] ?: continue
            val bits = pg.map { if (it == '1') 1 else 0 }
            // 第1卦：八纯卦
            val pure = bits + bits
            result[pure.joinToString("")] = GongInfo(gua.name, 5, 2, false, false)
            // 第2-6卦
            for (i in 0..4) {
                val cur = (bits + bits).toMutableList()
                for (j in 0..i) cur[j] = 1 - cur[j]
                result[cur.joinToString("")] = GongInfo(gua.name, i, (i + 3) % 6, false, false)
            }
            // 第7卦（游魂）
            val you = (bits + bits).toMutableList()
            for (j in 0..4) you[j] = 1 - you[j]
            you[3] = 1 - you[3]
            result[you.joinToString("")] = GongInfo(gua.name, 3, 0, true, false)
            // 第8卦（归魂）
            val gui = you.toMutableList()
            gui[0] = bits[0]; gui[1] = bits[1]; gui[2] = bits[2]
            result[gui.joinToString("")] = GongInfo(gua.name, 2, 5, false, true)
        }
        return result
    }

    fun getLiuqin(gongWx: String, yaoWx: String): String {
        if (gongWx == yaoWx) return "兄弟"
        if (GanZhi.wxSheng[gongWx] == yaoWx) return "子孙"
        if (GanZhi.wxSheng[yaoWx] == gongWx) return "父母"
        if (GanZhi.wxKe[gongWx] == yaoWx) return "妻财"
        if (GanZhi.wxKe[yaoWx] == gongWx) return "官鬼"
        return "?"
    }

    fun getLiuShen(riGan: String): List<String> {
        val start = LIUSHEN_START[riGan] ?: 0
        return (0 until 6).map { LIUSHEN_ORDER[(start + it) % 6] }
    }

    fun getWangShuai(wx: String, zhi: String): String {
        val status = CHANGSHENG[wx]?.get(zhi) ?: return "平"
        if (status in listOf("临官","帝旺")) return "旺"
        if (status in listOf("长生","冠带","沐浴")) return "相"
        if (status in listOf("墓","死","绝")) return "衰"
        if (status in listOf("病","胎","养")) return "弱"
        return "平"
    }

    fun getYueJianEffect(yaoDzWx: String, yueZhi: String): String {
        val yueWx = GanZhi.wuxingDiZhi[yueZhi] ?: ""
        if (yaoDzWx == yueWx) return "月建比和，旺"
        if (GanZhi.wxSheng[yueWx] == yaoDzWx) return "月建生之，旺"
        if (GanZhi.wxKe[yueWx] == yaoDzWx) return "月建克之，弱"
        if (GanZhi.wxSheng[yaoDzWx] == yueWx) return "泄气于月建，平"
        if (GanZhi.wxKe[yaoDzWx] == yueWx) return "耗力于月建，平"
        return "平"
    }

    fun zhuangGua(lines: List<Int>, riGan: String = "甲", riZhi: String = "子", yueZhi: String = "子"): GuaResult {
        // 1. 基本爻信息
        val baseBits = lines.map { if (it == 7 || it == 9) 1 else 0 }
        val changedBits = lines.map { v ->
            when (v) { 6 -> 1; 9 -> 0; else -> if (v == 7) 1 else 0 }
        }
        val changingIdx = lines.mapIndexedNotNull { i, v -> if (v == 6 || v == 9) i else null }
        val baseKey = baseBits.joinToString("")
        val changedKey = changedBits.joinToString("")
        val innerKey = baseKey.substring(0, 3)
        val outerKey = baseKey.substring(3, 6)
        val innerGua = BAGUA_TABLE[innerKey]!!
        val outerGua = BAGUA_TABLE[outerKey]!!

        // 2. 八宫归属
        val gongInfo = baGongTable[baseKey] ?: GongInfo(innerGua.name, 4, 1, false, false)
        val gongKey = BAGUA_TABLE.entries.firstOrNull { it.value.name == gongInfo.gong }?.key ?: "111"
        val gongWx = BAGUA_TABLE[gongKey]?.wuxing ?: "土"

        // 3. 六神
        val liushenArr = getLiuShen(riGan)

        // 4. 空亡
        val kw = CalendarCalc.kongWang(riGan, riZhi)

        // 5. 纳甲装卦
        val yaos = (0 until 6).map { i ->
            val isInner = i < 3
            val gua = if (isInner) innerGua else outerGua
            val localIdx = if (isInner) i else i - 3
            val tg = if (isInner) gua.najiaYang else gua.najiaYin
            val dzArr = if (isInner) NAJIA_DIZHI[gua.name]!!.first else NAJIA_DIZHI[gua.name]!!.second
            val dz = dzArr[localIdx]
            val dzWx = GanZhi.wuxingDiZhi[dz] ?: ""
            val liuqin = getLiuqin(gongWx, dzWx)
            val yueEffect = getYueJianEffect(dzWx, yueZhi)
            val riWang = getWangShuai(dzWx, riZhi)
            YaoData(i, YAO_NAMES[i], if (baseBits[i] == 1) "阳" else "阴",
                lines[i], YAO_LABELS[lines[i]] ?: "", lines[i] == 6 || lines[i] == 9,
                tg, dz, dzWx, liuqin, i == gongInfo.shi, i == gongInfo.ying,
                liushenArr[i], kw.contains(dz), yueEffect, riWang)
        }

        // 6. 变卦
        var changedYaos: List<YaoData>? = null
        if (changingIdx.isNotEmpty()) {
            val chInnerKey = changedKey.substring(0, 3)
            val chOuterKey = changedKey.substring(3, 6)
            val chInnerGua = BAGUA_TABLE[chInnerKey]!!
            val chOuterGua = BAGUA_TABLE[chOuterKey]!!
            changedYaos = (0 until 6).map { i ->
                val isInner = i < 3
                val gua = if (isInner) chInnerGua else chOuterGua
                val localIdx = if (isInner) i else i - 3
                val tg = if (isInner) gua.najiaYang else gua.najiaYin
                val dzArr = if (isInner) NAJIA_DIZHI[gua.name]!!.first else NAJIA_DIZHI[gua.name]!!.second
                val dz = dzArr[localIdx]
                val dzWx = GanZhi.wuxingDiZhi[dz] ?: ""
                val liuqin = getLiuqin(gongWx, dzWx)
                YaoData(i, YAO_NAMES[i], if (changedBits[i] == 1) "阳" else "阴",
                    0, "", false, tg, dz, dzWx, liuqin, false, false, "", false, "", "")
            }
        }

        // 7. 伏神
        val presentLiuqin = yaos.map { it.liuqin }.toSet()
        val allLiuqin = listOf("父母","兄弟","子孙","妻财","官鬼")
        val missingLiuqin = allLiuqin.filter { it !in presentLiuqin }
        val fuShen = mutableListOf<FuShenData>()
        if (missingLiuqin.isNotEmpty()) {
            val pureGua = BAGUA_TABLE[gongKey]!!
            for (i in 0 until 6) {
                val isInner = i < 3
                val localIdx = if (isInner) i else i - 3
                val tg = if (isInner) pureGua.najiaYang else pureGua.najiaYin
                val dzArr = if (isInner) NAJIA_DIZHI[pureGua.name]!!.first else NAJIA_DIZHI[pureGua.name]!!.second
                val dz = dzArr[localIdx]
                val dzWx = GanZhi.wuxingDiZhi[dz] ?: ""
                val lq = getLiuqin(gongWx, dzWx)
                if (lq in missingLiuqin) {
                    fuShen.add(FuShenData(lq, tg, dz, dzWx, i))
                }
            }
        }

        // 8. 动变分析
        val dongBianAnalysis = mutableListOf<DongBianData>()
        if (changingIdx.isNotEmpty() && changedYaos != null) {
            for (idx in changingIdx) {
                val benYao = yaos[idx]
                val bianYao = changedYaos[idx]
                val bWx = benYao.wuxing; val cWx = bianYao.wuxing
                val relation = when {
                    bWx == cWx -> "比和（化同）"
                    GanZhi.wxSheng[bWx] == cWx -> "化泄"
                    GanZhi.wxSheng[cWx] == bWx -> "化回头生（吉）"
                    GanZhi.wxKe[bWx] == cWx -> "化克出"
                    GanZhi.wxKe[cWx] == bWx -> "化回头克（凶）"
                    else -> ""
                }
                val bianStatus = CHANGSHENG[bWx]?.get(bianYao.diZhi) ?: ""
                val teXing = when (bianStatus) { "墓" -> "化入墓"; "绝" -> "化入绝"; else -> "" }
                dongBianAnalysis.add(DongBianData(benYao.posName, benYao.liuqin,
                    "${benYao.tianGan}${benYao.diZhi}", bWx, bianYao.liuqin,
                    "${bianYao.tianGan}${bianYao.diZhi}", cWx, relation, teXing))
            }
        }

        // 9. 六冲六合判断
        val liuHeMap = mapOf("子" to "丑","丑" to "子","寅" to "亥","亥" to "寅","卯" to "戌","戌" to "卯","辰" to "酉","酉" to "辰","巳" to "申","申" to "巳","午" to "未","未" to "午")
        var isLiuChongGua = true; var isLiuHeGua = true
        for (i in 0 until 3) {
            if (LIUCHONG_MAP[yaos[i].diZhi] != yaos[i + 3].diZhi) isLiuChongGua = false
            if (liuHeMap[yaos[i].diZhi] != yaos[i + 3].diZhi) isLiuHeGua = false
        }

        // 10. 暗动检测
        val anDong = mutableListOf<AnDongData>()
        for ((i, y) in yaos.withIndex()) {
            if (!y.isDong && LIUCHONG_MAP[riZhi] == y.diZhi) {
                val ws = getWangShuai(y.wuxing, riZhi)
                if (ws == "旺" || ws == "相") {
                    anDong.add(AnDongData(y.posName, y.liuqin, "${y.tianGan}${y.diZhi}", y.wuxing,
                        "日建${riZhi}冲${y.diZhi}，爻旺相故暗动"))
                }
            }
        }

        // 11. 月破检测
        val yuePo = mutableListOf<Int>()
        val yueWx = GanZhi.wuxingDiZhi[yueZhi] ?: ""
        for ((i, y) in yaos.withIndex()) {
            if (LIUCHONG_MAP[yueZhi] == y.diZhi) {
                val yaoGetsYue = (y.wuxing == yueWx || GanZhi.wxSheng[yueWx] == y.wuxing)
                if (!yaoGetsYue) yuePo.add(i)
            }
        }

        // 12. 反吟伏吟
        var isFanYin = false; var isFuYin = false
        if (changingIdx.isNotEmpty() && changedYaos != null) {
            var allChong = true; var allSame = true
            for (i in 0 until 6) {
                if (LIUCHONG_MAP[yaos[i].diZhi] != changedYaos[i].diZhi) allChong = false
                if (yaos[i].diZhi != changedYaos[i].diZhi) allSame = false
            }
            isFanYin = allChong; isFuYin = allSame
        }

        // 13. 三合局检测
        val sanHe = mutableListOf<SanHePanData>()
        val allDiZhi = yaos.map { it.diZhi }
        val sanHeJu = listOf(
            listOf("申","子","辰","水"), listOf("寅","午","戌","火"),
            listOf("亥","卯","未","木"), listOf("巳","酉","丑","金")
        )
        for (sh in sanHeJu) {
            val has = listOf(sh[0], sh[1], sh[2]).filter { it in allDiZhi }
            if (has.size == 3) {
                val hasDong = yaos.any { it.diZhi in listOf(sh[0], sh[1], sh[2]) && it.isDong }
                val hasAnDong = anDong.any { it.ganZhi.last().toString() in listOf(sh[0], sh[1], sh[2]) }
                if (hasDong || hasAnDong) {
                    sanHe.add(SanHePanData("${sh[0]}${sh[1]}${sh[2]}", sh[3],
                        "${sh[0]}${sh[1]}${sh[2]}三合${sh[3]}局，${sh[3]}五行力量大增"))
                }
            }
        }

        return GuaResult(
            HEXAGRAM_NAMES[baseKey] ?: "未知", baseKey,
            innerGua.name, outerGua.name, innerGua.wuxing, outerGua.wuxing,
            gongInfo.gong, gongWx, gongInfo.youHun, gongInfo.guiHun,
            yaos, changingIdx.isNotEmpty(), changingIdx,
            if (changingIdx.isNotEmpty()) HEXAGRAM_NAMES[changedKey] else null,
            if (changingIdx.isNotEmpty()) changedKey else null,
            changedYaos, dongBianAnalysis, fuShen,
            isLiuChongGua, kw, riGan, riZhi, yueZhi,
            isLiuHeGua, anDong, yuePo, isFanYin, isFuYin, sanHe
        )
    }

    fun formatGuaText(result: GuaResult): String {
        val sb = StringBuilder()
        sb.append("## ${result.gong}宫 · ${result.guaName}卦")
        if (result.youHun) sb.append("（游魂）")
        if (result.guiHun) sb.append("（归魂）")
        sb.append("\n\n")
        sb.append("${result.outerGua}（${result.outerWx}）上 · ${result.innerGua}（${result.innerWx}）下　宫属${result.gongWx}\n")
        if (result.isLiuChongGua) sb.append("⚡ 六冲卦 — 主事多变动、冲散\n")
        if (result.isLiuHeGua) sb.append("🤝 六合卦 — 主事和合、稳定\n")
        if (result.isFanYin) sb.append("⚠ 反吟卦 — 主反复不安、事多波折\n")
        if (result.isFuYin) sb.append("😩 伏吟卦 — 主呻吟痛苦、进退两难\n")
        sb.append("日建${result.riGan}${result.riZhi}　月建${result.yueZhi}月　空亡${result.kongWang.joinToString("·")}\n")
        if (result.hasChanging) {
            val dongNames = result.changingIdx.map { "${YAO_NAMES[it]}爻" }.joinToString("、")
            sb.append("\n**${result.guaName} → ${result.changedGuaName ?: ""}**　动爻：$dongNames\n")
        }
        sb.append("\n## 排盘\n\n")
        for (i in 5 downTo 0) {
            val y = result.yaos[i]
            val sy = if (y.isShi) "**世**" else if (y.isYing) "**应**" else ""
            val dm = if (y.isDong) "○" else ""
            val km = if (y.isKong) "⊘" else ""
            var cv = ""
            if (y.isDong && result.changedYaos != null) {
                val c = result.changedYaos[i]
                cv = "→ ${c.liuqin} ${c.tianGan}${c.diZhi}（${c.wuxing}）"
            }
            sb.append("${y.liuShen}　${y.liuqin}　${y.tianGan}${y.diZhi}（${y.wuxing}）${dm}　${sy}${km}　${cv}\n")
        }
        if (result.dongBianAnalysis.isNotEmpty()) {
            sb.append("\n## 动变分析\n\n")
            for (d in result.dongBianAnalysis) {
                sb.append("**${d.yaoPos}爻**：${d.benLiuqin}${d.benGanZhi}（${d.benWuxing}）→ ${d.bianLiuqin}${d.bianGanZhi}（${d.bianWuxing}）${d.relation}")
                if (d.teXing.isNotEmpty()) sb.append(" ${d.teXing}")
                sb.append("\n")
            }
        }
        if (result.fuShen.isNotEmpty()) {
            sb.append("\n## 伏神\n\n")
            for (fs in result.fuShen) {
                val underYao = result.yaos[fs.fuUnder]
                sb.append("**${fs.liuqin}**（${fs.tianGan}${fs.diZhi}${fs.wuxing}）伏于${underYao.posName}爻（${underYao.liuqin} ${underYao.tianGan}${underYao.diZhi}）之下\n")
            }
        }
        if (result.anDong.isNotEmpty()) {
            sb.append("\n## 暗动\n\n")
            for (ad in result.anDong) sb.append("**${ad.yaoPos}爻** ${ad.liuqin}${ad.ganZhi}（${ad.wuxing}）暗动 — ${ad.reason}\n")
        }
        if (result.yuePo.isNotEmpty()) {
            sb.append("\n## 月破\n\n")
            for (idx in result.yuePo) {
                val y = result.yaos[idx]
                sb.append("**${y.posName}爻** ${y.liuqin}${y.tianGan}${y.diZhi}（${y.wuxing}）月破 — 月建${result.yueZhi}冲之，爻不得月令，为月破，力量全失\n")
            }
        }
        if (result.sanHe.isNotEmpty()) {
            sb.append("\n## 三合局\n\n")
            for (sh in result.sanHe) sb.append("**${sh.branches}** 三合${sh.wuxing}局 — ${sh.detail}\n")
        }
        sb.append("\n## 旺衰分析\n\n")
        for (y in result.yaos) {
            val marks = mutableListOf<String>()
            if (y.isDong) marks.add("动")
            if (y.isShi) marks.add("世")
            if (y.isYing) marks.add("应")
            if (y.isKong) marks.add("空亡")
            if (result.yuePo.contains(y.pos)) marks.add("月破")
            if (result.anDong.any { it.yaoPos == y.posName }) marks.add("暗动")
            val markStr = if (marks.isEmpty()) "" else "[${marks.joinToString("·")}]"
            sb.append("${y.posName}爻 ${y.liuqin}${y.tianGan}${y.diZhi}（${y.wuxing}）${markStr}：月建${y.yueEffect}，日建${y.riWangShuai}\n")
        }
        return sb.toString()
    }
}
