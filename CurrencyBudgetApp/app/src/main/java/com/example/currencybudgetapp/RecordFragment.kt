package com.example.currencybudgetapp

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.currencybudgetapp.database.Expense
import com.example.currencybudgetapp.databinding.FragmentRecordBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class RecordFragment : Fragment() {

    private var _binding: FragmentRecordBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ExpenseViewModel by viewModels()
    private lateinit var adapter: ExpenseAdapter

    private val categories = listOf("餐飲", "交通", "購物", "住宿", "娛樂", "其他")
    private val currencies = listOf("JPY", "USD", "TWD", "EUR", "KRW", "CNY", "HKD")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()

        binding.fabAddExpense.setOnClickListener {
            UsageTracker(requireContext()).logEvent("按下了新增記帳按鈕")
            showExpenseDialog(null)
        }
    }

    private fun setupRecyclerView() {
        adapter = ExpenseAdapter(
            onEditClick = { expense ->
                UsageTracker(requireContext()).logEvent("編輯記帳: id=${expense.id}")
                showExpenseDialog(expense)
            },
            onDeleteClick = { expense ->
                confirmDelete(expense)
            }
        )
        binding.rvExpenses.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@RecordFragment.adapter
        }
    }

    private fun observeViewModel() {
        viewModel.allExpenses.observe(viewLifecycleOwner) { expenses ->
            adapter.submitList(expenses)
            if (expenses.isNullOrEmpty()) {
                binding.llEmptyState.visibility = View.VISIBLE
                binding.rvExpenses.visibility = View.GONE
            } else {
                binding.llEmptyState.visibility = View.GONE
                binding.rvExpenses.visibility = View.VISIBLE
            }
        }
    }

    private fun showExpenseDialog(expense: Expense?) {
        val context = requireContext()
        val builder = AlertDialog.Builder(context)
        builder.setTitle(if (expense == null) "新增記帳" else "編輯記帳")

        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_expense, null, false)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerCategory)
        val spinnerCurrency = dialogView.findViewById<Spinner>(R.id.spinnerCurrency)
        val etAmount = dialogView.findViewById<EditText>(R.id.etAmount)
        val etDate = dialogView.findViewById<EditText>(R.id.etDate)
        val etNote = dialogView.findViewById<EditText>(R.id.etNote)

        // Setup Spinners
        val categoryAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, categories)
        spinnerCategory.adapter = categoryAdapter

        val currencyAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, currencies)
        spinnerCurrency.adapter = currencyAdapter

        // Default / Prefill date
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        etDate.setText(expense?.date ?: todayStr)

        // DatePicker dialog on click
        etDate.setOnClickListener {
            val cal = Calendar.getInstance()
            val datePicker = DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val selectedCal = Calendar.getInstance().apply {
                        set(year, month, dayOfMonth)
                    }
                    etDate.setText(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedCal.time))
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        // Prefill existing data if editing
        if (expense != null) {
            val catIdx = categories.indexOf(expense.category)
            if (catIdx >= 0) spinnerCategory.setSelection(catIdx)

            val curIdx = currencies.indexOf(expense.currency)
            if (curIdx >= 0) spinnerCurrency.setSelection(curIdx)

            etAmount.setText(expense.amount.toString())
            etNote.setText(expense.note ?: "")
        }

        builder.setView(dialogView)
        builder.setPositiveButton("儲存") { dialog, _ ->
            val amountText = etAmount.text.toString().trim()
            val amount = amountText.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                Toast.makeText(context, "請輸入有效的金額", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            val category = spinnerCategory.selectedItem.toString()
            val currency = spinnerCurrency.selectedItem.toString()
            val date = etDate.text.toString().trim().ifEmpty { todayStr }
            val note = etNote.text.toString().trim()

            if (expense == null) {
                val newExpense = Expense(amount, currency, category, date, note)
                viewModel.insert(newExpense)
                Toast.makeText(context, "已成功新增記帳", Toast.LENGTH_SHORT).show()
                UsageTracker(context).logEvent("新增記帳: $amount $currency ($category)")
            } else {
                val updatedExpense = Expense(expense.id, amount, currency, category, date, note)
                viewModel.update(updatedExpense)
                Toast.makeText(context, "已更新記帳資料", Toast.LENGTH_SHORT).show()
                UsageTracker(context).logEvent("更新記帳: id=${expense.id}")
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("取消") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun confirmDelete(expense: Expense) {
        AlertDialog.Builder(requireContext())
            .setTitle("刪除確認")
            .setMessage("確定要刪除這筆支出嗎？ (${expense.amount} ${expense.currency})")
            .setPositiveButton("刪除") { dialog, _ ->
                viewModel.delete(expense)
                Toast.makeText(requireContext(), "已刪除該筆記帳", Toast.LENGTH_SHORT).show()
                UsageTracker(requireContext()).logEvent("刪除記帳: id=${expense.id}")
                dialog.dismiss()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
