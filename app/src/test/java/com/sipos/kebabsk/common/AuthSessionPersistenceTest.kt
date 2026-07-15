package com.sipos.kebabsk.common

import com.sipos.kebabsk.feature.auth.domain.model.AuthBranch
import com.sipos.kebabsk.feature.auth.domain.model.AuthSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthSessionPersistenceTest {
    @Test
    fun persistedRecordRoundTripKeepsBranchContext() {
        val original = AuthSession(
            token = "fixture-token",
            displayName = "Kasir Fixture",
            username = "kasir_fixture",
            email = "kasir.fixture@example.test",
            role = "kasir",
            branch = AuthBranch(7L, "Cabang Fixture", "FIX")
        )

        val restored = AuthSessionPersistence.fromValues(
            AuthSessionPersistence.toValues(original)
        )

        assertEquals(original, restored)
    }

    @Test
    fun persistedRecordAllowsNullableBranchWithoutLeavingPartialContext() {
        val original = AuthSession(
            token = "fixture-token",
            displayName = "Developer Fixture",
            username = "developer_fixture",
            email = "developer.fixture@example.test",
            role = "developer",
            branch = null
        )

        val values = AuthSessionPersistence.toValues(original).toMutableMap().apply {
            put(AuthSessionPersistence.BRANCH_NAME, "stale-name")
        }

        assertNull(AuthSessionPersistence.fromValues(values)?.branch)
    }
}
