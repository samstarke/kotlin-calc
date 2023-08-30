package com.example.calculator

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round

class MainActivity : AppCompatActivity() {

    private lateinit var tvDisplay: TextView

    private var currentNumber: String = "0"
    private var previousNumber: String = ""
    private var isNewInput = false
    private var operation: String = ""
    private var lastOperand: String = ""
    private var lastOperation: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvDisplay = findViewById(R.id.tvDisplay)
        tvDisplay.text = currentNumber

        val btn0: Button = findViewById(R.id.btn0)
        val btn1: Button = findViewById(R.id.btn1)
        val btn2: Button = findViewById(R.id.btn2)
        val btn3: Button = findViewById(R.id.btn3)
        val btn4: Button = findViewById(R.id.btn4)
        val btn5: Button = findViewById(R.id.btn5)
        val btn6: Button = findViewById(R.id.btn6)
        val btn7: Button = findViewById(R.id.btn7)
        val btn8: Button = findViewById(R.id.btn8)
        val btn9: Button = findViewById(R.id.btn9)
        val btnDot: Button = findViewById(R.id.btnDot)
        val btnPlusMinus: Button = findViewById(R.id.btnPlusMinus)
        val btnAdd: Button = findViewById(R.id.btnAdd)
        val btnSubtract: Button = findViewById(R.id.btnSubtract)
        val btnMultiply: Button = findViewById(R.id.btnMultiply)
        val btnDivide: Button = findViewById(R.id.btnDivide)
        val btnEquals: Button = findViewById(R.id.btnEquals)
        val btnClear: Button = findViewById(R.id.btnClear)
        val btnBackspace: Button = findViewById(R.id.btnBackspace)

        // Digits
        btn0.setOnClickListener { appendNumber("0") }
        btn1.setOnClickListener { appendNumber("1") }
        btn2.setOnClickListener { appendNumber("2") }
        btn3.setOnClickListener { appendNumber("3") }
        btn4.setOnClickListener { appendNumber("4") }
        btn5.setOnClickListener { appendNumber("5") }
        btn6.setOnClickListener { appendNumber("6") }
        btn7.setOnClickListener { appendNumber("7") }
        btn8.setOnClickListener { appendNumber("8") }
        btn9.setOnClickListener { appendNumber("9") }

        btnDot.setOnClickListener { appendNumber(".") }
        btnPlusMinus.setOnClickListener { switchSign() }


        // Operations
        btnAdd.setOnClickListener { setOperation("+") }
        btnSubtract.setOnClickListener { setOperation("-") }
        btnMultiply.setOnClickListener { setOperation("*") }
        btnDivide.setOnClickListener { setOperation("/") }
        btnEquals.setOnClickListener {
            if (operation.isEmpty() && previousNumber.isEmpty()) {
                currentNumber = formatNumber(currentNumber.toDouble())
                updateDisplay()
            } else {
                compute()
            }
        }

