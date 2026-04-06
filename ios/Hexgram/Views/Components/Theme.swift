import SwiftUI

// MARK: - Color Theme (易学三合配色)
extension Color {
    static let gold = Color(hex: "c9a96e")
    static let goldLight = Color(hex: "f5deb3")
    static let goldDark = Color(hex: "a07840")
    static let bgPrimary = Color(hex: "0d0b08")
    static let bgPanel = Color(hex: "1a1510")
    static let bgResult = Color(hex: "13100c")
    static let border = Color(hex: "3d3425")
    static let textPrimary = Color(hex: "e8dcc8")
    static let textSecondary = Color(hex: "8b7355")
    static let textTertiary = Color(hex: "5a4d3a")
    static let accent = Color(hex: "e8a444")

    // 五行颜色
    static let wxWood = Color(hex: "66bb6a")
    static let wxFire = Color(hex: "ef5350")
    static let wxEarth = Color(hex: "ffa726")
    static let wxMetal = Color(hex: "e0e0e0")
    static let wxWater = Color(hex: "42a5f5")

    static let yiGreen = Color(hex: "8bc34a")
    static let jiRed = Color(hex: "e57373")

    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let a, r, g, b: UInt64
        switch hex.count {
        case 6:
            (a, r, g, b) = (255, (int >> 16) & 0xFF, (int >> 8) & 0xFF, int & 0xFF)
        case 8:
            (a, r, g, b) = ((int >> 24) & 0xFF, (int >> 16) & 0xFF, (int >> 8) & 0xFF, int & 0xFF)
        default:
            (a, r, g, b) = (255, 0, 0, 0)
        }
        self.init(.sRGB, red: Double(r) / 255, green: Double(g) / 255, blue: Double(b) / 255, opacity: Double(a) / 255)
    }

    static func wuxingColor(_ wx: String) -> Color {
        switch wx {
        case "木": return .wxWood
        case "火": return .wxFire
        case "土": return .wxEarth
        case "金": return .wxMetal
        case "水": return .wxWater
        default: return .textSecondary
        }
    }
}

// MARK: - Common Modifiers
struct PanelStyle: ViewModifier {
    func body(content: Content) -> some View {
        content
            .padding(16)
            .background(Color.bgPanel.opacity(0.85))
            .clipShape(RoundedRectangle(cornerRadius: 14))
            .overlay(
                RoundedRectangle(cornerRadius: 14)
                    .stroke(Color.border, lineWidth: 1)
            )
    }
}

struct ResultStyle: ViewModifier {
    func body(content: Content) -> some View {
        content
            .padding(18)
            .background(Color.bgResult)
            .clipShape(RoundedRectangle(cornerRadius: 14))
            .overlay(
                RoundedRectangle(cornerRadius: 14)
                    .stroke(Color.border, lineWidth: 1)
            )
    }
}

struct GoldButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .padding(.horizontal, 20)
            .padding(.vertical, 12)
            .background(
                LinearGradient(colors: [.gold, .goldDark], startPoint: .topLeading, endPoint: .bottomTrailing)
            )
            .foregroundColor(.bgPrimary)
            .font(.system(size: 15, weight: .semibold, design: .serif))
            .clipShape(RoundedRectangle(cornerRadius: 10))
            .scaleEffect(configuration.isPressed ? 0.96 : 1.0)
    }
}

struct GhostButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .padding(.horizontal, 20)
            .padding(.vertical, 12)
            .background(Color.clear)
            .foregroundColor(.textSecondary)
            .font(.system(size: 13, design: .serif))
            .clipShape(RoundedRectangle(cornerRadius: 10))
            .overlay(
                RoundedRectangle(cornerRadius: 10)
                    .stroke(Color.border, lineWidth: 1)
            )
            .scaleEffect(configuration.isPressed ? 0.96 : 1.0)
    }
}

extension View {
    func panelStyle() -> some View {
        modifier(PanelStyle())
    }
    func resultStyle() -> some View {
        modifier(ResultStyle())
    }
}

// MARK: - Header View
struct SectionHeader: View {
    let subtitle: String
    let title: String

    var body: some View {
        VStack(spacing: 4) {
            Text(subtitle)
                .font(.system(size: 11, design: .serif))
                .tracking(6)
                .foregroundColor(.textSecondary)
            Text(title)
                .font(.system(size: 28, weight: .regular, design: .serif))
                .foregroundStyle(
                    LinearGradient(colors: [.goldLight, .gold], startPoint: .top, endPoint: .bottom)
                )
            Rectangle()
                .fill(
                    LinearGradient(colors: [.clear, .gold, .clear], startPoint: .leading, endPoint: .trailing)
                )
                .frame(width: 50, height: 1)
                .padding(.top, 2)
        }
        .padding(.bottom, 20)
    }
}

// MARK: - Loading Spinner
struct LoadingSpinner: View {
    @State private var isAnimating = false
    let text: String

    var body: some View {
        VStack(spacing: 12) {
            Circle()
                .trim(from: 0, to: 0.7)
                .stroke(Color.gold, lineWidth: 2)
                .frame(width: 32, height: 32)
                .rotationEffect(Angle(degrees: isAnimating ? 360 : 0))
                .animation(.linear(duration: 1).repeatForever(autoreverses: false), value: isAnimating)
                .onAppear { isAnimating = true }
            Text(text)
                .font(.system(size: 12, design: .serif))
                .foregroundColor(.textSecondary)
        }
        .padding(.vertical, 24)
    }
}

// MARK: - Yao Symbol
struct YaoSymbol: View {
    let isYang: Bool
    let isChanging: Bool
    let width: CGFloat

    init(isYang: Bool, isChanging: Bool = false, width: CGFloat = 120) {
        self.isYang = isYang
        self.isChanging = isChanging
        self.width = width
    }

    var lineColor: LinearGradient {
        if isChanging {
            return LinearGradient(colors: [Color.accent.opacity(0.3), .gold, Color.accent.opacity(0.3)], startPoint: .leading, endPoint: .trailing)
        }
        return LinearGradient(colors: [.gold, .goldLight, .gold], startPoint: .leading, endPoint: .trailing)
    }

    var body: some View {
        HStack(spacing: 0) {
            if isYang {
                RoundedRectangle(cornerRadius: 2)
                    .fill(lineColor)
                    .frame(width: width, height: 8)
            } else {
                RoundedRectangle(cornerRadius: 2)
                    .fill(lineColor)
                    .frame(width: width * 0.42, height: 8)
                Spacer().frame(width: width * 0.16)
                RoundedRectangle(cornerRadius: 2)
                    .fill(lineColor)
                    .frame(width: width * 0.42, height: 8)
            }
        }
        .frame(width: width)
    }
}
