package com.example.currencybudgetapp

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.currencybudgetapp.api.CurrencyHelper
import com.example.currencybudgetapp.database.Expense
import com.example.currencybudgetapp.databinding.FragmentHomeBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ExpenseViewModel by viewModels()

    // Animators for warning effects
    private var blinkAnimator: ObjectAnimator? = null
    private var iconPulseAnimator: AnimatorSet? = null

    private companion object {
        private const val PREF_BUDGET_NAME = "budget_pref"
        private const val KEY_MONTHLY_BUDGET = "monthly_budget"
        private const val DEFAULT_BUDGET = 20000.0
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUserHeader()
        setupListeners()
        observeViewModel()
    }

    private fun setupUserHeader() {
        val authManager = AuthManager(requireContext())
        val username = authManager.currentUser().ifEmpty { "使用者" }
        binding.tvWelcomeUser.text = "歡迎回來，$username 👋"

        val sdf = SimpleDateFormat("yyyy 年 M 月 d 日 EEEE", Locale.TAIWAN)
        binding.tvTodayDate.text = sdf.format(Date())
    }

    private fun setupListeners() {
        binding.btnEditBudget.setOnClickListener {
            showEditBudgetDialog()
        }

        binding.tvViewAllRecords.setOnClickListener {
            findNavController().navigate(R.id.recordFragment)
        }
    }

    private fun observeViewModel() {
        viewModel.allExpenses.observe(viewLifecycleOwner) { expenses ->
            updateDashboard(expenses ?: emptyList())
        }
    }

    private fun updateDashboard(expenses: List<Expense>) {
        val context = context ?: return

        // 1. Calculate Total Spend in TWD
        var totalSpendTwd = 0.0
        val categoryMap = mutableMapOf<String, Double>()

        for (expense in expenses) {
            val twdVal = CurrencyHelper.convertCurrency(
                context,
                expense.amount,
                expense.currency,
                "TWD"
            )
            totalSpendTwd += twdVal

            val cat = expense.category ?: "其他"
            categoryMap[cat] = (categoryMap[cat] ?: 0.0) + twdVal
        }

        // 2. Get Monthly Budget
        val monthlyBudget = getSavedBudget()
        val remainingTwd = max(0.0, monthlyBudget - totalSpendTwd)
        val percentUsed = if (monthlyBudget > 0) (totalSpendTwd / monthlyBudget) * 100.0 else 0.0

        // 3. Format numbers
        val formatter = NumberFormat.getNumberInstance(Locale.getDefault())
        binding.tvTotalSpent.text = "NT$ ${formatter.format(totalSpendTwd.toInt())}"
        binding.tvMonthlyBudget.text = "NT$ ${formatter.format(monthlyBudget.toInt())}"

        if (totalSpendTwd > monthlyBudget) {
            val overAmount = (totalSpendTwd - monthlyBudget).toInt()
            binding.tvRemainingBudget.text = "超支：NT$ ${formatter.format(overAmount)}"
            binding.tvRemainingBudget.setTextColor(ContextCompat.getColor(context, R.color.danger_red))
        } else {
            binding.tvRemainingBudget.text = "剩餘預算：NT$ ${formatter.format(remainingTwd.toInt())}"
            binding.tvRemainingBudget.setTextColor(ContextCompat.getColor(context, R.color.success_green))
        }

        binding.tvBudgetPercent.text = String.format(Locale.getDefault(), "使用 %.1f%%", percentUsed)
        binding.progressBudget.progress = percentUsed.toInt().coerceIn(0, 100)

        // 4. Update Warning Card & Warning Animations (警告動畫與超標邏輯)
        updateWarningCardAndAnimation(percentUsed, totalSpendTwd, monthlyBudget)

        // 5. Render Category Breakdown
        renderCategoryBreakdown(categoryMap, totalSpendTwd)

        // 6. Render Recent Records Preview
        renderRecentRecords(expenses.take(3))
    }

    /**
     * 處理預算警告狀態與警告動畫 (Blinking & Pulsing Animations)
     */
    private fun updateWarningCardAndAnimation(
        percentUsed: Double,
        totalSpendTwd: Double,
        monthlyBudget: Double
    ) {
        val context = context ?: return

        when {
            percentUsed >= 100.0 -> {
                // 超過 100%: 高度警報 (紅色)
                binding.cardWarning.visibility = View.VISIBLE
                binding.cardWarning.setStrokeColor(ContextCompat.getColor(context, R.color.danger_red))
                binding.cardWarning.setCardBackgroundColor(Color.parseColor("#FFF0F0"))
                binding.ivWarningIcon.setColorFilter(ContextCompat.getColor(context, R.color.danger_red))
                binding.tvWarningTitle.text = "⚠️ 警報：總支出已超過預算上限！"
                binding.tvWarningTitle.setTextColor(ContextCompat.getColor(context, R.color.danger_red))
                binding.tvWarningMessage.text = String.format(
                    Locale.getDefault(),
                    "目前累積支出 (NT$ %,d) 已超越月預算 (NT$ %,d)，請儘快調整支出規劃！",
                    totalSpendTwd.toInt(),
                    monthlyBudget.toInt()
                )

                startWarningAnimations(fastBlink = true)
            }
            percentUsed >= 80.0 -> {
                // 80% ~ 100%: 警戒狀態 (橘黃色)
                binding.cardWarning.visibility = View.VISIBLE
                binding.cardWarning.setStrokeColor(ContextCompat.getColor(context, R.color.warning_orange))
                binding.cardWarning.setCardBackgroundColor(Color.parseColor("#FFFDE7"))
                binding.ivWarningIcon.setColorFilter(ContextCompat.getColor(context, R.color.warning_orange))
                binding.tvWarningTitle.text = "⚠️ 警告：預算即將超標！"
                binding.tvWarningTitle.setTextColor(ContextCompat.getColor(context, R.color.warning_orange))
                binding.tvWarningMessage.text = String.format(
                    Locale.getDefault(),
                    "目前總支出已達到預算額度的 %.1f%%，請控制後續消費！",
                    percentUsed
                )

                startWarningAnimations(fastBlink = false)
            }
            else -> {
                // 正常範圍 (< 80%): 隱藏警告框並停止動畫
                binding.cardWarning.visibility = View.GONE
                stopWarningAnimations()
            }
        }
    }

    /**
     * 開啟警告動畫：全框閃爍 (Alpha Pulse) & 警告圖示放大縮小 (Scale Pulse)
     */
    private fun startWarningAnimations(fastBlink: Boolean) {
        stopWarningAnimations()

        val duration = if (fastBlink) 500L else 900L

        // 1. Alpha 閃爍動畫 (Card Blinking Animation)
        blinkAnimator = ObjectAnimator.ofFloat(binding.cardWarning, "alpha", 1.0f, 0.35f, 1.0f).apply {
            this.duration = duration
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            start()
        }

        // 2. Icon 脈衝放大縮小動畫 (Icon Pulsing Animation)
        val scaleX = ObjectAnimator.ofFloat(binding.ivWarningIcon, "scaleX", 1.0f, 1.25f, 1.0f).apply {
            this.duration = duration
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
        }
        val scaleY = ObjectAnimator.ofFloat(binding.ivWarningIcon, "scaleY", 1.0f, 1.25f, 1.0f).apply {
            this.duration = duration
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
        }

        iconPulseAnimator = AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            start()
        }

        // 3. 觸發一次震撼搖晃動畫 (Shake animation when triggered)
        val shakeAnimator = ObjectAnimator.ofFloat(
            binding.ivWarningIcon,
            "translationX",
            0f, 15f, -15f, 10f, -10f, 5f, -5f, 0f
        ).apply {
            this.duration = 600L
            start()
        }
    }

    private fun stopWarningAnimations() {
        blinkAnimator?.cancel()
        blinkAnimator = null

        iconPulseAnimator?.cancel()
        iconPulseAnimator = null

        binding.cardWarning.alpha = 1.0f
        binding.ivWarningIcon.scaleX = 1.0f
        binding.ivWarningIcon.scaleY = 1.0f
        binding.ivWarningIcon.translationX = 0f
    }

    private fun renderCategoryBreakdown(categoryMap: Map<String, Double>, totalSpendTwd: Double) {
        binding.llCategoryBreakdown.removeAllViews()

        if (categoryMap.isEmpty() || totalSpendTwd <= 0.0) {
            val emptyTv = TextView(requireContext()).apply {
                text = "尚無支出紀錄，趕快開始記帳吧！"
                setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                textSize = 14f
            }
            binding.llCategoryBreakdown.addView(emptyTv)
            return
        }

        val formatter = NumberFormat.getNumberInstance(Locale.getDefault())

        for ((cat, amount) in categoryMap.entries.sortedByDescending { it.value }) {
            val percent = (amount / totalSpendTwd) * 100.0

            val itemLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 10, 0, 10)
            }

            val emoji = when (cat) {
                "餐飲" -> "🍽️"
                "交通" -> "🚗"
                "購物" -> "🛍️"
                "住宿" -> "🏠"
                "娛樂" -> "🎮"
                else -> "📝"
            }

            val tvTitle = TextView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = "$emoji $cat (${String.format(Locale.getDefault(), "%.1f", percent)}%)"
                textSize = 14f
                setTextColor(ContextCompat.getColor(context, R.color.text_primary))
            }

            val tvVal = TextView(requireContext()).apply {
                text = "NT$ ${formatter.format(amount.toInt())}"
                textSize = 14f
                setTypeface(null, Typeface.BOLD)
                setTextColor(ContextCompat.getColor(context, R.color.primary))
            }

            itemLayout.addView(tvTitle)
            itemLayout.addView(tvVal)
            binding.llCategoryBreakdown.addView(itemLayout)
        }
    }

    private fun renderRecentRecords(recentExpenses: List<Expense>) {
        binding.llRecentRecords.removeAllViews()

        if (recentExpenses.isEmpty()) {
            val emptyTv = TextView(requireContext()).apply {
                text = "目前無任何記帳紀錄"
                setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                textSize = 14f
            }
            binding.llRecentRecords.addView(emptyTv)
            return
        }

        val context = requireContext()
        val formatter = NumberFormat.getNumberInstance(Locale.getDefault())

        for (exp in recentExpenses) {
            val itemLayout = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 12, 0, 12)
            }

            val tvInfo = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = "${exp.date} • ${exp.category}\n${exp.note ?: "無備註"}"
                textSize = 14f
                setTextColor(ContextCompat.getColor(context, R.color.text_primary))
            }

            val twdVal = CurrencyHelper.convertCurrency(context, exp.amount, exp.currency, "TWD")

            val tvAmt = TextView(context).apply {
                text = "${formatter.format(exp.amount)} ${exp.currency}\n≈ NT$ ${formatter.format(twdVal.toInt())}"
                textSize = 13f
                gravity = Gravity.END
                setTextColor(ContextCompat.getColor(context, R.color.primary))
                setTypeface(null, Typeface.BOLD)
            }

            itemLayout.addView(tvInfo)
            itemLayout.addView(tvAmt)
            binding.llRecentRecords.addView(itemLayout)
        }
    }

    private fun showEditBudgetDialog() {
        val context = requireContext()
        val currentBudget = getSavedBudget()

        val etInput = EditText(context).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(currentBudget.toInt().toString())
            setSelection(text.length)
            setHint("請輸入新的月預算 (TWD)")
        }

        AlertDialog.Builder(context)
            .setTitle("設定月預算上限 (TWD)")
            .setMessage("請設定本月的總預算金額：")
            .setView(etInput)
            .setPositiveButton("確定儲存") { dialog, _ ->
                val inputStr = etInput.text.toString().trim()
                val newBudget = inputStr.toDoubleOrNull()
                if (newBudget != null && newBudget > 0) {
                    saveBudget(newBudget)
                    Toast.makeText(context, "預算設定完成！", Toast.LENGTH_SHORT).show()
                    UsageTracker(context).logEvent("修訂預算: $newBudget TWD")
                    viewModel.allExpenses.value?.let { updateDashboard(it) }
                } else {
                    Toast.makeText(context, "請輸入有效的預算金額", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun getSavedBudget(): Double {
        val prefs = requireContext().getSharedPreferences(PREF_BUDGET_NAME, Context.MODE_PRIVATE)
        return prefs.getFloat(KEY_MONTHLY_BUDGET, DEFAULT_BUDGET.toFloat()).toDouble()
    }

    private fun saveBudget(amount: Double) {
        val prefs = requireContext().getSharedPreferences(PREF_BUDGET_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_MONTHLY_BUDGET, amount.toFloat()).apply()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopWarningAnimations()
        _binding = null
    }
}
