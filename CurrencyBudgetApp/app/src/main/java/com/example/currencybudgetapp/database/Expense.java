package com.example.currencybudgetapp.database;

import androidx.room.Entity;
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

    public Expense(double amount, String currency, String category, String date, String note) {
        this.amount = amount;
        this.currency = currency;
        this.category = category;
        this.date = date;
        this.note = note;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public double getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getCategory() { return category; }
    public String getDate() { return date; }
    public String getNote() { return note; }
}