package com.example.tokenmaker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tokenmaker.data.preferences.ThemeMode
import com.example.tokenmaker.data.preferences.UserPreferences
import com.example.tokenmaker.data.preferences.UserPreferencesRepository
import com.example.tokenmaker.ui.navigation.TokenMakerNavGraph
import com.example.tokenmaker.ui.theme.TokenMakerTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val preferencesRepository = UserPreferencesRepository(applicationContext)

        setContent {
            val userPreferences by preferencesRepository.userPreferencesFlow
                .collectAsStateWithLifecycle(initialValue = UserPreferences())

            TokenMakerTheme(themeMode = userPreferences.themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TokenMakerNavGraph(preferencesRepository = preferencesRepository)
                }
            }
        }
    }
}
