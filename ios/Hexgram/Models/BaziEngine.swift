import Foundation

// MARK: - 八字排盘结果
struct BaziPillar: Identifiable {
    let id = UUID()
    let label: String    // 年柱/月柱/日柱/时柱
    let gan: String
    let zhi: String
    let shiShen: String
    let wuxing: String
    let cangGan: [CangGanItem]
}

struct CangGanItem: Identifiable {
    let id = UUID()
    let gan: String
    let shiShen: String
    let wuxing: String
}

struct DaYunItem: Identifiable {
    let id = UUID()
    let gan: String
    let zhi: String
    let age: Int
    let year: Int
    let wuxing: String
}

struct BaziResult {
    let name: String
    let sex: String          // "M" or "F"
    let birthYear: Int
    let birthMonth: Int
    let birthDay: Int
    let birthHour: Int
    let pillars: [BaziPillar]
    let riGan: String
    let riGanWuxing: String
    let isStrong: Bool
    let wuxingCounts: [String: Double]
    let daYun: [DaYunItem]
    let isShunPai: Bool
    let liuNianGan: String
    let liuNianZhi: String
    let liuNianShiShen: String
    let currentYear: Int
    let shengXiao: String
}

// MARK: - 八字引擎
class BaziEngine {
    static func calculate(year: Int, month: Int, day: Int, hour: Int, sex: String, name: String = "") -> BaziResult {
        let ng = CalendarCalc.nianGanZhi(year, month, day)
        let yz = CalendarCalc.yueZhi(year, month, day)
        let yg = CalendarCalc.yueGan(nianGan: ng.gan, yueZhi: yz)
        let ri = CalendarCalc.riGanZhi(year, month, day)
        let si = CalendarCalc.shiGanZhi(riGan: ri.gan, hour: hour)

        let riGan = ri.gan

        // 四柱
        let pillarData: [(String, String, String)] = [
            ("年柱", ng.gan, ng.zhi),
            ("月柱", yg, yz),
            ("日柱", ri.gan, ri.zhi),
            ("时柱", si.gan, si.zhi)
        ]

        let pillars: [BaziPillar] = pillarData.enumerated().map { (i, p) in
            let ss = i == 2 ? "日主" : CalendarCalc.shiShen(riGan: riGan, otherGan: p.1)
            let wx = GanZhi.wuxingTianGan[p.1] ?? ""
            let cg = (GanZhi.cangGan[p.2] ?? []).map { g in
                CangGanItem(
                    gan: g,
                    shiShen: CalendarCalc.shiShen(riGan: riGan, otherGan: g),
                    wuxing: GanZhi.wuxingTianGan[g] ?? ""
                )
            }
            return BaziPillar(label: p.0, gan: p.1, zhi: p.2, shiShen: ss, wuxing: wx, cangGan: cg)
        }

        // 五行力量
        var wxC: [String: Double] = ["木":0,"火":0,"土":0,"金":0,"水":0]
        for p in pillars {
            wxC[p.wuxing, default: 0] += 1
            for cg in p.cangGan {
                wxC[cg.wuxing, default: 0] += 0.5
            }
        }

        let meWx = GanZhi.wuxingTianGan[riGan] ?? ""
        let shengMe = GanZhi.wxSheng.first(where: { $0.value == meWx })?.key ?? ""
        let helpS = (wxC[meWx] ?? 0) + (wxC[shengMe] ?? 0)
        let drainS = wxC.filter { $0.key != meWx && $0.key != shengMe }.values.reduce(0, +)
        let isStrong = helpS > drainS

        // 大运
        let nianYin = GanZhi.ganIndex(ng.gan) % 2
        let isShun = (nianYin == 0 && sex == "M") || (nianYin == 1 && sex == "F")
        let yueGIdx = GanZhi.ganIndex(yg)
        let yueZIdx = GanZhi.zhiIndex(yz)
        var daYun: [DaYunItem] = []
        for i in 1...8 {
            let off = isShun ? i : -i
            let dg = GanZhi.tianGan[((yueGIdx + off) % 10 + 10) % 10]
            let dz = GanZhi.diZhi[((yueZIdx + off) % 12 + 12) % 12]
            let age = i * 10 - 6
            daYun.append(DaYunItem(
                gan: dg, zhi: dz, age: age,
                year: year + age,
                wuxing: GanZhi.wuxingTianGan[dg] ?? ""
            ))
        }

        // 流年
        let now = Calendar.current.component(.year, from: Date())
        let lnGZ = CalendarCalc.nianGanZhi(now, 6, 1)

        let zhiIdx = GanZhi.zhiIndex(ng.zhi)

        return BaziResult(
            name: name,
            sex: sex,
            birthYear: year,
            birthMonth: month,
            birthDay: day,
            birthHour: hour,
            pillars: pillars,
            riGan: riGan,
            riGanWuxing: meWx,
            isStrong: isStrong,
            wuxingCounts: wxC,
            daYun: daYun,
            isShunPai: isShun,
            liuNianGan: lnGZ.gan,
            liuNianZhi: lnGZ.zhi,
            liuNianShiShen: CalendarCalc.shiShen(riGan: riGan, otherGan: lnGZ.gan),
            currentYear: now,
            shengXiao: GanZhi.shengXiao[zhiIdx]
        )
    }

    static func formatPlainText(_ result: BaziResult) -> String {
        var t = ""
        if !result.name.isEmpty { t += "\(result.name)，" }
        t += "\(result.sex == "M" ? "男" : "女")命，"
        let hourIdx = ((result.birthHour + 1) % 24) / 2
        t += "\(result.birthYear)年\(result.birthMonth)月\(result.birthDay)日\(GanZhi.diZhi[hourIdx])时生\n"
        t += "四柱：\(result.pillars.map { "\($0.gan)\($0.zhi)" }.joined(separator: " "))\n"
        t += "十神：\(result.pillars.map { $0.shiShen }.joined(separator: " "))\n"
        t += "藏干：\(result.pillars.map { "\($0.zhi)(\($0.cangGan.map { $0.gan }.joined(separator: ",")))" }.joined(separator: " "))\n"
        t += "日主\(result.riGan)（\(result.riGanWuxing)），\(result.isStrong ? "身旺" : "身弱")\n"
        t += "五行：\(GanZhi.wuxingAll.map { "\($0):\(String(format: "%.1f", result.wuxingCounts[$0] ?? 0))" }.joined(separator: " "))\n"
        t += "大运（\(result.isShunPai ? "顺" : "逆")排）：\(result.daYun.map { "\($0.gan)\($0.zhi)(\($0.year))" }.joined(separator: " "))\n"
        let curDy = result.daYun.first(where: { result.currentYear >= $0.year && result.currentYear < $0.year + 10 })
        if let dy = curDy {
            t += "当前大运：\(dy.gan)\(dy.zhi)\n"
        }
        t += "\(result.currentYear)年流年：\(result.liuNianGan)\(result.liuNianZhi)（对日主为\(result.liuNianShiShen)）"
        return t
    }
}
