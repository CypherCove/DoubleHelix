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
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;

public class UnlitShader implements Shader {
	Texture texture;
	ShaderProgram program;

	int u_texture, u_modelViewProjTrans;

	public UnlitShader(Texture texture){
		this.texture = texture;
	}

	@Override
	public void init() {
		reloadShader();
	}

    public void reloadShader (){
        if (program != null)
            program.dispose();

        final String prefix = "unlit";
        String vert = Gdx.files.internal(prefix + "_vs.glsl").readString();
        String frag = Gdx.files.internal(prefix + "_fs.glsl").readString();
        program = new ShaderProgram(vert, frag);
		if (!program.isCompiled())
			Gdx.app.log("Shader error", program.getLog());

        u_texture = program.getUniformLocation("u_texture");
        u_modelViewProjTrans = program.getUniformLocation("u_modelViewProjTrans");
    }

	@Override
	public void dispose() {
		program.dispose();
	}

	@Override
	public int compareTo(Shader other) {
		return 0;
	}

	@Override
	public boolean canRender(Renderable instance) {
		return true;
	}

	Matrix4 tmpMat = new Matrix4();
	Matrix4 viewProjTrans = new Matrix4();

	@Override
	public void begin(Camera camera, RenderContext context) {
        context.setDepthTest(GL20.GL_NONE);

		program.begin();
		viewProjTrans.set(camera.combined);

		context.setDepthTest(GL20.GL_LEQUAL);
		context.setCullFace(GL20.GL_BACK);
		context.setBlending(false, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		texture.bind(0);
		program.setUniformi(u_texture, 0);
	}


	@Override
	public void render(Renderable renderable) {
		tmpMat.set(renderable.worldTransform).mulLeft(viewProjTrans);
		program.setUniformMatrix(u_modelViewProjTrans, tmpMat);

		renderable.mesh.render(program,
				renderable.primitiveType,
				renderable.meshPartOffset,
				renderable.meshPartSize);
	}

	@Override
	public void end() {
		program.end();
	}
}
