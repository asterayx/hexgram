import Foundation

// MARK: - 八卦基础数据
struct BaGua {
    let name: String
    let wuxing: String
    let najiaYang: String  // 内卦天干
    let najiaYin: String   // 外卦天干
}

let BAGUA_TABLE: [String: BaGua] = [
    "111": BaGua(name: "乾", wuxing: "金", najiaYang: "甲", najiaYin: "壬"),
    "000": BaGua(name: "坤", wuxing: "土", najiaYang: "乙", najiaYin: "癸"),
    "010": BaGua(name: "坎", wuxing: "水", najiaYang: "戊", najiaYin: "戊"),
    "101": BaGua(name: "离", wuxing: "火", najiaYang: "己", najiaYin: "己"),
    "100": BaGua(name: "震", wuxing: "木", najiaYang: "庚", najiaYin: "庚"),
    "011": BaGua(name: "巽", wuxing: "木", najiaYang: "辛", najiaYin: "辛"),
    "001": BaGua(name: "艮", wuxing: "土", najiaYang: "丙", najiaYin: "丙"),
    "110": BaGua(name: "兑", wuxing: "金", najiaYang: "丁", najiaYin: "丁"),
]

// MARK: - 纳甲地支
let NAJIA_DIZHI: [String: (inner: [String], outer: [String])] = [
    "乾": (["子","寅","辰"], ["午","申","戌"]),
    "坤": (["未","巳","卯"], ["丑","亥","酉"]),
    "坎": (["寅","辰","午"], ["申","戌","子"]),
    "离": (["卯","丑","亥"], ["酉","未","巳"]),
    "震": (["子","寅","辰"], ["午","申","戌"]),
    "巽": (["丑","亥","酉"], ["未","巳","卯"]),
    "艮": (["辰","午","申"], ["戌","子","寅"]),
    "兑": (["巳","卯","丑"], ["亥","酉","未"]),
]

// 经卦象名：用于构造全卦名（如"泽火革"）
let GUA_XIANG: [String: String] = ["乾":"天","坤":"地","坎":"水","离":"火","震":"雷","巽":"风","艮":"山","兑":"泽"]

// MARK: - 六十四卦名表
let HEXAGRAM_NAMES: [String: String] = [
    "111111":"乾","111110":"夬","111101":"大有","111100":"大壮","111011":"小畜","111010":"需","111001":"大畜","111000":"泰",
    "110111":"履","110110":"兑","110101":"睽","110100":"归妹","110011":"中孚","110010":"节","110001":"损","110000":"临",
    "101111":"同人","101110":"革","101101":"离","101100":"丰","101011":"家人","101010":"既济","101001":"贲","101000":"明夷",
    "100111":"无妄","100110":"随","100101":"噬嗑","100100":"震","100011":"益","100010":"屯","100001":"颐","100000":"复",
    "011111":"姤","011110":"大过","011101":"鼎","011100":"恒","011011":"巽","011010":"井","011001":"蛊","011000":"升",
    "010111":"讼","010110":"困","010101":"未济","010100":"解","010011":"涣","010010":"坎","010001":"蒙","010000":"师",
    "001111":"遁","001110":"咸","001101":"旅","001100":"小过","001011":"渐","001010":"蹇","001001":"艮","001000":"谦",
    "000111":"否","000110":"萃","000101":"晋","000100":"豫","000011":"观","000010":"比","000001":"剥","000000":"坤"
]

// MARK: - 六神
let LIUSHEN_ORDER = ["青龙","朱雀","勾陈","螣蛇","白虎","玄武"]
let LIUSHEN_START: [String: Int] = [
    "甲":0,"乙":0,"丙":1,"丁":1,"戊":2,"己":3,"庚":4,"辛":4,"壬":5,"癸":5
]

// MARK: - 六冲
let LIUCHONG: [String: String] = [
    "子":"午","丑":"未","寅":"申","卯":"酉","辰":"戌","巳":"亥",
    "午":"子","未":"丑","申":"寅","酉":"卯","戌":"辰","亥":"巳"
]

