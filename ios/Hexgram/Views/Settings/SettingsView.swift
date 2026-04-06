import SwiftUI

struct SettingsView: View {
    @Environment(\.dismiss) private var dismiss
    @State private var config = AIConfig.load()

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 16) {
                    // Worker后端
                    VStack(alignment: .leading, spacing: 6) {
                        Text("方式一：Worker后端")
                            .font(.system(size: 13, weight: .medium, design: .serif))
                            .foregroundColor(.goldLight)

                        TextField("https://yijing-api.workers.dev", text: $config.endpoint)
                            .textFieldStyle(HexgramTextFieldStyle())
                            .autocapitalization(.none)
                            .keyboardType(.URL)

                        Text("API Key安全存储在服务端")
                            .font(.system(size: 9, design: .serif))
                            .foregroundColor(.textTertiary)
                    }

                    Divider().background(Color.border)

                    // 直连API
                    VStack(alignment: .leading, spacing: 8) {
                        Text("方式二：直连API")
                            .font(.system(size: 13, weight: .medium, design: .serif))
                            .foregroundColor(.goldLight)

                        VStack(alignment: .leading, spacing: 3) {
                            Text("Provider").font(.system(size: 11, design: .serif)).foregroundColor(.textSecondary)
                            Picker("", selection: $config.provider) {
                                Text("Anthropic Claude").tag("anthropic")
                                Text("OpenAI").tag("openai")
                                Text("OpenRouter").tag("openrouter")
                            }
                            .pickerStyle(.segmented)
                        }

                        VStack(alignment: .leading, spacing: 3) {
                            Text("API Key").font(.system(size: 11, design: .serif)).foregroundColor(.textSecondary)
                            SecureField("sk-...", text: $config.apiKey)
                                .textFieldStyle(HexgramTextFieldStyle())
                                .autocapitalization(.none)
                        }

                        VStack(alignment: .leading, spacing: 3) {
                            Text("Model").font(.system(size: 11, design: .serif)).foregroundColor(.textSecondary)
                            TextField("claude-sonnet-4-20250514", text: $config.model)
                                .textFieldStyle(HexgramTextFieldStyle())
                                .autocapitalization(.none)
                        }
                    }

                    Button("保存") {
                        config.save()
                        dismiss()
                    }
                    .buttonStyle(GoldButtonStyle())
                    .frame(maxWidth: .infinity)
                }
                .padding(16)
            }
            .background(Color.bgPrimary)
            .navigationTitle("AI 设置")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("关闭") { dismiss() }
                        .foregroundColor(.gold)
                }
            }
            .toolbarBackground(Color.bgPanel, for: .navigationBar)
            .toolbarBackground(.visible, for: .navigationBar)
        }
        .preferredColorScheme(.dark)
    }
}
