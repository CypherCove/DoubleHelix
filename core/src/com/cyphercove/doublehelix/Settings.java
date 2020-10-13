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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.cyphercove.covetools.utils.ColorUtil;

/**
 * The user-adjustable settings. This is static to avoid having to pass objects back and forth
 * between the modules. It must only be modified from the OpenGL thread.
 */
public class Settings {
    public static boolean dof = true;
    public static boolean bloom = true;
    public static boolean filmGrain = true;
    public static boolean vignette = true;
    public static boolean scanLines = false;
    public static boolean pseudoScrolling = true;
    public static boolean pointParticles = false; //invisible in portrait if FOV_PORT > ~50
    public static boolean trilinearParticles = true;
    public static boolean chromaticAberration = true;
    public static int numParticles = 1000;
    public static float speed = 0.05f;
    public static boolean flipH = false, flipV = false;
    static final Color backgroundColor = new Color(0x0f3466ff);
    static final Color frontHelixColor = new Color(0x0e4c89ff);
    static final Color rearHelixColor = new Color(0x185a9dff);

    static {
        updateColorsFromFrontHelixColor();
    }

    private static int[] screenshotColors = {
            0x0e4c89ff, 0x982727ff, 0x549a50ff, 0x6d6d6dff, 0x681373ff, 0x3e5e38ff
    };

    private static int screenshotColorIdx = 0;
    public static void advanceScreenshotColor (){
        screenshotColorIdx = (screenshotColorIdx + 1) % screenshotColors.length;
        frontHelixColor.set(screenshotColors[screenshotColorIdx]);
        updateColorsFromFrontHelixColor();
    }

    public static void updateColorsFromFrontHelixColor(){
        backgroundColor.set(frontHelixColor);
        ColorUtil.shiftHue(backgroundColor, 0.007f);
        ColorUtil.scaleSaturation(backgroundColor, 1.03f);
        backgroundColor.mul(0.7f);

        rearHelixColor.set(frontHelixColor);
        ColorUtil.shiftHue(backgroundColor, 0.0035f);
        ColorUtil.scaleSaturation(rearHelixColor, 1.03f);
        ColorUtil.scaleValue(rearHelixColor, 1.2f);
    }

    private static int[][] screenshotResolutions = {
        {480, 800}, {800, 480}, {1280, 800}, {1024, 600}
    };
    private static int screenshotResolutionIdx = -1;
    public static void advanceScreenshotResolution (){
        screenshotResolutionIdx = (screenshotResolutionIdx + 1) % screenshotResolutions.length;
        Gdx.graphics.setWindowedMode(screenshotResolutions[screenshotResolutionIdx][0],
                screenshotResolutions[screenshotResolutionIdx][1]);
    }
}
