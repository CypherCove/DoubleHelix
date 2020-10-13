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

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.cyphercove.covetools.android.LiveWallpaperAdapter;
import com.cyphercove.covetools.graphics.FullScreenFader;
import com.cyphercove.covetools.graphics.FullScreenQuad;
import com.cyphercove.covetools.graphics.GaussianBlur;
import com.cyphercove.covetools.graphics.ResizableFrameBuffer;
import com.cyphercove.doublehelix.points.BillboardDecalBatch;

/**
 * Main libGDX entry point for the live wallpaper. It uses CoveTools' LiveWallpaperAdapter to
 * support desktop testing of the live wallpaper. The input listener allows toggling of settings and
 * hot reloading of shaders using the keyboard.
 */
public class MainRenderer extends LiveWallpaperAdapter {

    public static final float FOV_LAND = 37.7f;
    public static final float FOV_PORT = 55f;
    private static final float MAX_FRAME_TIME = 0.15f;

    private static final float MAIN_HELIX_ROTATION_DPS = 60F;
    static final float AMBIENT_BRIGHTNESS = 0.2f;

    private boolean isPreview = false;

    final Vector3 camPosition = new Vector3();
    final Color tmpColor = new Color();

    public DepthOfFieldCamera cam;
    ModelBatch modelBatch;
    public Environment environment;
    Assets assets;
    AssetManager assetManager;

    SubsurfaceScatteringShader sssShader;
    ModelInstance mainHelixModelInstance;
    TransformManager mainHelixTransformManager;
    ModelInstance rearHelixModelInstance;
    TransformManager rearHelixTransformManager;

    ModelInstance backgroundModelInstance;
    BackgroundShader backgroundShader;
    ModelInstance backgroundBloomModelInstance;
    UnlitShader unlitShader;
    BlackShader blackShader;

    TextureRegion particleATextureRegion;
    Particles particles;
    DecalBatch decalBatch;
    BillboardDecalBatch billboardDecalBatch;
    ParticleGroupStrategy particleGroupStrategy;

    GaussianBlur bloom;
    GaussianBlur rearDOFBlur;
    ResizableFrameBuffer chromaticBuffer;
    FullScreenQuad chromaticQuad;

    EffectsOverlay effectsOverlay;

    FullScreenFader fader;
    boolean needFinishCreate; //used to postpone loading so daydream gets nice pre-fade
    boolean firstFrameDrawn;

    boolean screenshotPause = false;

    Array<Disposable> disposables = new Array<Disposable>(15);
    public interface SettingsAdapter {
        void updateAllSettings();
        void updateInLoop(float deltaTime);
    }
    SettingsAdapter settingsAdapter;

    public MainRenderer (){
        settingsAdapter = null;
    }

    public MainRenderer (SettingsAdapter settingsAdapter){
        this.settingsAdapter = settingsAdapter;
    }

