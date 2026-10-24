package com.example.telephonebook

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SecActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sec)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val edName = findViewById<EditText>(R.id.edName)
        val edPhone = findViewById<EditText>(R.id.edPhone)
        val edEmail = findViewById<EditText>(R.id.edEmail)
        val btnSend = findViewById<Button>(R.id.btnSend)

        btnSend.setOnClickListener {
            val email = edEmail.text.toString().trim()

            when {
                edName.text.isEmpty() -> showToast("請輸入姓名")
                edPhone.text.isEmpty() -> showToast("請輸入電話")
                email.isEmpty() -> showToast("請輸入 Email")
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> showToast("請輸入有效的 Email 格式")
                else -> {
                    val b = Bundle()
                    b.putString("name", edName.text.toString())
                    b.putString("phone", edPhone.text.toString())
                    b.putString("email", email)

                    setResult(RESULT_OK, Intent().putExtras(b))
                    finish()
                }
            }
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}