package com.example.currencybudgetapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.currencybudgetapp.api.CurrencyHelper
import com.example.currencybudgetapp.database.Expense
import com.example.currencybudgetapp.databinding.ItemExpenseBinding
import java.text.NumberFormat
import java.util.Locale

class ExpenseAdapter(
    private val onEditClick: (Expense) -> Unit,
    private val onDeleteClick: (Expense) -> Unit
) : ListAdapter<Expense, ExpenseAdapter.ExpenseViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemExpenseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ExpenseViewHolder(private val binding: ItemExpenseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(expense: Expense) {
            val context = binding.root.context

            binding.tvCategory.text = expense.category
            binding.tvCategoryIcon.text = getCategoryEmoji(expense.category)
            binding.tvNote.text = if (expense.note.isNullOrBlank()) "無備註" else expense.note
            binding.tvDate.text = expense.date

            val formatter = NumberFormat.getNumberInstance(Locale.getDefault())
            val originalText = "${formatter.format(expense.amount)} ${expense.currency}"
            binding.tvOriginalAmount.text = originalText

            // Convert currency to TWD using CurrencyHelper
            val twdVal = CurrencyHelper.convertCurrency(
                context,
                expense.amount,
                expense.currency,
                "TWD"
            )
            binding.tvTwdAmount.text = "≈ NT$ ${formatter.format(twdVal.toInt())}"

            binding.btnEdit.setOnClickListener { onEditClick(expense) }
            binding.btnDelete.setOnClickListener { onDeleteClick(expense) }
        }

        private fun getCategoryEmoji(category: String?): String {
            return when (category) {
                "餐飲" -> "🍽️"
                "交通" -> "🚗"
                "購物" -> "🛍️"
                "住宿" -> "🏠"
                "娛樂" -> "🎮"
                else -> "📝"
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Expense>() {
        override fun areItemsTheSame(oldItem: Expense, newItem: Expense): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Expense, newItem: Expense): Boolean {
            return oldItem.amount == newItem.amount &&
                    oldItem.currency == newItem.currency &&
                    oldItem.category == newItem.category &&
                    oldItem.date == newItem.date &&
                    oldItem.note == newItem.note
        }
    }
}
