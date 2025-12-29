package io.github.chy5301.chronomark.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.github.chy5301.chronomark.data.database.repository.HistoryRepository

/**
 * 事件模式历史记录 ViewModel 工厂类
 *
 * 用于创建 EventHistoryViewModel 实例并注入依赖。
 */
class EventHistoryViewModelFactory(
    private val historyRepository: HistoryRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EventHistoryViewModel::class.java)) {
            return EventHistoryViewModel(historyRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
