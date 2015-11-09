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
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.cyphercove.lwptools.core.GaussianBlur;

import java.util.Random;

public class FilmGrain implements Disposable, GaussianBlur.CustomShaderPreparer {

	static final float FILM_GRAIN_PER_LONG_SIDE = 600;
	static final float SCANLINE_PER_INCH = 100;
	static final float FILM_GRAIN_BRIGHTNESS = 0.05f;
	static final float SCANLINE_BRIGHTNESS = 0.10f;
	static final float VIGNETTE_BRIGHTNESS = 1f;

	Texture noiseTexture;
	Texture borderTexture;
	Texture scanlineTexture;
	ShaderProgram filmGrainShaderProgram;
	Random mRandom = new Random();
	Vector2 mFilmGrainInverse = new Vector2();
	Vector2 mScanLineInverse = new Vector2();

	SpriteBatch batch;
    boolean forBloom;

	int u_grainInverse, u_grainOffset, u_flicker, u_noiseTexture, u_scanLineTexture, u_scanLineInverse, u_borderTexture, u_flipped;

	public FilmGrain(Texture noiseTexture, Texture borderTexture, Texture scanlineTexture, boolean forBloom){
        this.forBloom = forBloom;

		this.noiseTexture = noiseTexture;
		this.borderTexture = borderTexture;
		this.scanlineTexture = scanlineTexture;


        String prefix = forBloom ? "filmgrain_bloom" : "filmgrain";

		String vertexShaderSrc = Gdx.files.internal(prefix + "_vs.glsl").readString();
		String fragmentShaderSrc = Gdx.files.internal(prefix + "_fs.glsl").readString();
		filmGrainShaderProgram = new ShaderProgram(vertexShaderSrc, fragmentShaderSrc);

        if (!forBloom) {
            batch = new SpriteBatch(1, filmGrainShaderProgram);
            batch.setProjectionMatrix(new Matrix4().idt());
        }

		u_grainInverse = filmGrainShaderProgram.getUniformLocation("u_grainInverse");
		u_grainOffset = filmGrainShaderProgram.getUniformLocation("u_grainOffset");
		u_flicker = filmGrainShaderProgram.getUniformLocation("u_flicker");
		u_noiseTexture = filmGrainShaderProgram.getUniformLocation("u_noiseTexture");
		u_scanLineTexture = filmGrainShaderProgram.getUniformLocation("u_scanLineTexture");
        u_scanLineInverse = filmGrainShaderProgram.getUniformLocation("u_scanLineInverse");
        if (forBloom) {
            u_borderTexture = filmGrainShaderProgram.getUniformLocation("u_borderTexture");
            u_flipped = filmGrainShaderProgram.getUniformLocation("u_flipped");
        }
	}

	@Override
	public void dispose() {
		filmGrainShaderProgram.dispose();
        if (batch != null)
            batch.dispose();
	}

	public void resize(int width, int height) {

		if (width>height){
			mFilmGrainInverse.x = FILM_GRAIN_PER_LONG_SIDE;
			mFilmGrainInverse.y = mFilmGrainInverse.x/(float)width*(float)height;
		} else {
			mFilmGrainInverse.y = FILM_GRAIN_PER_LONG_SIDE;
			mFilmGrainInverse.x = mFilmGrainInverse.y/(float)width*(float)height;
		}
        mFilmGrainInverse.scl(1f/noiseTexture.getWidth());

        float dpi = Gdx.graphics.getPpiX();
        float scanlinePixelsWide = ((float)width)* SCANLINE_PER_INCH/dpi;
        float scanlinePixelsTall = ((float)height)* SCANLINE_PER_INCH/dpi;
		mScanLineInverse.set(
                scanlinePixelsWide/scanlineTexture.getWidth(),
                scanlinePixelsTall/scanlineTexture.getHeight());
	}

	public void render(){
		batch.enableBlending();
		batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
		batch.begin();
		batch.setColor(
				Settings.filmGrain ? FILM_GRAIN_BRIGHTNESS : 0,
						Settings.scanLines ? SCANLINE_BRIGHTNESS : 0,
								Settings.vignette ? VIGNETTE_BRIGHTNESS : 0,
										1
				);
		filmGrainShaderProgram.setUniformf(u_grainInverse, mFilmGrainInverse);
		filmGrainShaderProgram.setUniformf(u_scanLineInverse, mScanLineInverse);
		filmGrainShaderProgram.setUniformf(u_grainOffset,
                mRandom.nextFloat() * mFilmGrainInverse.x, mRandom.nextFloat() * mFilmGrainInverse.y);
		filmGrainShaderProgram.setUniformf(u_flicker, lerp(0.96f, 1f, mRandom.nextFloat()));
		noiseTexture.bind(1);
		scanlineTexture.bind(2);
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
		filmGrainShaderProgram.setUniformi(u_noiseTexture, 1);
		filmGrainShaderProgram.setUniformi(u_scanLineTexture, 2);
		batch.draw(borderTexture, -1, -1, 2, 2);
		batch.end();
	}

	static final float lerp (float one, float two, float position){
		return (two-one)*position + one;
	}

    //For use with GaussianBlur bloom
    @Override
    public void applyCustomShaderParameters(SpriteBatch spriteBatch, boolean flipped) {
        spriteBatch.setColor(
                Settings.filmGrain ? FILM_GRAIN_BRIGHTNESS : 0,
                Settings.scanLines ? SCANLINE_BRIGHTNESS : 0,
                Settings.vignette ? VIGNETTE_BRIGHTNESS : 0,
                1
        );
        if (flipped){
            swapVector2(mFilmGrainInverse);
        }
        filmGrainShaderProgram.setUniformf(u_grainInverse, mFilmGrainInverse);
        filmGrainShaderProgram.setUniformf(u_scanLineInverse, mScanLineInverse);
        filmGrainShaderProgram.setUniformf(u_flipped, flipped ? 1f : 0f);
        filmGrainShaderProgram.setUniformf(u_grainOffset,
                mRandom.nextFloat() * mFilmGrainInverse.x, mRandom.nextFloat() * mFilmGrainInverse.y);
        filmGrainShaderProgram.setUniformf(u_flicker, lerp(0.96f, 1f, mRandom.nextFloat()));
        noiseTexture.bind(1);
        scanlineTexture.bind(2);
        borderTexture.bind(3);
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
        filmGrainShaderProgram.setUniformi(u_noiseTexture, 1);
        filmGrainShaderProgram.setUniformi(u_scanLineTexture, 2);
        filmGrainShaderProgram.setUniformi(u_borderTexture, 3);
        if (flipped){
            swapVector2(mFilmGrainInverse);
        }
    }

    static void swapVector2 (Vector2 vector){
        vector.set(vector.y, vector.x);
    }
}
