import SwiftUI

struct BaziView: View {
    @StateObject private var vm = BaziViewModel()
    @State private var showShareSheet = false

    var body: some View {
        ScrollView {
            VStack(spacing: 14) {
                SectionHeader(subtitle: "四 柱 命 理", title: "八字排盘")

                // 输入面板
                inputPanel

                // 结果
                if let result = vm.result {
                    resultView(result)
                }

                // AI
                aiSection
            }
            .padding(.horizontal, 14)
            .padding(.bottom, 20)
        }
    }

    // MARK: - 输入面板
    private var inputPanel: some View {
        VStack(spacing: 10) {
            HStack(spacing: 8) {
                VStack(alignment: .leading, spacing: 3) {
                    Text("出生日期").font(.system(size: 11, design: .serif)).foregroundColor(.textSecondary)
                    DatePicker("", selection: $vm.selectedDate, in: ...Date(), displayedComponents: .date)
                        .datePickerStyle(.compact)
                        .labelsHidden()
                        .tint(.gold)
                }

                VStack(alignment: .leading, spacing: 3) {
                    Text("时辰").font(.system(size: 11, design: .serif)).foregroundColor(.textSecondary)
                    Picker("", selection: $vm.selectedHour) {
                        ForEach(BaziViewModel.hourOptions, id: \.value) { option in
                            Text(option.label).tag(option.value)
                        }
                    }
                    .pickerStyle(.menu)
                    .tint(.gold)
                }
            }

            HStack(spacing: 8) {
                VStack(alignment: .leading, spacing: 3) {
                    Text("性别").font(.system(size: 11, design: .serif)).foregroundColor(.textSecondary)
                    Picker("", selection: $vm.sex) {
                        Text("男").tag("M")
                        Text("女").tag("F")
                    }
                    .pickerStyle(.segmented)
                }

                VStack(alignment: .leading, spacing: 3) {
                    Text("姓名（选填）").font(.system(size: 11, design: .serif)).foregroundColor(.textSecondary)
                    TextField("", text: $vm.name)
                        .textFieldStyle(HexgramTextFieldStyle())
                }
            }

            Button("排盘") { vm.calculate() }
                .buttonStyle(GoldButtonStyle())
        }
        .panelStyle()
    }

    // MARK: - 结果
    private func resultView(_ r: BaziResult) -> some View {
        VStack(spacing: 12) {
            // 标题
            Text("\(r.name.isEmpty ? "" : r.name + "的")八字命盘")
                .font(.system(size: 15, weight: .medium, design: .serif))
                .foregroundColor(.goldLight)

            Text("\(r.birthYear)年\(r.birthMonth)月\(r.birthDay)日 \(GanZhi.diZhi[((r.birthHour + 1) % 24) / 2])时　\(r.sex == "M" ? "男" : "女")命　\(r.shengXiao)年")
                .font(.system(size: 12, design: .serif))
                .foregroundColor(.textSecondary)

            // 四柱网格
            fourPillarsGrid(r.pillars)

            // 地支藏干
            cangGanSection(r.pillars)

            // 五行力量
            wuxingBar(r.wuxingCounts)

            // 日主分析
            dayMasterSection(r)

            // 纳音
            naYinSection(r)

            // 神煞
            if !r.shenSha.isEmpty {
                shenShaSection(r.shenSha)
            }

            // 地支关系
            if !r.diZhiRelations.isEmpty {
                diZhiRelationsSection(r.diZhiRelations)
            }

            // 大运
            daYunSection(r)

            // 流年
            liuNianSection(r)

            // 按钮
            if vm.aiService.isLoading {
                ThinkingButton(text: "命理师正在推演命盘…")
            } else {
                HStack(spacing: 10) {
                    Button(action: { Task { await vm.aiRead() } }) {
                        HStack(spacing: 4) {
                            Text("🤖")
                            Text("AI深度解读")
                        }
                    }
                    .buttonStyle(GoldButtonStyle())

                    Button(action: { showShareSheet = true }) {
                        HStack(spacing: 4) {
                            Image(systemName: "square.and.arrow.up")
                            Text("分享")
                        }
                    }
                    .buttonStyle(GhostButtonStyle())
                }
            }
        }
        .resultStyle()
        .sheet(isPresented: $showShareSheet) {
            if let r = vm.result {
                let card = PaipanSnapshot.baziCard(result: r)
                let image = PaipanSnapshot.render(card, size: CGSize(width: 360, height: 350))
                let items: [Any] = [image, vm.resultText].compactMap { $0 }
                ShareSheet(items: items)
            }
        }
    }

