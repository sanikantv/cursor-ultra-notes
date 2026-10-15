package app.lutex.proteincalculator

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.lutex.proteincalculator.core.designsystem.ProteinTheme
import app.lutex.proteincalculator.core.navigation.ProteinNavHost
import app.lutex.proteincalculator.domain.model.ThemePreference
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val appViewModel: AppViewModel = hiltViewModel()
            val preferences by appViewModel.preferences.collectAsStateWithLifecycle()
            val preference = preferences?.theme ?: ThemePreference.EMERALD_DARK
            val darkIcons = preference == ThemePreference.LIGHT ||
                (preference == ThemePreference.SYSTEM && !androidx.compose.foundation.isSystemInDarkTheme())
            SideEffect {
                enableEdgeToEdge(
                    statusBarStyle = if (darkIcons) {
                        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.dark(Color.TRANSPARENT)
                    },
                    navigationBarStyle = if (darkIcons) {
                        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.dark(Color.TRANSPARENT)
                    },
                )
            }
            ProteinTheme(preference = preference) {
                val loaded = preferences
                if (loaded == null) {
                    Box(Modifier.fillMaxSize())
                } else {
                    ProteinNavHost(onboardingComplete = loaded.onboardingComplete)
                }
            }
        }
    }
}
