package com.example.foodorderingwizard

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val viewModel: OrderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnChooseMainMeal = findViewById<Button>(R.id.btnChooseMainMeal)
        val btnChooseSideDishes = findViewById<Button>(R.id.btnChooseSideDishes)
        val btnChooseDrink = findViewById<Button>(R.id.btnChooseDrink)
        val btnOrder = findViewById<Button>(R.id.btnOrder)
        val tvCurrentSelection = findViewById<TextView>(R.id.tvCurrentSelection)

        // 觀察點餐狀態，即時更新主畫面的 Current Selection 內容
        viewModel.order.observe(this) { order ->
            val main = order.mainMeal ?: "--"
            val sides = if (order.sideDishes.isNotEmpty()) order.sideDishes.joinToString(", ") else "--"
            val drink = order.drink ?: "--"

            tvCurrentSelection.text = "Current Selection:\nMain: $main\nSides: $sides\nDrink: $drink"
        }

        // 1. 選擇主餐按鈕
        btnChooseMainMeal.setOnClickListener {
            showMainMealDialog()
        }

        // 2. 選擇副餐按鈕 (多選)
        btnChooseSideDishes.setOnClickListener {
            showSideDishesDialog()
        }

        // 3. 選擇飲料按鈕
        btnChooseDrink.setOnClickListener {
            showDrinkDialog()
        }

        // 4. 按下 Order / Confirm 按鈕邏輯
        btnOrder.setOnClickListener {
            val order = viewModel.order.value

            // 檢查主餐、副餐、飲料是否皆有選擇
            if (order?.isComplete() == true) {
                // 全部都有選 -> 跳出 AlertDialog 顯示所選項目
                val summaryMessage = "Main: ${order.mainMeal}\n" +
                        "Sides: ${order.sideDishes.joinToString(", ")}\n" +
                        "Drink: ${order.drink}\n\nSubmit this order?"

                AlertDialog.Builder(this)
                    .setTitle("Submit Order")
                    .setMessage(summaryMessage)
                    .setPositiveButton("Submit") { dialog, _ ->
                        // 點擊 Submit 後完成提交並顯示成功 Toast
                        Toast.makeText(this, "Order Submitted Successfully!", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            } else {
                // 有任何一項未選擇 -> 跳出提示 Toast
                Toast.makeText(
                    this,
                    "Please select main meal, side dishes, and drink first",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // 選擇主餐對話框
    private fun showMainMealDialog() {
        val meals = arrayOf("Burger", "Fried Chicken", "Fish Sandwich")
        AlertDialog.Builder(this)
            .setTitle("Choose Main Meal")
            .setItems(meals) { _, which ->
                viewModel.setMainMeal(meals[which])
            }
            .show()
    }

    // 選擇副餐對話框 (多選)
    private fun showSideDishesDialog() {
        val sides = arrayOf("French Fries", "Salad", "Corn Cup")
        val currentSides = viewModel.order.value?.sideDishes ?: mutableListOf()
        val checkedItems = BooleanArray(sides.size) { index -> currentSides.contains(sides[index]) }

        AlertDialog.Builder(this)
            .setTitle("Choose Side Dishes")
            .setMultiChoiceItems(sides, checkedItems) { _, which, isChecked ->
                val selected = sides[which]
                viewModel.toggleSideDish(selected, isChecked)
            }
            .setPositiveButton("OK", null)
            .show()
    }

    // 選擇飲料對話框
    private fun showDrinkDialog() {
        val drinks = arrayOf("Coke", "Green Tea", "Black Tea")
        AlertDialog.Builder(this)
            .setTitle("Choose Drink")
            .setItems(drinks) { _, which ->
                viewModel.setDrink(drinks[which])
            }
            .show()
    }

    // 保留相容性方法
    fun navigateToNextPage() {}
    fun navigateToFirstPage() {}
}