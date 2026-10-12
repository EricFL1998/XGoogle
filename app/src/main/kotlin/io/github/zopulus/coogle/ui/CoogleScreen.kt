// Material component conventions follow tiann/KernelSU (GPL-3.0); see NOTICE.txt.
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package io.github.zopulus.coogle.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.HighlightOff
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.DashboardCustomize
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.CenterFocusStrong
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import io.github.zopulus.coogle.BuildConfig
import io.github.zopulus.coogle.FeaturePolicy

@Composable
fun CoogleScreen(
    state: CoogleState,
    onSetting: (String, Boolean) -> Unit,
    onAssistant: () -> Unit,
    onIconVisibility: (Boolean) -> Unit,
    onStatus: () -> Unit,
    onAbout: () -> Unit,
    onTelegram: () -> Unit,
    onLanguage: (AppLanguage) -> Unit,
    onDialogConfirm: () -> Unit,
    onDialogDismiss: () -> Unit
) {
    val t = textsFor(state.language)
    val bar = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    Scaffold(
        modifier = Modifier.nestedScroll(bar.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("XGoogle") },
                subtitle = { Text(t.subtitle) },
                actions = { LanguageMenu(state.language, t.language, onLanguage) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer),
                windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
                scrollBehavior = bar
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(12.dp))
            // Same Surface, ListItem and StatusTag geometry as KernelSU HomeMaterial.
            val statusColor = if (state.connected || state.loading) MaterialTheme.colorScheme.secondaryContainer
                else MaterialTheme.colorScheme.errorContainer
            val statusContentColor = MaterialTheme.colorScheme.contentColorFor(statusColor)
            Surface(modifier = Modifier.fillMaxWidth(), color = statusColor,
                contentColor = statusContentColor, shape = MaterialTheme.shapes.large,
                onClick = onStatus) {
                ListItem(
                    leadingContent = {
                        Icon(when {
                            state.connected -> Icons.Rounded.CheckCircle
                            state.loading -> Icons.Rounded.HourglassTop
                            else -> Icons.Rounded.HighlightOff
                        }, contentDescription = null)
                    },
                    trailingContent = {
                        Box(Modifier.padding(end = 4.dp).background(
                            MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))) {
                            Text("API 102", Modifier.padding(vertical = 2.dp, horizontal = 4.dp),
                                style = MaterialTheme.typography.labelSmallEmphasized,
                                color = MaterialTheme.colorScheme.onPrimary)
                        }
                    },
                    overlineContent = null,
                    supportingContent = {
                        Text(t.version(BuildConfig.VERSION_NAME), style = MaterialTheme.typography.bodyMedium)
                    },
                    verticalAlignment = Alignment.CenterVertically,
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent,
                        contentColor = statusContentColor, leadingContentColor = statusContentColor,
                        trailingContentColor = statusContentColor,
                        supportingContentColor = statusContentColor.copy(alpha = 0.7f)),
                    elevation = ListItemDefaults.elevation(),
                    content = {
                        Text(when {
                            state.connected -> t.active
                            state.loading -> t.connecting
                            else -> t.inactive
                        }, style = MaterialTheme.typography.titleMediumEmphasized)
                    }
                )
            }
            // While connecting, show the cached switches as usable; taps wait for the connection.
            val available = state.connected || state.loading
            val assistant = when (val info = state.assistant) {
                AssistantInfo.Loading -> t.assistantReading
                AssistantInfo.None -> t.assistantNone
                AssistantInfo.Hidden -> t.assistantTap
                is AssistantInfo.App -> t.assistantCurrent(info.label)
            }
            Group(t.groupAssistant) {
                SettingRow(0, 4, Icons.Rounded.RecordVoiceOver, t.defaultAssistant, assistant, onClick = onAssistant)
                ToggleRow(t, 1, 4, Icons.Rounded.Mic, t.heyGoogle, t.companion(state.companion),
                    checked = state.features[FeaturePolicy.HEY_GOOGLE] == true, enabled = available && !state.busy,
                    onChange = { onSetting(FeaturePolicy.HEY_GOOGLE, it) })
                ToggleRow(t, 2, 4, Icons.Rounded.CenterFocusStrong, t.lens, t.lensSummary,
                    state.features[FeaturePolicy.LENS] == true, available, { onSetting(FeaturePolicy.LENS, it) })
                ToggleRow(t, 3, 4, Icons.Rounded.Translate, t.liveTranslate, t.liveTranslateSummary,
                    state.features[FeaturePolicy.LIVE_TRANSLATE] == true, available, { onSetting(FeaturePolicy.LIVE_TRANSLATE, it) })
            }
            Group(t.groupSystem) {
                ToggleRow(t, 0, 4, Icons.Rounded.TouchApp, t.circle, t.circleSummary,
                    state.features[FeaturePolicy.NAVIGATION] == true, available, { onSetting(FeaturePolicy.NAVIGATION, it) })
                ToggleRow(t, 1, 4, Icons.Rounded.AccountBalanceWallet, t.wallet, t.walletSummary,
                    state.features[FeaturePolicy.WALLET] == true, available, { onSetting(FeaturePolicy.WALLET, it) })
                ToggleRow(t, 2, 4, Icons.Rounded.DashboardCustomize, t.lockShortcuts, t.lockShortcutsSummary,
                    state.features[FeaturePolicy.QUICK_ENTRIES] == true, available, { onSetting(FeaturePolicy.QUICK_ENTRIES, it) })
                ToggleRow(t, 3, 4, Icons.Rounded.LockOpen, t.gms, t.gmsSummary,
                    state.features[FeaturePolicy.GMS] == true, available, { onSetting(FeaturePolicy.GMS, it) })
            }
            Group(t.groupOther) {
                ToggleRow(t, 0, 2, Icons.Rounded.VisibilityOff, t.hideIcon, t.hideIconSummary,
                    state.iconHidden, true, onIconVisibility)
                SettingRow(1, 2, Icons.Rounded.Info, t.about, t.aboutSummary(BuildConfig.VERSION_NAME),
                    onClick = onTelegram)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
    CoogleDialog(state, t, onDialogConfirm, onDialogDismiss)
}

