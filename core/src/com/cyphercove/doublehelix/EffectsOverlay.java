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
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.cyphercove.covetools.graphics.GaussianBlur;

/**
 * Various overlay effects.
 */
public class EffectsOverlay implements Disposable {

    static final float FILM_GRAIN_PER_LONG_SIDE = 600;
    static final float SCAN_LINES_PER_INCH = 22;
    static final float FILM_GRAIN_BRIGHTNESS = 0.05f;
    static final float SCAN_LINE_BRIGHTNESS = 0.10f;
    static final float VIGNETTE_BRIGHTNESS = 1f;

    private Assets assets;
	private Vector2 filmGrainInverse = new Vector2();
	private Vector2 scanLineUVScale = new Vector2();

	private SpriteBatch batch;

    public EffectsOverlay(Assets assets) {
		this.assets = assets;
        batch = new SpriteBatch(1, null);
        batch.setProjectionMatrix(new Matrix4().idt());
    }

    @Override
    public void dispose() {
        batch.dispose();
    }

    public void resize(int width, int height) {
        if (width > height) {
            filmGrainInverse.x = FILM_GRAIN_PER_LONG_SIDE;
            filmGrainInverse.y = filmGrainInverse.x / (float) width * (float) height;
        } else {
            filmGrainInverse.y = FILM_GRAIN_PER_LONG_SIDE;
            filmGrainInverse.x = filmGrainInverse.y / (float) width * (float) height;
        }
        filmGrainInverse.scl(1f / assets.filmNoiseTexture.getWidth());

        float dpi = Gdx.graphics.getPpiX();
        float scanlineRepeatY = ((float) height) * SCAN_LINES_PER_INCH / dpi;
        float scale = scanlineRepeatY * (float)assets.scanLineTexture.getHeight() / height;
        float scanlineRepeatX = scale * width / (float)assets.scanLineTexture.getWidth();
        scanLineUVScale.set(scanlineRepeatX, scanlineRepeatY);
    }

    public void render(boolean withBloom, GaussianBlur bloom) {
        ShaderProgram shader = withBloom ? assets.effectsOverlayBloomShader : assets.effectsOverlayShader;
        batch.setShader(shader);
        batch.enableBlending();
        batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.begin();

        shader.setUniformf("u_color", Settings.filmGrain ? FILM_GRAIN_BRIGHTNESS : 0f,
                Settings.scanLines ? SCAN_LINE_BRIGHTNESS : 0f,
                Settings.vignette ? VIGNETTE_BRIGHTNESS : 0f,
                1f);
        shader.setUniformf("u_grainInverse", filmGrainInverse);
        shader.setUniformf("u_scanLineUVScale", scanLineUVScale);
        shader.setUniformf("u_grainOffset",
                MathUtils.random() * filmGrainInverse.x, MathUtils.random() * filmGrainInverse.y);
        shader.setUniformf("u_flicker", MathUtils.lerp(0.96f, 1f, MathUtils.random()));
        assets.filmNoiseTexture.bind(1);
        shader.setUniformi("u_noiseTexture", 1);
        assets.scanLineTexture.bind(2);
        shader.setUniformi("u_scanLineTexture", 2);
        if (withBloom) {
            bloom.getTexture().bind(3);
            shader.setUniformi("u_bloomTexture", 3);
        }
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);

        batch.draw(assets.filmBorderTexture, -1f, 1f, 2f, -2f);
        batch.end();
        batch.setShader(null);
    }

}