    @Override
	public void create () {
        if (settingsAdapter != null) settingsAdapter.updateAllSettings();

        fader = new FullScreenFader(0, 1.5f);
        disposables.add(fader);

        bloom = new GaussianBlur(8f, false, true);
        bloom.setDepthTestingToScene(false);
        bloom.setBlending(true, GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
        disposables.add(bloom);

        rearDOFBlur = new GaussianBlur(8f, true, true);
        rearDOFBlur.setDepthTestingToScene(true);
        rearDOFBlur.setBlending(false, 0, 0);
        disposables.add(rearDOFBlur);

        cam = new DepthOfFieldCamera(0.10f, 3.5f, 1f, 28f);

        needFinishCreate = true;
        firstFrameDrawn = false;

        Gdx.gl.glEnable(GL20.GL_VERTEX_PROGRAM_POINT_SIZE);
        if(Gdx.app.getType() == Application.ApplicationType.Desktop) {
            Gdx.gl.glEnable(0x8861); // GL_POINT_OES
        }
    }

    private void finishCreate(){
        needFinishCreate = false;

        modelBatch = new ModelBatch();
        disposables.add(modelBatch);

        environment = new Environment();

        assets = new Assets();
        disposables.add(assets);

        assetManager = new AssetManager();
        disposables.add(assetManager);

        assetManager.finishLoading();

        mainHelixModelInstance = new ModelInstance(assets.helixModel);
        mainHelixModelInstance.userData = Settings.frontHelixColor;
        mainHelixTransformManager = new TransformManager(mainHelixModelInstance);
        rearHelixModelInstance = new ModelInstance(assets.helixModel);
        rearHelixModelInstance.userData = Settings.rearHelixColor;
        rearHelixTransformManager = new TransformManager(rearHelixModelInstance);
        rearHelixTransformManager.eulers.set(12.477f, -96.843f, -7.902f);
        rearHelixTransformManager.position.set(7.95135f,-5.46558f, 9.82413f);

        backgroundModelInstance = new ModelInstance(assets.backgroundModel);
        backgroundBloomModelInstance = new ModelInstance(assets.backgroundBloomModel);

        sssShader = new SubsurfaceScatteringShader(assets, (new Vector3(-0.6f, 1f, 1f)).nor());
        sssShader.init();
        disposables.add(sssShader);

        backgroundShader = new BackgroundShader(assets);
        backgroundShader.init();
        disposables.add(backgroundShader);

        unlitShader = new UnlitShader(assets);
        unlitShader.init();
        disposables.add(unlitShader);

        blackShader = new BlackShader(assets);
        blackShader.init();
        disposables.add(blackShader);

        particleATextureRegion = new TextureRegion(assets.particleATexture);

        particles = new Particles(assets.pointParticleTexture, particleATextureRegion, cam);

        FlippingInputMultiplexer inputMultiplexer = new FlippingInputMultiplexer(inputAdapter, particles.inputAdapter);
        Gdx.input.setInputProcessor(inputMultiplexer);

        particleGroupStrategy = new ParticleGroupStrategy(cam, assets);
        disposables.add(particleGroupStrategy);

        decalBatch = new DecalBatch(Particles.MAX_PARTICLES, particleGroupStrategy);
        disposables.add(decalBatch);

        billboardDecalBatch = new BillboardDecalBatch(Particles.MAX_PARTICLES, particleGroupStrategy);
        disposables.add(billboardDecalBatch);

        effectsOverlay = new EffectsOverlay(assets);
        disposables.add(effectsOverlay);

        chromaticBuffer =
                new ResizableFrameBuffer(Pixmap.Format.RGBA8888, true, false, true);
        disposables.add(chromaticBuffer);

        chromaticQuad = new FullScreenQuad();
        chromaticQuad.setBlending(false);
        disposables.add(chromaticQuad);
	}

    @Override
    public void dispose() {
        for (Disposable disposable : disposables) disposable.dispose();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void resize(int width, int height) {
        cam.viewportWidth = width;
        cam.viewportHeight = height;
        cam.position.set(0, 0, 0);
        cam.near = .05f;
        cam.far = 100f;
        cam.fieldOfView = (width>height)?FOV_LAND : FOV_PORT;
        cam.update();

        bloom.resize(512, width, height);
        rearDOFBlur.resize(200, width, height);

        if (particles != null) particles.setInputAdapterResolution(); //just in case

        if (effectsOverlay !=null)
            effectsOverlay.resize(width, height);

    }

    @Override
    public void render(float xOffset, float yOffset, float xOffsetLooping, float xOffsetFake) {
        float deltaTime = Math.min(Gdx.graphics.getDeltaTime(), MAX_FRAME_TIME);
        if (screenshotPause)
            deltaTime = 0;

        float _xOffset = isPreview? 0.5f :
                (Settings.pseudoScrolling? xOffsetFake : xOffsetLooping);

        if (Settings.flipH)
            _xOffset = 1f - _xOffset;

        GL20 gl = Gdx.gl;

        // Need to set winding order according to flipped camera
        gl.glFrontFace(Settings.flipH != Settings.flipV ? GL20.GL_CW : GL20.GL_CCW);

        if (!firstFrameDrawn){
            gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            gl.glClearColor(0, 0, 0, 1);
            gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
            firstFrameDrawn = true;
            return;
        }

        if (needFinishCreate){
            finishCreate();
            //film grain wasn't created til now so need to initialize its size
            effectsOverlay.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }

        if (settingsAdapter != null) settingsAdapter.updateInLoop(deltaTime);

        //UPDATES
        Texture.TextureFilter particleMinFilter = Settings.trilinearParticles ?
                Texture.TextureFilter.MipMapLinearLinear : Texture.TextureFilter.MipMapLinearNearest;
        assets.particleATexture.setFilter(particleMinFilter, Texture.TextureFilter.Linear);
        assets.pointParticleTexture.setFilter(particleMinFilter, Texture.TextureFilter.Linear);

        mainHelixTransformManager.localEulers.z += deltaTime * MAIN_HELIX_ROTATION_DPS * Settings.speed;
        mainHelixTransformManager.apply();
        rearHelixTransformManager.localEulers.z += deltaTime * 0.5f * MAIN_HELIX_ROTATION_DPS * Settings.speed;
        rearHelixTransformManager.apply();

        cam.up.set(0, 1, 0);
        cam.direction.set(0, 0, -1);
        cam.rotate(-41.998f, 1, 0, 0);
        cam.rotate(146.964f, 0, 1, 0);
        cam.rotate(18.503f, 0, 0, 1);

        camPosition.set(0.45617f, 4.11359f, 0.14467f); //set it here and reapply when needed.
        if (cam.viewportWidth > cam.viewportHeight){//landscape
            camPosition.rotate(Vector3.Z, -(_xOffset - 0.5f) * 47.5f - 15.0f);
        } else {//portrait
            camPosition.rotate(Vector3.Z, -(_xOffset - 0.5f) * 65f);
        }
        cam.position.set(camPosition); //temporarily apply it to get correct direction with lookAt
        cam.lookAt(-.4703f, 0f, 3.5032f);

        sssShader.getLightDirection().set(1, 0, 0).rotate(Vector3.Y, lightH).rotate(Vector3.X, lightV);

        particles.update(deltaTime, sssShader.getLightDirection()); //MUST BE DONE WHILE CAM IS IN POSITION

        //BLOOM SURFACE DRAWING
        if (Settings.bloom) {
            bloom.begin();
            Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            cam.position.set(0, 0, 0);
            cam.update();
            modelBatch.begin(cam);
            modelBatch.render(backgroundBloomModelInstance, unlitShader);
            modelBatch.end();

            cam.position.set(camPosition);
            cam.update();
            modelBatch.begin(cam);
            modelBatch.render(mainHelixModelInstance, blackShader);
            modelBatch.render(rearHelixModelInstance, blackShader);
            modelBatch.end();

            bloom.end();
        }

        //BLUR SURFACE DRAWING
        if (Settings.dof) {
            rearDOFBlur.begin();
            tmpColor.set(Settings.backgroundColor).mul(AMBIENT_BRIGHTNESS);
            Gdx.gl.glClearColor(tmpColor.r, tmpColor.g, tmpColor.b, tmpColor.a);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

            drawBackground();
            drawFar();
            rearDOFBlur.end();
        }

        //FOREGROUND SURFACE DRAWING
        if (Settings.chromaticAberration) {
            // Resize on demand so FrameBuffer is only created if setting is on.
            int surfaceWidth, surfaceHeight;
            if (Gdx.graphics.getWidth() > Gdx.graphics.getHeight()) {
                surfaceWidth = Math.min(Gdx.graphics.getWidth(), 2048);
                surfaceHeight = (int)(((float)surfaceWidth) / (float)Gdx.graphics.getWidth() * (float)Gdx.graphics.getHeight());
            } else {
                surfaceHeight = Math.min(Gdx.graphics.getHeight(), 2048);
                surfaceWidth = (int)(((float)surfaceHeight) / (float)Gdx.graphics.getHeight() * (float)Gdx.graphics.getWidth());
            }
            chromaticBuffer.resize(surfaceWidth, surfaceHeight);

            chromaticBuffer.getCurrent().begin();
            // Black empty clear color. Result will be drawn to screen with pre-multiplied alpha.
            Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        }

        //MAIN DRAWING
        Gdx.gl.glClearColor(Settings.backgroundColor.r, Settings.backgroundColor.g,
                Settings.backgroundColor.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        if (Settings.dof) {
            rearDOFBlur.render();
        } else {
            drawFar();
        }

        drawNear();

        if (!Settings.dof) {
            drawFar();
        }
        boolean doFilmGrain = Settings.filmGrain || Settings.scanLines || Settings.vignette;

        if (doFilmGrain) {
            effectsOverlay.render(Settings.bloom, bloom);
        } else if (Settings.bloom) {
            bloom.beginRender(assets.bloomShader);
            bloom.finishRender();
        }

        if (Settings.chromaticAberration) {
            chromaticBuffer.getCurrent().end();
            ShaderProgram program = assets.chromaticAberrationShader;
            program.bind();
            program.setUniformi("u_texture", 0);
            chromaticBuffer.getCurrent().getColorBufferTexture().bind(0);
            chromaticQuad.render(assets.chromaticAberrationShader);
        }


        fader.render(deltaTime);

    }

    private void drawBackground() {
        cam.position.set(0f, 0f, 0f);
        cam.update();
        modelBatch.begin(cam);
        modelBatch.render(backgroundModelInstance, backgroundShader);
        modelBatch.end();
    }

    private void drawFar() {
        cam.position.set(camPosition);
        cam.update();
        sssShader.setShouldFadeEnds(true);
        modelBatch.begin(cam);
        modelBatch.render(rearHelixModelInstance, sssShader);
        modelBatch.end();
    }

    private void drawNear() {
        cam.position.set(camPosition);
        cam.update();
        sssShader.setShouldFadeEnds(false);
        modelBatch.begin(cam);
        modelBatch.render(mainHelixModelInstance, sssShader);
        modelBatch.end();
        particles.draw(decalBatch, billboardDecalBatch);
    }

    float lightH = -90f, lightV = 38f;
    float baseLightH, baseLightV;
    int baseX, baseY;
    static final float DEG_PER_PIXEL = 0.5f;

    private InputAdapter inputAdapter = new InputAdapter(){
        boolean lightMode;

        public boolean keyDown (int keycode) {
            switch (keycode) {
                case Input.Keys.R:
                    assets.reloadShaders();
                    break;
                case Input.Keys.P:
                    screenshotPause = ! screenshotPause;
                    break;
                case Input.Keys.SPACE:
                    Settings.advanceScreenshotResolution();
                    break;
                case Input.Keys.C:
                    Settings.advanceScreenshotColor();
                    break;
                case Input.Keys.V:
                    Settings.vignette = !Settings.vignette;
                    break;
                case Input.Keys.S:
                    Settings.scanLines = !Settings.scanLines;
                    break;
                case Input.Keys.F:
                    Settings.filmGrain = !Settings.filmGrain;
                    break;
                case Input.Keys.B:
                    Settings.bloom = ! Settings.bloom;
                    break;
                case Input.Keys.A:
                    Settings.chromaticAberration = !Settings.chromaticAberration;
                    break;
                case Input.Keys.K:
                    Settings.flipH = !Settings.flipH;
                    break;
                case Input.Keys.L:
                    Settings.flipV = !Settings.flipV;
                    break;
            }
            return false;
        }

        public boolean touchDown (int screenX, int screenY, int pointer, int button) {
            baseLightH = lightH;
            baseLightV = lightV;
            baseX = screenX;
            baseY = screenY;
            lightMode = Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT);
            return false;
        }

        public boolean touchUp (int screenX, int screenY, int pointer, int button) {
            return false;
        }

        public boolean touchDragged (int screenX, int screenY, int pointer) {
            if (lightMode){
                lightH = baseLightH + (screenX - baseX) * DEG_PER_PIXEL;
                lightV = baseLightV - (screenY - baseY) * DEG_PER_PIXEL;
            }
            return false;
        }
    };

    @Override
    public void onSettingsChanged() {
        if (settingsAdapter != null) settingsAdapter.updateAllSettings();
    }

    @Override
    public void onPreviewStateChange(boolean isPreview) {
        this.isPreview = isPreview;
    }

}
