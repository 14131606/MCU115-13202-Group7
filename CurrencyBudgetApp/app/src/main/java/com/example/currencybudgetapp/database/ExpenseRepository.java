package com.example.currencybudgetapp.database;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExpenseRepository {
    private final ExpenseDao expenseDao;
    private final LiveData<List<Expense>> allExpenses;
    private final ExecutorService executorService;

    public ExpenseRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        expenseDao = database.expenseDao();
        allExpenses = expenseDao.getAllExpenses();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void insert(Expense expense) {
        executorService.execute(() -> expenseDao.insert(expense));
    }

    public void update(Expense expense) {
        executorService.execute(() -> expenseDao.update(expense));
    }

    public void delete(Expense expense) {
        executorService.execute(() -> expenseDao.delete(expense));
    }

    public LiveData<List<Expense>> getAllExpenses() {
        return allExpenses;
    }
}