package io.github.chy5301.chronomark.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.github.chy5301.chronomark.data.database.repository.HistoryRepository

/**
 * 秒表模式历史记录 ViewModel 工厂类
 *
 * 用于创建 StopwatchHistoryViewModel 实例并注入依赖。
 */
class StopwatchHistoryViewModelFactory(
    private val historyRepository: HistoryRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StopwatchHistoryViewModel::class.java)) {
            return StopwatchHistoryViewModel(historyRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
