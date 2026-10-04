package com.example.mycalculator

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import java.math.BigDecimal
import java.math.RoundingMode

class MainActivity : ComponentActivity() {

    lateinit var tvInput: TextView
    lateinit var btnOne: Button
    lateinit var btnTwo: Button
    lateinit var btnThree: Button
    lateinit var btnFour: Button
    lateinit var btnFive: Button
    lateinit var btnSix: Button
    lateinit var btnSeven: Button
    lateinit var btnEight: Button
    lateinit var btnNine: Button
    lateinit var btnZero: Button
    lateinit var btnDot: Button
    lateinit var btnPlus: Button
    lateinit var btnMinus: Button
    lateinit var btnMultiply: Button
    lateinit var btnDivide: Button
    lateinit var btnEqual: Button
    lateinit var clear: Button
    lateinit var allClear: Button
    lateinit var btnBackspace: Button

    lateinit var tvOldInput: TextView
    lateinit var tvCurrentOperand: TextView

    var currentInput = StringBuilder()
    var currentOperator = Operator.NONE
    var operand1: BigDecimal? = null
    private var isNewInput = false

    enum class Operator {
        NONE, ADD, SUBTRACT, MULTIPLY, DIVIDE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvInput = findViewById(R.id.tvInput)
        btnOne = findViewById(R.id.btnOne)
        btnTwo = findViewById(R.id.btnTwo)
        btnThree = findViewById(R.id.btnThree)
        btnFour = findViewById(R.id.btnFour)
        btnFive = findViewById(R.id.btnFive)
        btnSix = findViewById(R.id.btnSix)
        btnSeven = findViewById(R.id.btnSeven)
        btnEight = findViewById(R.id.btnEight)
        btnNine = findViewById(R.id.btnNine)
        btnZero = findViewById(R.id.btnZero)
        btnDot = findViewById(R.id.btnDot)
        btnPlus = findViewById(R.id.btnPlus)
        btnMinus = findViewById(R.id.btnMinus)
        btnMultiply = findViewById(R.id.btnMultiply)
        btnDivide = findViewById(R.id.btnDivide)
        btnEqual = findViewById(R.id.btnEqual)
        clear = findViewById(R.id.clear)
        allClear = findViewById(R.id.allClear)
        btnBackspace = findViewById(R.id.btnBackspace)
        tvOldInput = findViewById(R.id.tvOldInput)
        tvCurrentOperand = findViewById(R.id.tvCurrentOperand)

        btnOne.setOnClickListener { appendNumber("1") }
        btnTwo.setOnClickListener { appendNumber("2") }
        btnThree.setOnClickListener { appendNumber("3") }
        btnFour.setOnClickListener { appendNumber("4") }
        btnFive.setOnClickListener { appendNumber("5") }
        btnSix.setOnClickListener { appendNumber("6") }
        btnSeven.setOnClickListener { appendNumber("7") }
        btnEight.setOnClickListener { appendNumber("8") }
        btnNine.setOnClickListener { appendNumber("9") }
        btnZero.setOnClickListener { appendNumber("0") }
        btnDot.setOnClickListener { appendNumber(".") }

        btnPlus.setOnClickListener { setOperator(Operator.ADD) }
        btnMinus.setOnClickListener { setOperator(Operator.SUBTRACT) }
        btnMultiply.setOnClickListener { setOperator(Operator.MULTIPLY) }
        btnDivide.setOnClickListener { setOperator(Operator.DIVIDE) }

        btnEqual.setOnClickListener { calculateResult() }
        clear.setOnClickListener { clearInput() }
        allClear.setOnClickListener { allClearInput() }
        btnBackspace.setOnClickListener { handleBackspace() }

        tvInput.text = "0"
        tvOldInput.text = ""
        tvCurrentOperand.text = ""
    }

    private fun appendNumber(number: String) {
        if (isNewInput) {
            currentInput.clear()
            isNewInput = false
        }

        if (number == "." && currentInput.contains(".")) return

        currentInput.append(number)
        updateDisplay()
    }

    private fun handleBackspace() {
        if (currentInput.isNotEmpty()) {
            currentInput.deleteCharAt(currentInput.length - 1)
            updateDisplay()
        }
    }

    @Suppress("SetTextI18n")
    private fun setOperator(operator: Operator) {
        if (currentInput.isNotEmpty()) {
            if (operand1 == null) {
                operand1 = BigDecimal(currentInput.toString())
            } else if (currentOperator != Operator.NONE && !isNewInput) {
                performCalculation()
            }
        }

        if (operand1 != null) {
            currentOperator = operator
            tvOldInput.text = "${operand1?.stripTrailingZeros()?.toPlainString()}${operatorToString(operator)}"
            isNewInput = true
        }
        tvCurrentOperand.text = ""
    }

    private fun operatorToString(operator: Operator): String {
        return when (operator) {
            Operator.ADD -> "+"
            Operator.SUBTRACT -> "-"
            Operator.MULTIPLY -> "×"
            Operator.DIVIDE -> "÷"
            Operator.NONE -> ""
        }
    }

    private fun calculateResult() {
        if (currentInput.isEmpty() || operand1 == null || currentOperator == Operator.NONE) return

        performCalculation()

        // 結算後重置 state：按下數字會重新開始，按運算符會把目前顯示結果當成第一個數
        operand1 = null
        currentOperator = Operator.NONE
        isNewInput = true
    }

    @Suppress("SetTextI18n")
    private fun performCalculation() {
        val operand2 = BigDecimal(currentInput.toString())
        var result: BigDecimal?

        when (currentOperator) {
            Operator.ADD -> result = operand1?.add(operand2)
            Operator.SUBTRACT -> result = operand1?.subtract(operand2)
            Operator.MULTIPLY -> result = operand1?.multiply(operand2)
            Operator.DIVIDE -> {
                if (operand2 != BigDecimal.ZERO) {
                    result = operand1?.divide(operand2, 10, RoundingMode.HALF_UP)
                } else {
                    Log.e("CalculatorApp", "Division by zero attempted.")
                    tvInput.text = "Error: Div by 0"
                    currentInput.clear()
                    operand1 = null
                    currentOperator = Operator.NONE
                    tvOldInput.text = ""
                    tvCurrentOperand.text = ""
                    return
                }
            }
            Operator.NONE -> result = operand2
        }

        if (result != null) {
            val finalResult = result.stripTrailingZeros().toPlainString()
            tvOldInput.text = "${operand1?.stripTrailingZeros()?.toPlainString()}${operatorToString(currentOperator)}${operand2.stripTrailingZeros().toPlainString()}"
            tvInput.text = finalResult

            operand1 = result
            currentInput.clear()
            currentInput.append(finalResult)
        }
    }

    private fun allClearInput() {
        currentInput.clear()
        operand1 = null
        currentOperator = Operator.NONE
        isNewInput = false
        tvOldInput.text = ""
        tvInput.text = "0"
        tvCurrentOperand.text = ""
    }

    private fun clearInput() {
        currentInput.clear()
        tvInput.text = "0"
    }

    private fun updateDisplay() {
        tvInput.text = if (currentInput.isEmpty()) "0" else currentInput.toString()
    }
}