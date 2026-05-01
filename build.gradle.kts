// Top-level build file. Plugins are declared here with `apply false` so the
// individual modules (just `:app` for now) can opt-in to whichever they need.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
}
