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
import android.content.Intent;

import com.cyphercove.lwptools.core.MultiTapListener;

public class TripleTapSettingsListener implements MultiTapListener {
	Context context;
	public TripleTapSettingsListener(Context context){
		this.context=context;
	}
	@Override
	public void onDoubleTap() {
		
	}
	@Override
	public void onTripleTap() {
		if (SettingsPrefsAdapter.tripleTapSettings){
			Intent i = new Intent(context,PrefsActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(i);
		}
	}
}
