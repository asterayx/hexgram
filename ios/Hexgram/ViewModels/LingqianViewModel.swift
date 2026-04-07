import SwiftUI

struct LingqianCategory: Identifiable {
    let key: String
    let label: String
    var id: String { key }
}

let LINGQIAN_CATEGORIES: [LingqianCategory] = [
    LingqianCategory(key: "综合", label: "综合运势"),
    LingqianCategory(key: "家宅", label: "家宅运势"),
    LingqianCategory(key: "生意", label: "生意经营"),
    LingqianCategory(key: "谋望", label: "谋望求财"),
    LingqianCategory(key: "婚姻", label: "婚姻感情"),
    LingqianCategory(key: "功名", label: "学艺功名"),
    LingqianCategory(key: "出外", label: "出行外出"),
    LingqianCategory(key: "官讼", label: "官讼是非"),
    LingqianCategory(key: "占病", label: "健康疾病"),
    LingqianCategory(key: "失物", label: "失物寻找"),
    LingqianCategory(key: "行人", label: "行人音讯"),
]

struct LingqianResult {
    let qianNum: Int
    let qianName: String
    let qianType: String
    let guaXiang: String
    let shengXiao: String
    let xiWen: String
    let shiYue: String
    let neiZhao: String
    let detail: [String: Any]?
}

@MainActor
class LingqianViewModel: ObservableObject {
    enum Phase { case input, shaking, result }

    @Published var phase: Phase = .input
    @Published var question = ""
    @Published var selectedCategoryIndex = 0
    @Published var qianResult: LingqianResult?
    @Published var resultText = ""
    @Published var detailText = ""
    @Published var isShaking = false
    @Published var shakeProgress: CGFloat = 0

    let aiService = AIService()

    var selectedCategoryKey: String {
        LINGQIAN_CATEGORIES.indices.contains(selectedCategoryIndex)
            ? LINGQIAN_CATEGORIES[selectedCategoryIndex].key
            : "综合"
    }

    // MARK: - 摇签

    func shake() {
        guard phase == .input else { return }
        isShaking = true
        phase = .shaking
        shakeProgress = 0

        // 模拟摇签动画 (1.5秒)
        let steps = 15
        for i in 1...steps {
            DispatchQueue.main.asyncAfter(deadline: .now() + Double(i) * 0.1) { [weak self] in
                self?.shakeProgress = CGFloat(i) / CGFloat(steps)
                if i == steps {
                    self?.finishShake()
                }
            }
        }
    }

    private func finishShake() {
        let num = Int.random(in: 1...51)
        isShaking = false
        phase = .result

        // 查询签文
        Task {
            await fetchQian(num: num)
        }
    }

    // MARK: - 查询签文

    private func fetchQian(num: Int) async {
        let config = AIConfig.load()
        guard !config.endpoint.isEmpty else {
            qianResult = LingqianResult(
                qianNum: num, qianName: "第\(num)签", qianType: "",
                guaXiang: "", shengXiao: "", xiWen: "", shiYue: "", neiZhao: "",
                detail: nil
            )
            resultText = "## 第\(num)签\n\n⚠ 未配置服务器，无法获取签文"
            return
        }

        let baseUrl = config.endpoint.hasSuffix("/")
            ? String(config.endpoint.dropLast())
            : config.endpoint
        guard let url = URL(string: "\(baseUrl)/api/lingqian") else { return }

        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")

        let payload: [String: Any] = [
            "qianNum": num,
            "category": selectedCategoryKey,
            "question": question,
        ]
        request.httpBody = try? JSONSerialization.data(withJSONObject: payload)

        do {
            let (data, _) = try await URLSession.shared.data(for: request)
            guard let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
                  let qian = json["qian"] as? [String: Any] else {
                resultText = "## 第\(num)签\n\n获取签文失败"
                return
            }

            let result = LingqianResult(
                qianNum: qian["qianNum"] as? Int ?? num,
                qianName: qian["qianName"] as? String ?? "",
                qianType: qian["qianType"] as? String ?? "",
                guaXiang: qian["guaXiang"] as? String ?? "",
                shengXiao: qian["shengXiao"] as? String ?? "",
                xiWen: qian["xiWen"] as? String ?? "",
                shiYue: qian["shiYue"] as? String ?? "",
                neiZhao: qian["neiZhao"] as? String ?? "",
                detail: json["detail"] as? [String: Any]
            )
            qianResult = result
            resultText = formatResult(result)
            detailText = formatDetail(result.detail)

        } catch {
            resultText = "## 第\(num)签\n\n查询失败：\(error.localizedDescription)"
        }
    }

    // MARK: - 格式化

    private func formatResult(_ r: LingqianResult) -> String {
        var s = "## 第\(r.qianNum)签 · \(r.qianName)\n\n"
        if !r.guaXiang.isEmpty { s += "**\(r.guaXiang)** · \(r.qianType)\n\n" }
        if !r.shiYue.isEmpty { s += "### 诗曰\n\(r.shiYue)\n\n" }
        if !r.neiZhao.isEmpty { s += "**内兆**：\(r.neiZhao)\n\n" }
        if !r.xiWen.isEmpty { s += "### 典故\n\(r.xiWen)\n\n" }
        return s
    }

    private func formatDetail(_ detail: [String: Any]?) -> String {
        guard let detail = detail, !detail.isEmpty else { return "" }
        var s = "---\n### 分类详解\n\n"
        for (key, value) in detail {
            if let dict = value as? [String: Any] {
                s += "**\(key)**\n"
                for (k, v) in dict {
                    if let str = v as? String, !str.isEmpty {
                        s += "· \(k)：\(str)\n"
                    }
                }
                s += "\n"
            } else if let str = value as? String, !str.isEmpty {
                s += "**\(key)**：\(str)\n\n"
            }
        }
        return s
    }

    // MARK: - AI 解读

    func aiRead() async {
        guard qianResult != nil, !resultText.isEmpty else { return }
        let fullData = resultText + detailText
            + "\n\n所求事类：\(LINGQIAN_CATEGORIES[selectedCategoryIndex].label)"
            + (question.isEmpty ? "" : "\n用户所问：\(question)")
        await aiService.callWorker(type: "lingqian", data: fullData, question: question)
    }

    // MARK: - 重置

    func reset() {
        phase = .input
        qianResult = nil
        resultText = ""
        detailText = ""
        shakeProgress = 0
        isShaking = false
        aiService.result = nil
        aiService.error = nil
    }
}