    // MARK: - 四柱网格
    private func fourPillarsGrid(_ pillars: [BaziPillar]) -> some View {
        HStack(spacing: 6) {
            ForEach(Array(pillars.enumerated()), id: \.element.id) { i, p in
                VStack(spacing: 6) {
                    Text(p.label)
                        .font(.system(size: 10, design: .serif))
                        .foregroundColor(.textSecondary)

                    Text(p.gan)
                        .font(.system(size: 22, design: .serif))
                        .foregroundColor(i == 2 ? .accent : .goldLight)
                        .if(i == 2) { $0.underline() }

                    Text(p.zhi)
                        .font(.system(size: 22, design: .serif))
                        .foregroundColor(.gold)

                    Text(p.shiShen)
                        .font(.system(size: 10, design: .serif))
                        .foregroundColor(.textSecondary)

                    Text(p.wuxing)
                        .font(.system(size: 10))
                        .foregroundColor(Color.wuxingColor(p.wuxing))
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 10)
                .background(Color.bgPanel)
                .clipShape(RoundedRectangle(cornerRadius: 8))
                .overlay(RoundedRectangle(cornerRadius: 8).stroke(Color.border, lineWidth: 1))
            }
        }
    }

    // MARK: - 藏干
    private func cangGanSection(_ pillars: [BaziPillar]) -> some View {
        VStack(spacing: 4) {
            Text("地支藏干")
                .font(.system(size: 13, weight: .medium, design: .serif))
                .foregroundColor(.gold)

            HStack(spacing: 6) {
                ForEach(pillars) { p in
                    VStack(spacing: 2) {
                        Text(p.zhi)
                            .font(.system(size: 11, weight: .medium, design: .serif))
                            .foregroundColor(.gold)
                        ForEach(p.cangGan) { cg in
                            Text("\(cg.gan)(\(cg.shiShen))")
                                .font(.system(size: 10, design: .serif))
                                .foregroundColor(.textSecondary)
                        }
                    }
                    .frame(maxWidth: .infinity)
                }
            }
        }
    }

    // MARK: - 五行力量
    private func wuxingBar(_ counts: [String: Double]) -> some View {
        let maxW = max(counts.values.max() ?? 1, 1)
        return VStack(spacing: 4) {
            Text("五行力量")
                .font(.system(size: 13, weight: .medium, design: .serif))
                .foregroundColor(.gold)

            HStack(alignment: .bottom, spacing: 4) {
                ForEach(GanZhi.wuxingAll, id: \.self) { wx in
                    let count = counts[wx] ?? 0
                    let pct = count / maxW
                    VStack(spacing: 2) {
                        if count > 0 {
                            Text(String(format: "%.1f", count))
                                .font(.system(size: 10, weight: .semibold))
                                .foregroundColor(.bgPrimary)
                        }
                        RoundedRectangle(cornerRadius: 3)
                            .fill(Color.wuxingColor(wx))
                            .frame(height: max(CGFloat(pct) * 45, 6))
                        Text(wx)
                            .font(.system(size: 10, design: .serif))
                            .foregroundColor(.textSecondary)
                    }
                    .frame(maxWidth: .infinity)
                }
            }
            .frame(height: 70)
        }
    }

    // MARK: - 日主分析
    private func dayMasterSection(_ r: BaziResult) -> some View {
        VStack(spacing: 4) {
            Text("日主分析")
                .font(.system(size: 13, weight: .medium, design: .serif))
                .foregroundColor(.gold)

            HStack {
                Text("日主")
                Text(r.riGan).foregroundColor(.accent).bold()
                Text("（\(r.riGanWuxing)），")
                Text(r.isStrong ? "身旺" : "身弱").foregroundColor(r.isStrong ? .yiGreen : .jiRed).bold()
            }
            .font(.system(size: 13, design: .serif))
            .foregroundColor(.textPrimary)

            if r.isStrong {
                Text("喜用：食伤泄秀、财星耗身、官杀克制")
                    .font(.system(size: 12, design: .serif)).foregroundColor(.textSecondary)
                Text("忌：印星、比劫")
                    .font(.system(size: 12, design: .serif)).foregroundColor(.textSecondary)
            } else {
                Text("喜用：印星生扶、比劫帮身")
                    .font(.system(size: 12, design: .serif)).foregroundColor(.textSecondary)
                Text("忌：官杀克身、财星泄身")
                    .font(.system(size: 12, design: .serif)).foregroundColor(.textSecondary)
            }
        }
    }

