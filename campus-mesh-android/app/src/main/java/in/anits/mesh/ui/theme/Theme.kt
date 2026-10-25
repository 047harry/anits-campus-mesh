package `in`.anits.mesh.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

val DisasterColorScheme = darkColorScheme(
    primary = EmergencyRed,
    onPrimary = TextPrimary,
    secondary = AuthorityGold,
    onSecondary = TextPrimary,
    tertiary = MeshGreen,
    background = OledBlack,
    surface = SurfaceDark,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun CampusMeshTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DisasterColorScheme,
        content = content
    )
}
