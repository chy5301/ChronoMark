package io.github.chy5301.chronomark.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.github.chy5301.chronomark.data.database.dao.HistoryDao
import io.github.chy5301.chronomark.data.database.entity.HistorySessionEntity
import io.github.chy5301.chronomark.data.database.entity.TimeRecordEntity

/**
 * Room 数据库类
 * 应用的主数据库，存储历史会话和时间记录
 *
 * 使用单例模式确保整个应用只有一个数据库实例
 */
@Database(
    entities = [
        HistorySessionEntity::class,
        TimeRecordEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)  // 注册类型转换器
abstract class AppDatabase : RoomDatabase() {

    /**
     * 获取历史记录 DAO
     */
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * 获取数据库单例
         *
         * @param context 应用上下文
         * @return 数据库实例
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "chronomark_database"
                )
                    .fallbackToDestructiveMigration()  // 开发阶段：模式变更时删除旧数据
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
