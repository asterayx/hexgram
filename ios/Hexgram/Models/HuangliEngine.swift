import Foundation

// MARK: - 黄历数据
struct HuangliResult {
    let year: Int
    let month: Int
    let day: Int
    let weekDay: String
    let riGan: String
    let riZhi: String
    let nianGan: String
    let nianZhi: String
    let yueZhi: String
    let lunarMonth: String
    let shengXiao: String
    let jianChu: String
    let erShiBaXiu: String
    let yi: String
    let ji: String
    let xiShen: String
    let caiShen: String
    let fuShen: String
    let chongSha: String
    let pengZuGan: String
    let pengZuZhi: String
}

// MARK: - 黄历引擎
class HuangliEngine {
    static let jianChuNames = ["建","除","满","平","定","执","破","危","成","收","开","闭"]
    static let erShiBaXiuNames = ["角","亢","氐","房","心","尾","箕","斗","牛","女","虚","危","室","壁","奎","娄","胃","昴","毕","觜","参","井","鬼","柳","星","张","翼","轸"]

    static let xiShenMap: [String: String] = [
        "甲":"东北","乙":"西北","丙":"西南","丁":"正南","戊":"东南",
        "己":"东北","庚":"西南","辛":"正南","壬":"正南","癸":"东南"
    ]
    static let caiShenMap: [String: String] = [
        "甲":"东北","乙":"东方","丙":"西南","丁":"西方","戊":"北方",
        "己":"南方","庚":"东方","辛":"南方","壬":"南方","癸":"东方"
    ]
    static let fuShenMap: [String: String] = [
        "甲":"东南","乙":"东北","丙":"正西","丁":"西北","戊":"北方",
        "己":"西南","庚":"西南","辛":"正东","壬":"东南","癸":"正北"
    ]

    static let yiMap: [String: String] = [
        "建":"出行 上任 动土 开市","除":"治病 祭祀 解除 扫舍","满":"祈福 嫁娶 立券 入宅",
        "平":"修饰 涂泥 安机 会亲","定":"冠带 嫁娶 开市 修造","执":"祭祀 捕捉 栽种 修造",
        "破":"治病 求医","危":"祭祀 安床 纳畜 经络","成":"嫁娶 开市 立券 入学 上任",
        "收":"纳财 求嗣 祭祀 安葬","开":"开市 立券 求医 动土 嫁娶","闭":"安葬 收藏 筑堤"
    ]
    static let jiMap: [String: String] = [
        "建":"嫁娶 动土 开仓","除":"嫁娶 远行","满":"栽种 动土 服药",
        "平":"祈福 出行","定":"出行 诉讼 纳畜","执":"开市 出行 搬迁",
        "破":"诸事不宜","危":"出行 登高 动土","成":"诉讼 出行",
        "收":"安葬 开市","开":"安葬","闭":"开市 出行 嫁娶 远行"
    ]

    static let pengZuGan: [String: String] = [
        "甲":"甲不开仓财物耗散","乙":"乙不栽植千株不长","丙":"丙不修灶必见灾殃",
        "丁":"丁不剃头头必生疮","戊":"戊不受田田主不祥","己":"己不破券二比并亡",
        "庚":"庚不经络织机虚张","辛":"辛不合酱主人不尝","壬":"壬不汲水更难提防",
        "癸":"癸不词讼理弱敌强"
    ]
    static let pengZuZhi: [String: String] = [
        "子":"子不问卜自惹祸殃","丑":"丑不冠带主不还乡","寅":"寅不祭祀神鬼不尝",
        "卯":"卯不穿井水泉不香","辰":"辰不哭泣必主重丧","巳":"巳不远行财物伏藏",
        "午":"午不苫盖屋主更张","未":"未不服药毒气入肠","申":"申不安床鬼祟入房",
        "酉":"酉不宴客醉坐颠狂","戌":"戌不吃犬作怪上床","亥":"亥不嫁娶不利新郎"
    ]

