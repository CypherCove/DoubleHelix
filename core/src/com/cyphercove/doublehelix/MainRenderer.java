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
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.cyphercove.doublehelix.points.BillboardDecalBatch;
import com.cyphercove.lwptools.core.FullScreenFader;
import com.cyphercove.lwptools.core.GaussianBlur;
import com.cyphercove.lwptools.core.GaussianBlurShaderProvider;
import com.cyphercove.lwptools.core.LiveWallpaperBaseRenderer;

public class MainRenderer implements LiveWallpaperBaseRenderer {

    private static final String HELIX_MODEL = "helix.g3db";
    private static final String HELIX_NORMAL_AO_TEXTURE = "helix-normal-ao.png";
    private static final String BG_MODEL = "bg.g3db";
    private static final String BG_BLOOM_MODEL = "bg_bloom.g3db";
    private static final String BG_GLOW_TEXTURE = "bg.png";
    private static final String BG_BLOOM_TEXTURE = "bloom_source.png";
    private static final String POINT_PARTICLE_TEXTURE = "particlePoint.png";
    private static final String PARTICLE_A_TEXTURE = "particleA.png";
    private static final String FILM_BORDER_TEXTURE= "filmBorder.png";
    private static final String FILM_NOISE_TEXTURE= "filmNoise.png";
    private static final String SCANLINE_TEXTURE= "scanlines.png";

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
    AssetManager assets;

    SubsurfaceScatteringShader sssShader;
    Texture helixNormalAOTexture;
    ModelInstance mainHelixModelInstance;
    TransformManager mainHelixTransformManager;
    ModelInstance rearHelixModelInstance;
    TransformManager rearHelixTransformManager;

    ModelInstance backgroundModelInstance;
    BackgroundShader backgroundShader;
    Texture backgroundTexture;
    ModelInstance backgroundBloomModelInstance;
    UnlitShader unlitShader;
    Texture backgroundBloomTexture;
    BlackShader blackShader;
    ShaderProgram bloomShaderProgram;

    Texture particleATexture;
    Texture particlePointTexture;
    TextureRegion particleATextureRegion;
    Particles particles;
    DecalBatch decalBatch;
    BillboardDecalBatch billboardDecalBatch;
    ParticleGroupStrategy particleGroupStrategy;

    GaussianBlur bloom;
    GaussianBlur rearDOFBlur;

    FilmGrain filmGrain;
    FilmGrain filmGrainForBloom;

    FullScreenFader fader;
    boolean needFinishCreate; //used to postpone loading so daydream gets nice pre-fade
    boolean firstFrameDrawn;

    boolean screenshotPause = false;

    Array<Disposable> disposables = new Array<Disposable>(15);
    public interface SettingsAdapter{
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

        GaussianBlurShaderProvider gaussianBlurShaderProvider = new GaussianBlurShaderProvider();

        bloom = new GaussianBlur(8f, false, true, gaussianBlurShaderProvider);
        bloom.setClearColor(Color.BLACK);
        bloom.setDepthTestingToScene(false);
        bloom.setBlending(true, GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
        disposables.add(bloom);

        rearDOFBlur = new GaussianBlur(8f, true, true, gaussianBlurShaderProvider);
        rearDOFBlur.setDepthTestingToScene(true);
        rearDOFBlur.setBlending(false, 0, 0);
        disposables.add(rearDOFBlur);

        cam = new DepthOfFieldCamera(0.10f, 3.5f, 1f, 28f);

        needFinishCreate = true;
        firstFrameDrawn = false;

        //Enable point rendering for use when flake particles setting is off.
        Gdx.gl.glEnable(GL20.GL_VERTEX_PROGRAM_POINT_SIZE);
        if(Gdx.app.getType() == Application.ApplicationType.Desktop) {
            Gdx.gl.glEnable(0x8861); // GL_POINT_OES
        }
    }

