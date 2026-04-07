import SwiftUI

struct LingqianView: View {
    @StateObject private var vm = LingqianViewModel()

    var body: some View {
        ScrollViewReader { proxy in
            ScrollView {
                VStack(spacing: 14) {
                    SectionHeader(subtitle: "北 帝 灵 签", title: "玄天大帝")

                    // 输入区
                    if vm.phase == .input {
                        inputSection
                    }

                    // 摇签区
                    shakeSection

                    // 签文结果
                    if vm.phase == .result, !vm.resultText.isEmpty {
                        resultSection
                            .id("result")
                    }

                    // AI解读
                    aiSection
                        .id("ai")
                }
                .padding(.horizontal, 14)
                .padding(.bottom, 20)
            }
            .onChange(of: vm.phase) { _, newValue in
                if newValue == .result {
                    withAnimation {
                        proxy.scrollTo("result", anchor: .top)
                    }
                }
            }
        }
    }

    // MARK: - 输入区
    private var inputSection: some View {
        VStack(spacing: 8) {
            TextField("心中所问之事（可留空）", text: $vm.question)
                .textFieldStyle(HexgramTextFieldStyle())

            HStack(spacing: 8) {
                VStack(alignment: .leading, spacing: 3) {
                    Text("事类").font(.system(size: 11, design: .serif)).foregroundColor(.textSecondary)
                    Picker("", selection: $vm.selectedCategoryIndex) {
                        ForEach(Array(LINGQIAN_CATEGORIES.enumerated()), id: \.offset) { index, cat in
                            Text(cat.label).tag(index)
                        }
                    }
                    .pickerStyle(.menu)
                    .tint(.gold)
                }
                Spacer()
            }
        }
    }

    // MARK: - 摇签区
    private var shakeSection: some View {
        VStack(spacing: 12) {
            if vm.phase == .shaking {
                // 摇签动画
                VStack(spacing: 16) {
                    Text("🏮")
                        .font(.system(size: 60))
                        .rotationEffect(.degrees(vm.isShaking ? 15 : -15))
                        .animation(
                            .easeInOut(duration: 0.15).repeatForever(autoreverses: true),
                            value: vm.isShaking
                        )

                    ProgressView(value: vm.shakeProgress)
                        .progressViewStyle(LinearProgressViewStyle(tint: .gold))
                        .frame(width: 120)

                    Text("虔心摇签中…")
                        .font(.system(size: 13, design: .serif))
                        .foregroundColor(.textSecondary)
                }
                .frame(minHeight: 180)
            } else if vm.phase == .input {
                // 空状态
                VStack(spacing: 12) {
                    Text("🏮")
                        .font(.system(size: 50))
                        .opacity(0.4)
                    Text("诚心默念所求之事\n点击下方按钮摇签")
                        .font(.system(size: 12, design: .serif))
                        .foregroundColor(.textTertiary)
                        .multilineTextAlignment(.center)
                }
                .frame(minHeight: 180)
            } else if let q = vm.qianResult {
                // 签号显示
                VStack(spacing: 6) {
                    Text("第\(q.qianNum)签")
                        .font(.system(size: 36, weight: .medium, design: .serif))
                        .foregroundColor(.goldLight)
                    Text(q.qianName)
                        .font(.system(size: 16, design: .serif))
                        .foregroundColor(.gold)
                    if !q.guaXiang.isEmpty {
                        Text(q.guaXiang)
                            .font(.system(size: 12, design: .serif))
                            .foregroundColor(guaXiangColor(q.guaXiang))
                            .padding(.horizontal, 10)
                            .padding(.vertical, 3)
                            .background(guaXiangColor(q.guaXiang).opacity(0.12))
                            .clipShape(Capsule())
                    }
                }
                .frame(minHeight: 120)
            }

            // 操作按钮
            HStack(spacing: 10) {
                if vm.phase == .input {
                    Button(action: vm.shake) {
                        HStack(spacing: 6) {
                            Text("🏮")
                            Text("摇签")
                        }
                    }
                    .buttonStyle(GoldButtonStyle())
                } else if vm.phase == .result {
                    Button("再求一签", action: vm.reset)
                        .buttonStyle(GhostButtonStyle())
                }
            }
        }
        .panelStyle()
    }

    // MARK: - 签文结果
    private var resultSection: some View {
        VStack(spacing: 12) {
            MarkdownText(vm.resultText)

            if !vm.detailText.isEmpty {
                MarkdownText(vm.detailText)
            }

            HStack(spacing: 10) {
                if vm.aiService.isLoading {
                    ThinkingButton(text: "道长正在解签…")
                } else {
                    Button(action: {
                        Task { await vm.aiRead() }
                    }) {
                        HStack(spacing: 4) {
                            Text("🤖")
                            Text("AI详细解签")
                        }
                    }
                    .buttonStyle(GoldButtonStyle())
                }
            }

            Button("再求一签", action: vm.reset)
                .buttonStyle(GhostButtonStyle())
        }
        .resultStyle()
    }

    // MARK: - AI区域
    private var aiSection: some View {
        Group {
            if let error = vm.aiService.error {
                VStack(spacing: 8) {
                    Text("解签失败").font(.system(size: 15, weight: .medium, design: .serif)).foregroundColor(.jiRed)
                    Text(error).font(.system(size: 12, design: .serif)).foregroundColor(.jiRed.opacity(0.7))
                }
                .resultStyle()
            } else if let text = vm.aiService.result {
                VStack(alignment: .leading, spacing: 8) {
                    Text("AI详细解签").font(.system(size: 15, weight: .medium, design: .serif)).foregroundColor(.goldLight)
                    MarkdownText(text)
                }
                .resultStyle()
            }
        }
    }

    // MARK: - 辅助

    private func guaXiangColor(_ gx: String) -> Color {
        if gx.contains("上上") { return .yiGreen }
        if gx.contains("上") { return .gold }
        if gx.contains("中平") { return .textSecondary }
        if gx.contains("下下") { return .jiRed }
        if gx.contains("下") { return .accent }
        return .textSecondary
    }
}
