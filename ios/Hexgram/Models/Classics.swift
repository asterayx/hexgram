import Foundation

// MARK: - 数据类型

struct ClassicEntry {
    let name: String
    let judgment: String
    let yao: [String]
}

struct HuangjinceEntry {
    let category: String
    let label: String
    let text: String
}

struct QuestionCategory: Identifiable {
    let key: String
    let label: String
    var id: String { key }
}

struct ClassicsResult {
    let gaodao: ClassicEntry?
    let huangjince: HuangjinceEntry?
    let jiaoshi: String?
    let categories: [QuestionCategory]
}

// MARK: - 默认事类

let QUESTION_CATEGORIES: [QuestionCategory] = [
    QuestionCategory(key: "_总论", label: "综合"),
    QuestionCategory(key: "求财", label: "求财"),
    QuestionCategory(key: "事业", label: "事业"),
    QuestionCategory(key: "感情", label: "感情"),
    QuestionCategory(key: "婚姻", label: "婚姻"),
    QuestionCategory(key: "考试", label: "考试"),
    QuestionCategory(key: "家宅", label: "家宅"),
    QuestionCategory(key: "疾病", label: "疾病"),
    QuestionCategory(key: "出行", label: "出行"),
    QuestionCategory(key: "诉讼", label: "诉讼"),
    QuestionCategory(key: "失物", label: "失物"),
    QuestionCategory(key: "天气", label: "天气"),
    QuestionCategory(key: "怀孕", label: "怀孕"),
    QuestionCategory(key: "投资", label: "投资"),
    QuestionCategory(key: "求职", label: "求职"),
    QuestionCategory(key: "生意", label: "生意"),
]

// MARK: - 经典文献网络服务

/// 通过 Worker /api/classics 查询经典文献
/// Worker 先查 D1 缓存，缓存未命中则调用 LLM 生成并存入 D1
class ClassicsService {
    static let shared = ClassicsService()

    func query(
        guaKey: String,
        guaName: String = "",
        changedGuaKey: String? = nil,
        changedGuaName: String? = nil,
        category: String? = nil
    ) async throws -> ClassicsResult {
        let config = AIConfig.load()
        guard !config.endpoint.isEmpty else {
            return ClassicsResult(gaodao: nil, huangjince: nil, jiaoshi: nil, categories: QUESTION_CATEGORIES)
        }

        let baseUrl = config.endpoint.hasSuffix("/")
            ? String(config.endpoint.dropLast())
            : config.endpoint
        let classicsUrl = "\(baseUrl)/api/classics"

        guard let url = URL(string: classicsUrl) else {
            return ClassicsResult(gaodao: nil, huangjince: nil, jiaoshi: nil, categories: QUESTION_CATEGORIES)
        }

        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.timeoutInterval = 60

        var body: [String: Any] = ["guaKey": guaKey, "guaName": guaName]
        if let ck = changedGuaKey { body["changedGuaKey"] = ck }
        if let cn = changedGuaName { body["changedGuaName"] = cn }
        if let cat = category { body["category"] = cat }
        request.httpBody = try JSONSerialization.data(withJSONObject: body)

        let (data, response) = try await URLSession.shared.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 else {
            return ClassicsResult(gaodao: nil, huangjince: nil, jiaoshi: nil, categories: QUESTION_CATEGORIES)
        }

        guard let json = try JSONSerialization.jsonObject(with: data) as? [String: Any] else {
            return ClassicsResult(gaodao: nil, huangjince: nil, jiaoshi: nil, categories: QUESTION_CATEGORIES)
        }

        return parseResponse(json)
    }

    private func parseResponse(_ json: [String: Any]) -> ClassicsResult {
        // gaodao
        var gaodao: ClassicEntry? = nil
        if let g = json["gaodao"] as? [String: Any] {
            let yao = (g["yao"] as? [String]) ?? []
            gaodao = ClassicEntry(
                name: g["name"] as? String ?? "",
                judgment: g["judgment"] as? String ?? "",
                yao: yao
            )
        }

        // huangjince
        var huangjince: HuangjinceEntry? = nil
        if let h = json["huangjince"] as? [String: Any] {
            huangjince = HuangjinceEntry(
                category: h["category"] as? String ?? "",
                label: h["label"] as? String ?? "",
                text: h["text"] as? String ?? ""
            )
        }

        // jiaoshi
        let jiaoshi = json["jiaoshi"] as? String

        // categories
        var cats = QUESTION_CATEGORIES
        if let catArr = json["categories"] as? [[String: Any]], !catArr.isEmpty {
            cats = catArr.map {
                QuestionCategory(
                    key: $0["key"] as? String ?? "",
                    label: $0["label"] as? String ?? ""
                )
            }
        }

        return ClassicsResult(gaodao: gaodao, huangjince: huangjince, jiaoshi: jiaoshi, categories: cats)
    }
}
