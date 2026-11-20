package com.example.currencybudgetapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.currencybudgetapp.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val auth = AuthManager(this)

        binding.btnRegister.setOnClickListener {
            val u = binding.etRegUsername.text.toString().trim()
            val p = binding.etRegPassword.text.toString()
            val c = binding.etRegConfirm.text.toString()

            when {
                u.isEmpty() || p.isEmpty() -> toast("帳號和密碼不能空白")
                p != c -> toast("兩次密碼不一致")
                !auth.register(u, p) -> toast("這個帳號已經存在")
                else -> {
                    toast("註冊成功,請登入")
                    finish() // 回到登入頁
                }
            }
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}