package com.cyphercove.doublehelix;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.cyphercove.lwptools.core.LiveWallpaperGameAdapter;

public class AndroidLauncher extends AndroidApplication {
    static final int MAX_FPS = 80;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		initialize(new LiveWallpaperGameAdapter(new MainRenderer(), null, MAX_FPS), config);
	}
}
