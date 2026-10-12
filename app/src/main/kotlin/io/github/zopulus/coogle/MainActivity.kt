package io.github.zopulus.coogle

import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import io.github.zopulus.coogle.ui.AppLanguage
import io.github.zopulus.coogle.ui.AssistantInfo
import io.github.zopulus.coogle.ui.CoogleScreen
import io.github.zopulus.coogle.ui.CoogleState
import io.github.zopulus.coogle.ui.CoogleTheme
import io.github.zopulus.coogle.ui.CompanionState
import io.github.zopulus.coogle.ui.DialogKind
import io.github.zopulus.coogle.ui.textsFor
import io.github.libxposed.service.XposedService
import java.io.IOException
import java.util.Locale
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {
    private val worker = Executors.newSingleThreadExecutor()
    private val savingKeys = mutableSetOf<String>()
    private var preferences: SharedPreferences? = null
    private var companionCheckedThisResume = false
    private var companionCheckedAt: Long? = null
    private var state by mutableStateOf(CoogleState())
    private val texts get() = textsFor(state.language)
    private val uiPreferences by lazy { getSharedPreferences("xgoogle_ui", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // First run follows the system language; afterwards the user's choice wins.
        val saved = uiPreferences.getString("language", null)
        state = state.copy(language = saved?.let(AppLanguage::of) ?: AppLanguage.forSystem(Locale.getDefault().language))
        setContent {
            CoogleTheme {
                CoogleScreen(state, ::changeSetting, ::openAssistant, ::changeIconVisibility,
                    { showDialog(DialogKind.STATUS) }, ::openRepository, ::openTelegram, ::changeLanguage,
                    ::confirmDialog, ::dismissDialog)
            }
        }
        SettingsConnection.connect(this)
    }

    override fun onResume() {
        super.onResume()
        companionCheckedThisResume = false
        state = state.copy(assistant = readAssistant(), iconHidden = iconHidden())
        if (preferences != null && state.features[FeaturePolicy.HEY_GOOGLE] == true) checkCompanion(false)
    }

    override fun onDestroy() {
        SettingsConnection.detach(this)
        worker.shutdown()
        super.onDestroy()
    }

    fun onFrameworkChanged(service: XposedService?) {
        if (isDestroyed) return
        preferences = runCatching { service?.getRemotePreferences(FeatureConfig.GROUP) }.getOrNull()
        syncPreferences()
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) &&
            state.features[FeaturePolicy.HEY_GOOGLE] == true) checkCompanion(false)
    }

    private fun readAssistant(): AssistantInfo = when (val label = AssistantSelection.label(this)) {
        null -> AssistantInfo.Hidden
        "" -> AssistantInfo.None
        else -> AssistantInfo.App(label)
    }

    private fun changeLanguage(language: AppLanguage) {
        uiPreferences.edit().putString("language", language.code).apply()
        state = state.copy(language = language)
    }

    private fun syncPreferences() {
        val values = CoogleState.featureKeys.associateWith { key ->
            runCatching { preferences?.getBoolean(key, FeaturePolicy.defaultValue(key)) }
                .getOrNull() ?: FeaturePolicy.defaultValue(key)
        }
        state = state.copy(connected = preferences != null, features = values)
    }

    private fun changeSetting(key: String, enabled: Boolean) {
        if (state.busy || preferences == null || key in savingKeys) return
        if (key == FeaturePolicy.HEY_GOOGLE && enabled) checkCompanion(true)
        else saveSetting(key, enabled)
    }

    private fun saveSetting(key: String, enabled: Boolean, companionInstalled: Boolean? = null) {
        val settings = preferences ?: return
        if (!savingKeys.add(key)) return
        val t = texts
        worker.execute {
            val result = runCatching {
                val editor = settings.edit().putBoolean(key, enabled)
                companionInstalled?.let { editor.putBoolean("companion_ready", it) }
                if (!editor.commit()) throw IOException(t.errSave)
                // HyperOS already has a default-wallet action for double-pressing power.
                if (key == FeaturePolicy.WALLET && SystemCompatibility.isHyperOs())
                    runCatching { CompanionInstaller.applyHyperOsWalletPower(enabled) }.onFailure {
                        throw IOException(t.errWalletPower)
                    }
            }
            runOnUiThread {
                if (isDestroyed) return@runOnUiThread
                savingKeys.remove(key)
                result.onSuccess {
                    state = state.copy(features = state.features + (key to enabled))
                }.onFailure { error(it.message ?: t.errSaveGeneric) }
            }
        }
    }

    private fun checkCompanion(enableAfterCheck: Boolean) {
        if (state.busy) return
        val lastCheck = companionCheckedAt
        if (!enableAfterCheck && (companionCheckedThisResume ||
            (lastCheck != null && SystemClock.elapsedRealtime() - lastCheck < 30_000L))) return
        companionCheckedThisResume = true
        state = state.copy(busy = true, companion = CompanionState.CHECKING)
        worker.execute {
            val result = runCatching { CompanionState.valueOf(CompanionInstaller.check()) }
            runOnUiThread {
                if (isDestroyed) return@runOnUiThread
                state = state.copy(busy = false)
                result.onSuccess { installed ->
                    companionCheckedAt = SystemClock.elapsedRealtime()
                    state = state.copy(companion = installed)
                    if (enableAfterCheck) when (installed) {
                        CompanionState.READY, CompanionState.PENDING -> saveSetting(FeaturePolicy.HEY_GOOGLE, true, true)
                        CompanionState.DISABLED -> showDialog(DialogKind.DISABLED)
                        else -> showDialog(DialogKind.INSTALL)
                    }
                    else if (!installed.installed && state.features[FeaturePolicy.HEY_GOOGLE] == true)
                        saveSetting(FeaturePolicy.HEY_GOOGLE, false, false)
                }.onFailure {
                    companionCheckedAt = null
                    state = state.copy(companion = CompanionState.UNKNOWN)
                    if (enableAfterCheck) error(texts.errCheckCompanion)
                }
            }
        }
    }

    private fun installCompanion() {
        val settings = preferences ?: return
        companionCheckedAt = null
        state = state.copy(busy = true, companion = CompanionState.INSTALLING, dialog = DialogKind.NONE)
        val t = texts
        worker.execute {
            val result = runCatching {
                CompanionInstaller.install(this)
                if (!settings.edit().putBoolean("companion_ready", true).putBoolean(FeaturePolicy.HEY_GOOGLE, true).commit())
                    throw IOException(t.errCompanionSaved)
            }
            runOnUiThread {
                if (isDestroyed) return@runOnUiThread
                state = state.copy(busy = false)
                syncPreferences()
                result.onSuccess {
                    state = state.copy(companion = CompanionState.PENDING)
                    showDialog(DialogKind.INSTALLED)
                }.onFailure {
                    state = state.copy(companion = CompanionState.UNKNOWN)
                    error(it.message ?: t.errCompanionInstall)
                }
            }
        }
    }

    private fun openAssistant() {
        runCatching { AssistantSelection.open(this) }.onFailure { error(texts.errOpenAssistant) }
    }

    // The alias class is under the code namespace, which differs from the application id.
    private val launcherAlias get() = ComponentName(this, "${MainActivity::class.java.`package`!!.name}.Launcher")

    private fun iconHidden() = packageManager.getComponentEnabledSetting(launcherAlias) ==
        PackageManager.COMPONENT_ENABLED_STATE_DISABLED

    private fun changeIconVisibility(hidden: Boolean) {
        if (hidden) showDialog(DialogKind.HIDE_ICON) else setIconHidden(false)
    }

    private fun setIconHidden(hidden: Boolean) {
        runCatching {
            packageManager.setComponentEnabledSetting(launcherAlias,
                if (hidden) PackageManager.COMPONENT_ENABLED_STATE_DISABLED else PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP)
            state = state.copy(iconHidden = hidden)
        }.onFailure { error(texts.errIcon) }
    }

    private fun confirmDialog() {
        val dialog = state.dialog
        dismissDialog()
        when (dialog) {
            DialogKind.INSTALL -> installCompanion()
            DialogKind.HIDE_ICON -> setIconHidden(true)
            else -> Unit
        }
    }

    private fun showDialog(kind: DialogKind) { state = state.copy(dialog = kind) }
    private fun dismissDialog() { state = state.copy(dialog = DialogKind.NONE) }
    private fun error(message: String) { state = state.copy(dialog = DialogKind.ERROR, dialogText = message) }
    private fun openRepository() {
        runCatching { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/zopulus/coogle"))) }
            .onFailure { error(texts.errRepository) }
    }
    private fun openTelegram() {
        runCatching { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/xioamifetiscisi"))) }
            .onFailure { error(texts.errTelegram) }
    }

}