// MARK: - 十二长生
let CHANGSHENG: [String: [String: String]] = [
    "木": ["亥":"长生","子":"沐浴","丑":"冠带","寅":"临官","卯":"帝旺","辰":"衰","巳":"病","午":"死","未":"墓","申":"绝","酉":"胎","戌":"养"],
    "火": ["寅":"长生","卯":"沐浴","辰":"冠带","巳":"临官","午":"帝旺","未":"衰","申":"病","酉":"死","戌":"墓","亥":"绝","子":"胎","丑":"养"],
    "土": ["寅":"长生","卯":"沐浴","辰":"冠带","巳":"临官","午":"帝旺","未":"衰","申":"病","酉":"死","戌":"墓","亥":"绝","子":"胎","丑":"养"],
    "金": ["巳":"长生","午":"沐浴","未":"冠带","申":"临官","酉":"帝旺","戌":"衰","亥":"病","子":"死","丑":"墓","寅":"绝","卯":"胎","辰":"养"],
    "水": ["申":"长生","酉":"沐浴","戌":"冠带","亥":"临官","子":"帝旺","丑":"衰","寅":"病","卯":"死","辰":"墓","巳":"绝","午":"胎","未":"养"],
]

// MARK: - 爻位名称
let YAO_NAMES = ["初","二","三","四","五","上"]
let YAO_LABELS: [Int: String] = [6:"老阴", 7:"少阳", 8:"少阴", 9:"老阳"]

// MARK: - 八宫信息
struct GongInfo {
    let gong: String
    let shi: Int
    let ying: Int
    let youHun: Bool
    let guiHun: Bool
}

// MARK: - 爻数据
struct YaoData: Identifiable {
    let id = UUID()
    let pos: Int
    let posName: String
    let yinYang: String    // "阳" or "阴"
    let value: Int         // 6/7/8/9
    let valueLabel: String
    let isDong: Bool
    let tianGan: String
    let diZhi: String
    let wuxing: String
    let liuqin: String
    var isShi: Bool
    var isYing: Bool
    var liuShen: String
    var isKong: Bool
    var yueEffect: String
    var riWangShuai: String
}

// MARK: - 伏神数据
struct FuShenData: Identifiable {
    let id = UUID()
    let liuqin: String
    let tianGan: String
    let diZhi: String
    let wuxing: String
    let fuUnder: Int
}

// MARK: - 动变分析数据
struct DongBianData: Identifiable {
    let id = UUID()
    let yaoPos: String
    let benLiuqin: String
    let benGanZhi: String
    let benWuxing: String
    let bianLiuqin: String
    let bianGanZhi: String
    let bianWuxing: String
    let relation: String
    let teXing: String  // 化墓/化绝等
}

// MARK: - 暗动数据
struct AnDongData: Identifiable {
    let id = UUID()
    let yaoPos: String
    let liuqin: String
    let ganZhi: String
    let wuxing: String
    let reason: String  // 日冲/月冲
}

// MARK: - 三合局数据
struct SanHePanData: Identifiable {
    let id = UUID()
    let branches: String
    let wuxing: String
    let detail: String
}

// MARK: - 完整卦象结果
struct GuaResult {
    let guaName: String
    let guaKey: String
    let innerGua: String
    let outerGua: String
    let innerWx: String
    let outerWx: String
    let gong: String
    let gongWx: String
    let youHun: Bool
    let guiHun: Bool
    let yaos: [YaoData]
    let hasChanging: Bool
    let changingIdx: [Int]
    let changedGuaName: String?
    let changedGuaKey: String?
    let changedYaos: [YaoData]?
    let dongBianAnalysis: [DongBianData]
    let fuShen: [FuShenData]
    let isLiuChongGua: Bool
    let kongWang: [String]
    let riGan: String
    let riZhi: String
    let yueZhi: String
    // 新增
    let isLiuHeGua: Bool         // 六合卦
    let anDong: [AnDongData]     // 暗动
    let yuePo: [Int]             // 月破爻位
    let isFanYin: Bool           // 反吟
    let isFuYin: Bool            // 伏吟
    let sanHe: [SanHePanData]    // 三合局
}

// MARK: - 纳甲引擎
class NajiaEngine {

