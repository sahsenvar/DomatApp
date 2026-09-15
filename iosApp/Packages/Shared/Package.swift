// swift-tools-version:5.9
import PackageDescription

// The Kotlin side of the app, consumed as a Swift package instead of a framework linked by path.
//
// `Shared.xcframework` is NOT checked in - it is produced by Gradle and copied here:
//
//     ./gradlew :shared:syncDebugSharedXCFramework      # day-to-day, simulator + device
//     ./gradlew :shared:syncReleaseSharedXCFramework    # archiving
//
// Run one of those before opening `iosApp.xcodeproj`, and again after changing any Kotlin code.
// SwiftPM resolves a `.binaryTarget`'s `path` relative to this package's directory and refuses
// paths that escape it, which is why the XCFramework is copied here rather than referenced in
// `shared/build/XCFrameworks/`.
//
// A `url:` + `checksum:` binary target - the other shape the Kotlin docs describe - is for handing
// the framework to a separate iOS repository. This is one repository, so a local path avoids
// publishing a zip and re-computing a checksum on every Kotlin change.
let package = Package(
    name: "Shared",
    platforms: [
        .iOS(.v18)
    ],
    products: [
        .library(name: "Shared", targets: ["Shared"])
    ],
    targets: [
        .binaryTarget(name: "Shared", path: "Shared.xcframework")
    ]
)
