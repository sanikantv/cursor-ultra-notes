package app.lutex.proteincalculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.lutex.proteincalculator.domain.model.UserPreferences
import app.lutex.proteincalculator.domain.usecase.ObserveUserPreferencesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class AppViewModel @Inject constructor(
    observePreferences: ObserveUserPreferencesUseCase,
) : ViewModel() {
    val preferences: StateFlow<UserPreferences?> = observePreferences()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
