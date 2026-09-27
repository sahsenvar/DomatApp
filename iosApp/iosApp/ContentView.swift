import SwiftUI
import UIKit
import Shared

/// Hosts the shared Compose hierarchy.
///
/// `MainViewController()` is the Kotlin entry point in `:shared`; Kotlin/Native exports a file's
/// top-level functions on a class named after that file, hence `MainViewControllerKt`. Everything
/// the user sees - the theme, the navigation stack, every screen - is drawn by Compose
/// Multiplatform through Gezgin, the same code Android runs.
struct ContentView: UIViewControllerRepresentable {

    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
