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

import android.content.Context;
import android.content.SharedPreferences;

import com.badlogic.gdx.graphics.Color;
import com.cyphercove.lwptools.android.PowerUtil;
import com.cyphercove.lwptools.android.prefs.AdvancedColor;

import java.util.HashMap;
import java.util.Map;

public class SettingsPrefsAdapter implements MainRenderer.SettingsAdapter{
	
	public static boolean tripleTapSettings = true;
	
	Context context; 
	SharedPreferences sharedPrefs;
	PowerUtil mPowerUtil;
	
	Map<AdvancedColor, Color> mColors = new HashMap<AdvancedColor, Color>();

	public SettingsPrefsAdapter(Context context, SharedPreferences sharedPrefs){
		this.context = context;
		this.sharedPrefs = sharedPrefs;
		this.mPowerUtil = new PowerUtil(context, 10, 0.5f);
	}

	@Override
	public void updateAllSettings() {
		tripleTapSettings = PrefsActivity.tripleTap.getValue(sharedPrefs);
		
		prepareColor();
		updateColor(false);

        Settings.speed = (float)PrefsActivity.rotationSpeed.getValue(sharedPrefs)/10f + 0.05f;
		Settings.dof = PrefsActivity.depthOfField.getValue(sharedPrefs);
        Settings.bloom = PrefsActivity.bloom.getValue(sharedPrefs);
		Settings.filmGrain = PrefsActivity.filmGrain.getValue(sharedPrefs);
		Settings.scanLines = PrefsActivity.scanLines.getValue(sharedPrefs);
		Settings.vignette = PrefsActivity.vignette.getValue(sharedPrefs);
        Settings.numParticles = PrefsActivity.particleCount.getValue(sharedPrefs) * 100;
        Settings.pointParticles = PrefsActivity.pointParticles.getValue(sharedPrefs);
		Settings.pseudoScrolling = PrefsActivity.pseudoScrolling.getValue(sharedPrefs);
	}

	@Override
	public void updateInLoop(float deltaTime) {
		mPowerUtil.update(deltaTime);
		updateColor(true);
	}

	private void prepareColor() {
		mColors.clear();
		mColors.put(PrefsActivity.sceneColor.getAdvancedColor(sharedPrefs), Settings.frontHelixColor);
	}

	private void updateColor(boolean powerBasedColorsOnly) {
		AdvancedColor.updateLibgdxColorsFromMap(
				mColors, mPowerUtil.getBatteryLevel(), mPowerUtil.getChargingState(), powerBasedColorsOnly);

        Settings.updateColorsFromFrontHelixColor();
		
	}

}
