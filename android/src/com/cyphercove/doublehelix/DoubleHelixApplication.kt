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

import android.app.Application
import android.util.Log
import androidx.preference.PreferenceManager
import timber.log.Timber

class DoubleHelixApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        loadDefaultSettings()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(ErrorsAndWarningsTree())
        }
    }

    /**
     * Load all defaults from preference files. New preferences will automatically have their
     * defaults read. Old ones will not be overwritten.
     */
    private fun loadDefaultSettings() {
        arrayOf(R.xml.preferences_main)
                .forEach { PreferenceManager.setDefaultValues(this, it, true) }
    }

    private class ErrorsAndWarningsTree : Timber.DebugTree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO) {
                return
            }
            super.log(priority, tag, message, t)
        }
    }
}