    /// 构造全卦名：八纯卦用"X为Y"，其余用"外象内象名"（如"泽火革"）
    static func fullGuaName(_ key: String) -> String {
        guard let short = HEXAGRAM_NAMES[key] else { return "未知" }
        let ik = String(key.prefix(3)), ok = String(key.suffix(3))
        guard let ig = BAGUA_TABLE[ik], let og = BAGUA_TABLE[ok] else { return short }
        if ik == ok { return "\(ig.name)为\(GUA_XIANG[ig.name] ?? "")" }
        return "\(GUA_XIANG[og.name] ?? "")\(GUA_XIANG[ig.name] ?? "")\(short)"
    }

    // MARK: 八宫表
    private static var _baGongTable: [String: GongInfo]?

    static var baGongTable: [String: GongInfo] {
        if let t = _baGongTable { return t }
        let t = buildBaGong()
        _baGongTable = t
        return t
    }

    private static func buildBaGong() -> [String: GongInfo] {
        let pureGua = ["111","000","010","101","100","011","001","110"]
        var result: [String: GongInfo] = [:]

        for pg in pureGua {
            guard let gua = BAGUA_TABLE[pg] else { continue }
            let bits = pg.map { $0 == "1" ? 1 : 0 }

            // 第1卦：八纯卦
            let pure = bits + bits
            result[pure.map(String.init).joined()] = GongInfo(gong: gua.name, shi: 5, ying: 2, youHun: false, guiHun: false)

            // 第2-6卦
            for i in 0..<5 {
                var cur = bits + bits
                for j in 0...i { cur[j] = 1 - cur[j] }
                let key = cur.map(String.init).joined()
                result[key] = GongInfo(gong: gua.name, shi: i, ying: (i + 3) % 6, youHun: false, guiHun: false)
            }

            // 第7卦（游魂）
            var you = bits + bits
            for j in 0..<5 { you[j] = 1 - you[j] }
            you[3] = 1 - you[3]
            result[you.map(String.init).joined()] = GongInfo(gong: gua.name, shi: 3, ying: 0, youHun: true, guiHun: false)

            // 第8卦（归魂）
            var gui = you
            gui[0] = bits[0]; gui[1] = bits[1]; gui[2] = bits[2]
            result[gui.map(String.init).joined()] = GongInfo(gong: gua.name, shi: 2, ying: 5, youHun: false, guiHun: true)
        }
        return result
    }

    // MARK: 六亲
    static func getLiuqin(gongWx: String, yaoWx: String) -> String {
        if gongWx == yaoWx { return "兄弟" }
        if GanZhi.wxSheng[gongWx] == yaoWx { return "子孙" }
        if GanZhi.wxSheng[yaoWx] == gongWx { return "父母" }
        if GanZhi.wxKe[gongWx] == yaoWx { return "妻财" }
        if GanZhi.wxKe[yaoWx] == gongWx { return "官鬼" }
        return "?"
    }

    // MARK: 六神
    static func getLiuShen(riGan: String) -> [String] {
        let start = LIUSHEN_START[riGan] ?? 0
        return (0..<6).map { LIUSHEN_ORDER[(start + $0) % 6] }
    }

    // MARK: 旺衰
    static func getWangShuai(wx: String, zhi: String) -> String {
        guard let status = CHANGSHENG[wx]?[zhi] else { return "平" }
        if ["临官","帝旺"].contains(status) { return "旺" }
        if ["长生","冠带","沐浴"].contains(status) { return "相" }
        if ["墓","死","绝"].contains(status) { return "衰" }
        if ["病","胎","养"].contains(status) { return "弱" }
        return "平"
    }

    // MARK: 月建影响
    static func getYueJianEffect(yaoDzWx: String, yueZhi: String) -> String {
        let yueWx = GanZhi.wuxingDiZhi[yueZhi] ?? ""
        if yaoDzWx == yueWx { return "月建比和，旺" }
        if GanZhi.wxSheng[yueWx] == yaoDzWx { return "月建生之，旺" }
        if GanZhi.wxKe[yueWx] == yaoDzWx { return "月建克之，弱" }
        if GanZhi.wxSheng[yaoDzWx] == yueWx { return "泄气于月建，平" }
        if GanZhi.wxKe[yaoDzWx] == yueWx { return "耗力于月建，平" }
        return "平"
    }

