import SwiftUI

struct LiuyaoView: View {
    @StateObject private var vm = LiuyaoViewModel()
    @State private var showShareSheet = false

    var body: some View {
        ScrollViewReader { proxy in
            ScrollView {
                VStack(spacing: 14) {
                    SectionHeader(subtitle: "纳 甲 六 爻", title: "易经排盘")

                    // 输入区
                    if vm.phase == .input {
                        inputSection
                    }

                    // 卦象显示面板
                    guaPanel

                    // 进度指示
                    progressDots

                    // 操作按钮
                    actionButtons

                    // 排盘结果
                    if vm.phase == .done, !vm.resultText.isEmpty {
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
                if newValue == .done {
                    withAnimation {
                        proxy.scrollTo("result", anchor: .top)
                    }
                }
            }
            .sheet(isPresented: $showShareSheet) {
                if let gua = vm.guaResult {
                    let card = PaipanSnapshot.liuyaoCard(guaResult: gua, text: vm.resultText)
                    let image = PaipanSnapshot.render(card, size: CGSize(width: 360, height: 500))
                    let items: [Any] = [image as Any, vm.resultText]
                    ShareSheet(items: items.compactMap { $0 is NSNull ? nil : $0 })
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
                    Text("占卦日期").font(.system(size: 11, design: .serif)).foregroundColor(.textSecondary)
                    DatePicker("", selection: $vm.selectedDate, displayedComponents: .date)
                        .datePickerStyle(.compact)
                        .labelsHidden()
                        .tint(.gold)
                }

                VStack(alignment: .leading, spacing: 3) {
                    Text("时辰").font(.system(size: 11, design: .serif)).foregroundColor(.textSecondary)
                    Picker("", selection: $vm.selectedHour) {
                        ForEach(LiuyaoViewModel.hourOptions, id: \.value) { option in
                            Text(option.label).tag(option.value)
                        }
                    }
                    .pickerStyle(.menu)
                    .tint(.gold)
                }
            }

            Text(vm.dateGanZhiText)
                .font(.system(size: 11, design: .serif))
                .foregroundColor(.accent)
        }
    }

    // MARK: - 卦象面板
    private var guaPanel: some View {
        VStack(spacing: 10) {
            if vm.lines.isEmpty && !vm.isTossing {
                // 空状态
                VStack(spacing: 8) {
                    Text("☰")
                        .font(.system(size: 40))
                        .opacity(0.25)
                    Text("点击下方按钮开始摇卦")
                        .font(.system(size: 12, design: .serif))
                        .foregroundColor(.textTertiary)
                }
                .frame(minHeight: 180)
            } else {
                // 卦象爻线
                VStack(spacing: 6) {
                    ForEach(Array(vm.lines.enumerated()), id: \.offset) { idx, value in
                        HStack(spacing: 6) {
                            Text(YAO_NAMES[idx])
                                .font(.system(size: 10, design: .serif))
                                .foregroundColor(.textSecondary)
                                .frame(width: 14, alignment: .trailing)

                            YaoSymbol(
                                isYang: value == 7 || value == 9,
                                isChanging: value == 6 || value == 9,
                                width: min(UIScreen.main.bounds.width * 0.36, 160)
                            )

                            Text(YAO_LABELS[value] ?? "")
                                .font(.system(size: 9, design: .serif))
                                .foregroundColor((value == 6 || value == 9) ? .accent : .textTertiary)
                                .frame(width: 32)

                            if value == 9 {
                                Text("○").font(.system(size: 13)).foregroundColor(.accent)
                            } else if value == 6 {
                                Text("×").font(.system(size: 13)).foregroundColor(.accent)
                            }
                        }
                        .transition(.asymmetric(
                            insertion: .move(edge: .leading).combined(with: .opacity),
                            removal: .opacity
                        ))
                    }
                }
                .animation(.easeOut(duration: 0.3), value: vm.lines.count)
                .padding(.vertical, 12)

                // 卦名信息
                if vm.lines.count == 6 {
                    guaInfoView
                }
            }
        }
        .frame(minHeight: 180)
        .panelStyle()
    }

    // MARK: - 卦名信息
    private var guaInfoView: some View {
        let baseBits = vm.lines.map { ($0 == 7 || $0 == 9) ? "1" : "0" }.joined()
        let hasDong = vm.lines.contains(where: { $0 == 6 || $0 == 9 })
        let changedBits = vm.lines.map { v -> String in
            if v == 6 { return "1" }
            if v == 9 { return "0" }
            return (v == 7) ? "1" : "0"
        }.joined()
        let dongIdx = vm.lines.enumerated().compactMap { ($0.element == 6 || $0.element == 9) ? $0.offset : nil }

        return VStack(spacing: 4) {
            Text("\(HEXAGRAM_NAMES[baseBits] ?? "?")卦")
                .font(.system(size: 22, design: .serif))
                .foregroundColor(.goldLight)

            if let innerKey = BAGUA_TABLE[String(baseBits.prefix(3))],
               let outerKey = BAGUA_TABLE[String(baseBits.suffix(3))] {
                Text("\(outerKey.name)上·\(innerKey.name)下")
                    .font(.system(size: 11, design: .serif))
                    .foregroundColor(.textSecondary)
            }

            if hasDong {
                Text("→ 变卦：\(HEXAGRAM_NAMES[changedBits] ?? "?")（\(dongIdx.map { YAO_NAMES[$0] + "爻" }.joined(separator: "、"))动）")
                    .font(.system(size: 11, design: .serif))
                    .foregroundColor(.accent)
            }
        }
    }

    // MARK: - 进度点
    private var progressDots: some View {
        VStack(spacing: 4) {
            HStack(spacing: 5) {
                ForEach(0..<6, id: \.self) { i in
                    Circle()
                        .fill(dotColor(i))
                        .frame(width: 6, height: 6)
                }
            }
            Text(vm.lines.count < 6
                ? "第\(YAO_NAMES[vm.lines.count])爻（\(vm.lines.count)/6）"
                : "卦象已成")
                .font(.system(size: 11, design: .serif))
                .foregroundColor(.textSecondary)
        }
    }

    private func dotColor(_ index: Int) -> Color {
        guard index < vm.lines.count else { return .border }
        return (vm.lines[index] == 6 || vm.lines[index] == 9) ? .accent : .gold
    }

    // MARK: - 操作按钮
    private var actionButtons: some View {
        HStack(spacing: 10) {
            if vm.phase == .input {
                if vm.lines.count < 6 {
                    Button(action: vm.toss) {
                        HStack(spacing: 6) {
                            Text("🪙")
                            Text(vm.isTossing ? "摇卦中…" : "摇卦")
                        }
                    }
                    .buttonStyle(GoldButtonStyle())
                    .disabled(vm.isTossing)

                    if !vm.lines.isEmpty {
                        Button("撤回", action: vm.undo)
                            .buttonStyle(GhostButtonStyle())
                    }
                } else {
                    Button("排盘解卦") { vm.doReading() }
                        .buttonStyle(GoldButtonStyle())

                    Button("撤回", action: vm.undo)
                        .buttonStyle(GhostButtonStyle())

                    Button("重来", action: vm.reset)
                        .buttonStyle(GhostButtonStyle())
                }
            }
        }
    }

    // MARK: - 排盘结果
    private var resultSection: some View {
        VStack(spacing: 12) {
            if let gua = vm.guaResult {
                // 排盘表格
                guaTableView(gua)

                // 纯文本markdown结果
                MarkdownText(vm.resultText)
            }

            HStack(spacing: 10) {
                if vm.aiService.isLoading {
                    ThinkingButton(text: "卦师正在参详卦象…")
                } else {
                    Button(action: {
                        Task { await vm.aiRead() }
                    }) {
                        HStack(spacing: 4) {
                            Text("🤖")
                            Text("AI深度解读")
                        }
                    }
                    .buttonStyle(GoldButtonStyle())
                }
            }

            HStack(spacing: 10) {
                Button(action: { showShareSheet = true }) {
                    HStack(spacing: 4) {
                        Image(systemName: "square.and.arrow.up")
                        Text("分享")
                    }
                }
                .buttonStyle(GhostButtonStyle())

                Button("再占一卦", action: vm.reset)
                    .buttonStyle(GhostButtonStyle())
            }
        }
        .resultStyle()
    }

    // MARK: - 排盘表格
    private func guaTableView(_ gua: GuaResult) -> some View {
        VStack(spacing: 0) {
            // 标题
            Text("\(gua.gong)宫 · \(gua.guaName)卦\(gua.youHun ? "（游魂）" : "")\(gua.guiHun ? "（归魂）" : "")")
                .font(.system(size: 16, weight: .medium, design: .serif))
                .foregroundColor(.goldLight)
                .padding(.bottom, 8)

            // 特殊卦标记
            VStack(spacing: 2) {
                if gua.isLiuChongGua {
                    Text("⚡ 六冲卦 — 主事多变动")
                        .font(.system(size: 11, design: .serif))
                        .foregroundColor(.accent)
                }
                if gua.isLiuHeGua {
                    Text("🤝 六合卦 — 主事和合稳定")
                        .font(.system(size: 11, design: .serif))
                        .foregroundColor(.yiGreen)
                }
                if gua.isFanYin {
                    Text("⚠ 反吟 — 主反复不安")
                        .font(.system(size: 11, design: .serif))
                        .foregroundColor(.jiRed)
                }
                if gua.isFuYin {
                    Text("😩 伏吟 — 主呻吟痛苦")
                        .font(.system(size: 11, design: .serif))
                        .foregroundColor(.jiRed)
                }
                if !gua.anDong.isEmpty {
                    Text("👁 暗动：\(gua.anDong.map { "\($0.yaoPos)爻\($0.liuqin)" }.joined(separator: "、"))")
                        .font(.system(size: 11, design: .serif))
                        .foregroundColor(.textSecondary)
                }
                if !gua.yuePo.isEmpty {
                    Text("💔 月破：\(gua.yuePo.map { "\(gua.yaos[$0].posName)爻\(gua.yaos[$0].liuqin)" }.joined(separator: "、"))")
                        .font(.system(size: 11, design: .serif))
                        .foregroundColor(.jiRed)
                }
                if !gua.sanHe.isEmpty {
                    Text("🔄 \(gua.sanHe.map { $0.branches + "合" + $0.wuxing + "局" }.joined(separator: "、"))")
                        .font(.system(size: 11, design: .serif))
                        .foregroundColor(.yiGreen)
                }
            }
            .padding(.bottom, 4)

            // 表头
            HStack(spacing: 0) {
                Text("六神").frame(width: 40)
                Text("六亲").frame(width: 40)
                Text("本卦 \(gua.guaName)").frame(maxWidth: .infinity)
                if gua.hasChanging {
                    Text("变卦 \(gua.changedGuaName ?? "")").frame(maxWidth: .infinity)
                }
            }
            .font(.system(size: 10, weight: .medium, design: .serif))
            .foregroundColor(.gold)
            .padding(.vertical, 6)
            .background(Color.bgPanel)

            Divider().background(Color.border)

            // 爻行 (从上到初)
            ForEach(Array(stride(from: 5, through: 0, by: -1)), id: \.self) { i in
                let y = gua.yaos[i]
                HStack(spacing: 0) {
                    Text(y.liuShen)
                        .font(.system(size: 10, design: .serif))
                        .foregroundColor(.textSecondary)
                        .frame(width: 40)

                    Text(y.liuqin)
                        .font(.system(size: 10, design: .serif))
                        .foregroundColor(.goldLight)
                        .frame(width: 40)

                    HStack(spacing: 4) {
                        Text("\(y.tianGan)\(y.diZhi)")
                            .font(.system(size: 11, design: .serif))
                        Text(y.wuxing)
                            .font(.system(size: 9))
                            .foregroundColor(Color.wuxingColor(y.wuxing))
                        YaoSymbol(isYang: y.yinYang == "阳", isChanging: y.isDong, width: 36)
                        if y.isShi { Text("世").font(.system(size: 9, weight: .bold)).foregroundColor(.accent) }
                        if y.isYing { Text("应").font(.system(size: 9, weight: .bold)).foregroundColor(.gold) }
                        if y.isKong { Text("空").font(.system(size: 8)).foregroundColor(.jiRed) }
                        if y.isDong { Text("○").font(.system(size: 10)).foregroundColor(.accent) }
                    }
                    .frame(maxWidth: .infinity)

                    if gua.hasChanging, let cYaos = gua.changedYaos, y.isDong {
                        let c = cYaos[i]
                        HStack(spacing: 4) {
                            Text(c.liuqin)
                                .font(.system(size: 10))
                                .foregroundColor(.goldLight)
                            Text("\(c.tianGan)\(c.diZhi)")
                                .font(.system(size: 11, design: .serif))
                            Text(c.wuxing)
                                .font(.system(size: 9))
                                .foregroundColor(Color.wuxingColor(c.wuxing))
                        }
                        .frame(maxWidth: .infinity)
                    } else if gua.hasChanging {
                        Spacer().frame(maxWidth: .infinity)
                    }
                }
                .foregroundColor(.textPrimary)
                .padding(.vertical, 5)

                if i > 0 {
                    Divider().background(Color.border.opacity(0.5))
                }
            }
        }
        .padding(12)
        .background(Color.bgPanel.opacity(0.6))
        .clipShape(RoundedRectangle(cornerRadius: 10))
        .overlay(RoundedRectangle(cornerRadius: 10).stroke(Color.border, lineWidth: 0.5))
    }

    // MARK: - AI区域
    private var aiSection: some View {
        Group {
            if let error = vm.aiService.error {
                VStack(spacing: 8) {
                    Text("AI解读失败").font(.system(size: 15, weight: .medium, design: .serif)).foregroundColor(.jiRed)
                    Text(error).font(.system(size: 12, design: .serif)).foregroundColor(.jiRed.opacity(0.7))
                    Text("请检查设置中的API密钥").font(.system(size: 11)).foregroundColor(.textTertiary)
                }
                .resultStyle()
            } else if let text = vm.aiService.result {
                VStack(alignment: .leading, spacing: 8) {
                    Text("AI深度解读").font(.system(size: 15, weight: .medium, design: .serif)).foregroundColor(.goldLight)
                    MarkdownText(text)
                }
                .resultStyle()
            }
        }
    }
}

// MARK: - 自定义输入框样式
struct HexgramTextFieldStyle: TextFieldStyle {
    func _body(configuration: TextField<Self._Label>) -> some View {
        configuration
            .padding(12)
            .background(Color.clear)
            .foregroundColor(.textPrimary)
            .font(.system(size: 14, design: .serif))
            .overlay(
                RoundedRectangle(cornerRadius: 8)
                    .stroke(Color.border, lineWidth: 1)
            )
    }
}

// MARK: - 简易Markdown渲染
struct MarkdownText: View {
    let text: String

    init(_ text: String) {
        self.text = text
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            ForEach(Array(text.components(separatedBy: "\n").enumerated()), id: \.offset) { _, line in
                if line.hasPrefix("## ") {
                    Text(String(line.dropFirst(3)))
                        .font(.system(size: 15, weight: .medium, design: .serif))
                        .foregroundColor(.goldLight)
                        .padding(.top, 12)
                        .padding(.bottom, 4)
                } else if line.hasPrefix("### ") {
                    Text(String(line.dropFirst(4)))
                        .font(.system(size: 13, weight: .medium, design: .serif))
                        .foregroundColor(.gold)
                        .padding(.top, 8)
                } else if line.hasPrefix("---") {
                    Divider().background(Color.border)
                } else if !line.isEmpty {
                    styledLine(line)
                }
            }
        }
    }

    private func styledLine(_ line: String) -> some View {
        let parts = line.components(separatedBy: "**")
        var result = Text("")
        for (i, part) in parts.enumerated() {
            if i % 2 == 1 {
                result = result + Text(part).foregroundColor(.goldLight).bold()
            } else {
                result = result + Text(part)
            }
        }
        return result
            .font(.system(size: 13.5, design: .serif))
            .foregroundColor(Color(hex: "d4c4a8"))
            .lineSpacing(6)
    }
}
