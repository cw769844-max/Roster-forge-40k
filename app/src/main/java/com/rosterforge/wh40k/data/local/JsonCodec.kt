package com.rosterforge.wh40k.data.local

import kotlinx.serialization.json.Json

/**
 * Shared JSON codec for serialising complex domain fields into the Room schema's
 * `*Json` columns. Lenient + ignoring unknown keys means we can evolve domain
 * shapes without painful migrations in development.
 */
val AppJson: Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = false
    coerceInputValues = true
}
