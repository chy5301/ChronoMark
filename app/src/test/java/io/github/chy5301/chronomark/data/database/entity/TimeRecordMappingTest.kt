package io.github.chy5301.chronomark.data.database.entity

import io.github.chy5301.chronomark.data.model.TimeRecord
import org.junit.Assert.assertEquals
import org.junit.Test

class TimeRecordMappingTest {

    private val record = TimeRecord(
        id = "r1",
        wallClockTime = 1_705_280_400_000L,
        elapsedTimeNanos = 12_345L,
        splitTimeNanos = 678L,
        note = "备注"
    )

    @Test
    fun `toEntity - copies every field and attaches session id`() {
        val entity = record.toEntity(sessionId = "s1")

        assertEquals(
            TimeRecordEntity(
                id = "r1",
                sessionId = "s1",
                wallClockTime = 1_705_280_400_000L,
                elapsedTimeNanos = 12_345L,
                splitTimeNanos = 678L,
                note = "备注"
            ),
            entity
        )
    }

    @Test
    fun `toTimeRecord - round trip preserves every field`() {
        assertEquals(record, record.toEntity(sessionId = "s1").toTimeRecord())
    }
}
