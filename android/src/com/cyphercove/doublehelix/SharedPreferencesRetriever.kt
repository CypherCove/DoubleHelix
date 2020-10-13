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
import androidx.annotation.StringRes
import kotlin.reflect.KClass

/**
 * Allows easy retrieval of SharedPreferences values when the keys are stored in String resources.
 */
abstract class SharedPreferencesRetriever {
    protected abstract val context: Context
    
    @Suppress("UNCHECKED_CAST")
    protected fun <T : Any> SharedPreferences.get(@StringRes keyId: Int, cls: KClass<T>): T {
        val key = context.getString(keyId)
        return when (cls) {
            Int::class -> getInt(key, 0) as T
            Boolean::class -> getBoolean(key, false) as T
            String::class -> getString(key, "") as T
            Long::class -> getLong(key, 0L) as T
            Float::class -> getFloat(key, 0f) as T
            else -> error("Unsupported type")
        }
    }

    protected inline operator fun <reified T : Any> SharedPreferences.get(@StringRes keyId: Int)
            = get(keyId, T::class)
}