@Composable
private fun LanguageMenu(current: AppLanguage, label: String, onLanguage: (AppLanguage) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { open = true }) { Icon(Icons.Rounded.Language, contentDescription = label) }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            AppLanguage.entries.forEach { language ->
                DropdownMenuItem(
                    text = { Text(language.label) },
                    trailingIcon = if (language == current) { { Icon(Icons.Rounded.Check, null) } } else null,
                    onClick = { open = false; onLanguage(language) }
                )
            }
        }
    }
}

@Composable
private fun Group(title: String, content: @Composable () -> Unit) {
    Column(Modifier.padding(top = 24.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp), content = { content() })
    }
}

@Composable
private fun SettingRow(index: Int, count: Int, icon: ImageVector, title: String, summary: String,
    onClick: () -> Unit, enabled: Boolean = true, modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null) {
    SegmentedListItem(
        modifier = modifier.fillMaxWidth(),
        shapes = ListItemDefaults.segmentedShapes(index, count).let {
            if (count == 1) it.copy(shape = MaterialTheme.shapes.large) else it
        },
        colors = ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceBright,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceBright,
            supportingContentColor = MaterialTheme.colorScheme.onSurfaceVariant),
        enabled = enabled,
        onClick = onClick,
        verticalAlignment = Alignment.CenterVertically,
        leadingContent = { Icon(icon, null, Modifier.size(24.dp)) },
        trailingContent = trailing ?: { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null) },
        supportingContent = { Text(summary) },
        content = { Text(title) }
    )
}

@Composable
private fun ToggleRow(t: Texts, index: Int, count: Int, icon: ImageVector, title: String, summary: String,
    checked: Boolean, enabled: Boolean, onChange: (Boolean) -> Unit) {
    SettingRow(index, count, icon, title, summary, { onChange(!checked) }, enabled,
        Modifier.semantics { role = Role.Switch; stateDescription = if (checked) t.switchOn else t.switchOff }) {
        Switch(
            checked = checked,
            onCheckedChange = null,
            enabled = enabled,
            thumbContent = if (checked || enabled) {
                { Icon(if (checked) Icons.Rounded.Check else Icons.Rounded.Close, null, Modifier.size(SwitchDefaults.IconSize)) }
            } else null,
            colors = SwitchDefaults.colors(
                checkedIconColor = MaterialTheme.colorScheme.primary,
                uncheckedIconColor = MaterialTheme.colorScheme.surfaceContainerHighest)
        )
    }
}

@Composable
private fun CoogleDialog(state: CoogleState, t: Texts, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    if (state.dialog == DialogKind.NONE) return
    val title = when (state.dialog) {
        DialogKind.STATUS -> t.statusTitle
        DialogKind.INSTALL -> t.installTitle
        DialogKind.DISABLED -> t.disabledTitle
        DialogKind.INSTALLED -> t.installedTitle
        DialogKind.HIDE_ICON -> t.hideIconTitle
        else -> t.errorTitle
    }
    val message = when (state.dialog) {
        DialogKind.STATUS -> t.statusMessage
        DialogKind.INSTALL -> t.installMessage
        DialogKind.DISABLED -> t.disabledMessage
        DialogKind.INSTALLED -> t.installedMessage
        DialogKind.HIDE_ICON -> t.hideIconMessage
        else -> state.dialogText
    }
    val confirm = when (state.dialog) {
        DialogKind.INSTALL -> t.install
        DialogKind.HIDE_ICON -> t.hide
        else -> t.ok
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.extraLarge,
        icon = { Icon(if (state.dialog in setOf(DialogKind.INSTALL, DialogKind.INSTALLED, DialogKind.DISABLED))
            Icons.Rounded.Extension else if (state.dialog == DialogKind.HIDE_ICON) Icons.Rounded.VisibilityOff else Icons.Rounded.Info, null) },
        title = { Text(title) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(message)
            }
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirm) } },
        dismissButton = if (state.dialog in setOf(DialogKind.INSTALL, DialogKind.HIDE_ICON)) {
            { TextButton(onClick = onDismiss) { Text(t.cancel) } }
        } else null
    )
}
