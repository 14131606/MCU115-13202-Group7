package com.example.telephonebook

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var myAdapter: MyAdapter
    private val contacts = ArrayList<Contact>()
    private lateinit var etSearch: EditText

    private val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            val intent = result.data
            val name = intent?.getStringExtra("name") ?: ""
            val phone = intent?.getStringExtra("phone") ?: ""
            val email = intent?.getStringExtra("email") ?: ""

            if (name.isNotEmpty() && phone.isNotEmpty()) {
                contacts.add(0, Contact(name, phone, email))
                if (::myAdapter.isInitialized) {
                    myAdapter.filter(etSearch.text.toString())
                }
            }
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

        initDummyData()

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val btnAdd = findViewById<Button>(R.id.btnAdd)
        etSearch = findViewById(R.id.etSearch)

        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = linearLayoutManager

        myAdapter = MyAdapter(contacts)
        recyclerView.adapter = myAdapter

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                myAdapter.filter(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnAdd.setOnClickListener {
            val i = Intent(this, SecActivity::class.java)
            startForResult.launch(i)
        }
    }

    private fun initDummyData() {
        contacts.add(Contact("Johnny", "0988312345", "johnnyhong@gmail.com"))
        contacts.add(Contact("Jessica", "0981911934", "jessicahuang@gmail.com"))
        for (i in 3..30) {
            contacts.add(
                Contact(
                    name = "User $i",
                    phone = "09${"%08d".format(i)}",
                    email = "user$i@gmail.com"
                )
            )
        }
    }
}