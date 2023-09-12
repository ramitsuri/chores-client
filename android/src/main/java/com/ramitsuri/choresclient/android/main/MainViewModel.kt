package com.ramitsuri.choresclient.android.main

import androidx.lifecycle.ViewModelProvider
import com.ramitsuri.choresclient.android.Destinations
import com.ramitsuri.choresclient.data.settings.PrefManager
import com.ramitsuri.choresclient.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class MainViewModel(prefManager: PrefManager) : ViewModel() {
    private val _state: MutableStateFlow<MainViewState>
    val state: StateFlow<MainViewState>

    init {
        val loggedIn = prefManager.isLoggedIn()
        val startDestination = if (loggedIn) {
            Destinations.ASSIGNMENTS
        } else {
            Destinations.LOGIN
        }
        _state = MutableStateFlow(MainViewState(startDestination))
        state = _state
    }

    companion object : KoinComponent {
        @Suppress("UNCHECKED_CAST")
        val factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(prefManager = get<PrefManager>()) as T
            }
        }
    }
}