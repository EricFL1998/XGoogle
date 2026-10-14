// Material component conventions follow tiann/KernelSU (GPL-3.0); the HyperOS style uses
// compose-miuix-ui/miuix (Apache-2.0); see NOTICE.txt.
package io.github.zopulus.coogle.ui

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowInsetsControllerCompat
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController

@Composable
fun CoogleTheme(hyperStyle: Boolean, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val dark = isSystemInDarkTheme()
    LaunchedEffect(dark) {
        val activity = context as? Activity ?: return@LaunchedEffect
        WindowInsetsControllerCompat(activity.window, activity.window.decorView).apply {
            isAppearanceLightStatusBars = !dark
            isAppearanceLightNavigationBars = !dark
        }
    }
    if (hyperStyle) {
        // The stock HyperOS palette, light or dark with the system.
        val controller = remember { ThemeController(ColorSchemeMode.System) }
        MiuixTheme(controller = controller, content = content)
    } else {
        MaterialExpressiveTheme(
            colorScheme = if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context),
            motionScheme = MotionScheme.expressive(),
            content = content
        )
    }
}
