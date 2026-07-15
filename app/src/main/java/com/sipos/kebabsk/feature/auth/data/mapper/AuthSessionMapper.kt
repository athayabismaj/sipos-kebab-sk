package com.sipos.kebabsk.feature.auth.data.mapper

import com.google.gson.JsonObject
import com.sipos.kebabsk.common.firstString
import com.sipos.kebabsk.feature.auth.domain.model.AuthBranch
import com.sipos.kebabsk.feature.auth.domain.model.AuthSession

object AuthSessionMapper {
    fun fromResponse(body: JsonObject?, token: String, fallbackIdentifier: String): AuthSession {
        val userJson = extractUserJson(body)
        val displayName = userJson?.firstString("name") ?: body?.firstString("name") ?: fallbackIdentifier
        val username = userJson?.firstString("username") ?: fallbackIdentifier
        val email = userJson?.firstString("email") ?: ""
        val role = userJson?.firstString("role")

        return AuthSession(
            token = token,
            displayName = displayName,
            username = username,
            email = email,
            role = role,
            branch = parseBranch(userJson)
        )
    }

    private fun parseBranch(userJson: JsonObject?): AuthBranch? {
        val branch = userJson?.getAsJsonObjectOrNull("branch") ?: return null
        val id = branch.get("id")?.takeUnless { it.isJsonNull }
            ?.let { runCatching { it.asLong }.getOrNull() }
            ?: return null
        val name = branch.firstString("name")?.trim().orEmpty()
        val code = branch.firstString("code")?.trim().orEmpty()

        if (id <= 0L || name.isBlank() || code.isBlank()) return null

        return AuthBranch(id = id, name = name, code = code)
    }

    private fun extractUserJson(body: JsonObject?): JsonObject? {
        if (body == null) return null
        body.getAsJsonObjectOrNull("user")?.let { return it }

        val data = body.getAsJsonObjectOrNull("data") ?: return null
        return data.getAsJsonObjectOrNull("user") ?: data
    }

    private fun JsonObject.getAsJsonObjectOrNull(key: String): JsonObject? {
        val value = get(key) ?: return null
        return if (value.isJsonObject) value.asJsonObject else null
    }
}
