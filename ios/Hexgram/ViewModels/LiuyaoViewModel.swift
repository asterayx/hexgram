import SwiftUI

@MainActor
class LiuyaoViewModel: ObservableObject {
    @Published var lines: [Int] = []
    @Published var phase: Phase = .input
    @Published var isTossing = false
    @Published var question = ""
    @Published var selectedCategoryIndex = 0  // 综合
    @Published var selectedDate = Date()
    @Published var selectedHour = 11  // 午时
    @Published var guaResult: GuaResult?
    @Published var resultText = ""
    @Published var classicsText = ""
    @Published var classicsLoading = false

    // AI
    let aiService = AIService()

    // 事类列表（从 Worker 获取，有默认值兜底）
    @Published var categories: [QuestionCategory] = QUESTION_CATEGORIES

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

        DispatchQueue.main.asyncAfter(deadline: .now() + 0.7) { [weak self] in
            guard let self else { return }
            let coins = (0..<3).map { _ in Bool.random() ? 2 : 3 }
            let value = coins.reduce(0, +)
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
        classicsText = ""
        classicsLoading = false
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
        phase = .done

        // 异步查询经典文献
        Task {
            await fetchClassics(result: result)
        }
    }

    private func fetchClassics(result: GuaResult) async {
        classicsLoading = true
        classicsText = ""

        let catKey = categories.indices.contains(selectedCategoryIndex)
            ? categories[selectedCategoryIndex].key
            : "_总论"

        do {
            let classics = try await ClassicsService.shared.query(
                guaKey: result.guaKey,
                guaName: result.guaName,
                changedGuaKey: result.hasChanging ? result.changedGuaKey : nil,
                changedGuaName: result.hasChanging ? result.changedGuaName : nil,
                category: catKey
            )

            // 更新类目列表
            if !classics.categories.isEmpty {
                categories = classics.categories
            }

            // 拼装经典文本
            var sb = ""

            if let g = classics.gaodao {
                sb += "## 高岛易断 · \(g.name)卦\n\n"
                sb += "**【卦断】** \(g.judgment)\n\n"
                if !g.yao.isEmpty {
                    sb += "**【爻断】**\n"
                    for (i, line) in lines.enumerated() {
                        let marker = (line == 6 || line == 9) ? " ★" : ""
                        if i < g.yao.count && !g.yao[i].isEmpty {
                            sb += "\(YAO_NAMES[i])爻\(marker)：\(g.yao[i])\n"
                        }
                    }
                    sb += "\n"
                }
            }

            if let h = classics.huangjince {
                sb += "## 黄金策 · \(h.label)\n\n"
                sb += "\(h.text)\n\n"
            }

            if let j = classics.jiaoshi, !j.isEmpty {
                sb += "## 焦氏易林\n\n"
                sb += "**\(result.guaName)之\(result.changedGuaName ?? "")**：\(j)\n\n"
            }

            classicsText = sb
        } catch {
            classicsText = "经典查询失败：\(error.localizedDescription)"
        }

        classicsLoading = false
    }

    // MARK: - AI解读
    func aiRead() async {
        guard !resultText.isEmpty else { return }
        let fullData = resultText + (classicsText.isEmpty ? "" : "\n\n\(classicsText)")
        await aiService.callWorker(type: "liuyao", data: fullData, question: question)
    }
}
