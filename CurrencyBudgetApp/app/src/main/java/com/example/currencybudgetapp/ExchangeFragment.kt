//一進入這個頁面就會自動連網抓一次最新匯率並暫存，也可以手動按按鈕更新或進行試算

package com.example.currencybudgetapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.currencybudgetapp.api.CurrencyHelper
import com.example.currencybudgetapp.databinding.FragmentExchangeBinding
import kotlinx.coroutines.launch

class ExchangeFragment : Fragment() {

    private var _binding: FragmentExchangeBinding? = null
    private val binding get() = _binding!!

    // 常見旅遊幣別清單
    private val currencyList = listOf("JPY", "USD", "KRW", "EUR", "CNY", "HKD", "TWD")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExchangeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSpinners()
        updateTimeDisplay()

        // 進入頁面時自動向 API 抓取一次最新匯率並暫存
        fetchRatesFromApi(showToast = false)

        // 手動點擊更新匯率按鈕
        binding.btnRefreshRates.setOnClickListener {
            fetchRatesFromApi(showToast = true)
        }

        // 點擊立即換算按鈕
        binding.btnConvert.setOnClickListener {
            calculateConversion()
        }
    }

    private fun setupSpinners() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            currencyList
        )
        binding.spinnerFromCurrency.adapter = adapter
        binding.spinnerToCurrency.adapter = adapter

        // 預設設定為 JPY (日幣) 換成 TWD (台幣)
        binding.spinnerFromCurrency.setSelection(0)
        binding.spinnerToCurrency.setSelection(currencyList.indexOf("TWD"))
    }

    private fun fetchRatesFromApi(showToast: Boolean) {
        binding.btnRefreshRates.isEnabled = false
        binding.btnRefreshRates.text = "正在更新匯率..."

        viewLifecycleOwner.lifecycleScope.launch {
            val isSuccess = CurrencyHelper.fetchAndCacheRates(requireContext())
            binding.btnRefreshRates.isEnabled = true
            binding.btnRefreshRates.text = "連網更新最新匯率"

            if (isSuccess) {
                updateTimeDisplay()
                if (showToast) {
                    Toast.makeText(requireContext(), "匯率更新成功！", Toast.LENGTH_SHORT).show()
                }
            } else {
                updateTimeDisplay()
                if (showToast) {
                    Toast.makeText(requireContext(), "更新失敗，目前使用暫存或預設匯率", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun calculateConversion() {
        val amountText = binding.etAmount.text.toString().trim()
        val amount = amountText.toDoubleOrNull()

        if (amount == null || amount <= 0.0) {
            Toast.makeText(requireContext(), "請輸入有效的金額", Toast.LENGTH_SHORT).show()
            return
        }

        val fromCurrency = binding.spinnerFromCurrency.selectedItem.toString()
        val toCurrency = binding.spinnerToCurrency.selectedItem.toString()

        val convertedValue = CurrencyHelper.convertCurrency(
            context = requireContext(),
            amount = amount,
            fromCurrency = fromCurrency,
            toCurrency = toCurrency
        )

        binding.tvResult.text = String.format(
            java.util.Locale.getDefault(),
            "%.2f %s  =  %.2f %s",
            amount,
            fromCurrency,
            convertedValue,
            toCurrency
        )
    }

    private fun updateTimeDisplay() {
        val lastTime = CurrencyHelper.getLastUpdateTime(requireContext())
        binding.tvLastUpdate.text = "匯率更新時間：$lastTime"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}