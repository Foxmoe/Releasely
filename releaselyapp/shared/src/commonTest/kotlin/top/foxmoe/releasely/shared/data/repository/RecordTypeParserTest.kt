package top.foxmoe.releasely.shared.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import top.foxmoe.releasely.shared.domain.model.RecordType

class RecordTypeParserTest {

    @Test
    fun parseLegacyRelaxAsSexSolo() {
        assertEquals(RecordType.SEX_SOLO, safeParseRecordType("RELAX"))
    }

    @Test
    fun parseUnknownAsSafeFallback() {
        assertEquals(RecordType.SEX_PARTNER, safeParseRecordType("UNKNOWN_LEGACY_TYPE"))
    }
}