    //Defer most loading until first frame is drawn so it can quickly start when used as a daydream
    private void finishCreate(){
        needFinishCreate = false;

        modelBatch = new ModelBatch();
        disposables.add(modelBatch);

        environment = new Environment();

        assets = new AssetManager();
        disposables.add(assets);

        TextureLoader.TextureParameter trilinearRepeatTextureParams = new TextureLoader.TextureParameter();
        trilinearRepeatTextureParams.format = Pixmap.Format.RGBA8888;
        trilinearRepeatTextureParams.magFilter = Texture.TextureFilter.Linear;
        trilinearRepeatTextureParams.minFilter = Texture.TextureFilter.MipMapLinearLinear;
        trilinearRepeatTextureParams.wrapU = Texture.TextureWrap.Repeat;
        trilinearRepeatTextureParams.wrapV = Texture.TextureWrap.Repeat;
        trilinearRepeatTextureParams.genMipMaps = true;
        assets.load(HELIX_NORMAL_AO_TEXTURE, Texture.class, trilinearRepeatTextureParams);
        assets.load(PARTICLE_A_TEXTURE, Texture.class, trilinearRepeatTextureParams);

        TextureLoader.TextureParameter bilinearClampedTextureParams = new TextureLoader.TextureParameter();
        bilinearClampedTextureParams.format = Pixmap.Format.RGBA8888;
        bilinearClampedTextureParams.magFilter = Texture.TextureFilter.Linear;
        bilinearClampedTextureParams.minFilter = Texture.TextureFilter.MipMapLinearNearest;
        bilinearClampedTextureParams.wrapU = Texture.TextureWrap.ClampToEdge;
        bilinearClampedTextureParams.wrapV = Texture.TextureWrap.ClampToEdge;
        bilinearClampedTextureParams.genMipMaps = true;
        assets.load(BG_GLOW_TEXTURE, Texture.class, bilinearClampedTextureParams);
        assets.load(BG_BLOOM_TEXTURE, Texture.class, bilinearClampedTextureParams);
        assets.load(POINT_PARTICLE_TEXTURE, Texture.class, bilinearClampedTextureParams);

        TextureLoader.TextureParameter borderTexParams = new TextureLoader.TextureParameter();
        borderTexParams.format = Pixmap.Format.Alpha;
        borderTexParams.magFilter = Texture.TextureFilter.Linear;
        borderTexParams.minFilter = Texture.TextureFilter.Linear;
        assets.load(FILM_BORDER_TEXTURE, Texture.class, borderTexParams);

        TextureLoader.TextureParameter noiseTexParams = new TextureLoader.TextureParameter();
        noiseTexParams.format = Pixmap.Format.Alpha;
        noiseTexParams.magFilter = Texture.TextureFilter.Nearest;
        noiseTexParams.minFilter = Texture.TextureFilter.Nearest;
        noiseTexParams.wrapU = Texture.TextureWrap.Repeat;
        noiseTexParams.wrapV = Texture.TextureWrap.Repeat;
        assets.load(FILM_NOISE_TEXTURE, Texture.class, noiseTexParams);

        TextureLoader.TextureParameter scanlineTexParams = new TextureLoader.TextureParameter();
        scanlineTexParams.format = Pixmap.Format.Alpha;
        scanlineTexParams.magFilter = Texture.TextureFilter.Linear;
        scanlineTexParams.minFilter = Texture.TextureFilter.Linear;
        scanlineTexParams.wrapU = Texture.TextureWrap.Repeat;
        scanlineTexParams.wrapV = Texture.TextureWrap.Repeat;
        assets.load(SCANLINE_TEXTURE, Texture.class, scanlineTexParams);

        assets.load(HELIX_MODEL, Model.class);
        assets.load(BG_MODEL, Model.class);
        assets.load(BG_BLOOM_MODEL, Model.class);

        assets.finishLoading();

        helixNormalAOTexture = assets.get(HELIX_NORMAL_AO_TEXTURE, Texture.class);
        backgroundTexture = assets.get(BG_GLOW_TEXTURE, Texture.class);
        backgroundBloomTexture = assets.get(BG_BLOOM_TEXTURE, Texture.class);
        Texture filmNoiseTexture = assets.get(FILM_NOISE_TEXTURE, Texture.class);
        Texture filmBorderTexture = assets.get(FILM_BORDER_TEXTURE, Texture.class);
        Texture scanlineTexture = assets.get(SCANLINE_TEXTURE, Texture.class);

        Model helixModel = assets.get(HELIX_MODEL, Model.class);
        mainHelixModelInstance = new ModelInstance(helixModel);
        mainHelixModelInstance.userData = Settings.frontHelixColor;
        mainHelixTransformManager = new TransformManager(mainHelixModelInstance);
        rearHelixModelInstance = new ModelInstance(helixModel);
        rearHelixModelInstance.userData = Settings.rearHelixColor;
        rearHelixTransformManager = new TransformManager(rearHelixModelInstance);
        rearHelixTransformManager.eulers.set(12.477f, -96.843f, -7.902f);
        rearHelixTransformManager.position.set(7.95135f,-5.46558f, 9.82413f);

        Model bgModel = assets.get(BG_MODEL, Model.class);
        backgroundModelInstance = new ModelInstance(bgModel);

        Model bgBloomModel = assets.get(BG_BLOOM_MODEL, Model.class);
        backgroundBloomModelInstance = new ModelInstance(bgBloomModel);

        sssShader = new SubsurfaceScatteringShader(helixNormalAOTexture,(new Vector3(-0.6f, 1f, 1f)).nor());
        sssShader.init();
        disposables.add(sssShader);

        backgroundShader = new BackgroundShader(backgroundTexture);
        backgroundShader.init();
        disposables.add(backgroundShader);

        unlitShader = new UnlitShader(backgroundBloomTexture);
        unlitShader.init();
        disposables.add(unlitShader);

        blackShader = new BlackShader();
        blackShader.init();
        disposables.add(blackShader);

        bloomShaderProgram = loadShaderProgram("bloom");
        disposables.add(bloomShaderProgram);

        particleATexture = assets.get(PARTICLE_A_TEXTURE, Texture.class);
        particleATextureRegion = new TextureRegion(particleATexture);

        particlePointTexture = assets.get(POINT_PARTICLE_TEXTURE, Texture.class);

        particles = new Particles(particlePointTexture, particleATextureRegion, cam);
        InputMultiplexer inputMultiplexer = new InputMultiplexer(inputAdapter, particles.inputAdapter);
        Gdx.input.setInputProcessor(inputMultiplexer);

        particleGroupStrategy = new ParticleGroupStrategy(cam);
        disposables.add(particleGroupStrategy);

        decalBatch = new DecalBatch(Particles.MAX_PARTICLES, particleGroupStrategy);
        disposables.add(decalBatch);

        billboardDecalBatch = new BillboardDecalBatch(Particles.MAX_PARTICLES, particleGroupStrategy);
        disposables.add(billboardDecalBatch);

        filmGrain = new FilmGrain(filmNoiseTexture, filmBorderTexture, scanlineTexture, false);
        disposables.add(filmGrain);
        filmGrainForBloom = new FilmGrain(filmNoiseTexture, filmBorderTexture, scanlineTexture, true);
        disposables.add(filmGrainForBloom);
	}

