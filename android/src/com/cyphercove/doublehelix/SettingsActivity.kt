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

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.cyphercove.coveprefs.CovePrefs

private const val TAG_MAIN_SETTINGS_FRAGMENT = "MAIN"

class SettingsActivity : AppCompatActivity(), PreferenceFragmentCompat.OnPreferenceDisplayDialogCallback,
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.single_fragment_activity)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_wrapper, MainSettingsFragment(), TAG_MAIN_SETTINGS_FRAGMENT)
                .commit()
    }

    override fun onPreferenceDisplayDialog(caller: PreferenceFragmentCompat, pref: Preference): Boolean {
        return CovePrefs.onPreferenceDisplayDialog(caller, pref)
    }

    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat, pref: Preference): Boolean {
        startFragment(
                caller,
                supportFragmentManager.fragmentFactory.instantiate(classLoader, pref.fragment)
        )
        return true
    }

    private fun startFragment(caller: Fragment, fragment: Fragment) {
        fragment.setTargetFragment(caller, 0)
        supportFragmentManager.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.fragment_wrapper, fragment)
                .addToBackStack(null)
                .commit()
    }
}

class MainSettingsFragment : PreferenceFragmentCompat() {

    @Suppress("ConstantConditionIf")
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_main, null)

        // Remove the rating preference once it has been clicked
        findPreference<Preference>(getString(R.string.key_rated))?.setOnPreferenceClickListener { preference ->
            preference.parent?.removePreference(preference)
            PreferenceManager.getDefaultSharedPreferences(context).edit { putBoolean(getString(R.string.key_rated), true) }
            openLink(BuildConfig.APP_STORE_LINK_PREFIX + BuildConfig.APPLICATION_ID,
                    BuildConfig.BACKUP_APP_STORE_LINK_PREFIX + BuildConfig.APPLICATION_ID)
            true
        }

        // Remove the rating preference if it has been previously clicked
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(getString(R.string.key_rated), false)) {
            preferenceScreen.removePreferenceRecursively(getString(R.string.key_rated))
        }

        findPreference<Preference>(getString(R.string.key_upsell))?.setOnPreferenceClickListener { _ ->
            openLink(BuildConfig.APP_STORE_LINK_PREFIX + BuildConfig.PREMIUM_APPLICATION_ID,
                    BuildConfig.BACKUP_APP_STORE_LINK_PREFIX + BuildConfig.PREMIUM_APPLICATION_ID)
            true
        }

        findPreference<Preference>(getString(R.string.key_other_apps))?.setOnPreferenceClickListener { _ ->
            openLink(BuildConfig.COMPANY_STORE_LINK, BuildConfig.BACKUP_COMPANY_STORE_LINK)
            true
        }

        if (BuildConfig.PREMIUM) {
            preferenceScreen.removePreferenceRecursively(getString(R.string.key_upsell))
        } else {
            listOf(
                    R.string.key_scene_color,
                    R.string.key_rotation_speed,
                    R.string.key_film_grain,
                    R.string.key_scan_lines,
                    R.string.key_vignette
            ).forEach { key ->
                findPreference<Preference>(getString(key))?.isEnabled = false
            }
        }
    }

    /**
     * A backup URI should be an `http` address and is used in case the first URI is unusable, for
     * instance, if it is an `amzn` address when the Amazon App Store is not installed.
     */
    private fun openLink(uri: String, backupUri: String) {
        var intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            intent = Intent(Intent.ACTION_VIEW, Uri.parse(backupUri))
            startActivity(intent)
        }
    }

}