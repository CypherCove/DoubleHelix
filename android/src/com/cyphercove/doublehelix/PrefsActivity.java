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

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.MenuItem;

import com.cyphercove.lwptools.android.prefs.AboutDialogPref;
import com.cyphercove.lwptools.android.prefs.AdvancedColor;
import com.cyphercove.lwptools.android.prefs.AdvancedColorPickerDialog.TextResources;
import com.cyphercove.lwptools.android.prefs.AdvancedColorPref;
import com.cyphercove.lwptools.android.prefs.CheckBoxPref;
import com.cyphercove.lwptools.android.prefs.Pref;
import com.cyphercove.lwptools.android.prefs.Pref.SummaryMode;
import com.cyphercove.lwptools.android.prefs.PrefRoot;
import com.cyphercove.lwptools.android.prefs.SliderPref;

import java.util.ArrayList;

public class PrefsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener{
	public static final String TAG = "PrefsActivity";
    public static boolean IS_AMAZON = BuildConfig.AMAZON;
    public static boolean IS_PAID = BuildConfig.PAID;
	
	SharedPreferences sharedPrefs;
	Handler mHandler;
	
	private ArrayList<Pref> allPrefs = new ArrayList<Pref>();
	private ArrayList<Pref> dependentPrefs = new ArrayList<Pref>();
	
	public static AdvancedColorPref sceneColor = new AdvancedColorPref("sceneColor", 
			AdvancedColor.constantColorToAdvancedColorString(0xff0e4c89),
			SummaryMode.AdvancedColor, R.string.scene_color, 0, 2);
	public static CheckBoxPref depthOfField = new CheckBoxPref("depthOfField", true, SummaryMode.String,
			R.string.depth_of_field,R.string.summ_depth_of_field);
    public static CheckBoxPref bloom = new CheckBoxPref("bloom", true, SummaryMode.NoneOrCustom,
            R.string.bloom, 0);
    public static CheckBoxPref filmGrain = new CheckBoxPref("filmGrain", false, SummaryMode.NoneOrCustom,
            R.string.film_grain, 0);
	public static CheckBoxPref scanLines = new CheckBoxPref("scanLines", false, SummaryMode.NoneOrCustom,
			R.string.scan_lines, 0);
	public static CheckBoxPref vignette = new CheckBoxPref("vignette", false, SummaryMode.NoneOrCustom,
			R.string.vignette, 0);
    /**
     * The value is 0 to 10. To translate to actual number of particles, multiply by 100.
     */
    public static SliderPref particleCount = new SliderPref("particleCount", 3, SummaryMode.Value, R.string.particle_count, 0,
                    R.string.particle_count, R.string.particle_count_min, R.string.particle_count_max, 10, true, new SliderPref.SliderPrefValueFormatter() {
                @Override
                public String formatValue(int value) {
                    return "" + (value*100);
                }
            });

    /**
     * The value is 0.05 to 1.0, with ten values to choose from. To translate to actual number of particles, divide by 9 and add 0.05.
     */
    public static SliderPref rotationSpeed = new SliderPref("rotationSpeed", 3, SummaryMode.NoneOrCustom, R.string.speed, 0,
            R.string.speed, R.string.speed_min, R.string.speed_max, 9, false);
    public static CheckBoxPref pointParticles = new CheckBoxPref("pointParticles", false, SummaryMode.String,
            R.string.point_particles, R.string.summ_point_particles);
    public static CheckBoxPref trilinear = new CheckBoxPref("trilinear", false, SummaryMode.String,
            R.string.trilinear, R.string.summ_trilinear);

	public static CheckBoxPref pseudoScrolling = new CheckBoxPref("pseudoScrolling", false, SummaryMode.String,
			R.string.pseudoscrolling, R.string.summ_pseudoscrolling);
	public static CheckBoxPref tripleTap = new CheckBoxPref("tripleTap", false, SummaryMode.String,
			R.string.triple_tap, R.string.summ_triple_tap);
	public static AboutDialogPref about = new AboutDialogPref(R.string.about, R.string.version, R.string.about, 
			R.drawable.ic_launcher, R.string.about_message, R.string.ok);
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mHandler = new Handler();
		
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		setPreferenceScreen(createPreferenceHierarchy());
		updateAllSummaries();
		sharedPrefs.registerOnSharedPreferenceChangeListener(this);
		
