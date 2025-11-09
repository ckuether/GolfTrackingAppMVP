package org.example.arccosmvp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shared.data.model.Course
import com.example.shared.data.model.Player
import com.example.shared.usecase.GetAllScoreCards
import com.example.shared.usecase.LoadGolfCourse
import com.example.shared.usecase.LoadCurrentUser
import com.example.shared.platform.Logger
import com.example.core_ui.utils.UiEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppViewModel(
    private val loadGolfCourse: LoadGolfCourse,
    private val loadCurrentUser: LoadCurrentUser,
    private val getAllScoreCards: GetAllScoreCards,
    private val logger: Logger
) : ViewModel() {
    
    companion object {
        private const val TAG = "AppViewModel"
    }

    private var _uiEvent = MutableStateFlow<UiEvent?>(null)
    val uiEvent: StateFlow<UiEvent?> = _uiEvent.asStateFlow()

    fun updateUiEvent(event: UiEvent){
        _uiEvent.value = event
    }
    
    fun clearUiEvent() {
        _uiEvent.value = null
    }
    
    private val _course = MutableStateFlow<Course?>(null)
    val course: StateFlow<Course?> = _course.asStateFlow()

    private val _currentPlayer = MutableStateFlow<Player?>(null)
    val currentPlayer: StateFlow<Player?> = _currentPlayer.asStateFlow()

    val allScoreCards = getAllScoreCards()

    init {
        this@AppViewModel.loadGolfCourse()
        this@AppViewModel.loadCurrentUser()
    }
    
    private fun loadGolfCourse() {
        viewModelScope.launch {
            loadGolfCourse().fold(
                onSuccess = { course ->
                    _course.value = course
                    logger.info(TAG, "Golf course loaded: ${course?.name}")
                },
                onFailure = { error ->
                    logger.error(TAG, "Failed to load golf course", error)
                }
            )
        }
    }
    
    private fun loadCurrentUser() {
        viewModelScope.launch {
            loadCurrentUser().fold(
                onSuccess = { player ->
                    _currentPlayer.value = player
                    logger.info(TAG, "Current player loaded: ${player.name} (ID: ${player.id})")
                },
                onFailure = { error ->
                    logger.error(TAG, "Failed to load current user", error)
                    _currentPlayer.value = Player(name = "Player")
                }
            )
        }
    }
}