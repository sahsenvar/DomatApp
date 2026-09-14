plugins {
    alias(libs.plugins.domatapp.kmp.library)
}

// Deliberately empty. Scaffolded ahead of any real event-tracking work so the module boundary
// exists before code does - see README.md for what's expected to land here and why it's a
// separate module rather than living in core:common or core:data.
