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

enum class DialogKind { NONE, STATUS, INSTALL, DISABLED, INSTALLED, HIDE_ICON, RESTART, UPDATE, ERROR }

enum class UpdateStatus { IDLE, CHECKING, LATEST, AVAILABLE, FAILED }

/** What the system reports as the default assistant; the screen words it per language. */
sealed interface AssistantInfo {
    data object Loading : AssistantInfo
    data object None : AssistantInfo
    data object Hidden : AssistantInfo
    data class App(val label: String) : AssistantInfo
}

data class CoogleState(
    val connected: Boolean = false,
    val features: Map<String, Boolean> = featureKeys.associateWith(FeaturePolicy::defaultValue),
    val busy: Boolean = false,
    /** Root restart of the scope apps is running. */
    val restarting: Boolean = false,
    /** Apps the switched-on features need that are not ticked in LSPosed (labels; "system" for the framework). */
    val missingScopes: List<String> = emptyList(),
    val update: UpdateStatus = UpdateStatus.IDLE,
    val updateVersion: String = "",
    val assistant: AssistantInfo = AssistantInfo.Loading,
    val companion: CompanionState = CompanionState.UNKNOWN,
    val iconHidden: Boolean = false,
    val dialog: DialogKind = DialogKind.NONE,
    val dialogText: String = "",
    val language: AppLanguage = AppLanguage.TR,
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
