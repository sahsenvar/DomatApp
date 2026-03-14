import SwiftUI
import Shared

@main
struct iOSApp: App {
    @StateObject private var router = NavigationRouter()

    init() {
        // Initialize Koin DI container
        initializeKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView().environmentObject(router).domatTheme()
        }
    }
}
