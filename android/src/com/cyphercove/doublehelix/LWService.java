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

package com.cyphercove.doublehelix;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidLiveWallpaperService;
import com.cyphercove.lwptools.android.LiveWallpaperAndroidAdapter;

public final class LWService extends AndroidLiveWallpaperService implements SharedPreferences.OnSharedPreferenceChangeListener {
	private static final int FPS_MAX=39; //Allowing high FPS can make the launcher laggy.
	
	MainRenderer.SettingsAdapter mSettingsPrefsAdapter;
	LiveWallpaperAndroidAdapter applicationListener;
	
	@Override
	public void onCreateApplication(){
		SharedPreferences sharedPrefs=PreferenceManager.getDefaultSharedPreferences(LWService.this);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.disableAudio = true;
		config.numSamples = 2;
		config.useAccelerometer = false;
		config.useCompass = false;
		config.r=8;
		config.g=8;
		config.b=8;
		config.a=8;
        
        sharedPrefs.registerOnSharedPreferenceChangeListener(this);
        mSettingsPrefsAdapter = new SettingsPrefsAdapter(getApplicationContext(), sharedPrefs);
        
		applicationListener = new LiveWallpaperAndroidAdapter(
				new MainRenderer(mSettingsPrefsAdapter),
					new TripleTapSettingsListener(getBaseContext()),
					getBaseContext(), FPS_MAX);
		
		initialize(applicationListener, config);
		
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		SharedPreferences sharedPrefs=PreferenceManager.getDefaultSharedPreferences(LWService.this);
		sharedPrefs.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		applicationListener.onSharedPreferenceChanged(sharedPreferences, key);
	}
    
}