        // Clear & Backspace
        btnClear.setOnClickListener { clear() }
        btnBackspace.setOnClickListener { backspace() }
    }

    // Handles the addition of a number or a decimal point to the current displayed number.
    private fun appendNumber(number: String) {
        // Ensuring only one decimal point in the number.
        if (number == "." && currentNumber.contains(".")) return

        // If the length is already 9 characters, don't add more.
        if (currentNumber.length >= 9) {
            return
        }

        // If starting a new input, reset current displayed number.
        if (isNewInput) {
            currentNumber = ""
            isNewInput = false
        }

        // Special handling for starting with 0 or entering a decimal first.
        currentNumber = when {
            currentNumber == "0" && number != "." -> number
            currentNumber.isEmpty() && number == "." -> "0."
            else -> currentNumber + number
        }
        updateDisplay()
    }

    // Toggle the sign of the displayed number.
    private fun switchSign() {
        // Handling for non-empty and valid current numbers.
        if (currentNumber.isNotEmpty() && currentNumber != ".") {
            currentNumber = if (currentNumber.startsWith("-")) {
                currentNumber.drop(1)
            } else {
                "-$currentNumber"
            }
        } else if (operation.isNotEmpty() && previousNumber.isNotEmpty()) {
            // Handling for switching sign after an operation is set, but no new number is entered.
            currentNumber = if (previousNumber.startsWith("-")) {
                previousNumber.drop(1)
            } else {
                "-$previousNumber"
            }
            isNewInput = false
        }
        updateDisplay()
    }

    // Sets the arithmetic operation for the calculator.
    private fun setOperation(op: String) {
        // Avoid performing arithmetic if no new input is present.
        if (isNewInput) {
            operation = op
            return
        }

        // If all operands and operations are present, compute intermediate result.
        if (previousNumber.isNotEmpty() && currentNumber.isNotEmpty() && operation.isNotEmpty()) {
            computeIntermediateResult()
        } else {
            previousNumber = currentNumber
            currentNumber = ""
        }

        operation = op
        lastOperation = op
        isNewInput = true
    }

    // Computes intermediate results to allow for chained arithmetic operations.
    private fun computeIntermediateResult() {
        // Check for invalid numbers before proceeding.
        if (currentNumber.toDouble().isInfinite() || currentNumber.toDouble().isNaN()){
            displayError(currentNumber)
            return
        }

        // Calculate intermediate result based on selected operation.
        val intermediateResult = when (operation) {
            "+" -> previousNumber.toDouble() + currentNumber.toDouble()
            "-" -> previousNumber.toDouble() - currentNumber.toDouble()
            "*" -> previousNumber.toDouble() * currentNumber.toDouble()
            "/" -> if (currentNumber != "0") {
                previousNumber.toDouble() / currentNumber.toDouble()
            } else {
                // Handle division by zero to prevent crashes.
                displayError("Divide By Zero!")
                return
            }
            else -> 0.0
        }
        // Format the computed result and update previous and current numbers.
        currentNumber = formatNumber(intermediateResult)
        previousNumber = currentNumber
        updateDisplay()
    }

    // Handles the computation when the equals button is pressed.
    private fun compute() {
        // Check and reuse previous number if needed.
        if (currentNumber.isEmpty() && operation.isNotEmpty()) {
            currentNumber = previousNumber
        }

        // Preserve current number for potential subsequent computations.
        if (operation.isNotEmpty()) {
            lastOperand = currentNumber
        } else if (lastOperation.isNotEmpty()) {
            previousNumber = currentNumber
            currentNumber = lastOperand
        }

        // Calculate the result based on the operation.
        val rawResult = when (operation.ifEmpty { lastOperation }) {
            "+" -> previousNumber.toDouble() + currentNumber.toDouble()
            "-" -> previousNumber.toDouble() - currentNumber.toDouble()
            "*" -> previousNumber.toDouble() * currentNumber.toDouble()
            "/" -> {
                // Handle special cases for division.
                if (currentNumber == "." || currentNumber.toDouble() == 0.0 || currentNumber.toDouble() == -0.0) {
                    displayError("Divide By Zero!")
                    return // Exit without further calculations.
                }
                previousNumber.toDouble() / currentNumber.toDouble()
            }
            else -> return
        }

        // Round the raw result to avoid floating point inaccuracies.
        val roundedResult = roundToDecimals(rawResult, 10)

        // Format the rounded result for display.
        currentNumber = formatNumber(roundedResult)

        // Check for invalid numbers after computation.
        if (currentNumber.toDouble().isInfinite() || currentNumber.toDouble().isNaN()){
            displayError(currentNumber)
            return
        }

        // Reset operation after successful computation.
        operation = ""
        updateDisplay()
    }

    // Displays the provided error message and resets state to prevent subsequent errors.
    private fun displayError(message: String) {
        tvDisplay.text = message
        currentNumber = "0"
        previousNumber = ""
        operation = ""
        lastOperation = ""
    }

    // Formats numbers, either as whole numbers, in scientific notation, or retains decimal values.
    private fun formatNumber(number: Double): String {
        return when {
            isLargeNumber(number) -> "%.6e".format(number)  // For very large numbers, use scientific notation.
            number % 1.0 == 0.0 -> "%.0f".format(number)  // For integers, remove decimal point.
            else -> number.toString()  // Retain regular format.
        }
    }

    // Rounds a double to a specified number of decimal places.
    private fun roundToDecimals(number: Double, decimalPlaces: Int): Double {
        val factor = 10.0.pow(decimalPlaces)
        return round(number * factor) / factor
    }

    // Determines if a number should be formatted in scientific notation.
    private fun isLargeNumber(number: Double): Boolean {
        return abs(number) >= 1e7
    }

    // Resets the calculator's state to default, clearing all numbers and operations.
    private fun clear() {
        currentNumber = "0"
        previousNumber = ""
        operation = ""
        lastOperation = ""
        updateDisplay()
    }

    // Allows user to correct an entry by removing the last digit.
    private fun backspace() {
        if (currentNumber.isNotEmpty()) {
            currentNumber = currentNumber.dropLast(1)
            if (currentNumber.isEmpty() || currentNumber == "-") {
                currentNumber = "0"
            }
            updateDisplay()
        }
    }

    // Refreshes the display, showing either the current or previous number.
    private fun updateDisplay() {
        tvDisplay.text = currentNumber.ifEmpty {
            previousNumber
        }
    }
}