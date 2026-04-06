import WidgetKit
import SwiftUI

// MARK: - Widget Timeline Provider
struct HuangliProvider: TimelineProvider {
    func placeholder(in context: Context) -> HuangliEntry {
        HuangliEntry(date: Date(), riGan: "甲", riZhi: "子", jianChu: "建", xiu: "角", yi: "祈福 嫁娶", ji: "出行", chong: "马(南)", naYin: "海中金")
    }

    func getSnapshot(in context: Context, completion: @escaping (HuangliEntry) -> Void) {
        completion(makeEntry(for: Date()))
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<HuangliEntry>) -> Void) {
        let entry = makeEntry(for: Date())
        // 每天午夜更新
        let nextMidnight = Calendar.current.startOfDay(for: Date()).addingTimeInterval(86400)
        let timeline = Timeline(entries: [entry], policy: .after(nextMidnight))
        completion(timeline)
    }

    private func makeEntry(for date: Date) -> HuangliEntry {
        let cal = Calendar.current
        let y = cal.component(.year, from: date)
        let m = cal.component(.month, from: date)
        let d = cal.component(.day, from: date)

        let ri = CalendarCalcWidget.riGanZhi(y, m, d)
        let yz = CalendarCalcWidget.yueZhi(y, m, d)

        let jianChuNames = ["建","除","满","平","定","执","破","危","成","收","开","闭"]
        let erShiBaXiu = ["角","亢","氐","房","心","尾","箕","斗","牛","女","虚","危","室","壁","奎","娄","胃","昴","毕","觜","参","井","鬼","柳","星","张","翼","轸"]

        let yzIdx = CalendarCalcWidget.zhiIndex(yz)
        let riZIdx = CalendarCalcWidget.zhiIndex(ri.zhi)
        let jcIdx = ((riZIdx - yzIdx) % 12 + 12) % 12

        let refJDN = CalendarCalcWidget.jdn(2000, 1, 1)
        let curJDN = CalendarCalcWidget.jdn(y, m, d)
        let xiuIdx = ((Int(curJDN - refJDN) + 10) % 28 + 28) % 28

        let yiMap: [String: String] = [
            "建":"出行 上任 动土 开市","除":"治病 祭祀 解除","满":"祈福 嫁娶 立券 入宅",
            "平":"修饰 涂泥 安机","定":"冠带 嫁娶 开市 修造","执":"祭祀 捕捉 栽种 修造",
            "破":"治病 求医","危":"祭祀 安床 纳畜","成":"嫁娶 开市 立券 入学 上任",
            "收":"纳财 求嗣 祭祀 安葬","开":"开市 立券 求医 动土 嫁娶","闭":"安葬 收藏 筑堤"
        ]
        let jiMap: [String: String] = [
            "建":"嫁娶 动土 开仓","除":"嫁娶 远行","满":"栽种 动土 服药",
            "平":"祈福 出行","定":"出行 诉讼 纳畜","执":"开市 出行 搬迁",
            "破":"诸事不宜","危":"出行 登高 动土","成":"诉讼 出行",
            "收":"安葬 开市","开":"安葬","��":"开市 出行 嫁娶 远行"
        ]
        let chongMap: [String: String] = [
            "子":"马(南)","丑":"羊(西南)","寅":"猴(西)","卯":"鸡(西)",
            "辰":"狗(西北)","巳":"猪(北)","午":"鼠(北)","未":"牛(东北)",
            "申":"虎(东)","酉":"兔(东)","戌":"龙(东南)","亥":"蛇(南)"
        ]

        let jc = jianChuNames[jcIdx]
        // 纳音
        let naYinTable: [String] = [
            "海��金","海中金","炉中火","炉中火","大林木","大林木",
            "路旁土","路旁土","剑锋金","剑锋金","山头火","山头火",
            "涧下水","涧下水","城头土","城头土","白蜡金","白蜡金",
            "杨柳木","杨柳木","泉中水","泉中水","屋上土","屋上土",
            "霹雳火","霹雳火","松柏木","松柏木","长流水","长流水",
            "沙中金","沙中金","山下火","山下火","平地木","平地木",
            "壁上土","壁上土","金箔金","金箔金","覆灯火","覆灯火",
            "天河水","天河水","大驿土","大驿土","钗钏金","钗钏金",
            "桑柘木","桑柘木","大溪水","大溪水","沙中土","沙中土",
            "天上火","天上火","石榴木","石榴木","大海水","大海水",
        ]
        let gIdx = CalendarCalcWidget.ganIndex(ri.gan)
        let zIdx = CalendarCalcWidget.zhiIndex(ri.zhi)
        let jiazi = (gIdx * 6 + zIdx * 5) % 60
        let naYin = naYinTable[jiazi]

        return HuangliEntry(
            date: date,
            riGan: ri.gan, riZhi: ri.zhi,
            jianChu: jc, xiu: erShiBaXiu[xiuIdx],
            yi: yiMap[jc] ?? "", ji: jiMap[jc] ?? "诸事不宜",
            chong: chongMap[ri.zhi] ?? "?",
            naYin: naYin
        )
    }
}