		if (Build.VERSION.SDK_INT>=14){
			setHomeAsUpV14();
		}
	}
	
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void setHomeAsUpV14(){
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId()==android.R.id.home){
			finish();
		}
		return true;
	}

    @Override
    protected void onDestroy() {
    	sharedPrefs.unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }


	private PreferenceScreen createPreferenceHierarchy() {
		int indentRes = getIndentLayoutResource();
		
		allPrefs.clear();
		
		@SuppressWarnings("deprecation")
		PrefRoot root = new PrefRoot(getPreferenceManager(), this, sharedPrefs, indentRes, R.string.ok, 
				getAdvancedColorPickerStringResources(), allPrefs);
		
		Preference otherWallpapers = new Preference(this);
		otherWallpapers.setLayoutResource(R.layout.hive_ad_pref);
		otherWallpapers.setTitle(R.string.other_wallpapers);
		otherWallpapers.setSummary(R.string.summ_other_wallpapers);
		otherWallpapers.setOnPreferenceClickListener(new OnPreferenceClickListener(){
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				String url = IS_AMAZON ?
                        "amzn://apps/android?p=com.cyphercove.doublehelix&showAll=1" :
                        "market://search?q=pub:\"Cypher Cove\"";
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
				return false;
			}
		});
		root.addPreference(otherWallpapers);
		
		if (!sharedPrefs.getBoolean("ratePressed", false)){
			Preference rateThisApp = new Preference(this);
			rateThisApp.setTitle(R.string.rate);
			rateThisApp.setSummary(R.string.summ_rate);
			rateThisApp.setOnPreferenceClickListener(new OnPreferenceClickListener(){
				@Override
				public boolean onPreferenceClick(Preference arg0) {
					SharedPreferences.Editor editor = sharedPrefs.edit();
					editor.putBoolean("ratePressed", true);
					editor.commit();
					String url = IS_AMAZON? "amzn://apps/android?p=com.cyphercove.doublehelix" :
                            "market://details?id=" + PrefsActivity.this.getPackageName();
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setData(Uri.parse(url));
					startActivity(i);
					return false;
				}
			});
			root.addPreference(rateThisApp);
		}

        if (!IS_PAID){
            //Upsell pref
            @SuppressWarnings("deprecation")
            PreferenceScreen unpaidInfo = getPreferenceManager().createPreferenceScreen(this);
            unpaidInfo.setTitle(R.string.get_paid);

            Spannable summary = new SpannableString( getString(R.string.summ_get_paid) );
            summary.setSpan(new ForegroundColorSpan(0xffff0040), 0, summary.length(), 0);
            unpaidInfo.setSummary(summary);

            unpaidInfo.setOnPreferenceClickListener(new OnPreferenceClickListener(){
                @Override
                public boolean onPreferenceClick(Preference arg0) {
                    String url = IS_AMAZON? "amzn://apps/android?p=com.cyphercove.doublehelix" :
                            "market://details?id=" + PrefsActivity.this.getPackageName();
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                    return false;
                }
            });
            root.addPreference(unpaidInfo);

        }
		
		PreferenceCategory settingsCategory = new PreferenceCategory(this);
		settingsCategory.setTitle(R.string.cat_settings);
		root.addPreference(settingsCategory);
		
		root.addPreference(sceneColor);
        sceneColor.pref.setEnabled(IS_PAID);
        root.addPreference(rotationSpeed);
        rotationSpeed.pref.setEnabled(IS_PAID);
		root.addPreference(depthOfField);
        root.addPreference(bloom);
        root.addPreference(filmGrain);
        filmGrain.pref.setEnabled(IS_PAID);
		root.addPreference(scanLines);
        scanLines.pref.setEnabled(IS_PAID);
		root.addPreference(vignette);
        vignette.pref.setEnabled(IS_PAID);
        root.addPreference(particleCount);

        //TODO currently not worthwhile performance-wise.
//		root.addPreference(pointParticles, true);
//        pointParticles.setDependency(particleCount, dependentPrefs, new Pref.DependencyEvaluator(){
//            public boolean evaluate(Pref dependencyPref) {
//                SliderPref pref = (SliderPref) dependencyPref;
//                return (pref.getValue(sharedPrefs) != 0);
//            }
//        });

        root.addPreference(trilinear);
		root.addPreference(pseudoScrolling);
		root.addPreference(tripleTap);
		
		PreferenceCategory aboutCategory = new PreferenceCategory(this);
		aboutCategory.setTitle(R.string.cat_about);
		root.addPreference(aboutCategory);
		
		root.addPreference(about,false);
		
		return root.root;
	}
	
	protected void updateAllSummaries(){
		for (Pref p : allPrefs)
			p.updateSummary(this, sharedPrefs);
	}
	
	protected void updateAllSummariesWithKey(String key){
		for (Pref p : allPrefs)
			p.updateSummaryWithKey(this, sharedPrefs, key);
	}

	private void updateDependencies(){
		for (Pref p : dependentPrefs)
			p.checkDependency();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		updateDependencies();
		updateAllSummariesWithKey(key);	}
	
	protected int getIndentLayoutResource() {
		TypedArray a = obtainStyledAttributes(R.styleable.ChildPrefAttrs);
		int indentRes = a.getResourceId(
				R.styleable.ChildPrefAttrs_android_preferenceLayoutChild,0);
		a.recycle();
		return indentRes;
	}
	
	TextResources sAdvancedColorPickerTextResources;
	protected TextResources getAdvancedColorPickerStringResources() {
		if (sAdvancedColorPickerTextResources==null){
			sAdvancedColorPickerTextResources = new TextResources();
			sAdvancedColorPickerTextResources.title = R.string.choose_color;
			sAdvancedColorPickerTextResources.mode = R.string.color_mode;
			sAdvancedColorPickerTextResources.batteryBasedColor = R.string.battery_colors;
			sAdvancedColorPickerTextResources.constantColor = R.string.single_colors;
			sAdvancedColorPickerTextResources.plugBasedColor = R.string.plugged_colors;
			sAdvancedColorPickerTextResources.selectedConstant = R.string.constant_color_selected;
			sAdvancedColorPickerTextResources.selectedEmpty = R.string.battery_empty_selected;
			sAdvancedColorPickerTextResources.selectedFull = R.string.battery_full_selected;
			sAdvancedColorPickerTextResources.selectedMiddle = R.string.battery_middle_selected;
			sAdvancedColorPickerTextResources.selectedPlugged = R.string.plugged_selected;
			sAdvancedColorPickerTextResources.selectedUnplugged = R.string.unplugged_selected;	
		}
		return sAdvancedColorPickerTextResources;
	}
	
}