    // MARK: - 核心：装卦
    static func zhuangGua(lines: [Int], riGan: String = "甲", riZhi: String = "子", yueZhi: String = "子") -> GuaResult {
        // 1. 基本爻信息
        let baseBits = lines.map { ($0 == 7 || $0 == 9) ? 1 : 0 }
        let changedBits = lines.map { v -> Int in
            if v == 6 { return 1 }  // 老阴变阳
            if v == 9 { return 0 }  // 老阳变阴
            return (v == 7) ? 1 : 0
        }
        let changingIdx = lines.enumerated().compactMap { $0.element == 6 || $0.element == 9 ? $0.offset : nil }

        let baseKey = baseBits.map(String.init).joined()
        let changedKey = changedBits.map(String.init).joined()
        let innerKey = String(baseKey.prefix(3))
        let outerKey = String(baseKey.suffix(3))

        let innerGua = BAGUA_TABLE[innerKey]!
        let outerGua = BAGUA_TABLE[outerKey]!

        // 2. 八宫归属
        let gongInfo = baGongTable[baseKey] ?? GongInfo(gong: innerGua.name, shi: 4, ying: 1, youHun: false, guiHun: false)
        let gongKey = BAGUA_TABLE.first(where: { $0.value.name == gongInfo.gong })?.key ?? "111"
        let gongWx = BAGUA_TABLE[gongKey]?.wuxing ?? "土"

        // 3. 六神
        let liushenArr = getLiuShen(riGan: riGan)

        // 4. 空亡
        let kw = CalendarCalc.kongWang(riGan: riGan, riZhi: riZhi)

        // 5. 纳甲装卦
        var yaos: [YaoData] = []
        for i in 0..<6 {
            let isInner = i < 3
            let gua = isInner ? innerGua : outerGua
            let localIdx = isInner ? i : i - 3
            let tg = isInner ? gua.najiaYang : gua.najiaYin
            let dzArr = isInner ? NAJIA_DIZHI[gua.name]!.inner : NAJIA_DIZHI[gua.name]!.outer
            let dz = dzArr[localIdx]
            let dzWx = GanZhi.wuxingDiZhi[dz] ?? ""
            let liuqin = getLiuqin(gongWx: gongWx, yaoWx: dzWx)
            let yueEffect = getYueJianEffect(yaoDzWx: dzWx, yueZhi: yueZhi)
            let riWang = getWangShuai(wx: dzWx, zhi: riZhi)

            yaos.append(YaoData(
                pos: i,
                posName: YAO_NAMES[i],
                yinYang: baseBits[i] == 1 ? "阳" : "阴",
                value: lines[i],
                valueLabel: YAO_LABELS[lines[i]] ?? "",
                isDong: lines[i] == 6 || lines[i] == 9,
                tianGan: tg,
                diZhi: dz,
                wuxing: dzWx,
                liuqin: liuqin,
                isShi: i == gongInfo.shi,
                isYing: i == gongInfo.ying,
                liuShen: liushenArr[i],
                isKong: kw.contains(dz),
                yueEffect: yueEffect,
                riWangShuai: riWang
            ))
        }

        // 6. 变卦
        var changedYaos: [YaoData]?
        if !changingIdx.isEmpty {
            let chInnerKey = String(changedKey.prefix(3))
            let chOuterKey = String(changedKey.suffix(3))
            let chInnerGua = BAGUA_TABLE[chInnerKey]!
            let chOuterGua = BAGUA_TABLE[chOuterKey]!

            changedYaos = (0..<6).map { i -> YaoData in
                let isInner = i < 3
                let gua = isInner ? chInnerGua : chOuterGua
                let localIdx = isInner ? i : i - 3
                let tg = isInner ? gua.najiaYang : gua.najiaYin
                let dzArr = isInner ? NAJIA_DIZHI[gua.name]!.inner : NAJIA_DIZHI[gua.name]!.outer
                let dz = dzArr[localIdx]
                let dzWx = GanZhi.wuxingDiZhi[dz] ?? ""
                let liuqin = getLiuqin(gongWx: gongWx, yaoWx: dzWx)
                return YaoData(
                    pos: i, posName: YAO_NAMES[i],
                    yinYang: changedBits[i] == 1 ? "阳" : "阴",
                    value: 0, valueLabel: "",
                    isDong: false, tianGan: tg, diZhi: dz,
                    wuxing: dzWx, liuqin: liuqin,
                    isShi: false, isYing: false,
                    liuShen: "", isKong: false,
                    yueEffect: "", riWangShuai: ""
                )
            }
        }

        // 7. 伏神
        let presentLiuqin = Set(yaos.map { $0.liuqin })
        let allLiuqin = ["父母","兄弟","子孙","妻财","官鬼"]
        let missingLiuqin = allLiuqin.filter { !presentLiuqin.contains($0) }
        var fuShen: [FuShenData] = []

        if !missingLiuqin.isEmpty {
            let pureGua = BAGUA_TABLE[gongKey]!
            for i in 0..<6 {
                let isInner = i < 3
                let localIdx = isInner ? i : i - 3
                let tg = isInner ? pureGua.najiaYang : pureGua.najiaYin
                let dzArr = isInner ? NAJIA_DIZHI[pureGua.name]!.inner : NAJIA_DIZHI[pureGua.name]!.outer
                let dz = dzArr[localIdx]
                let dzWx = GanZhi.wuxingDiZhi[dz] ?? ""
                let lq = getLiuqin(gongWx: gongWx, yaoWx: dzWx)
                if missingLiuqin.contains(lq) {
                    fuShen.append(FuShenData(liuqin: lq, tianGan: tg, diZhi: dz, wuxing: dzWx, fuUnder: i))
                }
            }
        }

        // 8. 动变分析
        var dongBianAnalysis: [DongBianData] = []
        if !changingIdx.isEmpty, let cYaos = changedYaos {
            for idx in changingIdx {
                let benYao = yaos[idx]
                let bianYao = cYaos[idx]
                let bWx = benYao.wuxing
                let cWx = bianYao.wuxing
                var relation = ""
                if bWx == cWx { relation = "比和（化同）" }
                else if GanZhi.wxSheng[bWx] == cWx { relation = "化泄" }
                else if GanZhi.wxSheng[cWx] == bWx { relation = "化回头生（吉）" }
                else if GanZhi.wxKe[bWx] == cWx { relation = "化克出" }
                else if GanZhi.wxKe[cWx] == bWx { relation = "化回头克（凶）" }

                let bianStatus = CHANGSHENG[bWx]?[bianYao.diZhi] ?? ""
                var teXing = ""
                if bianStatus == "墓" { teXing = "化入墓" }
                else if bianStatus == "绝" { teXing = "化入绝" }

                dongBianAnalysis.append(DongBianData(
                    yaoPos: benYao.posName,
                    benLiuqin: benYao.liuqin,
                    benGanZhi: "\(benYao.tianGan)\(benYao.diZhi)",
                    benWuxing: bWx,
                    bianLiuqin: bianYao.liuqin,
                    bianGanZhi: "\(bianYao.tianGan)\(bianYao.diZhi)",
                    bianWuxing: cWx,
                    relation: relation,
                    teXing: teXing
                ))
            }
        }

        // 9. 六冲六合判断
        var isLiuChongGua = true
        var isLiuHeGua = true
        let liuHeMap: [String: String] = ["子":"丑","丑":"子","寅":"亥","亥":"寅","卯":"戌","戌":"卯","辰":"酉","酉":"辰","巳":"申","申":"巳","午":"未","未":"午"]
        for i in 0..<3 {
            if LIUCHONG[yaos[i].diZhi] != yaos[i + 3].diZhi {
                isLiuChongGua = false
            }
            if liuHeMap[yaos[i].diZhi] != yaos[i + 3].diZhi {
                isLiuHeGua = false
            }
        }

        // 10. 暗动检测：日建冲静爻使之暗动（爻旺相时被日冲则暗动）
        var anDong: [AnDongData] = []
        for (i, y) in yaos.enumerated() {
            if !y.isDong { // 仅静爻
                // 日冲：日支与爻支六冲
                if LIUCHONG[riZhi] == y.diZhi {
                    let ws = getWangShuai(wx: y.wuxing, zhi: riZhi)
                    // 旺相之爻被日冲则暗动，衰弱之爻被日冲则为日破
                    if ws == "旺" || ws == "相" {
                        anDong.append(AnDongData(
                            yaoPos: y.posName, liuqin: y.liuqin,
                            ganZhi: "\(y.tianGan)\(y.diZhi)", wuxing: y.wuxing,
                            reason: "日建\(riZhi)冲\(y.diZhi)，爻旺相故暗动"
                        ))
                    }
                }
            }
        }

        // 11. 月破检测：月建冲爻（爻休囚时被月冲为月破）
        var yuePo: [Int] = []
        let yueWx = GanZhi.wuxingDiZhi[yueZhi] ?? ""
        for (i, y) in yaos.enumerated() {
            if LIUCHONG[yueZhi] == y.diZhi {
                // 月冲且爻不得月令生扶 → 月破
                let yaoGetsYue = (y.wuxing == yueWx || GanZhi.wxSheng[yueWx] == y.wuxing)
                if !yaoGetsYue {
                    yuePo.append(i)
                }
            }
        }

        // 12. 反吟伏吟
        var isFanYin = false
        var isFuYin = false
        if !changingIdx.isEmpty, let cYaos = changedYaos {
            // 反吟：本卦与变卦六冲（所有爻地支互冲）
            var allChong = true
            var allSame = true
            for i in 0..<6 {
                if LIUCHONG[yaos[i].diZhi] != cYaos[i].diZhi { allChong = false }
                if yaos[i].diZhi != cYaos[i].diZhi { allSame = false }
            }
            isFanYin = allChong
            isFuYin = allSame
        }

        // 13. 三合局检测
        var sanHe: [SanHePanData] = []
        let allDiZhi = yaos.map { $0.diZhi }
        let sanHeJu: [(String, String, String, String)] = [
            ("申","子","辰","水"), ("寅","午","戌","火"),
            ("亥","卯","未","木"), ("巳","酉","丑","金")
        ]
        for sh in sanHeJu {
            let has = [sh.0, sh.1, sh.2].filter { z in allDiZhi.contains(z) }
            if has.count == 3 {
                // 至少有一个是动爻才算成局
                let hasDong = yaos.filter { [sh.0, sh.1, sh.2].contains($0.diZhi) && $0.isDong }.count > 0
                if hasDong || anDong.contains(where: { [sh.0, sh.1, sh.2].contains(String($0.ganZhi.suffix(1))) }) {
                    sanHe.append(SanHePanData(
                        branches: "\(sh.0)\(sh.1)\(sh.2)",
                        wuxing: sh.3,
                        detail: "\(sh.0)\(sh.1)\(sh.2)三合\(sh.3)局，\(sh.3)五行力量大增"
                    ))
                }
            }
        }

        return GuaResult(
            guaName: fullGuaName(baseKey),
            guaKey: baseKey,
            innerGua: innerGua.name,
            outerGua: outerGua.name,
            innerWx: innerGua.wuxing,
            outerWx: outerGua.wuxing,
            gong: gongInfo.gong,
            gongWx: gongWx,
            youHun: gongInfo.youHun,
            guiHun: gongInfo.guiHun,
            yaos: yaos,
            hasChanging: !changingIdx.isEmpty,
            changingIdx: changingIdx,
            changedGuaName: !changingIdx.isEmpty ? fullGuaName(changedKey) : nil,
            changedGuaKey: !changingIdx.isEmpty ? changedKey : nil,
            changedYaos: changedYaos,
            dongBianAnalysis: dongBianAnalysis,
            fuShen: fuShen,
            isLiuChongGua: isLiuChongGua,
            kongWang: kw,
            riGan: riGan,
            riZhi: riZhi,
            yueZhi: yueZhi,
            isLiuHeGua: isLiuHeGua,
            anDong: anDong,
            yuePo: yuePo,
            isFanYin: isFanYin,
            isFuYin: isFuYin,
            sanHe: sanHe
        )
    }

