package io.github.chy5301.chronomark.data.model

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * 时间记录数据模型
 *
 * @property id 唯一标识符
 * @property index 序号（从1开始）
 * @property wallClockTime 标记时的系统时间戳（毫秒）
 * @property elapsedTimeNanos 累计经过时间（纳秒）
 * @property splitTimeNanos 与上次标记的时间差（纳秒）
 * @property note 备注
 */
@Serializable
data class TimeRecord(
    val id: String = UUID.randomUUID().toString(),
    val index: Int,
    val wallClockTime: Long,
    val elapsedTimeNanos: Long,
    val splitTimeNanos: Long,
    val note: String = ""
)
