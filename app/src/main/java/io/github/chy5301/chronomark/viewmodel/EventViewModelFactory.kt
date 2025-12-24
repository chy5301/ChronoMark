package io.github.chy5301.chronomark.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.github.chy5301.chronomark.data.DataStoreManager
import io.github.chy5301.chronomark.data.database.repository.HistoryRepository

/**
 * EventViewModel 工厂类
 */
class EventViewModelFactory(
    private val dataStoreManager: DataStoreManager,
    private val historyRepository: HistoryRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EventViewModel::class.java)) {
            return EventViewModel(dataStoreManager, historyRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
