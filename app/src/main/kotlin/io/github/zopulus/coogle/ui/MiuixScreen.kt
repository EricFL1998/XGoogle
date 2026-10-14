// HyperOS-style components from compose-miuix-ui/miuix (Apache-2.0); see NOTICE.txt.
package io.github.zopulus.coogle.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.CenterFocusStrong
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DashboardCustomize
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.HighlightOff
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.BlurOn
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import io.github.zopulus.coogle.BuildConfig
import io.github.zopulus.coogle.FeaturePolicy
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.DropdownImpl
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurColors
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.window.WindowDialog
import top.yukonga.miuix.kmp.window.WindowListPopup

@Composable
fun MiuixScreen(
    state: CoogleState,
    onSetting: (String, Boolean) -> Unit,
    onAssistant: () -> Unit,
    onIconVisibility: (Boolean) -> Unit,
    onStatus: () -> Unit,
    onAbout: () -> Unit,
    onTelegram: () -> Unit,
    onLanguage: (AppLanguage) -> Unit,
    onLook: (Look) -> Unit,
    onDialogConfirm: () -> Unit,
    onDialogDismiss: () -> Unit,
    onRestartApps: () -> Unit
) {
    val t = textsFor(state.language)
    val bar = MiuixScrollBehavior()
    // Frosted bar: the list is recorded as a backdrop and blurred behind the top bar.
    val blur = state.look.blur
    val backdrop = rememberLayerBackdrop()
    val surface = MiuixTheme.colorScheme.surface
    Scaffold(
        topBar = {
            TopAppBar(
                title = "XGoogle",
                subtitle = t.subtitle,
                modifier = if (blur) Modifier.textureBlur(backdrop, RectangleShape, 25f,
                    colors = BlurColors(blendColors = listOf(BlendColorEntry(surface.copy(alpha = 0.8f))))) else Modifier,
                color = if (blur) Color.Transparent else surface,
                navigationIcon = { ThemeButton(state.look, t, onLook) },
                actions = { LanguageMenu(state.language, t.language, onLanguage) },
                scrollBehavior = bar
            )
        }
    ) { padding ->
        // While connecting, show the cached switches as usable; taps wait for the connection.
        val available = state.connected || state.loading
        LazyColumn(
            Modifier.fillMaxHeight()
                .then(if (blur) Modifier.layerBackdrop(backdrop).background(surface) else Modifier)
                .overScrollVertical().nestedScroll(bar.nestedScrollConnection),
            contentPadding = PaddingValues(top = padding.calculateTopPadding()),
            overscrollEffect = null
        ) {
            item {
                StatusCard(state, t, onStatus)
                val assistant = when (val info = state.assistant) {
                    AssistantInfo.Loading -> t.assistantReading
                    AssistantInfo.None -> t.assistantNone
                    AssistantInfo.Hidden -> t.assistantTap
                    is AssistantInfo.App -> t.assistantCurrent(info.label)
                }
                Group(t.groupAssistant) {
                    ArrowPreference(title = t.defaultAssistant, summary = assistant,
                        startAction = { RowIcon(Icons.Rounded.RecordVoiceOver) }, onClick = onAssistant)
                    Toggle(Icons.Rounded.Mic, t.heyGoogle, t.companion(state.companion),
                        state.features[FeaturePolicy.HEY_GOOGLE] == true, available && !state.busy,
                    ) { onSetting(FeaturePolicy.HEY_GOOGLE, it) }
                }
                Group(t.groupSearch) {
                    // Circle to Search's own options fold under it, so the list stays short.
                    var circleOpen by rememberSaveable { mutableStateOf(false) }
                    val circleOn = state.features[FeaturePolicy.NAVIGATION] == true
                    val turn by animateFloatAsState(if (circleOpen) 180f else 0f, label = "circleOptions")
                    BasicComponent(
                        modifier = Modifier.semantics { stateDescription = if (circleOn) t.switchOn else t.switchOff },
                        title = t.circle, summary = t.circleSummary,
                        startAction = { RowIcon(Icons.Rounded.TouchApp) },
                        endActions = {
                            Icon(Icons.Rounded.ExpandMore, t.circleOptions,
                                Modifier.padding(end = 8.dp).rotate(turn),
                                tint = MiuixTheme.colorScheme.onSurfaceVariantActions)
                            Switch(circleOn, { onSetting(FeaturePolicy.NAVIGATION, it) }, enabled = available)
                        },
                        onClick = { circleOpen = !circleOpen }
                    )
                    AnimatedVisibility(circleOpen,
                        enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                        exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()) {
                        Column {
                            Toggle(Icons.Rounded.Translate, t.liveTranslate, t.liveTranslateSummary,
                                state.features[FeaturePolicy.LIVE_TRANSLATE] == true, available
                            ) { onSetting(FeaturePolicy.LIVE_TRANSLATE, it) }
                            Toggle(Icons.Rounded.Vibration, t.circleVibrate, t.circleVibrateSummary,
                                state.features[FeaturePolicy.CIRCLE_VIBRATE] == true, available
                            ) { onSetting(FeaturePolicy.CIRCLE_VIBRATE, it) }
                        }
                    }
                    Toggle(Icons.Rounded.CenterFocusStrong, t.lens, t.lensSummary,
                        state.features[FeaturePolicy.LENS] == true, available) { onSetting(FeaturePolicy.LENS, it) }
                    Toggle(Icons.Rounded.PhotoCamera, t.cameraLens, t.cameraLensSummary,
                        state.features[FeaturePolicy.CAMERA_LENS] == true, available) { onSetting(FeaturePolicy.CAMERA_LENS, it) }
                }
                Group(t.groupLock) {
                    Toggle(Icons.Rounded.DashboardCustomize, t.lockShortcuts, t.lockShortcutsSummary,
                        state.features[FeaturePolicy.QUICK_ENTRIES] == true, available) { onSetting(FeaturePolicy.QUICK_ENTRIES, it) }
                    Toggle(Icons.Rounded.AccountBalanceWallet, t.wallet, t.walletSummary,
                        state.features[FeaturePolicy.WALLET] == true, available) { onSetting(FeaturePolicy.WALLET, it) }
                }
                Group(t.groupSystem) {
                    Toggle(Icons.Rounded.LockOpen, t.gms, t.gmsSummary,
                        state.features[FeaturePolicy.GMS] == true, available) { onSetting(FeaturePolicy.GMS, it) }
                    Toggle(Icons.Rounded.Settings, t.googleEntry, t.googleEntrySummary,
                        state.features[FeaturePolicy.GOOGLE_ENTRY] == true, available) { onSetting(FeaturePolicy.GOOGLE_ENTRY, it) }
                }
                Group(t.groupLook) {
                    Toggle(Icons.Rounded.Palette, t.hyperStyle, t.hyperStyleSummary, true, true) {
                        onLook(state.look.copy(hyper = it)) }
                    Toggle(Icons.Rounded.Wallpaper, t.monet, t.monetSummary, state.look.monet, true) {
                        onLook(state.look.copy(monet = it)) }
                    Toggle(Icons.Rounded.BlurOn, t.blur, t.blurSummary, state.look.blur, true) {
                        onLook(state.look.copy(blur = it)) }
                }
                Group(t.groupOther) {
                    ArrowPreference(title = t.restartApps, summary = t.restartAppsSummary,
                        startAction = { RowIcon(Icons.Rounded.RestartAlt) }, onClick = onRestartApps,
                        enabled = state.connected && !state.restarting)
                    Toggle(Icons.Rounded.VisibilityOff, t.hideIcon, t.hideIconSummary,
                        state.iconHidden, true, onIconVisibility)
                    ArrowPreference(title = t.about, summary = t.aboutSummary(BuildConfig.VERSION_NAME),
                        startAction = { RowIcon(Icons.Rounded.Info) }, onClick = onTelegram)
                }
                Spacer(Modifier.height(24.dp))
                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            }
        }
    }
    CoogleDialog(state, t, onDialogConfirm, onDialogDismiss)
}

