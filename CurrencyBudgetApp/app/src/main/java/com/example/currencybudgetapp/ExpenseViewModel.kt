package com.example.currencybudgetapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.currencybudgetapp.database.Expense
import com.example.currencybudgetapp.database.ExpenseRepository

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ExpenseRepository = ExpenseRepository(application)
    val allExpenses: LiveData<List<Expense>> = repository.allExpenses

    fun insert(expense: Expense) {
        repository.insert(expense)
    }

    fun update(expense: Expense) {
        repository.update(expense)
    }

    fun delete(expense: Expense) {
        repository.delete(expense)
    }
}
