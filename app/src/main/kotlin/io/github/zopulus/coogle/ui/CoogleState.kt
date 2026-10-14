package io.github.zopulus.coogle.ui

import io.github.zopulus.coogle.FeaturePolicy

enum class CompanionState(val installed: Boolean = false) {
    UNKNOWN,
    CHECKING,
    MISSING,
    DISABLED,
    READY(true),
    PENDING(true),
    INSTALLING
}

enum class DialogKind { NONE, STATUS, INSTALL, DISABLED, INSTALLED, HIDE_ICON, RESTART, ERROR }

/** What the system reports as the default assistant; the screen words it per language. */
sealed interface AssistantInfo {
    data object Loading : AssistantInfo
    data object None : AssistantInfo
    data object Hidden : AssistantInfo
    data class App(val label: String) : AssistantInfo
}

/**
 * How the settings screen looks. [hyper] picks the Miuix (HyperOS-style) screen; the other
 * options only apply to it. [theme]: 0 follows the system, 1 light, 2 dark.
 */
data class Look(val hyper: Boolean = false, val theme: Int = 0, val monet: Boolean = false, val blur: Boolean = false)

data class CoogleState(
    val connected: Boolean = false,
    val features: Map<String, Boolean> = featureKeys.associateWith(FeaturePolicy::defaultValue),
    val busy: Boolean = false,
    /** Root restart of the scope apps is running. */
    val restarting: Boolean = false,
    val assistant: AssistantInfo = AssistantInfo.Loading,
    val companion: CompanionState = CompanionState.UNKNOWN,
    val iconHidden: Boolean = false,
    val dialog: DialogKind = DialogKind.NONE,
    val dialogText: String = "",
    val language: AppLanguage = AppLanguage.TR,
    val look: Look = Look(),
    /** True until the LSPosed service answers, so cached values are shown instead of defaults. */
    val loading: Boolean = true,
) {
    companion object {
        val featureKeys = listOf(FeaturePolicy.HEY_GOOGLE, FeaturePolicy.GMS, FeaturePolicy.GOOGLE_ENTRY,
            FeaturePolicy.NAVIGATION, FeaturePolicy.LENS,
            FeaturePolicy.WALLET, FeaturePolicy.QUICK_ENTRIES, FeaturePolicy.LIVE_TRANSLATE,
            FeaturePolicy.CIRCLE_VIBRATE, FeaturePolicy.CAMERA_LENS)
    }
}
