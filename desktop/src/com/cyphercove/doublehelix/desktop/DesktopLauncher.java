package com.cyphercove.doublehelix.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.cyphercove.covetools.android.DesktopLiveWallpaperWrapper;
import com.cyphercove.doublehelix.MainRenderer;

public class DesktopLauncher {

	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.useVsync(true);
		config.setBackBufferConfig(8, 8, 8, 8, 16, 0, 8);
		config.setWindowedMode(1280, 720);
		new Lwjgl3Application(new DesktopLiveWallpaperWrapper(new MainRenderer()), config);
	}

}
