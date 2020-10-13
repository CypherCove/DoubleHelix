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

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.math.Matrix4;

/**
 * Used to draw a model pure black (the silhouette used for generating bloom lighting).
 */
public class BlackShader implements Shader {
	private Assets assets;
	private final Matrix4 tmpMat = new Matrix4();
	private final Matrix4 viewProjTrans = new Matrix4();

	public BlackShader(Assets assets) {
		this.assets = assets;
	}

	@Override
	public void init() {
	}

	@Override
	public void dispose() {
	}

	@Override
	public int compareTo(Shader other) {
		return 0;
	}

	@Override
	public boolean canRender(Renderable instance) {
		return true;
	}



	@Override
	public void begin(Camera camera, RenderContext context) {
        context.setDepthTest(GL20.GL_NONE);

		assets.blackShader.bind();
		viewProjTrans.set(camera.combined);

		context.setCullFace(GL20.GL_BACK);
		context.setBlending(false, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

	}


	@Override
	public void render(Renderable renderable) {
		tmpMat.set(renderable.worldTransform).mulLeft(viewProjTrans);
		assets.blackShader.setUniformMatrix("u_modelViewProjTrans", tmpMat);
		renderable.meshPart.render(assets.blackShader);
	}

	@Override
	public void end() {
	}
}
