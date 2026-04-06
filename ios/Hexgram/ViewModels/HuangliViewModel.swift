import SwiftUI

@MainActor
class HuangliViewModel: ObservableObject {
    @Published var selectedDate = Date()
    @Published var result: HuangliResult?
    @Published var resultText = ""

    let aiService = AIService()

    func calculate() {
        let comps = Calendar.current.dateComponents([.year, .month, .day], from: selectedDate)
        guard let y = comps.year, let m = comps.month, let d = comps.day else { return }
        let r = HuangliEngine.calculate(year: y, month: m, day: d)
        result = r
        resultText = HuangliEngine.formatPlainText(r)
    }

    func aiRead() async {
        guard !resultText.isEmpty else { return }
        await aiService.callWorker(type: "huangli", data: resultText)
    }
}