    static let chongMap: [String: String] = [
        "子":"马(南)","丑":"羊(西南)","寅":"猴(西)","卯":"鸡(西)",
        "辰":"狗(西北)","巳":"猪(北)","午":"鼠(北)","未":"牛(东北)",
        "申":"虎(东)","酉":"兔(东)","戌":"龙(东南)","亥":"蛇(南)"
    ]

    static func calculate(year: Int, month: Int, day: Int) -> HuangliResult {
        let ri = CalendarCalc.riGanZhi(year, month, day)
        let yz = CalendarCalc.yueZhi(year, month, day)
        let ng = CalendarCalc.nianGanZhi(year, month, day)

        let weekDays = ["日","一","二","三","四","五","六"]
        var cal = Calendar(identifier: .gregorian)
        cal.timeZone = TimeZone(identifier: "Asia/Shanghai")!
        let dow = cal.component(.weekday, from: cal.date(from: DateComponents(year: year, month: month, day: day))!) - 1

        // 建除十二神
        let yzIdx = GanZhi.zhiIndex(yz)
        let riZIdx = GanZhi.zhiIndex(ri.zhi)
        let jcIdx = ((riZIdx - yzIdx) % 12 + 12) % 12
        let jianChu = jianChuNames[jcIdx]

        // 二十八宿
        let refJDN = CalendarCalc.jdn(2000, 1, 1)
        let curJDN = CalendarCalc.jdn(year, month, day)
        let xiuIdx = ((Int(curJDN - refJDN) + 10) % 28 + 28) % 28
        let xiu = erShiBaXiuNames[xiuIdx]

        // 农历月
        let lunarMonths = ["正","二","三","四","五","六","七","八","九","十","冬","腊"]
        let mzIdx = GanZhi.zhiIndex(yz)
        let lm = lunarMonths[(mzIdx - 2 + 12) % 12]

        let yi = (yiMap[jianChu] ?? "").trimmingCharacters(in: .whitespaces)
        let ji = (jiMap[jianChu] ?? "诸事不宜").trimmingCharacters(in: .whitespaces)

        let zhiIdx = GanZhi.zhiIndex(ng.zhi)

        return HuangliResult(
            year: year, month: month, day: day,
            weekDay: weekDays[dow],
            riGan: ri.gan, riZhi: ri.zhi,
            nianGan: ng.gan, nianZhi: ng.zhi,
            yueZhi: yz,
            lunarMonth: lm,
            shengXiao: GanZhi.shengXiao[zhiIdx],
            jianChu: jianChu,
            erShiBaXiu: xiu,
            yi: yi, ji: ji,
            xiShen: xiShenMap[ri.gan] ?? "?",
            caiShen: caiShenMap[ri.gan] ?? "?",
            fuShen: fuShenMap[ri.gan] ?? "?",
            chongSha: chongMap[ri.zhi] ?? "?",
            pengZuGan: pengZuGan[ri.gan] ?? "",
            pengZuZhi: pengZuZhi[ri.zhi] ?? ""
        )
    }

    static func formatPlainText(_ r: HuangliResult) -> String {
        var t = "\(r.year)年\(r.month)月\(r.day)日 星期\(r.weekDay)\n"
        t += "日柱：\(r.riGan)\(r.riZhi) 年柱：\(r.nianGan)\(r.nianZhi) \(r.shengXiao)年\n"
        t += "月建：\(r.yueZhi)月 十二建星：\(r.jianChu)日 二十八宿：\(r.erShiBaXiu)宿\n"
        t += "宜：\(r.yi.isEmpty ? "无" : r.yi)\n忌：\(r.ji)\n"
        t += "喜神方位：\(r.xiShen) 财神方位：\(r.caiShen) 福神方位：\(r.fuShen)\n"
        t += "冲煞：冲\(r.chongSha)\n"
        t += "彭祖百忌：\(r.pengZuGan)；\(r.pengZuZhi)"
        return t
    }
}
