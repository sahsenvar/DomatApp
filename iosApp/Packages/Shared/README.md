# `Shared` Swift package

A thin SwiftPM wrapper around the Kotlin Multiplatform `Shared.xcframework`, so the Xcode project
depends on a package rather than on a hardcoded framework path.

## Before you build

`Shared.xcframework` is generated, gitignored, and must exist before SwiftPM can resolve this
package:

```bash
./gradlew :shared:syncDebugSharedXCFramework
```

Re-run it after any change to Kotlin code. Apple targets only link on macOS.

## Why a local `path:` binary target

The Kotlin documentation's SPM export recipe uses `.binaryTarget(url:checksum:)`, which expects the
XCFramework to be published as a zip somewhere and the checksum refreshed on every release. That fits
a separate iOS repository consuming a published KMP library. Here the Xcode project and the Gradle
build live in one repository, so a local path is both simpler and always in sync.

Automatic `Package.swift` generation (`assembleSharedXCFramework` writing this file for you) needs
Kotlin 2.4.20-RC3 or newer. This project is pinned to Kotlin 2.4.10 by SKIE 0.10.14, so the manifest
above is maintained by hand - it is nine lines of it, and it only changes if the product name or the
deployment target does.
