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
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.GroupStrategy;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.cyphercove.doublehelix.points.BillboardDecal;
import com.cyphercove.doublehelix.points.BillboardGroupStrategy;

import java.util.Comparator;

public class ParticleGroupStrategy implements GroupStrategy, BillboardGroupStrategy, Disposable {
//    private static final int GROUP_BLUR_ONLY = 0;
//    private static final int GROUP_DOF = 1;
//    private static final int GROUP_NO_DOF = 2;

    private static final float WHITENESS = 0.4f;

    private Camera camera;
    private Assets assets;
    private final Comparator<Decal> cameraSorter;
    private final Comparator<BillboardDecal> billboardCameraSorter;

    final Color tmpColor = new Color();

    public ParticleGroupStrategy(final Camera camera, Assets assets) {
        this.camera = camera;
        this.assets = assets;

        cameraSorter = new Comparator<Decal>() {
            @Override
            public int compare (Decal o1, Decal o2) {
                return (int)Math.signum(((DecalPlus)o2).cameraDistance - ((DecalPlus)o1).cameraDistance);
            }
        };

        billboardCameraSorter = new Comparator<BillboardDecal>() {
            @Override
            public int compare (BillboardDecal o1, BillboardDecal o2) {
                return (int)Math.signum(o2.floatValue - o1.floatValue);
            }
        };

    }

    @Override
    public int decideGroup (Decal decal) {
//        if (!Settings.depthOfField)
//            return GROUP_NO_DOF;
//        if (forceBlurOnly)
//            return GROUP_BLUR_ONLY;
//        return (decal.getPosition().dst(cameraPosition) < BLUR_ONLY_DISTANCE) ?
//                GROUP_DOF : GROUP_BLUR_ONLY;
        return 1;
    }

    @Override
    public int decideBillboardGroup(BillboardDecal decal) {
        return 1;
    }

    @Override
    public void beforeGroup (int group, Array<Decal> contents) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_COLOR);
        contents.sort(cameraSorter);

        tmpColor.set(Settings.backgroundColor).lerp(Color.WHITE, WHITENESS);

        ShaderProgram shader = assets.particleShader;
        shader.bind();
        shader.setUniformMatrix("u_projTrans", camera.combined);
        shader.setUniformi("u_texture", 0);
        shader.setUniformf("u_baseColor", tmpColor);
    }

    @Override
    public void beforeBillboardGroup(int group, Array<BillboardDecal> contents) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_COLOR);
        contents.sort(billboardCameraSorter);

        tmpColor.set(Settings.backgroundColor).lerp(Color.WHITE, WHITENESS);

        ShaderProgram billboardShader = assets.billboardParticleShader;
        billboardShader.bind();
        billboardShader.setUniformMatrix("u_projTrans", camera.combined);
        billboardShader.setUniformi("u_texture", 0);
        billboardShader.setUniformf("u_baseColor", tmpColor);
    }

    @Override
    public void afterGroup (int group) {
    }

    @Override
    public void afterBillboardGroup (int group) {
    }

    @Override
    public void beforeGroups () {
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDepthMask(false);
    }

    @Override
    public void beforeBillboardGroups () {
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDepthMask(false);
    }

    @Override
    public void afterGroups () {
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDepthMask(true);
    }

    @Override
    public void afterBillboardGroups () {
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDepthMask(true);
    }

    @Override
    public ShaderProgram getGroupShader (int group) {
//        switch (group){
//            case GROUP_NO_DOF:
//                return noDOFShader;
//            case GROUP_BLUR_ONLY:
//                return blurOnlyShader;
//            case GROUP_DOF:
//            default:
//                return dofShader;
//        }
        return assets.particleShader;
    }

    @Override
    public ShaderProgram getBillboardGroupShader (int group) {
//        switch (group){
//            case GROUP_NO_DOF:
//                return noDOFShader;
//            case GROUP_BLUR_ONLY:
//                return blurOnlyShader;
//            case GROUP_DOF:
//            default:
//                return dofShader;
//        }
        return assets.billboardParticleShader;
    }

    @Override
    public void dispose () {
    }
}
