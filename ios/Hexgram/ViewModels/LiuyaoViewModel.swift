import SwiftUI

@MainActor
class LiuyaoViewModel: ObservableObject {
    @Published var lines: [Int] = []
    @Published var phase: Phase = .input
    @Published var isTossing = false
    @Published var question = ""
    @Published var selectedDate = Date()
    @Published var selectedHour = 11  // 午时
    @Published var guaResult: GuaResult?
    @Published var resultText = ""

    // AI
    let aiService = AIService()

    enum Phase {
        case input, reading, done
    }

    // 日期干支显示
    var dateGanZhiText: String {
        let comps = Calendar.current.dateComponents([.year, .month, .day], from: selectedDate)
        guard let y = comps.year, let m = comps.month, let d = comps.day else { return "" }
        let ri = CalendarCalc.riGanZhi(y, m, d)
        let yz = CalendarCalc.yueZhi(y, m, d)
        let ng = CalendarCalc.nianGanZhi(y, m, d)
        let yg = CalendarCalc.yueGan(nianGan: ng.gan, yueZhi: yz)
        return "日柱\(ri.gan)\(ri.zhi)　月建\(yg)\(yz)"
    }

    var riGan: String {
        let comps = Calendar.current.dateComponents([.year, .month, .day], from: selectedDate)
        let ri = CalendarCalc.riGanZhi(comps.year!, comps.month!, comps.day!)
        return ri.gan
    }

    var riZhi: String {
        let comps = Calendar.current.dateComponents([.year, .month, .day], from: selectedDate)
        let ri = CalendarCalc.riGanZhi(comps.year!, comps.month!, comps.day!)
        return ri.zhi
    }

    var yueZhi: String {
        let comps = Calendar.current.dateComponents([.year, .month, .day], from: selectedDate)
        return CalendarCalc.yueZhi(comps.year!, comps.month!, comps.day!)
    }

    // 时辰列表
    static let hourOptions: [(label: String, value: Int)] = [
        ("子时", 23), ("丑时", 1), ("寅时", 3), ("卯时", 5),
        ("辰时", 7), ("巳时", 9), ("午时", 11), ("未时", 13),
        ("申时", 15), ("酉时", 17), ("戌时", 19), ("亥时", 21)
    ]

    // MARK: - 摇卦
    func toss() {
        guard lines.count < 6, !isTossing else { return }
        isTossing = true

        // 模拟投掷延迟
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.7) { [weak self] in
            guard let self else { return }
            // 三枚铜钱
            let coins = (0..<3).map { _ in Bool.random() ? 2 : 3 }
            let value = coins.reduce(0, +) // 6/7/8/9
            self.lines.append(value)
            self.isTossing = false
        }
    }

    func undo() {
        guard !lines.isEmpty, !isTossing else { return }
        lines.removeLast()
    }

    func reset() {
        lines = []
        phase = .input
        guaResult = nil
        resultText = ""
        aiService.result = nil
        aiService.error = nil
    }

    // MARK: - 排盘
    func doReading() {
        guard lines.count == 6 else { return }
        phase = .reading

        let result = NajiaEngine.zhuangGua(
            lines: lines,
            riGan: riGan,
            riZhi: riZhi,
            yueZhi: yueZhi
        )
        guaResult = result
        resultText = NajiaEngine.formatGuaText(result)

        // 附加经典文献
        if let classic = GAODAO[result.guaKey] {
            resultText += "\n## 高岛易断\n\n"
            resultText += "**【卦断】** \(classic.judgment)\n\n"
            if !classic.yao.isEmpty {
                resultText += "**【爻断】**\n"
                for (i, line) in lines.enumerated() {
                    let marker = (line == 6 || line == 9) ? " ★" : ""
                    resultText += "\(YAO_NAMES[i])爻\(marker)：\(classic.yao[i])\n"
                }
            }
        }

        // 黄金策
        var hjKeys: [String] = []
        let q = question
        if q.contains(where: { "财钱利润业绩收入投资".contains($0) }) { hjKeys.append("求财") }
        if q.contains(where: { "工作事业升职官".contains($0) }) { hjKeys.append("事业") }
        if q.contains(where: { "婚恋感情对象桃花".contains($0) }) { hjKeys.append("婚姻") }
        if q.contains(where: { "病健康身体医".contains($0) }) { hjKeys.append("疾病") }
        if q.contains(where: { "出行旅行程出差".contains($0) }) { hjKeys.append("出行") }
        if q.contains(where: { "诉官司纠纷法".contains($0) }) { hjKeys.append("诉讼") }
        if hjKeys.isEmpty { hjKeys.append("求财") }

        resultText += "\n## 黄金策断语\n\n"
        for key in hjKeys {
            if let text = HUANGJINCE[key] {
                resultText += "**【\(key)】**\n\(text)\n\n"
            }
        }

        phase = .done
    }

    // MARK: - AI解读
    func aiRead() async {
        guard !resultText.isEmpty else { return }
        await aiService.callWorker(type: "liuyao", data: resultText, question: question)
    }
}
