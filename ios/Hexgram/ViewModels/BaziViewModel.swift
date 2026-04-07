import SwiftUI

@MainActor
class BaziViewModel: ObservableObject {
    @Published var selectedDate = Calendar.current.date(from: DateComponents(year: 1990, month: 1, day: 1))!
    @Published var selectedHour = 11
    @Published var sex = "M"
    @Published var name = ""
    @Published var result: BaziResult?
    @Published var resultText = ""

    // 阴历模式
    @Published var isLunar = false
    @Published var lunarYear = 1990
    @Published var lunarMonth = 1
    @Published var lunarDay = 1
    @Published var lunarDisplayString = ""

    let aiService = AIService()

    static let hourOptions: [(label: String, value: Int)] = [
        ("子时", 23), ("丑时", 1), ("寅时", 3), ("卯时", 5),
        ("辰时", 7), ("巳时", 9), ("午时", 11), ("未时", 13),
        ("申时", 15), ("酉时", 17), ("戌时", 19), ("亥时", 21)
    ]

    func calculate() {
        var y: Int, m: Int, d: Int

        if isLunar {
            // 阴历→公历转换
            guard let solar = LunarCalendar.lunarToSolar(year: lunarYear, month: lunarMonth, day: lunarDay) else {
                return
            }
            y = solar.year; m = solar.month; d = solar.day
            let lunarDate = LunarCalendar.LunarDate(year: lunarYear, month: lunarMonth, day: lunarDay, isLeapMonth: false)
            lunarDisplayString = lunarDate.displayString
        } else {
            let comps = Calendar.current.dateComponents([.year, .month, .day], from: selectedDate)
            guard let cy = comps.year, let cm = comps.month, let cd = comps.day else { return }
            y = cy; m = cm; d = cd
            lunarDisplayString = ""
        }

        let r = BaziEngine.calculate(
            year: y, month: m, day: d,
            hour: selectedHour, sex: sex, name: name
        )
        result = r
        var text = BaziEngine.formatPlainText(r)
        if isLunar, !lunarDisplayString.isEmpty {
            text = "【\(lunarDisplayString)】\n" + text
        }
        resultText = text
    }

    func aiRead() async {
        guard !resultText.isEmpty else { return }
        await aiService.callWorker(type: "bazi", data: resultText)
    }

    // 阴历月最大天数
    var lunarMaxDay: Int {
        LunarCalendar.daysInLunarMonth(year: lunarYear, month: lunarMonth)
    }
}