@Composable
private fun StatusCard(state: CoogleState, t: Texts, onStatus: () -> Unit) {
    val colors = MiuixTheme.colorScheme
    val tint = if (state.connected || state.loading) colors.primary else colors.error
    Card(Modifier.padding(horizontal = 12.dp).padding(top = 12.dp), onClick = onStatus, showIndication = true) {
        BasicComponent(
            title = when {
                state.connected -> t.active
                state.loading -> t.connecting
                else -> t.inactive
            },
            summary = t.version(BuildConfig.VERSION_NAME) + " · API 102",
            startAction = {
                Icon(when {
                    state.connected -> Icons.Rounded.CheckCircle
                    state.loading -> Icons.Rounded.HourglassTop
                    else -> Icons.Rounded.HighlightOff
                }, null, Modifier.padding(end = 16.dp).size(28.dp), tint = tint)
            }
        )
    }
}

/** Top left: each tap moves the theme on, system → light → dark. */
@Composable
private fun ThemeButton(look: Look, t: Texts, onLook: (Look) -> Unit) {
    val (icon, label) = when (look.theme) {
        1 -> Icons.Rounded.LightMode to t.themeLight
        2 -> Icons.Rounded.DarkMode to t.themeDark
        else -> Icons.Rounded.BrightnessAuto to t.themeSystem
    }
    IconButton(onClick = { onLook(look.copy(theme = (look.theme + 1) % 3)) }, modifier = Modifier.padding(start = 12.dp)) {
        Icon(icon, contentDescription = "${t.theme}: $label", tint = MiuixTheme.colorScheme.onSurface)
    }
}

