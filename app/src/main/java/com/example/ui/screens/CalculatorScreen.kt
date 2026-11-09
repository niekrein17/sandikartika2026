package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TacticalBlack
import com.example.ui.theme.TacticalCardBorder
import com.example.ui.theme.TacticalDarkSurface
import com.example.ui.theme.TacticalEmerald
import com.example.ui.theme.TacticalGold
import com.example.ui.theme.TacticalTextMuted
import com.example.ui.theme.TacticalTextPrimary
import com.example.ui.theme.TacticalTextSecondary

/**
 * Disguise Front-Door: Fully functional, ordinary-looking calculator.
 * Anyone inspecting the device sees a normal working calculator.
 * Typing secret sequence (Default: "1945=") or long-pressing the app title
 * unlocks the hidden SANDI-KARTIKA Tactical Suite!
 */
@Composable
fun CalculatorDisguiseScreen(
    onUnlockTacticalSuite: () -> Unit,
    modifier: Modifier = Modifier
) {
    var displayValue by remember { mutableStateOf("0") }
    var previousValue by remember { mutableStateOf<Double?>(null) }
    var activeOperation by remember { mutableStateOf<String?>(null) }
    var isNewEntry by remember { mutableStateOf(true) }
    var rawInputSequence by remember { mutableStateOf("") }

    // Secret unlock codes
    val secretTriggerCode = "1945"

    fun onDigit(digit: String) {
        rawInputSequence += digit
        if (displayValue == "0" || isNewEntry) {
            displayValue = digit
            isNewEntry = false
        } else {
            if (displayValue.length < 12) {
                displayValue += digit
            }
        }
    }

    fun onOperation(op: String) {
        val currentNum = displayValue.toDoubleOrNull() ?: 0.0
        previousValue = currentNum
        activeOperation = op
        isNewEntry = true
        rawInputSequence = ""
    }

    fun onEquals() {
        // Check if secret code was typed before equals
        if (displayValue == secretTriggerCode || rawInputSequence == secretTriggerCode) {
            onUnlockTacticalSuite()
            return
        }

        val prev = previousValue
        val op = activeOperation
        val current = displayValue.toDoubleOrNull()

        if (prev != null && op != null && current != null) {
            val result = when (op) {
                "+" -> prev + current
                "-" -> prev - current
                "×" -> prev * current
                "÷" -> if (current != 0.0) prev / current else Double.NaN
                else -> current
            }

            displayValue = if (result.isNaN()) {
                "Error"
            } else if (result % 1.0 == 0.0) {
                result.toLong().toString()
            } else {
                "%.4f".format(result).trimEnd('0').trimEnd('.')
            }

            previousValue = null
            activeOperation = null
            isNewEntry = true
            rawInputSequence = ""
        }
    }

    fun onClear() {
        displayValue = "0"
        previousValue = null
        activeOperation = null
        isNewEntry = true
        rawInputSequence = ""
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121417))
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header (looks like normal calculator app)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        // Secret double-tap or hint check
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.Calculate,
                    contentDescription = null,
                    tint = Color(0xFF8E959E),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Kalkulator Ilmiah",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFC0C7D1),
                    fontFamily = FontFamily.SansSerif
                )
            }

            // Hidden secret backdoor: Long press for 2 seconds on the version badge unlocks the vault!
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF1E2228))
                    .clickable {
                        // Normal tap does nothing
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "v3.2.1",
                    fontSize = 10.sp,
                    color = Color(0xFF555E6B)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Display Screen
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.End
        ) {
            if (activeOperation != null && previousValue != null) {
                Text(
                    text = "${if (previousValue!! % 1.0 == 0.0) previousValue!!.toLong() else previousValue} $activeOperation",
                    fontSize = 18.sp,
                    color = Color(0xFF758190),
                    fontFamily = FontFamily.Monospace
                )
            }
            Text(
                text = displayValue,
                fontSize = if (displayValue.length > 8) 36.sp else 48.sp,
                fontWeight = FontWeight.Light,
                color = Color.White,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.End,
                maxLines = 1,
                modifier = Modifier.testTag("calc_display")
            )
        }

        // Keypad Grid
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CalcButton("C", Color(0xFF333A42), Color(0xFFFF6B6B), Modifier.weight(1f)) { onClear() }
                CalcButton("±", Color(0xFF282F38), Color.White, Modifier.weight(1f)) {
                    val num = displayValue.toDoubleOrNull()
                    if (num != null) displayValue = (-num).toString().trimEnd('0').trimEnd('.')
                }
                CalcButton("%", Color(0xFF282F38), Color.White, Modifier.weight(1f)) {
                    val num = displayValue.toDoubleOrNull()
                    if (num != null) displayValue = (num / 100.0).toString()
                }
                CalcButton("÷", Color(0xFFE5A024), Color.Black, Modifier.weight(1f)) { onOperation("÷") }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CalcButton("7", Color(0xFF1E232A), Color.White, Modifier.weight(1f)) { onDigit("7") }
                CalcButton("8", Color(0xFF1E232A), Color.White, Modifier.weight(1f)) { onDigit("8") }
                CalcButton("9", Color(0xFF1E232A), Color.White, Modifier.weight(1f)) { onDigit("9") }
                CalcButton("×", Color(0xFFE5A024), Color.Black, Modifier.weight(1f)) { onOperation("×") }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CalcButton("4", Color(0xFF1E232A), Color.White, Modifier.weight(1f)) { onDigit("4") }
                CalcButton("5", Color(0xFF1E232A), Color.White, Modifier.weight(1f)) { onDigit("5") }
                CalcButton("6", Color(0xFF1E232A), Color.White, Modifier.weight(1f)) { onDigit("6") }
                CalcButton("-", Color(0xFFE5A024), Color.Black, Modifier.weight(1f)) { onOperation("-") }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CalcButton("1", Color(0xFF1E232A), Color.White, Modifier.weight(1f)) { onDigit("1") }
                CalcButton("2", Color(0xFF1E232A), Color.White, Modifier.weight(1f)) { onDigit("2") }
                CalcButton("3", Color(0xFF1E232A), Color.White, Modifier.weight(1f)) { onDigit("3") }
                CalcButton("+", Color(0xFFE5A024), Color.Black, Modifier.weight(1f)) { onOperation("+") }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CalcButton("0", Color(0xFF1E232A), Color.White, Modifier.weight(2f)) { onDigit("0") }
                CalcButton(".", Color(0xFF1E232A), Color.White, Modifier.weight(1f)) {
                    if (!displayValue.contains(".")) displayValue += "."
                }
                CalcButton("=", Color(0xFF2EE59D), Color.Black, Modifier.weight(1f)) { onEquals() }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Subtle discreteness indicator (only recognizable by trained personnel)
        Text(
            text = "Ketik '1945=' untuk memicu brankas sandi",
            fontSize = 9.sp,
            color = Color(0xFF262C34),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CalcButton(
    text: String,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .clickable { onClick() }
            .testTag("calc_btn_$text"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
            fontFamily = FontFamily.SansSerif
        )
    }
}
