package dev.andrewohara.getit.api.security

import dev.andrewohara.getit.UserId

fun interface Authorizer {
    operator fun invoke(token: String): UserId?

    companion object
}

fun Authorizer.Companion.google() = Authorizer {
    TODO("unimplemented")
}