    private ShaderProgram loadShaderProgram (final String prefix){
        String vert = Gdx.files.internal(prefix + "_vs.glsl").readString();
        String frag = Gdx.files.internal(prefix + "_fs.glsl").readString();
        ShaderProgram program = new ShaderProgram(vert, frag);
        if (!program.isCompiled())
            Gdx.app.log("Shader error", program.getLog());
        return program;
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

        if (filmGrain!=null)
            filmGrain.resize(width, height);

        if (filmGrainForBloom!=null)
            filmGrainForBloom.resize(width, height);

    }

    @Override
    public void draw(float deltaTime, float xOffsetFake, float xOffsetLooping, float xOffsetSmooth, float yOffset) {
        deltaTime = Math.min(deltaTime, MAX_FRAME_TIME);
        if (screenshotPause)
            deltaTime = 0;

        float _xOffset = isPreview? 0.5f :
                (Settings.pseudoScrolling? xOffsetFake :(Settings.smoothScrolling? xOffsetSmooth : xOffsetLooping));

        GL20 gl = Gdx.gl;

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
            filmGrain.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            filmGrainForBloom.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }

        if (settingsAdapter != null) settingsAdapter.updateInLoop(deltaTime);

        //UPDATES
        Texture.TextureFilter particleMinFilter = Settings.trilinearParticles ?
                Texture.TextureFilter.MipMapLinearLinear : Texture.TextureFilter.MipMapLinearNearest;
        particleATexture.setFilter(particleMinFilter, Texture.TextureFilter.Linear);
        particlePointTexture.setFilter(particleMinFilter, Texture.TextureFilter.Linear);

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

