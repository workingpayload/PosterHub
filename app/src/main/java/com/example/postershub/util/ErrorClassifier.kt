package com.example.postershub.util

import retrofit2.HttpException
import java.io.IOException

enum class ErrorKind { NETWORK, AUTH, SERVER, UNKNOWN }

data class ClassifiedError(val kind: ErrorKind, val message: String)

/** Turns a raw network exception into a kind (network/auth/server) + a user-facing message. */
fun Throwable.classify(): ClassifiedError = when (this) {
    is HttpException -> when (code()) {
        401, 403 -> ClassifiedError(ErrorKind.AUTH, "Invalid or missing API key.")
        in 500..599 -> ClassifiedError(ErrorKind.SERVER, "Server error (${code()}). Try again shortly.")
        else -> ClassifiedError(ErrorKind.SERVER, "Request failed (${code()}).")
    }
    is IOException -> ClassifiedError(ErrorKind.NETWORK, "No internet connection.")
    else -> ClassifiedError(ErrorKind.UNKNOWN, message ?: "Something went wrong.")
}
