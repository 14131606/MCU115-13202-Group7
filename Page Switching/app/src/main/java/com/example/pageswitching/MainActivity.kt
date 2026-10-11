package com.example.pageswitching

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var editSendMsg: EditText
    private lateinit var textView2: TextView
    private lateinit var btnGoToSecond: Button

    private val pageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val backData = result.data?.getStringExtra("EXTRA_REPLY")
            textView2.text = backData ?: "無回傳內容"
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        editSendMsg = findViewById(R.id.editSendMsg)
        textView2 = findViewById(R.id.textView2)
        btnGoToSecond = findViewById(R.id.btnGoToSecond)

        btnGoToSecond.setOnClickListener {
            val inputText = editSendMsg.text.toString()

            val intent = Intent(this, SecActivity::class.java).apply {
                putExtra("EXTRA_MESSAGE", inputText)
            }
            pageLauncher.launch(intent)
        }
    }
}