import SwiftUI

/// 太极八卦主题 App Icon 视图（用于 Preview 和导出）
/// 在 Xcode 中运行 Preview，截图后设置为 AppIcon
struct AppIconView: View {
    let size: CGFloat

    var body: some View {
        ZStack {
            // 背景
            RoundedRectangle(cornerRadius: size * 0.22)
                .fill(
                    RadialGradient(
                        colors: [Color(hex: "1a1510"), Color(hex: "0d0b08")],
                        center: .center,
                        startRadius: 0,
                        endRadius: size * 0.6
                    )
                )

            // 外圈金边
            Circle()
                .stroke(
                    LinearGradient(
                        colors: [Color(hex: "f5deb3"), Color(hex: "c9a96e"), Color(hex: "a07840")],
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    ),
                    lineWidth: size * 0.02
                )
                .frame(width: size * 0.82, height: size * 0.82)

            // 八卦符号圆环（简化：上下左右四卦）
            ForEach(0..<8, id: \.self) { i in
                let angle = Double(i) * 45.0 - 90.0
                let radius = size * 0.37
                let x = cos(angle * .pi / 180) * radius
                let y = sin(angle * .pi / 180) * radius

                // 简化三爻符号
                trigramSymbol(index: i)
                    .offset(x: x, y: y)
                    .rotationEffect(.degrees(angle + 90))
            }

            // 中心太极
            taijiSymbol(size: size * 0.35)

            // "易" 字
            Text("易")
                .font(.system(size: size * 0.12, weight: .medium, design: .serif))
                .foregroundColor(Color(hex: "c9a96e"))
                .offset(y: size * 0.01)
        }
        .frame(width: size, height: size)
    }

    // 简化八卦三线符号
    private func trigramSymbol(index: Int) -> some View {
        let patterns: [[Bool]] = [
            [true, true, true],    // 乾
            [false, true, true],   // 兑
            [true, false, true],   // 离
            [false, false, true],  // 震
            [true, true, false],   // 巽
            [true, false, false],  // 坎
            [false, true, false],  // 艮
            [false, false, false], // 坤
        ]
        let pat = patterns[index % 8]
        let lineW: CGFloat = size * 0.06
        let lineH: CGFloat = size * 0.012
        let gap: CGFloat = size * 0.005

        return VStack(spacing: gap) {
            ForEach(0..<3, id: \.self) { j in
                if pat[2 - j] {
                    // 阳爻
                    RoundedRectangle(cornerRadius: 1)
                        .fill(Color(hex: "c9a96e").opacity(0.7))
                        .frame(width: lineW, height: lineH)
                } else {
                    // 阴爻
                    HStack(spacing: lineW * 0.2) {
                        RoundedRectangle(cornerRadius: 1)
                            .fill(Color(hex: "c9a96e").opacity(0.7))
                            .frame(width: lineW * 0.38, height: lineH)
                        RoundedRectangle(cornerRadius: 1)
                            .fill(Color(hex: "c9a96e").opacity(0.7))
                            .frame(width: lineW * 0.38, height: lineH)
                    }
                    .frame(width: lineW)
                }
            }
        }
    }

    // 太极图
    private func taijiSymbol(size: CGFloat) -> some View {
        ZStack {
            // 外圆背景
            Circle()
                .fill(Color(hex: "0d0b08"))
                .frame(width: size, height: size)

            // 上半白（金色）
            Circle()
                .fill(Color(hex: "c9a96e").opacity(0.8))
                .frame(width: size, height: size)
                .mask(
                    Rectangle().frame(width: size, height: size / 2).offset(y: -size / 4)
                )

            // 下半黑
            Circle()
                .fill(Color(hex: "1a1510"))
                .frame(width: size, height: size)
                .mask(
                    Rectangle().frame(width: size, height: size / 2).offset(y: size / 4)
                )

            // 左小圆
            Circle()
                .fill(Color(hex: "1a1510"))
                .frame(width: size / 2, height: size / 2)
                .offset(x: -size / 4)
                .mask(Circle().frame(width: size, height: size))

            // 右小圆
            Circle()
                .fill(Color(hex: "c9a96e").opacity(0.8))
                .frame(width: size / 2, height: size / 2)
                .offset(x: size / 4)
                .mask(Circle().frame(width: size, height: size))

            // 鱼眼
            Circle().fill(Color(hex: "c9a96e")).frame(width: size * 0.1).offset(x: -size / 4)
            Circle().fill(Color(hex: "1a1510")).frame(width: size * 0.1).offset(x: size / 4)

            // 外圈
            Circle()
                .stroke(Color(hex: "c9a96e"), lineWidth: size * 0.03)
                .frame(width: size, height: size)
        }
    }
}

#Preview("App Icon 1024") {
    AppIconView(size: 1024)
        .previewLayout(.sizeThatFits)
}

#Preview("App Icon 180") {
    AppIconView(size: 180)
        .previewLayout(.sizeThatFits)
}