// MARK: - Widget Entry
struct HuangliEntry: TimelineEntry {
    let date: Date
    let riGan: String
    let riZhi: String
    let jianChu: String
    let xiu: String
    let yi: String
    let ji: String
    let chong: String
    let naYin: String
}

// MARK: - 简化的日历计算（Widget不能引用主target）
enum CalendarCalcWidget {
    static let tianGan = ["甲","乙","丙","丁","戊","己","庚","辛","壬","癸"]
    static let diZhi = ["子","丑","寅","卯","辰","巳","午","未","申","酉","戌","亥"]

    static func ganIndex(_ g: String) -> Int { tianGan.firstIndex(of: g) ?? 0 }
    static func zhiIndex(_ z: String) -> Int { diZhi.firstIndex(of: z) ?? 0 }

    static func jdn(_ year: Int, _ month: Int, _ day: Int) -> Double {
        var y = year, m = month
        if m <= 2 { y -= 1; m += 12 }
        let a = y / 100; let b = 2 - a + a / 4
        return Double(Int(365.25 * Double(y + 4716))) + Double(Int(30.6001 * Double(m + 1))) + Double(day) + Double(b) - 1524.5
    }

    static func riGanZhi(_ year: Int, _ month: Int, _ day: Int) -> (gan: String, zhi: String) {
        let j = Int(jdn(year, month, day) + 0.5)
        return (tianGan[((j - 1) % 10 + 10) % 10], diZhi[((j + 1) % 12 + 12) % 12])
    }

    static func yueZhi(_ year: Int, _ month: Int, _ day: Int) -> String {
        let jq: [(Int, Int, String)] = [
            (2,4,"寅"),(3,6,"卯"),(4,5,"辰"),(5,6,"巳"),(6,6,"午"),(7,7,"未"),
            (8,7,"申"),(9,8,"酉"),(10,8,"戌"),(11,7,"亥"),(12,7,"子"),(1,6,"丑")
        ]
        for i in stride(from: jq.count - 1, through: 0, by: -1) {
            if month > jq[i].0 || (month == jq[i].0 && day >= jq[i].1) { return jq[i].2 }
        }
        return "丑"
    }
}

// MARK: - Widget View
struct HuangliWidgetView: View {
    var entry: HuangliEntry

    @Environment(\.widgetFamily) var family

    var body: some View {
        switch family {
        case .systemSmall:
            smallView
        case .systemMedium:
            mediumView
        default:
            smallView
        }
    }

