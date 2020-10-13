/*******************************************************************************
 * Copyright 2015 Cypher Cove, LLC
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

import android.annotation.TargetApi
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.badlogic.gdx.backends.android.AndroidDaydream
import com.cyphercove.covetools.android.LiveWallpaperWrapper

@TargetApi(17)
class Daydream : AndroidDaydream() {

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isInteractive = false
        val sharedPrefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)
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
        val mSettingsPrefsAdapter = SettingsApplicator(applicationContext, sharedPrefs)
        val applicationListener = LiveWallpaperWrapper(
                MainRenderer(mSettingsPrefsAdapter),
                baseContext,
                sharedPrefs
        )
        initialize(applicationListener, config)
    }

}