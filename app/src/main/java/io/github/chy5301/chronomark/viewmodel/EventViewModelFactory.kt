package io.github.chy5301.chronomark.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.github.chy5301.chronomark.data.DataStoreManager

/**
 * EventViewModel 工厂类
 */
class EventViewModelFactory(
    private val dataStoreManager: DataStoreManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EventViewModel::class.java)) {
            return EventViewModel(dataStoreManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
