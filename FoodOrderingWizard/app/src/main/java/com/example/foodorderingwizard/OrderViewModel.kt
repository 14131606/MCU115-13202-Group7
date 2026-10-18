package com.example.foodorderingwizard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class OrderViewModel : ViewModel() {
    private val _order = MutableLiveData(Order())
    val order: LiveData<Order> = _order

    private val _finalOrderSummary = MutableLiveData<String>()
    val finalOrderSummary: LiveData<String> = _finalOrderSummary

    fun setMainMeal(meal: String) {
        val current = _order.value ?: Order()
        current.mainMeal = meal
        _order.value = current
    }

    fun toggleSideDish(dish: String, isChecked: Boolean) {
        val current = _order.value ?: Order()
        if (isChecked) {
            current.sideDishes.add(dish)
        } else {
            current.sideDishes.remove(dish)
        }
        _order.value = current
    }

    fun setDrink(drink: String) {
        val current = _order.value ?: Order()
        current.drink = drink
        _order.value = current
    }

    fun submitOrder() {
        val current = _order.value ?: return
        val summary = "Order Summary:\n" +
                "Main: ${current.mainMeal}\n" +
                "Sides: ${current.sideDishes.joinToString(", ")}\n" +
                "Drink: ${current.drink}"
        _finalOrderSummary.value = summary
    }
}