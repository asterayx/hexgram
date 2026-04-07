import SwiftUI

struct ContentView: View {
    @State private var selectedTab = 0
    @State private var showSettings = false

    var body: some View {
        ZStack(alignment: .bottomTrailing) {
            TabView(selection: $selectedTab) {
                LiuyaoView()
                    .tabItem {
                        Label {
                            Text("六爻")
                        } icon: {
                            Image(systemName: "hexagon")
                        }
                    }
                    .tag(0)

                BaziView()
                    .tabItem {
                        Label {
                            Text("八字")
                        } icon: {
                            Image(systemName: "person.text.rectangle")
                        }
                    }
                    .tag(1)

                HuangliView()
                    .tabItem {
                        Label {
                            Text("黄历")
                        } icon: {
                            Image(systemName: "calendar")
                        }
                    }
                    .tag(2)

                LingqianView()
                    .tabItem {
                        Label {
                            Text("灵签")
                        } icon: {
                            Image(systemName: "flame")
                        }
                    }
                    .tag(3)
            }
            .tint(.gold)

            // 设置按钮
            Button(action: { showSettings = true }) {
                Image(systemName: "gearshape.fill")
                    .font(.system(size: 14))
                    .foregroundColor(.textSecondary)
                    .frame(width: 34, height: 34)
                    .background(Color.bgPanel)
                    .clipShape(Circle())
                    .overlay(Circle().stroke(Color.border, lineWidth: 1))
            }
            .padding(.trailing, 12)
            .padding(.bottom, 60)
        }
        .sheet(isPresented: $showSettings) {
            SettingsView()
        }
        .preferredColorScheme(.dark)
    }
}
