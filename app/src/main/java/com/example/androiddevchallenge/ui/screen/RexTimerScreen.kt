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
package com.example.androiddevchallenge.ui.screen

import android.util.Log
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.androiddevchallenge.MainActivityViewModel
import com.example.androiddevchallenge.R
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun RexTimerScreen(mainActivityViewModel: MainActivityViewModel) {
    val rem = mainActivityViewModel.remainingInMillis
    val hours = TimeUnit.HOURS.toDays(TimeUnit.MILLISECONDS.toHours(rem))
    val minutes = TimeUnit.MILLISECONDS.toMinutes(rem) - TimeUnit.HOURS.toMinutes(hours)
    val seconds =
        TimeUnit.MILLISECONDS.toSeconds(rem) - TimeUnit.MINUTES.toSeconds(minutes) - TimeUnit.HOURS.toSeconds(
            hours
        )
    val tenthOfSeconds =
        (
            rem - TimeUnit.MINUTES.toMillis(minutes) - TimeUnit.HOURS.toMillis(hours) - TimeUnit.SECONDS.toMillis(
                seconds
            )
            ) * 10 / 1000

    val sliderPosition =
        (mainActivityViewModel.MAX_DURATION - mainActivityViewModel.remainingInMillis) * 100f / mainActivityViewModel.MAX_DURATION

    // Divide frames into n groups of (0..49) ... (0..49) and (50-100), each group has 1 sec duration
    // Repeat frame 0 to 49 till the last but one second. In the last second we will play frame 50-99.
    // There are N groups. N = totalSeconds-1, each group has 50 frames
    // For each frame, there are 1/50 seconds, which is 1000*1/50 = 20 milliseconds
    // For each group, the duration is 1 second.
    // timePassed = frameNumber*20 + groupNumber*1000
    val currentTimeInMillis =
        mainActivityViewModel.MAX_DURATION - mainActivityViewModel.remainingInMillis
    Log.d("T-REX", "currentTimeInMillis: $currentTimeInMillis")

    val nbOfGroupsBeforeCurrentGroup = (currentTimeInMillis / 1000).toLong()
    Log.d("T-REX", "nbOfGroupsBeforeCurrentGroup: $nbOfGroupsBeforeCurrentGroup")

    val timePassedBeforeCurrentGroupInMillis = nbOfGroupsBeforeCurrentGroup * 1000
    Log.d(
        "T-REX",
        "timePassedBeforeCurrentGroupInMillis: $timePassedBeforeCurrentGroupInMillis"
    )

    val currentGroupFrameTimePassedInMillis =
        currentTimeInMillis - timePassedBeforeCurrentGroupInMillis
    Log.d("T-REX", "currentGroupFrameTimePassedInMillis: $currentGroupFrameTimePassedInMillis")

    val frameNumber = if (mainActivityViewModel.remainingInMillis > 1000) {
        // During the last 1 second, we should repeatedly use frame_0 .. frame_49
        currentGroupFrameTimePassedInMillis / 20
    } else {
        // During the last 1 second, we should use frame_50 .. frame_99
        50 + currentGroupFrameTimePassedInMillis / 20
    }

    val resId = LocalContext.current.resources.getIdentifier(
        "frame_$frameNumber",
        "drawable",
        LocalContext.current.packageName
    )

    RexTimerCountDownTimerUI(
        hours = hours.toString(),
        minutes = minutes.toString(),
        seconds = seconds.toString(),
        tenthOfSeconds = tenthOfSeconds.toString(),
        sliderPosition = sliderPosition,
        resId = resId,
        onInitialPositionSelected = mainActivityViewModel::onSliderPositionChange
    )
}

@Composable
fun RexTimerCountDownTimerUI(
    hours: String,
    minutes: String,
    seconds: String,
    tenthOfSeconds: String,
    sliderPosition: Float,
    resId: Int,
    onInitialPositionSelected: (Float) -> Unit
) {
    Column {
        ConstraintLayout {
            // Create references for the composables to constrain
            val (animatedScene, timer) = createRefs()

            Image(
                painter = painterResource(id = resId),
                modifier = Modifier.fillMaxWidth().constrainAs(animatedScene) {},
                contentScale = ContentScale.Crop,
                contentDescription = "T-Rex Timer"
            )

            Text(
                text = hours.padStart(2, '0') +
                    "h " +
                    minutes.padStart(2, '0') +
                    "m " +
                    seconds.padStart(2, '0') +
                    "s " +
                    tenthOfSeconds.padStart(2, '0'),
                style = MaterialTheme.typography.h5,
                modifier = Modifier.constrainAs(timer) {
                    top.linkTo(animatedScene.top, margin = 180.dp)
                    absoluteRight.linkTo(animatedScene.absoluteRight, margin = 50.dp)
                }
            )
        }

        InitialPositionSelectionSlider(
            sliderPosition = sliderPosition,
            onInitialPositionSelected = onInitialPositionSelected
        )
    }
}

@Composable
private fun InitialPositionSelectionSlider(
    sliderPosition: Float,
    onInitialPositionSelected: (Float) -> Unit,
) {
    Column {
        Text(
            style = MaterialTheme.typography.caption,
            text = stringResource(id = R.string.select_duration),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Slider(
            value = sliderPosition,
            onValueChange = {
                onInitialPositionSelected(it)
            },
            valueRange = 0f..100f,
            steps = 100,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        )
    }
}