    private var smallView: some View {
        VStack(spacing: 4) {
            // 日期
            Text(entry.date, style: .date)
                .font(.system(size: 10))
                .foregroundColor(.secondary)

            // 干支大字
            Text("\(entry.riGan)\(entry.riZhi)")
                .font(.system(size: 28, weight: .medium, design: .serif))
                .foregroundColor(Color(hex: "c9a96e"))

            Text(entry.naYin)
                .font(.system(size: 11, design: .serif))
                .foregroundColor(Color(hex: "8b7355"))

            Text("\(entry.jianChu)日 · \(entry.xiu)宿")
                .font(.system(size: 10, design: .serif))
                .foregroundColor(.secondary)

            Divider()

            HStack(spacing: 12) {
                HStack(spacing: 2) {
                    Circle().fill(Color.green.opacity(0.6)).frame(width: 5, height: 5)
                    Text(entry.yi.components(separatedBy: " ").prefix(2).joined(separator: " "))
                        .font(.system(size: 9)).foregroundColor(.green)
                }
                HStack(spacing: 2) {
                    Circle().fill(Color.red.opacity(0.6)).frame(width: 5, height: 5)
                    Text(entry.ji.components(separatedBy: " ").prefix(2).joined(separator: " "))
                        .font(.system(size: 9)).foregroundColor(.red)
                }
            }
        }
        .padding(12)
        .containerBackground(for: .widget) {
            Color(hex: "0d0b08")
        }
    }

    private var mediumView: some View {
        HStack(spacing: 16) {
            // 左侧：干支
            VStack(spacing: 4) {
                Text(entry.date, style: .date)
                    .font(.system(size: 10))
                    .foregroundColor(.secondary)

                Text("\(entry.riGan)\(entry.riZhi)")
                    .font(.system(size: 32, weight: .medium, design: .serif))
                    .foregroundColor(Color(hex: "c9a96e"))

                Text(entry.naYin)
                    .font(.system(size: 12, design: .serif))
                    .foregroundColor(Color(hex: "8b7355"))

                Text("\(entry.jianChu)日 · \(entry.xiu)宿")
                    .font(.system(size: 11, design: .serif))
                    .foregroundColor(.secondary)
            }
            .frame(maxWidth: .infinity)

            Divider()

            // 右侧：宜忌
            VStack(alignment: .leading, spacing: 8) {
                VStack(alignment: .leading, spacing: 2) {
                    Text("宜").font(.system(size: 11, weight: .medium, design: .serif)).foregroundColor(.green)
                    Text(entry.yi)
                        .font(.system(size: 10, design: .serif))
                        .foregroundColor(Color(hex: "e8dcc8"))
                        .lineLimit(2)
                }

                VStack(alignment: .leading, spacing: 2) {
                    Text("忌").font(.system(size: 11, weight: .medium, design: .serif)).foregroundColor(.red)
                    Text(entry.ji)
                        .font(.system(size: 10, design: .serif))
                        .foregroundColor(Color(hex: "e8dcc8"))
                        .lineLimit(2)
                }

                Text("冲\(entry.chong)")
                    .font(.system(size: 9, design: .serif))
                    .foregroundColor(.secondary)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .padding(14)
        .containerBackground(for: .widget) {
            Color(hex: "0d0b08")
        }
    }
}

// Widget Color extension (standalone for widget target)
extension Color {
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let r = Double((int >> 16) & 0xFF) / 255
        let g = Double((int >> 8) & 0xFF) / 255
        let b = Double(int & 0xFF) / 255
        self.init(.sRGB, red: r, green: g, blue: b, opacity: 1)
    }
}

// MARK: - Widget Configuration
@main
struct HexgramWidget: Widget {
    let kind: String = "HexgramHuangliWidget"

    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: HuangliProvider()) { entry in
            HuangliWidgetView(entry: entry)
        }
        .configurationDisplayName("每日黄历")
        .description("显示当日干支、纳音、宜忌、建星")
        .supportedFamilies([.systemSmall, .systemMedium])
    }
}
