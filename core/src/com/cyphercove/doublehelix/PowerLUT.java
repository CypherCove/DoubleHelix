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

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.badlogic.gdx.utils.Disposable;

public class PowerLUT implements Disposable {
	private Texture texture;
	
	/** W power will be in luminance, and H power will be in alpha**/
	public PowerLUT(float powerW, float intensityW, float powerH, float intensityH, int width, int height){

		Pixmap pixmap = new Pixmap(width, height, Format.RGBA8888);
		for (int i=0; i<width; i++){
			float valueW = (float)Math.pow((float)i/width, powerW) * intensityW;
            for (int j = 0; j < height; j++) {
                float valueH = (float)Math.pow((float)j/height, powerH) * intensityH;
                pixmap.setColor(valueW, valueH, 1.0f, 1.0f);
                pixmap.drawPixel(i, j);
            }
		}
		
		PixmapTextureData data = new PixmapTextureData(pixmap, Format.RGBA8888, false, false, true);
		
		texture = new Texture(data);
		texture.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
	}
	
	public Texture getTexture(){
		return texture;
	}


	@Override
	public void dispose() {
		texture.dispose();
		texture = null;
	}

}
