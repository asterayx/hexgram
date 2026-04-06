import SwiftUI

// MARK: - 分享功能
struct ShareSheet: UIViewControllerRepresentable {
    let items: [Any]

    func makeUIViewController(context: Context) -> UIActivityViewController {
        UIActivityViewController(activityItems: items, applicationActivities: nil)
    }

    func updateUIViewController(_ uiViewController: UIActivityViewController, context: Context) {}
}

// MARK: - 排盘��图生��
@MainActor
struct PaipanSnapshot {

    /// 将SwiftUI视图渲染为UIImage
    static func render<V: View>(_ view: V, size: CGSize) -> UIImage? {
        let renderer = ImageRenderer(content: view.frame(width: size.width))
        renderer.scale = UIScreen.main.scale
        return renderer.uiImage
    }

    /// 六爻排盘分享卡片
    static func liuyaoCard(guaResult: GuaResult, text: String) -> some View {
        VStack(spacing: 12) {
            // 标题
            HStack {
                VStack(alignment: .leading, spacing: 2) {
                    Text("\(guaResult.gong)宫 · \(guaResult.guaName)卦")
                        .font(.system(size: 20, weight: .medium, design: .serif))
                        .foregroundColor(Color(hex: "f5deb3"))
                    if guaResult.hasChanging {
                        Text("→ 变卦 \(guaResult.changedGuaName ?? "")")
                            .font(.system(size: 13, design: .serif))
                            .foregroundColor(Color(hex: "e8a444"))
                    }
                }
                Spacer()
                VStack(alignment: .trailing, spacing: 2) {
                    Text("\(guaResult.outerGua)上·\(guaResult.innerGua)下")
                        .font(.system(size: 11, design: .serif))
                        .foregroundColor(Color(hex: "8b7355"))
                    Text("日建\(guaResult.riGan)\(guaResult.riZhi)　月建\(guaResult.yueZhi)月")
                        .font(.system(size: 11, design: .serif))
                        .foregroundColor(Color(hex: "8b7355"))
                }
            }

            Divider().background(Color(hex: "3d3425"))

            // 六爻表
            VStack(spacing: 0) {
                ForEach(Array(stride(from: 5, through: 0, by: -1)), id: \.self) { i in
                    let y = guaResult.yaos[i]
                    HStack(spacing: 8) {
                        Text(y.liuShen).font(.system(size: 10, design: .serif)).foregroundColor(Color(hex: "8b7355")).frame(width: 28)
                        Text(y.liuqin).font(.system(size: 10, design: .serif)).foregroundColor(Color(hex: "f5deb3")).frame(width: 28)
                        Text("\(y.tianGan)\(y.diZhi)").font(.system(size: 12, design: .serif)).foregroundColor(Color(hex: "e8dcc8"))
                        Text(y.wuxing).font(.system(size: 9)).foregroundColor(Color(hex: "8b7355"))

                        // 爻线
                        YaoSymbol(isYang: y.yinYang == "阳", isChanging: y.isDong, width: 30)

                        if y.isShi { Text("世").font(.system(size: 9, weight: .bold)).foregroundColor(Color(hex: "e8a444")) }
                        else if y.isYing { Text("应").font(.system(size: 9, weight: .bold)).foregroundColor(Color(hex: "c9a96e")) }
                        else { Text("　").font(.system(size: 9)) }

                        if y.isDong, let cYaos = guaResult.changedYaos {
                            let c = cYaos[i]
                            Text("→\(c.liuqin)\(c.tianGan)\(c.diZhi)")
                                .font(.system(size: 10, design: .serif))
                                .foregroundColor(Color(hex: "e8a444"))
                        }

                        Spacer()
                    }
                    .padding(.vertical, 3)
                }
            }

            Divider().background(Color(hex: "3d3425"))

            // 底部水印
            HStack {
                Text("易学三合 · 六爻纳甲排盘")
                    .font(.system(size: 9, design: .serif))
                    .foregroundColor(Color(hex: "5a4d3a"))
                Spacer()
                Text("空亡：\(guaResult.kongWang.joined(separator: "·"))")
                    .font(.system(size: 9, design: .serif))
                    .foregroundColor(Color(hex: "5a4d3a"))
            }
        }
        .padding(16)
        .background(Color(hex: "0d0b08"))
    }

    /// 八字排盘分享卡片
    static func baziCard(result: BaziResult) -> some View {
        VStack(spacing: 12) {
            Text("\(result.name.isEmpty ? "" : result.name + "的")八字命盘")
                .font(.system(size: 18, weight: .medium, design: .serif))
                .foregroundColor(Color(hex: "f5deb3"))

            // 四柱
            HStack(spacing: 8) {
                ForEach(Array(result.pillars.enumerated()), id: \.element.id) { i, p in
                    VStack(spacing: 4) {
                        Text(p.label).font(.system(size: 9, design: .serif)).foregroundColor(Color(hex: "8b7355"))
                        Text(p.gan).font(.system(size: 20, design: .serif)).foregroundColor(i == 2 ? Color(hex: "e8a444") : Color(hex: "f5deb3"))
                        Text(p.zhi).font(.system(size: 20, design: .serif)).foregroundColor(Color(hex: "c9a96e"))
                        Text(p.shiShen).font(.system(size: 9, design: .serif)).foregroundColor(Color(hex: "8b7355"))
                        Text(result.naYinPillars[i]).font(.system(size: 8, design: .serif)).foregroundColor(Color(hex: "5a4d3a"))
                    }
                    .frame(maxWidth: .infinity)
                }
            }

            Divider().background(Color(hex: "3d3425"))

            HStack {
                Text("日主\(result.riGan)（\(result.riGanWuxing)）\(result.isStrong ? "身旺" : "身弱")")
                    .font(.system(size: 11, design: .serif))
                    .foregroundColor(Color(hex: "e8dcc8"))
                Spacer()
                Text("易学三合 · 八字排盘")
                    .font(.system(size: 9, design: .serif))
                    .foregroundColor(Color(hex: "5a4d3a"))
            }
        }
        .padding(16)
        .background(Color(hex: "0d0b08"))
    }
}
