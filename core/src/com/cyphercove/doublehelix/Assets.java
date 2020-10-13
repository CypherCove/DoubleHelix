/*******************************************************************************
 * Copyright 2020 Cypher Cove, LLC
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
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Logger;
import com.cyphercove.covetools.assets.Asset;
import com.cyphercove.covetools.assets.AssetContainer;
import com.cyphercove.covetools.assets.AssignmentAssetManager;
import com.cyphercove.covetools.assets.ShaderProgramAsset;
import com.cyphercove.covetools.assets.TextureAsset;
import com.cyphercove.covetools.graphics.TextureFilterPair;

public class Assets implements Disposable, AssetContainer {

    private final AssignmentAssetManager assetManager = new AssignmentAssetManager();

    @Asset("particle.vert") ShaderProgram particleShader;
    @Asset("billboardParticle.vert") ShaderProgram billboardParticleShader;
    @Asset("background.vert") ShaderProgram backgroundShader;
    @Asset("black.vert") ShaderProgram blackShader;
    @Asset("effectsOverlay.vert") ShaderProgram effectsOverlayShader;
    @ShaderProgramAsset(value = "effectsOverlay.frag", prependFragmentCode = "#define BLOOM\n")
    ShaderProgram effectsOverlayBloomShader;
    @Asset("sss.vert") ShaderProgram subsurfaceScatteringShader;
    @Asset("unlit.vert") ShaderProgram unlitShader;
    @Asset("bloom.vert") ShaderProgram bloomShader;
    @Asset("ca.vert") ShaderProgram chromaticAberrationShader;

    @Asset("helix.g3db") Model helixModel;
    @Asset("bg.g3db") Model backgroundModel;
    @Asset("bg_bloom.g3db") Model backgroundBloomModel;

    @TextureAsset(value = "bg.png", filter = TextureFilterPair.Bilinear)
    Texture backgroundTexture;
    @TextureAsset(value = "bloom_source.png", filter = TextureFilterPair.Bilinear)
    Texture backgroundBloomTexture;
    @TextureAsset(value = "particlePoint.png", filter = TextureFilterPair.Bilinear)
    Texture pointParticleTexture;
    @TextureAsset(value = "helix-normal-ao.png", filter = TextureFilterPair.Trilinear, wrap = Texture.TextureWrap.Repeat)
    Texture helixNormalAOTexture;
    @TextureAsset(value = "particleA.png", filter = TextureFilterPair.Trilinear, wrap = Texture.TextureWrap.Repeat)
    Texture particleATexture;
    @TextureAsset(value = "filmBorder.png", format = Pixmap.Format.Alpha, filter = TextureFilterPair.Linear)
    Texture filmBorderTexture;
    @TextureAsset(value = "filmNoise.png", format = Pixmap.Format.Alpha, wrap = Texture.TextureWrap.Repeat)
    Texture filmNoiseTexture;
    @TextureAsset(value = "scanlines.png", format = Pixmap.Format.Alpha, wrap = Texture.TextureWrap.Repeat, filter = TextureFilterPair.Linear)
    Texture scanLineTexture;

    public Assets() {
        ShaderProgram.prependFragmentCode = "#version 100\n";
        assetManager.getLogger().setLevel(Logger.ERROR);
        assetManager.loadAssetFields(this);
        assetManager.finishLoading();
    }

    public void reloadShaders() {
        assetManager.unloadAssetFields(this, ShaderProgram.class);
        assetManager.loadAssetFields(this);
        assetManager.finishLoading();
    }

    @Override
    public void onAssetsLoaded() {
    }

    @Override
    public String getAssetPathPrefix() {
        return "";
    }

    @Override
    public void dispose() {
        assetManager.dispose();
    }
}
