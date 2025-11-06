package com.example.core_ui.utils

sealed class UiEvent {
    data class Navigate(val route: String) : UiEvent()
    data class NavigateAndClearBackStack(val route: String) : UiEvent()
    object NavigateUp : UiEvent()
    data class ShowSnackbar(val uiText: UiText) : UiEvent()
    data class ShowErrorSnackbar(val uiText: UiText) : UiEvent()
    data class IsLoading(val isLoading: Boolean) : UiEvent()
}