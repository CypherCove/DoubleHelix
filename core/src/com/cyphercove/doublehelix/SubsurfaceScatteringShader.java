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
import com.badlogic.gdx.math.Vector3;

public class SubsurfaceScatteringShader implements Shader {
	Texture texture;
	ShaderProgram program;

	Vector3 lightDirection;
	static final float SPECULAR_SHININESS = 20f;
    static final float SPECULAR_INTENSITY = 0.3f;
    static final float SUBSURFACE_DROPOFF = 20f;
    static final float SUBSURFACE_INTENSITY = 1f;

	PowerLUT specLUT;

	int u_cameraPosition, u_worldLightDir, u_texture, u_modelViewProjTrans, u_specularPowerLUTTexture, u_invWorldTrans, u_color;

    private boolean shouldFadeEnds = false;

	public SubsurfaceScatteringShader(Texture texture, Vector3 lightDirection){
		this.texture = texture;
		this.lightDirection = lightDirection;
	}

	@Override
	public void init() {
		reloadShader();

		specLUT = new PowerLUT(SPECULAR_SHININESS, SPECULAR_INTENSITY, SUBSURFACE_DROPOFF, SUBSURFACE_INTENSITY, 512, 512);
	}

    public void reloadShader (){
        if (program != null)
            program.dispose();

        final String prefix = "sss";
        String vert = Gdx.files.internal(prefix + "_vs.glsl").readString();
        String frag = Gdx.files.internal(prefix + "_fs.glsl").readString();
        program = new ShaderProgram(vert, frag);
        if (!program.isCompiled())
            Gdx.app.log("Shader error", program.getLog());

        u_cameraPosition = program.getUniformLocation("u_cameraPosition");
        u_worldLightDir = program.getUniformLocation("u_worldLightDir");
        u_texture = program.getUniformLocation("u_texture");
        u_specularPowerLUTTexture = program.getUniformLocation("u_specularPowerLUTTexture");
        u_modelViewProjTrans = program.getUniformLocation("u_modelViewProjTrans");
        u_invWorldTrans = program.getUniformLocation("u_invWorldTrans");
        u_color = program.getUniformLocation("u_color");
    }

	@Override
	public void dispose() {
		program.dispose();
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


	Matrix4 tmpMat = new Matrix4();
	Matrix4 viewProjTrans = new Matrix4();

	@Override
	public void begin(Camera camera, RenderContext context) {
		program.begin();
		viewProjTrans.set(camera.combined);

		program.setUniformf(u_cameraPosition, camera.position.x, camera.position.y, 
				camera.position.z, 1);
		program.setUniformf(u_worldLightDir, lightDirection.x, lightDirection.y, lightDirection.z, 0);

		context.setDepthTest(GL20.GL_LEQUAL);
		context.setCullFace(GL20.GL_BACK);
		context.setBlending(shouldFadeEnds ? true : false, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		texture.bind(0);
		program.setUniformi(u_texture, 0);

		specLUT.getTexture().bind(1);
		program.setUniformi(u_specularPowerLUTTexture, 1);

	}


	@Override
	public void render(Renderable renderable) {
		tmpMat.set(renderable.worldTransform).mulLeft(viewProjTrans);
		program.setUniformMatrix(u_modelViewProjTrans, tmpMat);
        Color color = (Color) renderable.userData;
        program.setUniformf(u_color, color.r, color.g, color.b);

		tmpMat.set(renderable.worldTransform).inv();
		program.setUniformMatrix(u_invWorldTrans, tmpMat);
		renderable.mesh.render(program,
				renderable.primitiveType,
				renderable.meshPartOffset,
				renderable.meshPartSize);
	}

	@Override
	public void end() {
		program.end();
	}

    public void setShouldFadeEnds(boolean shouldFadeEnds) {
        this.shouldFadeEnds = shouldFadeEnds;
    }
}
