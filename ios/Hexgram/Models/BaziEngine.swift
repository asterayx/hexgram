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

struct ShenShaItem: Identifiable {
    let id = UUID()
    let name: String
    let pillar: String   // 在哪柱
    let description: String
}

struct DiZhiRelation: Identifiable {
    let id = UUID()
    let type: String     // 六合/三合/六冲/三刑/相害
    let branches: String // 涉及的地支
    let detail: String
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
    // 新增
    let naYinPillars: [String]       // 四柱纳音
    let shenSha: [ShenShaItem]       // 神煞
    let diZhiRelations: [DiZhiRelation]  // 合冲刑害
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

        // 纳音
        let naYinPillars = pillarData.map { GanZhi.naYin(gan: $0.1, zhi: $0.2) }

        // 神煞
        let shenSha = calculateShenSha(riGan: riGan, pillars: pillarData)

        // 地支关系
        let allZhi = pillarData.map { $0.2 }
        let diZhiRelations = analyzeDiZhiRelations(allZhi, pillarLabels: pillarData.map { $0.0 })

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
            shengXiao: GanZhi.shengXiao[zhiIdx],
            naYinPillars: naYinPillars,
            shenSha: shenSha,
            diZhiRelations: diZhiRelations
        )
    }

    // MARK: - 神煞计算
    private static func calculateShenSha(riGan: String, pillars: [(String, String, String)]) -> [ShenShaItem] {
        var result: [ShenShaItem] = []

        // 天乙贵人：以日干查
        let tianYi: [String: [String]] = [
            "甲":["丑","未"],"乙":["子","申"],"丙":["亥","酉"],"丁":["亥","酉"],
            "戊":["丑","未"],"己":["子","申"],"庚":["丑","未"],"辛":["寅","午"],
            "壬":["卯","巳"],"癸":["卯","巳"]
        ]
        if let guiRen = tianYi[riGan] {
            for (i, p) in pillars.enumerated() {
                if guiRen.contains(p.2) {
                    result.append(ShenShaItem(name: "天乙贵人", pillar: p.0, description: "逢凶化吉、遇难呈祥之神"))
                }
            }
        }

        // 文昌：以日干查
        let wenChang: [String: String] = [
            "甲":"巳","乙":"午","丙":"申","丁":"酉","戊":"申",
            "己":"酉","庚":"亥","辛":"子","壬":"寅","癸":"卯"
        ]
        if let wc = wenChang[riGan] {
            for p in pillars {
                if p.2 == wc {
                    result.append(ShenShaItem(name: "文昌", pillar: p.0, description: "主聪明好学、利考试文书"))
                }
            }
        }

        // 驿马：以日支查（年支亦可）
        let yiMa: [String: String] = [
            "寅":"申","申":"寅","巳":"亥","亥":"巳",
            "子":"寅","午":"申","卯":"巳","酉":"亥",
            "辰":"寅","戌":"申","丑":"亥","未":"巳"
        ]
        let riZhi = pillars[2].2
        if let ym = yiMa[riZhi] {
            for p in pillars where p.0 != "日柱" {
                if p.2 == ym {
                    result.append(ShenShaItem(name: "驿马", pillar: p.0, description: "主奔波走动、迁移变动"))
                }
            }
        }

        // 桃花（咸池）：以日支查
        let taoHua: [String: String] = [
            "寅":"卯","午":"卯","戌":"卯",
            "申":"酉","子":"酉","辰":"酉",
            "巳":"午","酉":"午","丑":"午",
            "亥":"子","卯":"子","未":"子"
        ]
        if let th = taoHua[riZhi] {
            for p in pillars where p.0 != "日柱" {
                if p.2 == th {
                    result.append(ShenShaItem(name: "桃花", pillar: p.0, description: "主人缘佳、异性缘旺"))
                }
            }
        }

        // 华盖：以日支查
        let huaGai: [String: String] = [
            "寅":"戌","午":"戌","戌":"戌",
            "申":"辰","子":"辰","辰":"辰",
            "巳":"丑","酉":"丑","丑":"丑",
            "亥":"未","卯":"未","未":"未"
        ]
        if let hg = huaGai[riZhi] {
            for p in pillars where p.0 != "日柱" {
                if p.2 == hg {
                    result.append(ShenShaItem(name: "华盖", pillar: p.0, description: "主孤高、艺术才华、宗教缘"))
                }
            }
        }

        // 羊刃：以日干查
        let yangRen: [String: String] = [
            "甲":"卯","乙":"寅","丙":"午","丁":"巳","戊":"午",
            "己":"巳","庚":"酉","辛":"申","壬":"子","癸":"亥"
        ]
        if let yr = yangRen[riGan] {
            for p in pillars {
                if p.2 == yr {
                    result.append(ShenShaItem(name: "羊刃", pillar: p.0, description: "刚烈之星、主性格刚强、防灾伤"))
                }
            }
        }

        return result
    }

    // MARK: - 地支关系分析
    private static func analyzeDiZhiRelations(_ allZhi: [String], pillarLabels: [String]) -> [DiZhiRelation] {
        var result: [DiZhiRelation] = []

        // 六合
        for he in GanZhi.liuHe {
            var found: [Int] = []
            for (i, z) in allZhi.enumerated() {
                if z == he.0 || z == he.1 { found.append(i) }
            }
            if found.count >= 2 {
                let has0 = allZhi.contains(he.0)
                let has1 = allZhi.contains(he.1)
                if has0 && has1 {
                    result.append(DiZhiRelation(
                        type: "六合",
                        branches: "\(he.0)\(he.1)合\(he.2)",
                        detail: "\(he.0)\(he.1)六合化\(he.2)，主和合、亲近"
                    ))
                }
            }
        }

        // 三合局
        for sh in GanZhi.sanHe {
            let has = [sh.0, sh.1, sh.2].filter { z in allZhi.contains(z) }
            if has.count == 3 {
                result.append(DiZhiRelation(
                    type: "三合局",
                    branches: "\(sh.0)\(sh.1)\(sh.2)合\(sh.3)局",
                    detail: "三合\(sh.3)局成化，\(sh.3)五行力量大增"
                ))
            } else if has.count == 2 {
                let missing = [sh.0, sh.1, sh.2].first { !allZhi.contains($0) }!
                result.append(DiZhiRelation(
                    type: "半合",
                    branches: "\(has.joined())半合\(sh.3)局",
                    detail: "缺\(missing)，半合\(sh.3)局，力量稍弱"
                ))
            }
        }

        // 六冲
        for ch in GanZhi.liuChong {
            let has0 = allZhi.contains(ch.0)
            let has1 = allZhi.contains(ch.1)
            if has0 && has1 {
                let idx0 = allZhi.firstIndex(of: ch.0)!
                let idx1 = allZhi.firstIndex(of: ch.1)!
                result.append(DiZhiRelation(
                    type: "六冲",
                    branches: "\(ch.0)\(ch.1)冲",
                    detail: "\(pillarLabels[idx0])\(ch.0)冲\(pillarLabels[idx1])\(ch.1)，主动荡变化"
                ))
            }
        }

        // 三刑（查非自刑的组合）
        let xingPairs: [(String, String, String)] = [
            ("寅","巳","无恩之刑"), ("巳","申","无恩之刑"), ("申","寅","无恩之刑"),
            ("丑","戌","恃势之刑"), ("戌","未","恃势之刑"), ("未","丑","恃势之刑"),
            ("子","卯","无礼之刑"), ("卯","子","无礼之刑"),
        ]
        for xp in xingPairs {
            if allZhi.contains(xp.0) && allZhi.contains(xp.1) {
                // 避免重复
                let key = [xp.0, xp.1].sorted().joined()
                if !result.contains(where: { $0.type == "三刑" && $0.branches.contains(key) }) {
                    result.append(DiZhiRelation(
                        type: "三刑",
                        branches: "\(xp.0)\(xp.1)刑",
                        detail: "\(xp.2)：\(xp.0)刑\(xp.1)，主是非口舌、刑伤"
                    ))
                }
            }
        }
        // 自刑
        for zhi in ["辰","午","酉","亥"] {
            if allZhi.filter({ $0 == zhi }).count >= 2 {
                result.append(DiZhiRelation(
                    type: "自刑",
                    branches: "\(zhi)\(zhi)自刑",
                    detail: "\(zhi)见\(zhi)为自刑，主自我困扰"
                ))
            }
        }

        // 相害
        for hai in GanZhi.xiangHai {
            if allZhi.contains(hai.0) && allZhi.contains(hai.1) {
                result.append(DiZhiRelation(
                    type: "相害",
                    branches: "\(hai.0)\(hai.1)害",
                    detail: "\(hai.0)\(hai.1)相害，主暗中损耗、不顺"
                ))
            }
        }

        return result
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
        t += "大运（\(result.isShunPai ? "顺" : "逆")排）：\(result.daYun.map { "\($0.gan)\($0.zhi)(\($0.age)-\($0.age + 9)岁,\($0.year)-\($0.year + 9)年)" }.joined(separator: " "))\n"
        let curDy = result.daYun.first(where: { result.currentYear >= $0.year && result.currentYear < $0.year + 10 })
        if let dy = curDy {
            t += "当前大运：\(dy.gan)\(dy.zhi)\n"
        }
        t += "\(result.currentYear)年流年：\(result.liuNianGan)\(result.liuNianZhi)（对日主为\(result.liuNianShiShen)）\n"
        t += "纳音：\(result.naYinPillars.joined(separator: " "))\n"
        if !result.shenSha.isEmpty {
            t += "神煞：\(result.shenSha.map { "\($0.name)(\($0.pillar))" }.joined(separator: "、"))\n"
        }
        if !result.diZhiRelations.isEmpty {
            t += "地支关系：\(result.diZhiRelations.map { $0.branches }.joined(separator: "、"))"
        }
        return t
    }
}
