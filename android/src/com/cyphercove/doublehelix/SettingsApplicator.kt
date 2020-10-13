/*******************************************************************************
 * Copyright 2020 Cypher Cove, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.cyphercove.doublehelix

import android.content.Context
import android.content.SharedPreferences
import com.badlogic.gdx.graphics.Color
import com.cyphercove.coveprefs.utils.MultiColor
import com.cyphercove.coveprefs.utils.MultiColor.Definition
import com.cyphercove.covetools.android.utils.DevicePoller
import com.cyphercove.doublehelix.MainRenderer.SettingsAdapter
import com.cyphercove.doublehelix.R.string.*
import com.cyphercove.gdxtween.graphics.ColorSpace
import com.cyphercove.gdxtween.graphics.GtColor

class SettingsApplicator(
        override val context: Context,
        private val prefs: SharedPreferences
) : SharedPreferencesRetriever(), SettingsAdapter {

    companion object {
        var tripleTapSettings = true
            private set
        private val TMP0 = Color()
        private val TMP1 = Color()
    }

    private val devicePoller = DevicePoller(context, 10f, 0.5f)
    private val multiColorDefinition = Definition(context, R.array.multicolor_definition, null)
    private lateinit var sceneColor: MultiColor
    private var haveBatteryLevelColor = false
    private var haveChargeStateColor = false
    private var lastBatteryLevel = 0f
    private var lastChargeState = false

    override fun updateAllSettings() {
        tripleTapSettings = prefs[key_triple_tap]
        prepareColor()
        updateColor(false)
        Settings.speed = prefs.get<Int>(key_rotation_speed) / 9f + 0.05f
        Settings.dof = prefs[key_depth_of_field]
        Settings.bloom = prefs[key_bloom]
        Settings.filmGrain = prefs[key_film_grain]
        Settings.scanLines = prefs[key_scan_lines]
        Settings.vignette = prefs[key_vignette]
        Settings.chromaticAberration = prefs[key_chromatic_aberration]
        Settings.numParticles = prefs.get<Int>(key_particle_count) * 100
        Settings.flipH = prefs[key_flip_horizontal]
        Settings.flipV = prefs[key_flip_vertical]
        Settings.pseudoScrolling = prefs[key_psuedo_scrolling]
    }

    override fun updateInLoop(deltaTime: Float) {
        devicePoller.update(deltaTime)
        updateColor(true)
    }

    private fun prepareColor() {
        sceneColor = MultiColor(multiColorDefinition, prefs[key_scene_color])
        val type = sceneColor.type
        haveBatteryLevelColor = type == 1
        haveChargeStateColor = type == 2
    }

    private fun updateColor(powerBasedColorsOnly: Boolean) {
        var batteryLevelChanged = false
        var chargeStateChanged = false
        if (haveBatteryLevelColor) {
            val batteryLevel = devicePoller.batteryLevel
            batteryLevelChanged = batteryLevel != lastBatteryLevel
            lastBatteryLevel = batteryLevel
        }
        if (haveChargeStateColor) {
            val chargeState = devicePoller.chargingState
            chargeStateChanged = chargeState != lastChargeState
            lastChargeState = chargeState
        }
        if (powerBasedColorsOnly) {
            if (!batteryLevelChanged && !chargeStateChanged) return
        }
        val multiColor = sceneColor
        val destination = Settings.frontHelixColor
        when (multiColor.type) {
            0 -> {
                if (!powerBasedColorsOnly)
                    Color.argb8888ToColor(destination, multiColor.values[0])
            }
            1 -> {
                Color.argb8888ToColor(TMP0, multiColor.values[0])
                Color.argb8888ToColor(TMP1, multiColor.values[1])
                GtColor.lerp(TMP0, TMP1, lastBatteryLevel, ColorSpace.DegammaLmsCompressed, false)
                destination.set(TMP0)
            }
            2 -> Color.argb8888ToColor(destination, multiColor.values[if (lastChargeState) 1 else 0])
        }
        Settings.updateColorsFromFrontHelixColor()
    }
}