        sssShader.lightDirection.set(1, 0, 0).rotate(Vector3.Y, lightH).rotate(Vector3.X, lightV);

        particles.update(deltaTime, sssShader.lightDirection); //MUST BE DONE WHILE CAM IS IN POSITION

        backgroundShader.color.set(Settings.backgroundColor);

        //BLOOM SURFACE DRAWING
        if (Settings.bloom) {
            bloom.begin();
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
            rearDOFBlur.setClearColor(tmpColor.set(Settings.backgroundColor).mul(AMBIENT_BRIGHTNESS));
            rearDOFBlur.begin();
            modelBatch.begin(cam);
            modelBatch.render(backgroundModelInstance, backgroundShader);
            modelBatch.end();

            cam.position.set(camPosition);
            cam.update();

            sssShader.setShouldFadeEnds(true);
            modelBatch.begin(cam);
            modelBatch.render(rearHelixModelInstance, sssShader);
            modelBatch.end();
            rearDOFBlur.end();
        }

        //MAIN DRAWING
        Gdx.gl.glClearColor(Settings.backgroundColor.r, Settings.backgroundColor.g,
                Settings.backgroundColor.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        if (Settings.dof) {
            rearDOFBlur.render();
        } else {
            modelBatch.begin(cam);
            modelBatch.render(backgroundModelInstance, backgroundShader);
            modelBatch.end();
        }

        cam.position.set(camPosition);
        cam.update();

        sssShader.setShouldFadeEnds(false);
        modelBatch.begin(cam);
        modelBatch.render(mainHelixModelInstance, sssShader);
        modelBatch.end();

        if (!Settings.dof) {
            sssShader.setShouldFadeEnds(true);
            modelBatch.begin(cam);
            modelBatch.render(rearHelixModelInstance, sssShader);
            modelBatch.end();
        }

        boolean doFilmGrain = Settings.filmGrain || Settings.scanLines || Settings.vignette;

        if (Settings.bloom) {
            if (doFilmGrain){
                bloom.setCustomShaderPreparer(filmGrainForBloom);
                bloom.render(filmGrainForBloom.filmGrainShaderProgram);
            } else {
                bloom.setCustomShaderPreparer(null);
                bloom.render(bloomShaderProgram);
            }
        } else if (doFilmGrain){
            filmGrain.render();
        }

        particles.draw(decalBatch, billboardDecalBatch);

        fader.render(deltaTime);
    }

    float lightH = -90f, lightV = 38f;
    float baseLightH, baseLightV;
    int baseX, baseY;
    static final float DEG_PER_PIXEL = 0.5f;

    private InputAdapter inputAdapter = new InputAdapter(){
        boolean lightMode;

        public boolean keyDown (int keycode) {
            if (keycode == Input.Keys.R){
                sssShader.reloadShader();
                backgroundShader.reloadShader();
                {
                    if (bloomShaderProgram != null) bloomShaderProgram.dispose();
                    disposables.removeValue(bloomShaderProgram, true);
                    bloomShaderProgram = loadShaderProgram("bloom");
                    disposables.add(bloomShaderProgram);
                }
            } else if (keycode == Input.Keys.P){
                screenshotPause = ! screenshotPause;
            } else if (keycode == Input.Keys.SPACE){
                Settings.advanceScreenshotResolution();
            } else if (keycode == Input.Keys.C){
                Settings.advanceScreenshotColor();
            } else if (keycode == Input.Keys.V){
                Settings.vignette = ! Settings.vignette;
            } else if (keycode == Input.Keys.S){
                Settings.scanLines = ! Settings.scanLines;
            } else if (keycode == Input.Keys.F){
                Settings.filmGrain = ! Settings.filmGrain;
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
    public void onDoubleTap() {

    }

    @Override
    public void onTripleTap() {

    }

    @Override
    public void setIsPreview(boolean isPreview) {
        this.isPreview = isPreview;
    }


}
