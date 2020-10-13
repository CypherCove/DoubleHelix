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
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;

/**
 * Used to render the background ("skybox") for generating bloom.
 */
public class UnlitShader implements Shader {

	private Assets assets;
	private final Matrix4 tmpMat = new Matrix4();
	private final Matrix4 viewProjTrans = new Matrix4();

	public UnlitShader(Assets assets){
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

        ShaderProgram program = assets.unlitShader;

		program.bind();
		viewProjTrans.set(camera.combined);

		context.setDepthTest(GL20.GL_LEQUAL);
		context.setCullFace(GL20.GL_BACK);
		context.setBlending(false, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		assets.backgroundBloomTexture.bind(0);
		program.setUniformi("u_texture", 0);
	}


	@Override
	public void render(Renderable renderable) {
		tmpMat.set(renderable.worldTransform).mulLeft(viewProjTrans);
		assets.unlitShader.setUniformMatrix("u_modelViewProjTrans", tmpMat);
		renderable.meshPart.render(assets.unlitShader);
	}

	@Override
	public void end() {
	}
}
