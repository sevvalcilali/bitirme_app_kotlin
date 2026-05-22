package com.example.bitirmeapp.util

import android.content.Context

// Oturumu SharedPreferences'taki "aktif_email" ile tutar. Boşsa oturum yok.
object OturumYoneticisi {
    private const val PREFS = "oturum_prefs"
    private const val KEY_AKTIF_EMAIL = "aktif_email"
    private const val KEY_PASIF_BILGI_GOSTERILDI = "pasif_bilgi_gosterildi"

    fun aktifEmail(context: Context): String? {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_AKTIF_EMAIL, null)
            ?.takeIf { it.isNotBlank() }
    }

    fun girisYapildi(context: Context): Boolean = aktifEmail(context) != null

    fun girisYap(context: Context, email: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_AKTIF_EMAIL, email)
            .apply()
    }

    fun cikis(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_AKTIF_EMAIL)
            .apply()
    }

    fun pasifBilgiGosterildiMi(context: Context): Boolean {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_PASIF_BILGI_GOSTERILDI, false)
    }

    fun pasifBilgiyiIsaretle(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_PASIF_BILGI_GOSTERILDI, true)
            .apply()
    }
}
