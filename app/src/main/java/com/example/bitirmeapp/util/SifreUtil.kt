package com.example.bitirmeapp.util

import java.security.MessageDigest

/** Şifreyi düz metin saklamamak için SHA-256 hex özetine çevirir. */
object SifreUtil {
    fun hashle(sifre: String): String {
        val ozet = MessageDigest.getInstance("SHA-256").digest(sifre.toByteArray())
        return ozet.joinToString("") { "%02x".format(it) }
    }
}
