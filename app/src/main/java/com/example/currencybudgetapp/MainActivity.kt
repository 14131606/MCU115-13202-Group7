package com.example.currencybudgetapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav: BottomNavigationView = findViewById(R.id.bottom_nav)

        // 預設打開記帳頁
        loadFragment(RecordFragment())

        bottomNav.setOnItemSelectedListener {
            when(it.itemId){
                R.id.nav_exchange -> loadFragment(ExchangeFragment())
                R.id.nav_record -> loadFragment(RecordFragment())
                R.id.nav_budget -> loadFragment(BudgetFragment())
            }
            true
        }
    }

    // 切換Fragment函式
    private fun loadFragment(fragment: Fragment){
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
