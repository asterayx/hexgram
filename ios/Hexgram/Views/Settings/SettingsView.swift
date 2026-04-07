import SwiftUI

struct SettingsView: View {
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 20) {
                    // 关于
                    VStack(spacing: 8) {
                        Text("易学")
                            .font(.system(size: 28, weight: .bold, design: .serif))
                            .foregroundColor(.gold)

                        Text("Hexgram")
                            .font(.system(size: 14, design: .serif))
                            .foregroundColor(.textTertiary)

                        Text("六爻纳甲 · 四柱八字 · 黄历查询 · 北帝灵签")
                            .font(.system(size: 13, design: .serif))
                            .foregroundColor(.textSecondary)
                            .multilineTextAlignment(.center)

                        Text("传统易学 · 现代呈现")
                            .font(.system(size: 12, design: .serif))
                            .foregroundColor(.textTertiary)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 20)

                    // 说明
                    VStack(alignment: .leading, spacing: 8) {
                        Text("关于")
                            .font(.system(size: 14, weight: .medium, design: .serif))
                            .foregroundColor(.goldLight)

                        Text("集六爻纳甲排盘、四柱八字命理、传统黄历择日、北帝玄天大帝灵签四大功能于一体的专业易学应用。排盘计算在本地完成，AI深度解读由云端提供。")
                            .font(.system(size: 12, design: .serif))
                            .foregroundColor(.textSecondary)
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(12)
                    .background(Color.bgPanel)
                    .clipShape(RoundedRectangle(cornerRadius: 10))

                    // 版本信息
                    VStack(spacing: 4) {
                        HStack {
                            Text("版本").foregroundColor(.textSecondary)
                            Spacer()
                            Text(Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "1.0.0")
                                .foregroundColor(.textPrimary)
                        }
                        .font(.system(size: 12, design: .serif))

                        HStack {
                            Text("Build").foregroundColor(.textSecondary)
                            Spacer()
                            Text(BuildInfo.gitHash)
                                .foregroundColor(.textTertiary)
                                .font(.system(size: 11, design: .monospaced))
                        }
                        .font(.system(size: 12, design: .serif))

                        HStack {
                            Text("构建日期").foregroundColor(.textSecondary)
                            Spacer()
                            Text(BuildInfo.buildDate)
                                .foregroundColor(.textTertiary)
                        }
                        .font(.system(size: 12, design: .serif))
                    }
                    .padding(12)
                    .background(Color.bgPanel)
                    .clipShape(RoundedRectangle(cornerRadius: 10))
                }
                .padding(16)
            }
            .background(Color.bgPrimary)
            .navigationTitle("关于")
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