@Composable
private fun LanguageMenu(current: AppLanguage, label: String, onLanguage: (AppLanguage) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { open = true }, modifier = Modifier.padding(end = 12.dp)) {
            Icon(Icons.Rounded.Language, contentDescription = label, tint = MiuixTheme.colorScheme.onSurface)
        }
        WindowListPopup(show = open, onDismissRequest = { open = false },
            alignment = top.yukonga.miuix.kmp.basic.PopupPositionProvider.Align.End) {
            ListPopupColumn {
                val languages = AppLanguage.entries
                languages.forEachIndexed { index, language ->
                    DropdownImpl(text = language.label, optionSize = languages.size,
                        isSelected = language == current, index = index,
                        onSelectedIndexChange = { open = false; onLanguage(languages[it]) })
                }
            }
        }
    }
}

@Composable
private fun Group(title: String, content: @Composable () -> Unit) {
    SmallTitle(title, Modifier.padding(top = 6.dp))
    Card(Modifier.padding(horizontal = 12.dp).padding(bottom = 6.dp)) { content() }
}

@Composable
private fun RowIcon(icon: ImageVector) {
    Icon(icon, null, Modifier.padding(end = 16.dp).size(24.dp), tint = MiuixTheme.colorScheme.onSurfaceVariantSummary)
}

@Composable
private fun Toggle(icon: ImageVector, title: String, summary: String, checked: Boolean, enabled: Boolean,
    onChange: (Boolean) -> Unit) {
    SwitchPreference(
        checked = checked, onCheckedChange = onChange, title = title, summary = summary,
        startAction = { RowIcon(icon) }, enabled = enabled,
        modifier = Modifier.semantics { role = Role.Switch }
    )
}

@Composable
private fun CoogleDialog(state: CoogleState, t: Texts, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val show = state.dialog != DialogKind.NONE
    // Keep the last dialog's text while it animates out.
    var last by remember { mutableStateOf(state.dialog) }
    var lastText by remember { mutableStateOf(state.dialogText) }
    if (show) { last = state.dialog; lastText = state.dialogText }
    val title = when (last) {
        DialogKind.STATUS -> t.statusTitle
        DialogKind.INSTALL -> t.installTitle
        DialogKind.DISABLED -> t.disabledTitle
        DialogKind.INSTALLED -> t.installedTitle
        DialogKind.HIDE_ICON -> t.hideIconTitle
        DialogKind.RESTART -> t.restartTitle
        else -> t.errorTitle
    }
    val message = when (last) {
        DialogKind.STATUS -> t.statusMessage
        DialogKind.INSTALL -> t.installMessage
        DialogKind.DISABLED -> t.disabledMessage
        DialogKind.INSTALLED -> t.installedMessage
        DialogKind.HIDE_ICON -> t.hideIconMessage
        DialogKind.RESTART -> t.restartMessage
        else -> lastText
    }
    val confirm = when (last) {
        DialogKind.INSTALL -> t.install
        DialogKind.HIDE_ICON -> t.hide
        DialogKind.RESTART -> t.restart
        else -> t.ok
    }
    val cancellable = last in setOf(DialogKind.INSTALL, DialogKind.HIDE_ICON, DialogKind.RESTART)
    WindowDialog(show = show, title = title, onDismissRequest = onDismiss) {
        Column {
            Text(message, Modifier.fillMaxWidth().heightIn(max = 420.dp)
                .verticalScroll(rememberScrollState()), color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
            Spacer(Modifier.height(24.dp))
            Row {
                if (cancellable) {
                    TextButton(t.cancel, onDismiss, Modifier.weight(1f))
                    Spacer(Modifier.size(16.dp))
                }
                TextButton(confirm, onConfirm, Modifier.weight(1f), colors = ButtonDefaults.textButtonColorsPrimary())
            }
        }
    }
}
