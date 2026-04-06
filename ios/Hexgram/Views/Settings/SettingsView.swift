import SwiftUI

struct SettingsView: View {
    @Environment(\.dismiss) private var dismiss
    @State private var config = AIConfig.load()

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 16) {
                    VStack(alignment: .leading, spacing: 6) {
                        Text("Worker后端地址")
                            .font(.system(size: 13, weight: .medium, design: .serif))
                            .foregroundColor(.goldLight)

                        TextField("https://yijing-api.workers.dev", text: $config.endpoint)
                            .textFieldStyle(HexgramTextFieldStyle())
                            .autocapitalization(.none)
                            .keyboardType(.URL)

                        Text("AI提示词和API Key安全存储在服务端，App仅发送排盘数据")
                            .font(.system(size: 9, design: .serif))
                            .foregroundColor(.textTertiary)
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
