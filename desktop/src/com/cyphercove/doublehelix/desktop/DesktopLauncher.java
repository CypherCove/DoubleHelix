package com.cyphercove.doublehelix.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.cyphercove.lwptools.core.LiveWallpaperGameAdapter;
import com.cyphercove.doublehelix.MainRenderer;

public class DesktopLauncher {
    static final int MAX_FPS = 80;

	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.vSyncEnabled = true;
        config.r=8;
        config.g=8;
        config.b=8;
        config.a=8;
        config.width = 1280;
        config.height = 720;
        config.samples = 8;
		new LwjglApplication(new LiveWallpaperGameAdapter(new MainRenderer(), null, MAX_FPS), config);
	}
}