    // MARK: - 大运
    private func daYunSection(_ r: BaziResult) -> some View {
        VStack(spacing: 4) {
            Text("大运（\(r.isShunPai ? "顺排" : "逆排")）")
                .font(.system(size: 13, weight: .medium, design: .serif))
                .foregroundColor(.gold)

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 6) {
                    ForEach(r.daYun) { dy in
                        let isCur = r.currentYear >= dy.year && r.currentYear < dy.year + 10
                        VStack(spacing: 2) {
                            Text("\(dy.gan)\(dy.zhi)")
                                .font(.system(size: 14, design: .serif))
                                .foregroundColor(.goldLight)
                            Text("\(dy.age)岁")
                                .font(.system(size: 9, design: .serif))
                                .foregroundColor(.textTertiary)
                            Text("\(dy.year)")
                                .font(.system(size: 9, design: .serif))
                                .foregroundColor(.textTertiary)
                            Text(dy.wuxing)
                                .font(.system(size: 10))
                                .foregroundColor(Color.wuxingColor(dy.wuxing))
                        }
                        .padding(.horizontal, 4)
                        .padding(.vertical, 8)
                        .frame(width: 60)
                        .background(isCur ? Color.accent.opacity(0.08) : Color.clear)
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                        .overlay(
                            RoundedRectangle(cornerRadius: 8)
                                .stroke(isCur ? Color.accent : Color.border, lineWidth: 1)
                        )
                    }
                }
            }
        }
    }

    // MARK: - 流年
    private func liuNianSection(_ r: BaziResult) -> some View {
        VStack(spacing: 4) {
            Text("\(r.currentYear)年流年")
                .font(.system(size: 13, weight: .medium, design: .serif))
                .foregroundColor(.gold)

            HStack {
                Text("流年")
                Text("\(r.liuNianGan)\(r.liuNianZhi)").foregroundColor(.goldLight).bold()
                Text("（\(GanZhi.wuxingTianGan[r.liuNianGan] ?? "")）")
                Text("对日主为")
                Text(r.liuNianShiShen).foregroundColor(.accent).bold()
            }
            .font(.system(size: 12, design: .serif))
            .foregroundColor(.textPrimary)
        }
    }

    // MARK: - 纳音
    private func naYinSection(_ r: BaziResult) -> some View {
        VStack(spacing: 4) {
            Text("六十甲���纳音")
                .font(.system(size: 13, weight: .medium, design: .serif))
                .foregroundColor(.gold)

            HStack(spacing: 6) {
                ForEach(Array(r.pillars.enumerated()), id: \.element.id) { i, p in
                    VStack(spacing: 2) {
                        Text("\(p.gan)\(p.zhi)")
                            .font(.system(size: 11, design: .serif))
                            .foregroundColor(.goldLight)
                        Text(r.naYinPillars[i])
                            .font(.system(size: 10, design: .serif))
                            .foregroundColor(.textSecondary)
                    }
                    .frame(maxWidth: .infinity)
                }
            }
        }
    }

    // MARK: - 神煞
    private func shenShaSection(_ items: [ShenShaItem]) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            Text("神煞")
                .font(.system(size: 13, weight: .medium, design: .serif))
                .foregroundColor(.gold)

            FlowLayout(spacing: 6) {
                ForEach(items) { item in
                    HStack(spacing: 4) {
                        Text(item.name)
                            .font(.system(size: 11, weight: .medium, design: .serif))
                            .foregroundColor(.goldLight)
                        Text(item.pillar)
                            .font(.system(size: 9, design: .serif))
                            .foregroundColor(.textTertiary)
                    }
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(Color.bgPanel)
                    .clipShape(RoundedRectangle(cornerRadius: 6))
                    .overlay(RoundedRectangle(cornerRadius: 6).stroke(Color.border, lineWidth: 0.5))
                }
            }

            ForEach(items) { item in
                Text("\(item.name)（\(item.pillar)）：\(item.description)")
                    .font(.system(size: 10, design: .serif))
                    .foregroundColor(.textSecondary)
            }
        }
    }

    // MARK: - 地支关系
    private func diZhiRelationsSection(_ items: [DiZhiRelation]) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            Text("合冲刑害")
                .font(.system(size: 13, weight: .medium, design: .serif))
                .foregroundColor(.gold)

            ForEach(items) { item in
                HStack(alignment: .top, spacing: 6) {
                    Text(item.type)
                        .font(.system(size: 10, weight: .medium, design: .serif))
                        .foregroundColor(relationColor(item.type))
                        .frame(width: 40, alignment: .trailing)
                    Text(item.detail)
                        .font(.system(size: 11, design: .serif))
                        .foregroundColor(.textPrimary)
                }
            }
        }
    }

    private func relationColor(_ type: String) -> Color {
        switch type {
        case "六合", "三合局", "半合": return .yiGreen
        case "六冲": return .jiRed
        case "三刑", "自刑": return .accent
        case "相害": return .textSecondary
        default: return .gold
        }
    }

    // MARK: - AI
    private var aiSection: some View {
        Group {
            if let error = vm.aiService.error {
                VStack(spacing: 8) {
                    Text("AI解读失败").font(.system(size: 15, weight: .medium, design: .serif)).foregroundColor(.jiRed)
                    Text(error).font(.system(size: 12)).foregroundColor(.jiRed.opacity(0.7))
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

// MARK: - Conditional modifier
extension View {
    @ViewBuilder func `if`<Content: View>(_ condition: Bool, transform: (Self) -> Content) -> some View {
        if condition { transform(self) }
        else { self }
    }
}
