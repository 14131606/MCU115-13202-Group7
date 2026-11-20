package com.example.currencybudgetapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = AuthManager(this)

        // 已登入就直接進主畫面
        if (auth.isLoggedIn()) {
            goMain()
            return
        }
        setContentView(R.layout.activity_login)

        val etUser = findViewById<EditText>(R.id.etUsername)
        val etPass = findViewById<EditText>(R.id.etPassword)

        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            val u = etUser.text.toString().trim()
            val p = etPass.text.toString()
            if (auth.login(u, p)) goMain()
            else Toast.makeText(this, "帳號或密碼錯誤", Toast.LENGTH_SHORT).show()
        }
        findViewById<Button>(R.id.btnToRegister).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun goMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}