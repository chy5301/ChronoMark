package io.github.chy5301.chronomark.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 2,
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
         * 数据库迁移：版本 1 -> 2
         * 移除 time_records 表中的 index 列（序号改为动态计算）
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // SQLite 不支持直接删除列，需要重建表
                database.execSQL(
                    """
                    CREATE TABLE time_records_new (
                        id TEXT NOT NULL PRIMARY KEY,
                        session_id TEXT NOT NULL,
                        wall_clock_time INTEGER NOT NULL,
                        elapsed_time_nanos INTEGER NOT NULL,
                        split_time_nanos INTEGER NOT NULL,
                        note TEXT NOT NULL,
                        FOREIGN KEY (session_id) REFERENCES history_sessions(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    INSERT INTO time_records_new (id, session_id, wall_clock_time, elapsed_time_nanos, split_time_nanos, note)
                    SELECT id, session_id, wall_clock_time, elapsed_time_nanos, split_time_nanos, note
                    FROM time_records
                    """.trimIndent()
                )
                database.execSQL("DROP TABLE time_records")
                database.execSQL("ALTER TABLE time_records_new RENAME TO time_records")
                database.execSQL("CREATE INDEX index_time_records_session_id ON time_records(session_id)")
            }
        }

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
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
