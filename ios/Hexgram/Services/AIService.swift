import Foundation

// MARK: - AI配置（仅需Worker端点）
struct AIConfig: Codable {
    var endpoint: String

    static var `default`: AIConfig {
        AIConfig(endpoint: "https://yijing-api.asternos.workers.dev")
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

// MARK: - AI服务（统一通过Worker代理）
@MainActor
class AIService: ObservableObject {
    @Published var isLoading = false
    @Published var result: String?
    @Published var error: String?

    /// 统一调用接口
    /// - Parameters:
    ///   - type: "liuyao", "bazi", "huangli"
    ///   - data: 排盘结果文本
    ///   - question: 用户问题（可选）
    func callWorker(type: String, data: String, question: String = "") async {
        isLoading = true
        result = nil
        error = nil

        let config = AIConfig.load()
        guard !config.endpoint.isEmpty else {
            self.error = "未配置Worker地址。请在设置中配置后端地址。"
            isLoading = false
            return
        }

        guard let url = URL(string: config.endpoint) else {
            self.error = "无效的Worker地址"
            isLoading = false
            return
        }

        do {
            var request = URLRequest(url: url)
            request.httpMethod = "POST"
            request.setValue("application/json", forHTTPHeaderField: "Content-Type")
            request.timeoutInterval = 120

            let body: [String: Any] = [
                "type": type,
                "data": data,
                "question": question
            ]
            request.httpBody = try JSONSerialization.data(withJSONObject: body)

            let (responseData, _) = try await URLSession.shared.data(for: request)
            guard let json = try JSONSerialization.jsonObject(with: responseData) as? [String: Any] else {
                throw AIError.parseError
            }
            if let errorMsg = json["error"] as? String {
                throw AIError.serverError(errorMsg)
            }
            guard let reading = json["reading"] as? String else {
                throw AIError.parseError
            }
            self.result = reading
        } catch let err as AIError {
            self.error = err.localizedDescription
        } catch {
            self.error = error.localizedDescription
        }

        isLoading = false
    }
}

enum AIError: LocalizedError {
    case parseError
    case serverError(String)

    var errorDescription: String? {
        switch self {
        case .parseError: return "解析响应失败"
        case .serverError(let msg): return msg
        }
    }
}
