package com.example.currencybudgetapp.database;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "expenses")
public class Expense {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private double amount;       // 金額
    private String currency;     // 幣別 (如 JPY, USD, TWD)
    private String category;     // 分類 (如 餐飲, 交通, 購物, 住宿)
    private String date;         // 日期 (yyyy-MM-dd)
    private String note;         // 備註

    // Constructor for inserting new expenses (without id)
    @Ignore
    public Expense(double amount, String currency, String category, String date, String note) {
        this.amount = amount;
        this.currency = currency;
        this.category = category;
        this.date = date;
        this.note = note;
    }

    // Constructor for Room when querying from database (with id)
    public Expense(int id, double amount, String currency, String category, String date, String note) {
        this.id = id;
        this.amount = amount;
        this.currency = currency;
        this.category = category;
        this.date = date;
        this.note = note;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
