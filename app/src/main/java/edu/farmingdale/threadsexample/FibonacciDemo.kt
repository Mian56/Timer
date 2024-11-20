package edu.farmingdale.threadsexample

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@Composable
fun FibonacciDemoNoBgThrd() {
    var answer by remember { mutableStateOf("") }
    var textInput by remember { mutableStateOf("40") }
    val coroutineScope = rememberCoroutineScope()


    Column {
        Row {
            TextField(
                value = textInput,
                onValueChange = { textInput = it },
                label = { Text("Number?") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                )
            )
            Button(onClick = {
                val num = textInput.toLongOrNull() ?: 0
                coroutineScope.launch(Dispatchers.Default) {
                    val fibNumber = calculateFibonacci(num)
                    answer = NumberFormat.getNumberInstance(Locale.US).format(fibNumber)
                }
            }) {
                Text("Fibonacci")
            }
        }

        Text("Result: $answer")
    }
}

fun calculateFibonacci(n: Long): Long {
    if (n <= 1) return n
    var a = 0L
    var b = 1L
    for (i in 2..n) {
        val temp = a + b
        a = b
        b = temp
    }
    return b
}

fun fibonacci(n: Long): Long {
    return if (n <= 1) n else fibonacci(n - 1) + fibonacci(n - 2)
}
