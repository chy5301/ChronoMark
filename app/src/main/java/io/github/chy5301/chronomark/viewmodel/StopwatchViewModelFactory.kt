package io.github.chy5301.chronomark.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.github.chy5301.chronomark.data.DataStoreManager

/**
 * StopwatchViewModel 工厂类
 */
class StopwatchViewModelFactory(
    private val dataStoreManager: DataStoreManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StopwatchViewModel::class.java)) {
            return StopwatchViewModel(dataStoreManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
