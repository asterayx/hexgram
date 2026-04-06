import Foundation

// MARK: - 天干地支基础数据
enum GanZhi {
    static let tianGan = ["甲","乙","丙","丁","戊","己","庚","辛","壬","癸"]
    static let diZhi = ["子","丑","寅","卯","辰","巳","午","未","申","酉","戌","亥"]
    static let shengXiao = ["鼠","牛","虎","兔","龙","蛇","马","羊","猴","鸡","狗","猪"]

    static let wuxingTianGan: [String: String] = [
        "甲":"木","乙":"木","丙":"火","丁":"火","戊":"土",
        "己":"土","庚":"金","辛":"金","壬":"水","癸":"水"
    ]
    static let wuxingDiZhi: [String: String] = [
        "子":"水","丑":"土","寅":"木","卯":"木","辰":"土","巳":"火",
        "午":"火","未":"土","申":"金","酉":"金","戌":"土","亥":"水"
    ]

    // 五行生克
    static let wxSheng: [String: String] = ["木":"火","火":"土","土":"金","金":"水","水":"木"]
    static let wxKe: [String: String] = ["木":"土","土":"水","水":"火","火":"金","金":"木"]

    // 五行颜色名
    static let wuxingAll = ["木","火","土","金","水"]

    // 藏干
    static let cangGan: [String: [String]] = [
        "子":["癸"], "丑":["己","癸","辛"], "寅":["甲","丙","戊"],
        "卯":["乙"], "辰":["戊","乙","癸"], "巳":["丙","庚","戊"],
        "午":["丁","己"], "未":["己","丁","乙"], "申":["庚","壬","戊"],
        "酉":["辛"], "戌":["戊","辛","丁"], "亥":["壬","甲"]
    ]

    static func ganIndex(_ g: String) -> Int {
        tianGan.firstIndex(of: g) ?? 0
    }

    static func zhiIndex(_ z: String) -> Int {
        diZhi.firstIndex(of: z) ?? 0
    }
}

// MARK: - 儒略日计算
struct CalendarCalc {
    /// 公历转儒略日数
    static func jdn(_ year: Int, _ month: Int, _ day: Int) -> Double {
        var y = year, m = month
        if m <= 2 { y -= 1; m += 12 }
        let a = y / 100
        let b = 2 - a + a / 4
        return Double(Int(365.25 * Double(y + 4716))) + Double(Int(30.6001 * Double(m + 1))) + Double(day) + Double(b) - 1524.5
    }

    /// 日柱干支
    static func riGanZhi(_ year: Int, _ month: Int, _ day: Int) -> (gan: String, zhi: String) {
        let j = Int(jdn(year, month, day) + 0.5)
        let gIdx = ((j - 1) % 10 + 10) % 10
        let zIdx = ((j + 1) % 12 + 12) % 12
        return (GanZhi.tianGan[gIdx], GanZhi.diZhi[zIdx])
    }

    /// 节气月支近似表: (月, 日, 地支)
    private static let jieQi: [(Int, Int, String)] = [
        (2, 4, "寅"), (3, 6, "卯"), (4, 5, "辰"), (5, 6, "巳"),
        (6, 6, "午"), (7, 7, "未"), (8, 7, "申"), (9, 8, "酉"),
        (10, 8, "戌"), (11, 7, "亥"), (12, 7, "子"), (1, 6, "丑")
    ]

    /// 月支 (根据节气)
    static func yueZhi(_ year: Int, _ month: Int, _ day: Int) -> String {
        for i in stride(from: jieQi.count - 1, through: 0, by: -1) {
            let jq = jieQi[i]
            if month > jq.0 || (month == jq.0 && day >= jq.1) {
                return jq.2
            }
        }
        return "丑"
    }

    /// 年柱干支 (以立春为界)
    static func nianGanZhi(_ year: Int, _ month: Int, _ day: Int) -> (gan: String, zhi: String) {
        var y = year
        if month < 2 || (month == 2 && day < 4) { y -= 1 }
        let gIdx = ((y - 4) % 10 + 600) % 10
        let zIdx = ((y - 4) % 12 + 600) % 12
        return (GanZhi.tianGan[gIdx], GanZhi.diZhi[zIdx])
    }

    /// 月干 (年上起月法)
    static func yueGan(nianGan: String, yueZhi: String) -> String {
        let base: [String: Int] = ["甲":2,"己":2,"乙":4,"庚":4,"丙":6,"辛":6,"丁":8,"壬":8,"戊":0,"癸":0]
        let off = (GanZhi.zhiIndex(yueZhi) - 2 + 12) % 12
        return GanZhi.tianGan[((base[nianGan] ?? 0) + off) % 10]
    }

    /// 时柱干支 (日上起时法)
    static func shiGanZhi(riGan: String, hour: Int) -> (gan: String, zhi: String) {
        let idx = ((hour + 1) % 24) / 2
        let base: [String: Int] = ["甲":0,"己":0,"乙":2,"庚":2,"丙":4,"辛":4,"丁":6,"壬":6,"戊":8,"癸":8]
        return (GanZhi.tianGan[((base[riGan] ?? 0) + idx) % 10], GanZhi.diZhi[idx])
    }

    /// 十神
    static func shiShen(riGan: String, otherGan: String) -> String {
        let me = GanZhi.wuxingTianGan[riGan]!
        let other = GanZhi.wuxingTianGan[otherGan]!
        let sameYinYang = GanZhi.ganIndex(riGan) % 2 == GanZhi.ganIndex(otherGan) % 2
        if me == other { return sameYinYang ? "比肩" : "劫财" }
        if GanZhi.wxSheng[me] == other { return sameYinYang ? "食神" : "伤官" }
        if GanZhi.wxKe[me] == other { return sameYinYang ? "偏财" : "正财" }
        if GanZhi.wxKe[other] == me { return sameYinYang ? "七杀" : "正官" }
        if GanZhi.wxSheng[other] == me { return sameYinYang ? "偏印" : "正印" }
        return "?"
    }

    /// 空亡计算
    static func kongWang(riGan: String, riZhi: String) -> [String] {
        let tgIdx = GanZhi.ganIndex(riGan)
        let dzIdx = GanZhi.zhiIndex(riZhi)
        var diff = dzIdx - tgIdx
        if diff < 0 { diff += 12 }
        let xunStart = (dzIdx - diff + 120) % 12
        return [GanZhi.diZhi[(xunStart + 10) % 12], GanZhi.diZhi[(xunStart + 11) % 12]]
    }
}
