package com.sipos.kebabsk.feature.auth.data.mapper

import com.sipos.kebabsk.testutil.ContractFixtureLoader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthSessionMapperTest {
    @Test
    fun loginFixtureMapsTokenUserRoleAndBranch() {
        val body = ContractFixtureLoader.jsonObject("login_success.json")

        val session = AuthSessionMapper.fromResponse(
            body = body,
            token = "fixture_token_not_real",
            fallbackIdentifier = "fallback"
        )

        assertEquals("fixture_token_not_real", session.token)
        assertEquals("Kasir Fixture", session.displayName)
        assertEquals("kasir_fixture", session.username)
        assertEquals("kasir.fixture@example.test", session.email)
        assertEquals("kasir", session.role)
        assertEquals(7L, session.branch?.id)
        assertEquals("Cabang Fixture", session.branch?.name)
        assertEquals("FIX", session.branch?.code)
    }

    @Test
    fun profileFixtureAllowsNullableBranchAndUnknownFields() {
        val body = ContractFixtureLoader.jsonObject("profile_without_branch.json")

        val session = AuthSessionMapper.fromResponse(body, "token", "fallback")

        assertEquals("developer", session.role)
        assertNull(session.branch)
    }
}
