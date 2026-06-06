package com.example.katzen.Helper

import org.junit.Assert.assertEquals
import org.junit.Test

class RtdbSnapshotMapperTest {

    @Test
    fun firstNonBlank_returnsFirstNonEmpty() {
        assertEquals("web", RtdbSnapshotMapper.firstNonBlank(null, "", "web", "legacy"))
        assertEquals("legacy", RtdbSnapshotMapper.firstNonBlank(null, "legacy"))
        assertEquals("", RtdbSnapshotMapper.firstNonBlank(null, "", null))
    }
}
