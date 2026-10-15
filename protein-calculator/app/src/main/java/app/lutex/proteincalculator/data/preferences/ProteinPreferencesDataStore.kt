package app.lutex.proteincalculator.data.preferences

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

val Context.proteinPreferencesDataStore by preferencesDataStore(name = "protein_preferences")
