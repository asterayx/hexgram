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

    // 六十甲子纳音表
    static let naYinTable: [String] = [
        "海中金","海中金","炉中火","炉中火","大林木","大林木",  // 甲子乙丑 丙寅丁卯 戊辰己巳
        "路旁土","路旁土","剑锋金","剑锋金","山头火","山头火",  // 庚午辛未 壬申癸酉 甲戌乙亥
        "涧下水","涧下水","城头土","城头土","白蜡金","白蜡金",  // 丙子丁丑 戊寅己卯 庚辰辛巳
        "杨柳木","杨柳木","泉中水","泉中水","屋上土","屋上土",  // 壬午癸未 甲申乙酉 丙戌丁亥
        "霹雳火","霹雳火","松柏木","松柏木","长流水","长流水",  // 戊子己丑 庚寅辛卯 壬辰癸巳
        "沙中金","沙中金","山下火","山下火","平地木","平地木",  // 甲午乙未 丙申丁酉 戊戌己亥
        "壁上土","壁上土","金箔金","金箔金","覆灯火","覆灯火",  // 庚子辛丑 壬寅癸卯 甲辰乙巳
        "天河水","天河水","大驿土","大驿土","钗钏金","钗钏金",  // 丙午丁未 戊申己酉 庚戌辛亥
        "桑柘木","桑柘木","大溪水","大溪水","沙中土","沙中土",  // 壬子癸丑 甲寅乙卯 丙辰丁巳
        "天上火","天上火","石榴木","石榴木","大海水","大海水",  // 戊午己未 庚申辛酉 壬戌癸亥
    ]

    /// 获取纳音：根据天干地支序号
    static func naYin(gan: String, zhi: String) -> String {
        let gIdx = ganIndex(gan)
        let zIdx = zhiIndex(zhi)
        // 六十甲子序号 = (天干序*6 + 地支序) % 60 的对应关系
        // 更简洁：用干支序号直接算
        let jiazi = (gIdx * 6 + zIdx * 5) % 60  // 等价于甲子序号
        return naYinTable[jiazi]
    }

    // 地支六合
    static let liuHe: [(String, String, String)] = [
        ("子","丑","土"), ("寅","亥","木"), ("卯","戌","火"),
        ("辰","酉","金"), ("巳","申","水"), ("午","未","火")
    ]

    // 地支三合局
    static let sanHe: [(String, String, String, String)] = [
        ("申","子","辰","水"), ("寅","午","戌","火"),
        ("亥","卯","未","木"), ("巳","酉","丑","金")
    ]

    // 地支三会方
    static let sanHui: [(String, String, String, String)] = [
        ("寅","卯","辰","木"), ("巳","午","未","火"),
        ("申","酉","戌","金"), ("亥","子","丑","水")
    ]

    // 地支六冲
    static let liuChong: [(String, String)] = [
        ("子","午"), ("丑","未"), ("寅","申"),
        ("卯","酉"), ("辰","戌"), ("巳","亥")
    ]

    // 地支三刑
    static let sanXing: [(String, String)] = [
        ("寅","巳"), ("巳","申"), ("申","寅"),  // 无恩之刑
        ("丑","戌"), ("戌","未"), ("未","丑"),  // 恃势之刑
        ("子","卯"), ("卯","子"),              // 无礼之刑
        ("辰","辰"), ("午","午"), ("酉","酉"), ("亥","亥")  // 自刑
    ]

    // 地支相害（六害/穿）
    static let xiangHai: [(String, String)] = [
        ("子","未"), ("丑","午"), ("寅","巳"),
        ("卯","辰"), ("申","亥"), ("酉","戌")
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

    // MARK: - 精确节气计算 (Jean Meeus 天文算法)

    /// 计算指定年份某节气的精确日期 (返回公历月日)
    /// solarTermIndex: 0=小寒, 1=大寒, 2=立春, 3=雨水, ... 23=大寒
    /// 节序：每年24节气，奇数为"节"(月首)，偶数为"气"(月中)
    /// 排盘用的"节"：立春(2)、惊蛰(4)、清明(6)、立夏(8)、芒种(10)、小暑(12)、立秋(14)、白露(16)、寒露(18)、立冬(20)、大雪(22)、小寒(0)
    private static func solarTermJDE(_ year: Int, _ termIndex: Int) -> Double {
        // 世纪 T (以 J2000.0 为基准)
        let y = Double(year) + (Double(termIndex) * 15.0 / 360.0)
        let jdY2000 = 2451545.0
        let t = (y - 2000.0) / 1000.0

        // 太阳黄经目标角度
        let targetLng = Double(termIndex) * 15.0  // 0°, 15°, 30°...

        // 太阳平黄经近似 (以春分为0°)
        // 使用 VSOP87 简化的太阳位置算法
        let l0 = 280.46646 + 36000.76983 * (y - 2000.0) / 100.0
        let m = 357.52911 + 35999.05029 * (y - 2000.0) / 100.0
        let mRad = m * .pi / 180.0

        // 太阳方程
        let c = (1.9146 - 0.004817 * t) * sin(mRad)
              + 0.019993 * sin(2 * mRad)
              + 0.00029 * sin(3 * mRad)

        let sunLng = (l0 + c).truncatingRemainder(dividingBy: 360.0)

        // 目标黄经（以小寒=285°为起点）
        let target = (targetLng + 285.0).truncatingRemainder(dividingBy: 360.0)
        var diff = target - sunLng
        if diff > 180 { diff -= 360 }
        if diff < -180 { diff += 360 }

        // 迭代修正 (每度约1天)
        let jd0 = jdY2000 + (y - 2000.0) * 365.25
        let jdApprox = jd0 + diff / 360.0 * 365.25

        return jdApprox
    }

    /// 将 JDE 转换为公历日期 (年, 月, 日)
    private static func jdeToDate(_ jd: Double) -> (year: Int, month: Int, day: Int) {
        let z = Int(jd + 0.5)
        let a: Int
        if z < 2299161 {
            a = z
        } else {
            let alpha = Int((Double(z) - 1867216.25) / 36524.25)
            a = z + 1 + alpha - alpha / 4
        }
        let b = a + 1524
        let c = Int((Double(b) - 122.1) / 365.25)
        let d = Int(365.25 * Double(c))
        let e = Int(Double(b - d) / 30.6001)

        let day = b - d - Int(30.6001 * Double(e))
        let month = e < 14 ? e - 1 : e - 13
        let year = month > 2 ? c - 4716 : c - 4715

        return (year, month, day)
    }

    /// 十二节(月首节气)对应的节气序号和地支
    /// 立春=寅月, 惊蛰=卯月, 清明=辰月, 立夏=巳月, 芒种=午月, 小暑=未月
    /// 立秋=申月, 白露=酉月, 寒露=戌月, 立冬=亥月, 大雪=子月, 小寒=丑月
    private static let jieTerms: [(termIndex: Int, zhi: String)] = [
        (2, "寅"),   // 立春
        (4, "卯"),   // 惊蛰
        (6, "辰"),   // 清明
        (8, "巳"),   // 立夏
        (10, "午"),  // 芒种
        (12, "未"),  // 小暑
        (14, "申"),  // 立秋
        (16, "酉"),  // 白露
        (18, "戌"),  // 寒露
        (20, "亥"),  // 立冬
        (22, "子"),  // 大雪
        (0, "丑"),   // 小寒 (属下一年计算)
    ]

    /// 查表缓存：存储年份→节气日期
    private static var solarTermCache: [Int: [(month: Int, day: Int, zhi: String)]] = [:]

    /// 获取指定年份的12个节(月首)的精确日期
    private static func getJieDates(_ year: Int) -> [(month: Int, day: Int, zhi: String)] {
        if let cached = solarTermCache[year] { return cached }

        var dates: [(month: Int, day: Int, zhi: String)] = []
        for jt in jieTerms {
            let calcYear = jt.termIndex == 0 ? year : year  // 小寒在当年1月
            let jde = solarTermJDE(calcYear, jt.termIndex)
            let d = jdeToDate(jde)
            dates.append((d.month, d.day, jt.zhi))
        }
        // 按月日排序
        dates.sort { ($0.month, $0.day) < ($1.month, $1.day) }

        solarTermCache[year] = dates
        return dates
    }

    /// 月支 (精确节气算法)
    static func yueZhi(_ year: Int, _ month: Int, _ day: Int) -> String {
        let dates = getJieDates(year)
        // 从后往前找第一个不超过当前日期的节
        for i in stride(from: dates.count - 1, through: 0, by: -1) {
            let d = dates[i]
            if month > d.month || (month == d.month && day >= d.day) {
                return d.zhi
            }
        }
        // 在第一个节之前，取上一年最后一个节(大雪→子月 或 小寒→丑月)
        let prevDates = getJieDates(year - 1)
        return prevDates.last?.zhi ?? "丑"
    }

    /// 获取立春精确日期
    static func liChunDate(_ year: Int) -> (month: Int, day: Int) {
        let jde = solarTermJDE(year, 2) // 立春 = term index 2
        let d = jdeToDate(jde)
        return (d.month, d.day)
    }

    /// 年柱干支 (以精确立春为界)
    static func nianGanZhi(_ year: Int, _ month: Int, _ day: Int) -> (gan: String, zhi: String) {
        var y = year
        let lc = liChunDate(year)
        if month < lc.month || (month == lc.month && day < lc.day) { y -= 1 }
        let gIdx = ((y - 4) % 10 + 600) % 10
        let zIdx = ((y - 4) % 12 + 600) % 12
        return (GanZhi.tianGan[gIdx], GanZhi.diZhi[zIdx])
    }

    /// 纳音查询
    static func naYin(gan: String, zhi: String) -> String {
        GanZhi.naYin(gan: gan, zhi: zhi)
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
