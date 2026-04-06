import SwiftUI

@main
struct HexgramApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
                .background(
                    RadialGradient(
                        colors: [Color.bgPanel, Color.bgPrimary],
                        center: .init(x: 0.5, y: 0.15),
                        startRadius: 0,
                        endRadius: 600
                    )
                    .ignoresSafeArea()
                )
        }
    }
}
