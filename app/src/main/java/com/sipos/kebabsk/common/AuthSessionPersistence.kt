package com.sipos.kebabsk.common

import com.sipos.kebabsk.feature.auth.domain.model.AuthBranch
import com.sipos.kebabsk.feature.auth.domain.model.AuthSession

object AuthSessionPersistence {
    const val TOKEN = "token"
    const val DISPLAY_NAME = "display_name"
    const val USERNAME = "username"
    const val EMAIL = "email"
    const val ROLE = "role"
    const val BRANCH_ID = "branch_id"
    const val BRANCH_NAME = "branch_name"
    const val BRANCH_CODE = "branch_code"

    val keys = setOf(TOKEN, DISPLAY_NAME, USERNAME, EMAIL, ROLE, BRANCH_ID, BRANCH_NAME, BRANCH_CODE)

    fun toValues(session: AuthSession): Map<String, String?> = mapOf(
        TOKEN to session.token,
        DISPLAY_NAME to session.displayName,
        USERNAME to session.username,
        EMAIL to session.email,
        ROLE to session.role,
        BRANCH_ID to session.branch?.id?.toString(),
        BRANCH_NAME to session.branch?.name,
        BRANCH_CODE to session.branch?.code
    )

    fun fromValues(values: Map<String, String?>): AuthSession? {
        val token = values[TOKEN]?.takeIf { it.isNotBlank() } ?: return null
        val branchId = values[BRANCH_ID]?.toLongOrNull()
        val branchName = values[BRANCH_NAME].orEmpty()
        val branchCode = values[BRANCH_CODE].orEmpty()
        val branch = if (branchId != null && branchId > 0L && branchName.isNotBlank() && branchCode.isNotBlank()) {
            AuthBranch(branchId, branchName, branchCode)
        } else {
            null
        }

        return AuthSession(
            token = token,
            displayName = values[DISPLAY_NAME].orEmpty(),
            username = values[USERNAME].orEmpty(),
            email = values[EMAIL].orEmpty(),
            role = values[ROLE],
            branch = branch
        )
    }
}
