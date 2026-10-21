package io.github.zopulus.coogle

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import io.github.zopulus.coogle.ui.AppLanguage
import io.github.zopulus.coogle.ui.textsFor
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * The notification fix's app list: the picked apps are always woken by their pushes and keep
 * their full-screen call screens. Saved as a package set in XGoogle's LSPosed settings.
 */
class AppPickerActivity : ComponentActivity() {
    private class App(val pkg: String, val label: String, val icon: ImageBitmap?)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val ui = getSharedPreferences("xgoogle_ui", MODE_PRIVATE)
        val t = textsFor(ui.getString("language", null)?.let(AppLanguage::of) ?: AppLanguage.forSystem(Locale.getDefault().language))
        val settings = SettingsConnection.preferences()
        if (settings == null) {
            Toast.makeText(this, t.errSave, Toast.LENGTH_LONG).show()
            finish()
            return
        }
        val saved = runCatching { settings.getStringSet(FeaturePolicy.NOTIFY_APPS, null) }.getOrNull().orEmpty()
        setContent {
            MaterialTheme(colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()) {
                Surface(Modifier.fillMaxSize()) {
                    val picked = remember { mutableStateListOf<String>().apply { addAll(saved) } }
                    var apps by remember { mutableStateOf<List<App>?>(null) }
                    var query by remember { mutableStateOf("") }
                    LaunchedEffect(Unit) { apps = withContext(Dispatchers.IO) { launchableApps() } }
                    Column(Modifier.safeDrawingPadding().padding(16.dp)) {
                        Text(t.notifyApps, style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.size(12.dp))
                        OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), singleLine = true,
                            placeholder = { Text(t.pickerSearch) })
                        Spacer(Modifier.size(8.dp))
                        Box(Modifier.weight(1f).fillMaxWidth()) {
                            val list = apps
                            if (list == null) CircularProgressIndicator(Modifier.align(Alignment.Center))
                            else LazyColumn {
                                // Picked apps first, then by name, so the choice is easy to review.
                                val shown = list.filter { query.isBlank() || it.label.contains(query, true) || it.pkg.contains(query, true) }
                                    .sortedWith(compareBy<App>({ it.pkg !in picked }, { it.label.lowercase() }))
                                items(shown, key = { it.pkg }) { app ->
                                    AppRow(app, app.pkg in picked) { on -> if (on) picked.add(app.pkg) else picked.remove(app.pkg) }
                                }
                            }
                        }
                        Spacer(Modifier.size(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton({
                                val installed = apps?.map { it.pkg }.orEmpty().toSet()
                                RECOMMENDED.filter { it in installed && it !in picked }.forEach(picked::add)
                            }, Modifier.weight(1f)) { Text(t.pickerRecommended, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                            Button({
                                val ok = runCatching { settings.edit().putStringSet(FeaturePolicy.NOTIFY_APPS, picked.toSet()).commit() }
                                    .getOrDefault(false)
                                // The system reads the list on every push, so no restart is needed.
                                if (ok) finish() else Toast.makeText(this@AppPickerActivity, t.errSave, Toast.LENGTH_LONG).show()
                            }, Modifier.weight(1f)) { Text(t.pickerSave) }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun AppRow(app: App, checked: Boolean, onChange: (Boolean) -> Unit) {
        Row(Modifier.fillMaxWidth().clickable { onChange(!checked) }.padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically) {
            app.icon?.let { Image(it, null, Modifier.size(40.dp)) } ?: Spacer(Modifier.size(40.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(app.label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(app.pkg, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Checkbox(checked, onChange)
        }
    }

    /** Apps with a launcher icon, without XGoogle itself. */
    private fun launchableApps(): List<App> {
        val pm = packageManager
        val launcher = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(launcher, PackageManager.ResolveInfoFlags.of(0))
            .map { it.activityInfo.applicationInfo }
            .distinctBy { it.packageName }
            .filter { it.packageName != packageName }
            .map { info ->
                val icon = runCatching { pm.getApplicationIcon(info).toBitmap(96, 96).asImageBitmap() }.getOrNull()
                App(info.packageName, pm.getApplicationLabel(info).toString(), icon)
            }
    }

    private companion object {
        /** Messaging, calls and mail: the apps whose late notifications hurt most. */
        val RECOMMENDED = listOf(
            "org.telegram.messenger", "org.telegram.messenger.web", "org.thunderdog.challegram",
            "com.whatsapp", "com.whatsapp.w4b", "com.instagram.android", "com.facebook.orca",
            "org.thoughtcrime.securesms", "com.discord", "com.Slack", "com.viber.voip",
            "jp.naver.line.android", "com.tencent.mm", "com.microsoft.teams",
            "com.google.android.gm", "com.microsoft.office.outlook", "com.google.android.apps.messaging",
        )
    }
}
