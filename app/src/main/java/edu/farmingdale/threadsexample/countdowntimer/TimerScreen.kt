package edu.farmingdale.threadsexample.countdowntimer

import android.content.Context
import android.media.AudioManager
import android.util.Log
import android.widget.NumberPicker
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.DecimalFormat
import java.util.Locale
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build
import edu.farmingdale.threadsexample.R
import androidx.compose.ui.platform.LocalContext

@Composable
fun TimerScreen(
    modifier: Modifier = Modifier,
    timerViewModel: TimerViewModel = viewModel()

) {
    val context = LocalContext.current

    // Handle the sound when the timer reaches 0
    if (timerViewModel.remainingMillis <= 0 && timerViewModel.isRunning) {
        Log.d("TimerScreen", "Timer finished, playing sound.")
        playSound(context) // Play sound when the timer hits 0
        timerViewModel.cancelTimer() // Stop the timer
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = modifier
                .padding(20.dp)
                .size(240.dp),
            contentAlignment = Alignment.Center
        ) {

            if (timerViewModel.isRunning) {

            }

            val progress by animateFloatAsState(
                targetValue = if (timerViewModel.totalMillis > 0) {
                    timerViewModel.remainingMillis.toFloat() / timerViewModel.totalMillis
                } else 0f,
                animationSpec = tween(durationMillis = 1000, easing = LinearEasing), label = ""
            )

            // Circular Progress Indicator
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(240.dp),
                color = if (progress < 0.1f) Color.Red else Color.Green,
                strokeWidth = 10.dp,
            )

            //10 second timer
            val isLast10Seconds = timerViewModel.remainingMillis <= 10_000
            Text(
                text = timerText(timerViewModel.remainingMillis),
                fontSize = 40.sp,
                fontWeight = if (isLast10Seconds) FontWeight.Bold else FontWeight.Normal,
                color = if (isLast10Seconds) Color.Red else Color.Black
            )
//
            Text(
                text = timerText(timerViewModel.remainingMillis),
                fontSize = 40.sp,
            )
        }

        Text(
            text = timerText(timerViewModel.remainingMillis),
            fontSize = 60.sp, // Increased size
            fontWeight = FontWeight.Bold // Optional for emphasis
        )

        TimePicker(
            hour = timerViewModel.selectedHour,
            min = timerViewModel.selectedMinute,
            sec = timerViewModel.selectedSecond,
            onTimePick = timerViewModel::selectTime
        )
        if (timerViewModel.isRunning) {
            Button(
                onClick = timerViewModel::cancelTimer,
                modifier = modifier.padding(50.dp)
            ) {
                Text("Cancel")
            }
        } else {
            Button(
                enabled = timerViewModel.selectedHour +
                        timerViewModel.selectedMinute +
                        timerViewModel.selectedSecond > 0,
                onClick = timerViewModel::startTimer,
                modifier = modifier.padding(top = 50.dp)
            ) {
                Text("Start")
            }
        }

        Button(
            onClick = {
                timerViewModel.cancelTimer()
                timerViewModel.selectTime(0, 0, 0) // Reset values
            },
            modifier = Modifier.padding(top = 20.dp)
        ) {
            Text("Reset")
        }

    }

}

// Function to play sound using MediaPlayer
fun playSound(context: Context) {
    try {
        // Initialize MediaPlayer
        val mediaPlayer = MediaPlayer.create(context, R.raw.timerend)
        mediaPlayer?.start() // Start playing sound

        // Release media player once the sound is finished
        mediaPlayer?.setOnCompletionListener {
            mediaPlayer.release()
        }
    } catch (e: Exception) {
        Log.e("TimerScreen", "Error playing sound: ${e.message}")
    }
}

fun timerText(timeInMillis: Long): String {
    val duration: Duration = timeInMillis.milliseconds
    return String.format(
        Locale.getDefault(),"%02d:%02d:%02d",
        duration.inWholeHours, duration.inWholeMinutes % 60, duration.inWholeSeconds % 60)
}

@Composable
fun TimePicker(
    hour: Int = 0,
    min: Int = 0,
    sec: Int = 0,
    onTimePick: (Int, Int, Int) -> Unit = { _: Int, _: Int, _: Int -> }
) {
    // Values must be remembered for calls to onPick()
    var hourVal by remember { mutableIntStateOf(hour) }
    var minVal by remember { mutableIntStateOf(min) }
    var secVal by remember { mutableIntStateOf(sec) }

    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Hours")
            NumberPickerWrapper(
                initVal = hourVal,
                maxVal = 99,
                onNumPick = {
                    hourVal = it
                    onTimePick(hourVal, minVal, secVal)
                }
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp)
        ) {
            Text("Minutes")
            NumberPickerWrapper(
                initVal = minVal,
                onNumPick = {
                    minVal = it
                    onTimePick(hourVal, minVal, secVal)
                }
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Seconds")
            NumberPickerWrapper(
                initVal = secVal,
                onNumPick = {
                    secVal = it
                    onTimePick(hourVal, minVal, secVal)
                }
            )
        }

    }
}

@Composable
fun NumberPickerWrapper(
    initVal: Int = 0,
    minVal: Int = 0,
    maxVal: Int = 59,
    onNumPick: (Int) -> Unit = {}
) {
    val numFormat = NumberPicker.Formatter { i: Int ->
        DecimalFormat("00").format(i)
    }

    AndroidView(
        factory = { context ->
            NumberPicker(context).apply {
                setOnValueChangedListener { numberPicker, oldVal, newVal -> onNumPick(newVal) }
                minValue = minVal
                maxValue = maxVal
                value = initVal
                setFormatter(numFormat)
            }
        }
    )
}






