package com.movtery.zalithlauncher.game.account

import org.junit.Assert.assertEquals
import org.junit.Test

class ElyByUserTypeTest {
    @Test
    fun elyByAccountsUseMojangUserType() {
        val account = Account(otherBaseUrl = "https://authserver.ely.by/api/authlib-injector")

        assertEquals("mojang", account.minecraftUserType())
    }

    @Test
    fun elyBySubdomainsAreRecognizedCaseInsensitively() {
        val account = Account(otherBaseUrl = "https://AUTHSERVER.ELY.BY/api/authlib-injector")

        assertEquals("mojang", account.minecraftUserType())
    }

    @Test
    fun otherAuthProvidersKeepExistingUserType() {
        val account = Account(otherBaseUrl = "https://auth.example.org/api/authlib-injector")

        assertEquals("msa", account.minecraftUserType())
    }

    @Test
    fun lookalikeDomainsAreNotTreatedAsElyBy() {
        val account = Account(otherBaseUrl = "https://notely.by/api/authlib-injector")

        assertEquals("msa", account.minecraftUserType())
    }
}
