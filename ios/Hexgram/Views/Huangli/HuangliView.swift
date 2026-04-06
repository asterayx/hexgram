import SwiftUI

struct HuangliView: View {
    @StateObject private var vm = HuangliViewModel()

    var body: some View {
        ScrollView {
            VStack(spacing: 14) {
                SectionHeader(subtitle: "传 统 黄 历", title: "每日宜忌")

                // 日期选择
                HStack(spacing: 10) {
                    DatePicker("", selection: $vm.selectedDate, displayedComponents: .date)
                        .datePickerStyle(.compact)
                        .labelsHidden()
                        .tint(.gold)

                    Button("查询") { vm.calculate() }
                        .buttonStyle(GoldButtonStyle())
                }
                .panelStyle()

                // 结果
                if let r = vm.result {
                    resultView(r)
                }

                // AI
                aiSection
            }
            .padding(.horizontal, 14)
            .padding(.bottom, 20)
        }
        .onAppear { vm.calculate() }
        .onChange(of: vm.selectedDate) { _, _ in vm.calculate() }
    }

    // MARK: - 结果
    private func resultView(_ r: HuangliResult) -> some View {
        VStack(spacing: 12) {
            // 日期与干支
            dateHeader(r)

            // 宜
            yiSection(r.yi)

            // 忌
            jiSection(r.ji)

            // 吉神方位
            directionsGrid(r)

            // 详细信息
            detailInfo(r)

            // AI按钮
            Button(action: { Task { await vm.aiRead() } }) {
                HStack(spacing: 4) {
                    Text("🤖")
                    Text("AI深度解读")
                }
            }
            .buttonStyle(GoldButtonStyle())
        }
    }

    // MARK: - 日期头
    private func dateHeader(_ r: HuangliResult) -> some View {
        VStack(spacing: 6) {
            Text("\(r.year)年\(r.month)月\(r.day)日　星期\(r.weekDay)")
                .font(.system(size: 18, design: .serif))
                .foregroundColor(.goldLight)

            Text("\(r.riGan)\(r.riZhi)日")
                .font(.system(size: 30, weight: .medium, design: .serif))
                .foregroundColor(.gold)

            Text("\(r.nianGan)\(r.nianZhi)年　\(r.lunarMonth)月　\(r.shengXiao)年　\(r.jianChu)日　\(r.erShiBaXiu)宿")
                .font(.system(size: 12, design: .serif))
                .foregroundColor(.textSecondary)
        }
        .panelStyle()
    }

    // MARK: - 宜
    private func yiSection(_ yi: String) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            Text("宜")
                .font(.system(size: 14, weight: .medium, design: .serif))
                .foregroundColor(.yiGreen)

            Text(yi.isEmpty ? "无特别宜事" : yi)
                .font(.system(size: 13, design: .serif))
                .foregroundColor(.textPrimary)
                .lineSpacing(6)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(12)
        .background(Color.yiGreen.opacity(0.08))
        .clipShape(RoundedRectangle(cornerRadius: 10))
        .overlay(RoundedRectangle(cornerRadius: 10).stroke(Color.yiGreen.opacity(0.2), lineWidth: 1))
    }

    // MARK: - 忌
    private func jiSection(_ ji: String) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            Text("忌")
                .font(.system(size: 14, weight: .medium, design: .serif))
                .foregroundColor(.jiRed)

            Text(ji)
                .font(.system(size: 13, design: .serif))
                .foregroundColor(.textPrimary)
                .lineSpacing(6)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(12)
        .background(Color.jiRed.opacity(0.06))
        .clipShape(RoundedRectangle(cornerRadius: 10))
        .overlay(RoundedRectangle(cornerRadius: 10).stroke(Color.jiRed.opacity(0.15), lineWidth: 1))
    }

    // MARK: - 吉神方位
    private func directionsGrid(_ r: HuangliResult) -> some View {
        VStack(spacing: 8) {
            Text("吉神方位")
                .font(.system(size: 14, weight: .medium, design: .serif))
                .foregroundColor(.goldLight)

            HStack(spacing: 6) {
                directionItem(name: "喜神", symbol: "囍", direction: r.xiShen)
                directionItem(name: "财神", symbol: "$", direction: r.caiShen)
                directionItem(name: "福神", symbol: "福", direction: r.fuShen)
            }
        }
        .panelStyle()
    }

    private func directionItem(name: String, symbol: String, direction: String) -> some View {
        VStack(spacing: 3) {
            Text(name)
                .font(.system(size: 11, design: .serif))
                .foregroundColor(.textSecondary)
            Text(symbol)
                .font(.system(size: 18, design: .serif))
                .foregroundColor(.goldLight)
            Text(direction)
                .font(.system(size: 11, design: .serif))
                .foregroundColor(.gold)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 10)
        .background(Color.bgPanel)
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .overlay(RoundedRectangle(cornerRadius: 8).stroke(Color.border, lineWidth: 1))
    }

    // MARK: - 详细信息
    private func detailInfo(_ r: HuangliResult) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack {
                Text("冲煞").font(.system(size: 12, weight: .medium, design: .serif)).foregroundColor(.goldLight)
                Text("冲\(r.chongSha)")
                    .font(.system(size: 12, design: .serif)).foregroundColor(.textPrimary)
            }

            VStack(alignment: .leading, spacing: 2) {
                Text("彭祖百忌").font(.system(size: 12, weight: .medium, design: .serif)).foregroundColor(.goldLight)
                Text(r.pengZuGan)
                    .font(.system(size: 12, design: .serif)).foregroundColor(.textSecondary)
                Text(r.pengZuZhi)
                    .font(.system(size: 12, design: .serif)).foregroundColor(.textSecondary)
            }

            HStack {
                Text("十二建星").font(.system(size: 12, weight: .medium, design: .serif)).foregroundColor(.goldLight)
                Text("\(r.jianChu)日")
                    .font(.system(size: 12, design: .serif)).foregroundColor(.textPrimary)
                Spacer()
                Text("二十八宿").font(.system(size: 12, weight: .medium, design: .serif)).foregroundColor(.goldLight)
                Text("\(r.erShiBaXiu)宿")
                    .font(.system(size: 12, design: .serif)).foregroundColor(.textPrimary)
            }
        }
        .panelStyle()
    }

    // MARK: - AI
    private var aiSection: some View {
        Group {
            if vm.aiService.isLoading {
                LoadingSpinner(text: "择日师正在分析今日运势…")
            } else if let error = vm.aiService.error {
                VStack(spacing: 8) {
                    Text("AI解读失败").foregroundColor(.jiRed)
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
