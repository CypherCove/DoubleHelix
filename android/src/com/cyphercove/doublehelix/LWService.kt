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

import android.content.Intent
import androidx.preference.PreferenceManager
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.badlogic.gdx.backends.android.AndroidLiveWallpaperService
import com.cyphercove.covetools.android.LiveWallpaperWrapper
import com.cyphercove.covetools.android.WallpaperEventAdapter

class LWService : AndroidLiveWallpaperService() {

    override fun onCreateApplication() {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        val config = AndroidApplicationConfiguration().apply {
            disableAudio = true
            numSamples = 2
            useAccelerometer = false
            useCompass = false
            r = 8
            g = 8
            b = 8
            a = 8
        }

        val settingsApplicator = SettingsApplicator(applicationContext, sharedPrefs)
        val applicationListener = LiveWallpaperWrapper(
                MainRenderer(settingsApplicator),
                baseContext, sharedPrefs, object : WallpaperEventAdapter() {
            override fun onMultiTap(tapCount: Int): Boolean {
                if (tapCount == 3) {
                    if (SettingsApplicator.tripleTapSettings) {
                        with(Intent(applicationContext, SettingsActivity::class.java)) {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            baseContext.startActivity(this)
                        }
                    }
                    return true
                }
                return false
            }
        })
        initialize(applicationListener, config)
    }

}