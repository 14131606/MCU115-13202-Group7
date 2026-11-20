package com.example.currencybudgetapp

import android.content.Context
import java.security.MessageDigest

class AuthManager(context: Context) {
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    private fun hash(text: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(text.toByteArray())
            .joinToString("") { "%02x".format(it) }

    fun register(username: String, password: String): Boolean {
        if (prefs.contains("user_$username")) return false // 帳號已存在
        prefs.edit().putString("user_$username", hash(password)).apply()
        return true
    }

    fun login(username: String, password: String): Boolean {
        val saved = prefs.getString("user_$username", null) ?: return false
        if (saved != hash(password)) return false
        prefs.edit().putBoolean("logged_in", true).putString("current_user", username).apply()
        return true
    }

    fun isLoggedIn() = prefs.getBoolean("logged_in", false)
    fun currentUser(): String = prefs.getString("current_user", "") ?: ""
    fun logout() = prefs.edit().putBoolean("logged_in", false).apply()
}