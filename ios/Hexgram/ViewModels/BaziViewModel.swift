import SwiftUI

@MainActor
class BaziViewModel: ObservableObject {
    @Published var selectedDate = Calendar.current.date(from: DateComponents(year: 1990, month: 1, day: 1))!
    @Published var selectedHour = 11
    @Published var sex = "M"
    @Published var name = ""
    @Published var result: BaziResult?
    @Published var resultText = ""

    let aiService = AIService()

    static let hourOptions: [(label: String, value: Int)] = [
        ("子时", 23), ("丑时", 1), ("寅时", 3), ("卯时", 5),
        ("辰时", 7), ("巳时", 9), ("午时", 11), ("未时", 13),
        ("申时", 15), ("酉时", 17), ("戌时", 19), ("亥时", 21)
    ]

    func calculate() {
        let comps = Calendar.current.dateComponents([.year, .month, .day], from: selectedDate)
        guard let y = comps.year, let m = comps.month, let d = comps.day else { return }

        let r = BaziEngine.calculate(
            year: y, month: m, day: d,
            hour: selectedHour, sex: sex, name: name
        )
        result = r
        resultText = BaziEngine.formatPlainText(r)
    }

    func aiRead() async {
        guard !resultText.isEmpty else { return }
        await aiService.callWorker(type: "bazi", data: resultText)
    }
}
