import Foundation

// MARK: - AI配置
struct AIConfig: Codable {
    var endpoint: String
    var apiKey: String
    var model: String
    var provider: String

    static var `default`: AIConfig {
        AIConfig(endpoint: "", apiKey: "", model: "claude-sonnet-4-20250514", provider: "anthropic")
    }

    static func load() -> AIConfig {
        guard let data = UserDefaults.standard.data(forKey: "aiConfig"),
              let config = try? JSONDecoder().decode(AIConfig.self, from: data) else {
            return .default
        }
        return config
    }

    func save() {
        if let data = try? JSONEncoder().encode(self) {
            UserDefaults.standard.set(data, forKey: "aiConfig")
        }
    }
}

// MARK: - 专业提示词
struct AIPrompts {
    static let liuyao = """
    你是一位精通六爻纳甲筮法的资深卦师，从业三十余年，深研以下经典并融会贯通：

    【核心典籍修养】
    - 《增删卜易》（野鹤老人）：实战断卦之圭臬
    - 《卜筮正宗》（王洪绪）：六爻断卦规则体系的集大成之作
    - 《易隐》（曹九锡）：高级技法参考
    - 《火珠林》：纳甲法源头
    - 《焦氏易林》：四千零九十六卦变占辞

    【断卦核心原则】
    1. 用神是断卦第一要务。根据所问之事确定用神。
    2. 用神旺衰的判断优先级：月建 > 日建 > 动爻。月建为提纲。
    3. 动爻生克用神是最直接的吉凶信号。
    4. 必须检查：用神是否旬空、月破、入墓、化绝、化回头克。
    5. 世爻代表求卦人，应爻代表对方或外部环境。
    6. 六神辅助断象，不可喧宾夺主。
    7. 变卦代表事态发展方向。
    8. 应期：用神逢值逢冲之日月为应期。

    【风格要求】
    - 说话直白、判断果断、不模棱两可
    - 每个判断都要说明理据
    - 必须给出明确的吉凶结论和具体建议
    - 回答不少于1200字
    """

    static let bazi = """
    你是一位精通四柱八字命理的资深命理师，从业三十余年，熟读以下经典：

    【核心典籍】
    - 《子平真诠》：格局论命的理论巅峰
    - 《滴天髓》：命理哲学之最高峰
    - 《穷通宝鉴》：调候用神
    - 《三命通会》：命理百科全书
    - 《千里命稿》：近代实战命理之精华

    【论命核心原则】
    1. 日主旺衰判断是基础：得令、得地、得势。
    2. 格局判断：看月令透出何神定格。
    3. 喜用神基于格局确定。
    4. 大运：天干管前五年、地支管后五年。
    5. 流年与大运、命局的三者关系是断吉凶的关键。
    6. 合冲刑害穿的作用不可忽略。

    【风格要求】
    - 分析层层递进、逻辑严密
    - 先论格局，再论喜忌，后论大运流年
    - 回答不少于1500字
    """

    static let huangli = """
    你是一位精通中国传统择日学的资深择日师，熟读《协纪辨方书》《玉匣记》《象吉通书》。

    【分析内容】
    1. 日辰总论：当日天干地支五行属性
    2. 建除十二神详解
    3. 二十八宿值日分析
    4. 吉神方位详解
    5. 冲煞详解
    6. 彭祖百忌解读
    7. 今日行事建议
    8. 特定人群提醒

    【风格要求】
    - 语气温和亲切
    - 既有传统底蕴又接地气
    - 回答不少于800字
    """
}

// MARK: - AI服务
@MainActor
class AIService: ObservableObject {
    @Published var isLoading = false
    @Published var result: String?
    @Published var error: String?

    func callLLM(systemPrompt: String, userContent: String) async {
        isLoading = true
        result = nil
        error = nil

        let config = AIConfig.load()

        do {
            let text: String
            if !config.endpoint.isEmpty {
                text = try await callWorker(config: config, systemPrompt: systemPrompt, userContent: userContent)
            } else if !config.apiKey.isEmpty {
                text = try await callDirectAPI(config: config, systemPrompt: systemPrompt, userContent: userContent)
            } else {
                throw AIError.noConfig
            }
            result = text
        } catch {
            self.error = error.localizedDescription
        }

        isLoading = false
    }

    private func callWorker(config: AIConfig, systemPrompt: String, userContent: String) async throws -> String {
        guard let url = URL(string: config.endpoint) else { throw AIError.invalidURL }

        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")

        let body: [String: Any] = [
            "systemPrompt": systemPrompt,
            "hexagramData": ["panText": userContent],
            "question": userContent
        ]
        request.httpBody = try JSONSerialization.data(withJSONObject: body)

        let (data, _) = try await URLSession.shared.data(for: request)
        guard let json = try JSONSerialization.jsonObject(with: data) as? [String: Any] else {
            throw AIError.parseError
        }
        if let error = json["error"] as? String { throw AIError.serverError(error) }
        guard let reading = json["reading"] as? String else { throw AIError.parseError }
        return reading
    }

    private func callDirectAPI(config: AIConfig, systemPrompt: String, userContent: String) async throws -> String {
        let url: URL
        let headers: [String: String]
        let body: [String: Any]

        if config.provider == "anthropic" {
            url = URL(string: "https://api.anthropic.com/v1/messages")!
            headers = [
                "Content-Type": "application/json",
                "x-api-key": config.apiKey,
                "anthropic-version": "2023-06-01"
            ]
            body = [
                "model": config.model,
                "max_tokens": 4096,
                "system": systemPrompt,
                "messages": [["role": "user", "content": userContent]]
            ]
        } else {
            let baseURL = config.provider == "openrouter"
                ? "https://openrouter.ai/api/v1/chat/completions"
                : "https://api.openai.com/v1/chat/completions"
            url = URL(string: baseURL)!
            headers = [
                "Content-Type": "application/json",
                "Authorization": "Bearer \(config.apiKey)"
            ]
            body = [
                "model": config.model,
                "max_tokens": 4096,
                "messages": [
                    ["role": "system", "content": systemPrompt],
                    ["role": "user", "content": userContent]
                ]
            ]
        }

        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        for (key, value) in headers {
            request.setValue(value, forHTTPHeaderField: key)
        }
        request.httpBody = try JSONSerialization.data(withJSONObject: body)

        let (data, _) = try await URLSession.shared.data(for: request)
        guard let json = try JSONSerialization.jsonObject(with: data) as? [String: Any] else {
            throw AIError.parseError
        }

        // Anthropic format
        if let content = json["content"] as? [[String: Any]] {
            let texts = content.compactMap { $0["type"] as? String == "text" ? $0["text"] as? String : nil }
            if !texts.isEmpty { return texts.joined() }
        }

        // OpenAI format
        if let choices = json["choices"] as? [[String: Any]],
           let message = choices.first?["message"] as? [String: Any],
           let text = message["content"] as? String {
            return text
        }

        // Error
        if let error = json["error"] as? [String: Any], let msg = error["message"] as? String {
            throw AIError.serverError(msg)
        }

        throw AIError.parseError
    }
}

enum AIError: LocalizedError {
    case noConfig
    case invalidURL
    case parseError
    case serverError(String)

    var errorDescription: String? {
        switch self {
        case .noConfig: return "未配置API。请在设置中配置API密钥或后端地址。"
        case .invalidURL: return "无效的API地址"
        case .parseError: return "解析响应失败"
        case .serverError(let msg): return msg
        }
    }
}
