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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

/**
 * The shader used to draw the models with a translucent appearance. The approach is based on a
 * <a href="https://colinbarrebrisebois.com/2011/03/07/gdc-2011-approximating-translucency-for-a-fast-cheap-and-convincing-subsurface-scattering-look/">GDC presentation from DICE.</a>
 * There is an additional optimization. The local thickness map is omitted, and instead the shader
 * compares the axis of the local part of the helix model to the camera direction.
 */
public class SubsurfaceScatteringShader implements Shader {

	static final float SPECULAR_SHININESS = 20f;
    static final float SPECULAR_INTENSITY = 0.3f;
    static final float SUBSURFACE_DROPOFF = 20f;
    static final float SUBSURFACE_INTENSITY = 1f;

	private Assets assets;
	private PowerLUT specLUT;
	private Vector3 lightDirection;
	private final Matrix4 tmpMat = new Matrix4();
	private final Matrix4 viewProjTrans = new Matrix4();

    private boolean shouldFadeEnds = false;

	public SubsurfaceScatteringShader(Assets assets, Vector3 lightDirection){
		this.assets = assets;
		this.lightDirection = lightDirection;
	}

	@Override
	public void init() {
		specLUT = new PowerLUT(SPECULAR_SHININESS, SPECULAR_INTENSITY, SUBSURFACE_DROPOFF, SUBSURFACE_INTENSITY, 512, 512);
	}

	@Override
	public void dispose() {
		specLUT.dispose();
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
		ShaderProgram program = assets.subsurfaceScatteringShader;
		program.bind();
		viewProjTrans.set(camera.combined);

		program.setUniformf("u_cameraPosition", camera.position.x, camera.position.y,
				camera.position.z, 1);
		program.setUniformf("u_worldLightDir", lightDirection.x, lightDirection.y, lightDirection.z, 0);

		context.setDepthTest(GL20.GL_LEQUAL);
		context.setCullFace(GL20.GL_BACK);
		context.setBlending(shouldFadeEnds, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		assets.helixNormalAOTexture.bind(0);
		program.setUniformi("u_texture", 0);

		specLUT.getTexture().bind(1);
		program.setUniformi("u_specularPowerLUTTexture", 1);

	}

	@Override
	public void render(Renderable renderable) {
		ShaderProgram program = assets.subsurfaceScatteringShader;
		tmpMat.set(renderable.worldTransform).mulLeft(viewProjTrans);
		program.setUniformMatrix("u_modelViewProjTrans", tmpMat);
        Color color = (Color) renderable.userData;
        program.setUniformf("u_color", color.r, color.g, color.b);

		tmpMat.set(renderable.worldTransform).inv();
		program.setUniformMatrix("u_invWorldTrans", tmpMat);
		renderable.meshPart.render(program);
	}

	@Override
	public void end() {
	}

    public void setShouldFadeEnds(boolean shouldFadeEnds) {
        this.shouldFadeEnds = shouldFadeEnds;
    }

	public Vector3 getLightDirection() {
		return lightDirection;
	}

}
