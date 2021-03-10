/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.androiddevchallenge

import android.os.CountDownTimer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class MainActivityViewModel : ViewModel() {
    val MAX_DURATION =
        30 * 1000L // max duration of the slider, also the max duration of our countdown timer

    // LiveData holds state which is observed by the UI
    // (state flows down from ViewModel)
    var isCountingDown: Boolean by mutableStateOf(false)
        private set

    var durationInMillis: Long by mutableStateOf(0L)
        private set

    var remainingInMillis: Long by mutableStateOf(0L)
        private set

    private var countDownTimer: CountDownTimer? = null

    private fun start(sliderPosition: Float) {
        isCountingDown = true
        durationInMillis = MAX_DURATION - (MAX_DURATION * sliderPosition / 100f).toLong()
        countDownTimer = object : CountDownTimer(durationInMillis, 10) {
            override fun onTick(millisUntilFinished: Long) {
                remainingInMillis = millisUntilFinished
            }

            override fun onFinish() {
                isCountingDown = false
            }
        }.start()
    }

    // onSliderPositionChange is an event we're defining that the UI can invoke
    // (events flow up from UI)
    fun onSliderPositionChange(_SliderPosition: Float) {
        isCountingDown = false
        countDownTimer?.cancel()
        remainingInMillis = (MAX_DURATION * (100f - _SliderPosition) / 100f).toLong()
        start(_SliderPosition)
    }
}
