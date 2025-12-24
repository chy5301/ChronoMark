package io.github.chy5301.chronomark.data.database

import androidx.room.TypeConverter
import io.github.chy5301.chronomark.data.model.SessionType

/**
 * Room 数据库类型转换器
 * 用于在数据库存储和内存对象之间转换自定义类型
 */
class Converters {

    /**
     * 将 SessionType 枚举转换为字符串存储到数据库
     *
     * @param value SessionType 枚举值
     * @return 字符串表示（"EVENT" 或 "STOPWATCH"）
     */
    @TypeConverter
    fun fromSessionType(value: SessionType): String {
        return value.name  // EVENT → "EVENT", STOPWATCH → "STOPWATCH"
    }

    /**
     * 将数据库中的字符串转换为 SessionType 枚举
     *
     * @param value 字符串表示（"EVENT" 或 "STOPWATCH"）
     * @return SessionType 枚举值
     */
    @TypeConverter
    fun toSessionType(value: String): SessionType {
        return SessionType.valueOf(value)  // "EVENT" → EVENT, "STOPWATCH" → STOPWATCH
    }
}