    // MARK: - 格式化排盘文本
    static func formatGuaText(_ result: GuaResult) -> String {
        var t = "## \(result.gong)宫 · \(result.guaName)卦"
        if result.youHun { t += "（游魂）" }
        if result.guiHun { t += "（归魂）" }
        t += "\n\n"
        t += "\(result.outerGua)（\(result.outerWx)）上 · \(result.innerGua)（\(result.innerWx)）下　宫属\(result.gongWx)\n"
        if result.isLiuChongGua { t += "⚡ 六冲卦 — 主事多变动、冲散\n" }
        if result.isLiuHeGua { t += "🤝 六合卦 — 主事和合、稳定\n" }
        if result.isFanYin { t += "⚠ 反吟卦 — 主反复不安、事多波折\n" }
        if result.isFuYin { t += "😩 伏吟卦 — 主呻吟痛苦、进退两难\n" }
        t += "日建\(result.riGan)\(result.riZhi)　月建\(result.yueZhi)月　空亡\(result.kongWang.joined(separator: "·"))\n"

        if result.hasChanging {
            t += "\n**\(result.guaName) → \(result.changedGuaName ?? "")**　动爻：\(result.changingIdx.map { YAO_NAMES[$0] + "爻" }.joined(separator: "、"))\n"
        }

        t += "\n## 排盘\n\n"
        for i in stride(from: 5, through: 0, by: -1) {
            let y = result.yaos[i]
            let sy = y.isShi ? "**世**" : (y.isYing ? "**应**" : "")
            let dm = y.isDong ? "○" : ""
            let km = y.isKong ? "⊘" : ""
            var cv = ""
            if y.isDong, let cYaos = result.changedYaos {
                let c = cYaos[i]
                cv = "→ \(c.liuqin) \(c.tianGan)\(c.diZhi)（\(c.wuxing)）"
            }
            t += "\(y.liuShen)　\(y.liuqin)　\(y.tianGan)\(y.diZhi)（\(y.wuxing)）\(dm)　\(sy)\(km)　\(cv)\n"
        }

        if !result.dongBianAnalysis.isEmpty {
            t += "\n## 动变分析\n\n"
            for d in result.dongBianAnalysis {
                t += "**\(d.yaoPos)爻**：\(d.benLiuqin)\(d.benGanZhi)（\(d.benWuxing)）→ \(d.bianLiuqin)\(d.bianGanZhi)（\(d.bianWuxing)）\(d.relation)"
                if !d.teXing.isEmpty { t += " \(d.teXing)" }
                t += "\n"
            }
        }

        if !result.fuShen.isEmpty {
            t += "\n## 伏神\n\n"
            for fs in result.fuShen {
                let underYao = result.yaos[fs.fuUnder]
                t += "**\(fs.liuqin)**（\(fs.tianGan)\(fs.diZhi)\(fs.wuxing)）伏于\(underYao.posName)爻（\(underYao.liuqin) \(underYao.tianGan)\(underYao.diZhi)）之下\n"
            }
        }

        // 暗动
        if !result.anDong.isEmpty {
            t += "\n## 暗动\n\n"
            for ad in result.anDong {
                t += "**\(ad.yaoPos)爻** \(ad.liuqin)\(ad.ganZhi)（\(ad.wuxing)）暗动 — \(ad.reason)\n"
            }
        }

        // 月破
        if !result.yuePo.isEmpty {
            t += "\n## 月破\n\n"
            for idx in result.yuePo {
                let y = result.yaos[idx]
                t += "**\(y.posName)爻** \(y.liuqin)\(y.tianGan)\(y.diZhi)（\(y.wuxing)）月破 — 月建\(result.yueZhi)冲之，爻不得月令，为月破，力量全失\n"
            }
        }

        // 三合局
        if !result.sanHe.isEmpty {
            t += "\n## 三合局\n\n"
            for sh in result.sanHe {
                t += "**\(sh.branches)** 三合\(sh.wuxing)局 — \(sh.detail)\n"
            }
        }

        t += "\n## 旺衰分析\n\n"
        for y in result.yaos {
            var marks: [String] = []
            if y.isDong { marks.append("动") }
            if y.isShi { marks.append("世") }
            if y.isYing { marks.append("应") }
            if y.isKong { marks.append("空亡") }
            if result.yuePo.contains(y.pos) { marks.append("月破") }
            if result.anDong.contains(where: { $0.yaoPos == y.posName }) { marks.append("暗动") }
            let markStr = marks.isEmpty ? "" : "[\(marks.joined(separator: "·"))]"
            t += "\(y.posName)爻 \(y.liuqin)\(y.tianGan)\(y.diZhi)（\(y.wuxing)）\(markStr)：月建\(y.yueEffect)，日建\(y.riWangShuai)\n"
        }

        return t
    }
}
