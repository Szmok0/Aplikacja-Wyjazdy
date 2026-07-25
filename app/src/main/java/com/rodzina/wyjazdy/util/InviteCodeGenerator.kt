package com.rodzina.wyjazdy.util

object InviteCodeGenerator {
    // Bez znaków łatwych do pomylenia (0/O, 1/I).
    private const val ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

    fun generate(length: Int = 6): String =
        (1..length).map { ALPHABET.random() }.joinToString("")
}
