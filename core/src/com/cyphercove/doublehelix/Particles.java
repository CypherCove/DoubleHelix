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
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.cyphercove.doublehelix.points.BillboardDecalBatch;

import java.util.Random;

/**
 * Created by Darren on 9/7/2015.
 */
public class Particles {
    public static final int MAX_PARTICLES = 1000;
    private Array<Particle> particles = new Array<Particle>(MAX_PARTICLES);
    private DepthOfFieldCamera camera;
    private static final Vector3 TMPV = new Vector3();
    private static final Vector3 TMPV2 = new Vector3();
    static final float TOUCH_RESOLUTION_INCHES = 0.1f;
    static final float STALL_TIME = 0.10f;
    static final float CHECK_TIME = 0.025f;
    static final float TOUCH_DISTANCE = 3f;

    float inputAdapterResolutionSquared = 5f;
    boolean isTouching = false;
    final Vector2 tmpV2 = new Vector2();
    final Vector2 lastPoint = new Vector2();
    final Vector2 newPoint = new Vector2();
    final Vector3 touchVelocity = new Vector3();
    final Vector3 touchPoint = new Vector3();
    float lastPointAge;
    boolean haveSecondPoint;

    final Random random = new Random();

    public Particles (Texture pointTexture, TextureRegion textureRegion, DepthOfFieldCamera camera){
        this.camera = camera;
        for (int i = 0; i < MAX_PARTICLES; i++) {
            Particle particle = new Particle(textureRegion, pointTexture);
            //in wedge of a cylinder in the viewable area:
            particle.originalCenter.set(random.nextFloat() * 18f, 0, random.nextFloat()*8f+1f)
                    .rotateRad(Vector3.Z, -(0.225f + 0.75f * random.nextFloat()) * MathUtils.PI).add(0, 2f, 0);
            particle.reset();
            particles.add(particle);
        }
        setInputAdapterResolution();
    }

    public void update (float delta, Vector3 lightDirection){

        if (isTouching){

            lastPointAge += delta;

            if (lastPointAge > CHECK_TIME && !newPoint.equals(lastPoint)){
                //moved
                tmpV2.set(newPoint).sub(lastPoint);
                float distance = tmpV2.len2();
                if (distance >= inputAdapterResolutionSquared){
                    if (!haveSecondPoint){
                        Ray pickRay = camera.getPickRay(lastPoint.x, lastPoint.y);
                        touchPoint.set(pickRay.origin).add(pickRay.direction.nor().scl(TOUCH_DISTANCE));
                    }

                    haveSecondPoint = true;
                    TMPV.set(touchPoint); //previous touch point

                    Ray pickRay = camera.getPickRay(newPoint.x, newPoint.y);
                    touchPoint.set(pickRay.origin).add(pickRay.direction.nor().scl(TOUCH_DISTANCE));

                    touchVelocity.set(touchPoint).sub(TMPV).scl(1f / lastPointAge);

                    lastPoint.set(newPoint);
                    lastPointAge = 0;
                }
            } else if (lastPointAge >= STALL_TIME){
                lastPointAge = 0;
                touchVelocity.set(0,0,0);
            }

        }

        TMPV.set(lightDirection).scl(100);
        TMPV2.set(camera.position).add(TMPV);//light source position in world space
        camera.project(TMPV2); //projected light

        boolean haveTouchVelocity = isTouching && haveSecondPoint;
        if (Settings.pointParticles){
            float eyeSpaceScreenHeight = camera.near *
                    MathUtils.sinDeg(camera.fieldOfView / 2f) / MathUtils.cos(camera.fieldOfView / 2f);
            float sizeFactor = camera.near / eyeSpaceScreenHeight * camera.viewportHeight;
            if (Settings.dof) {
                for (int i = 0; i < Settings.numParticles; i++) {
                    particles.get(i).updateBillboard(delta, camera, TMPV2, haveTouchVelocity, touchPoint,
                            touchVelocity, sizeFactor);
                }
            } else {
                for (int i = 0; i < Settings.numParticles; i++) {
                    particles.get(i).updateBillboardNoDOF(delta, camera, TMPV2, haveTouchVelocity, touchPoint,
                            touchVelocity, sizeFactor);
                }
            }
        } else {
            if (Settings.dof) {
                for (int i = 0; i < Settings.numParticles; i++) {
                    particles.get(i).updateFlake(delta, camera, TMPV2, haveTouchVelocity, touchPoint, touchVelocity);
                }
            } else {
                for (int i = 0; i < Settings.numParticles; i++) {
                    particles.get(i).updateFlakeNoDOF(delta, camera, TMPV2, haveTouchVelocity, touchPoint, touchVelocity);
                }
            }
        }
    }

    public void draw (DecalBatch decalBatch, BillboardDecalBatch billboardDecalBatch){
        if (Settings.pointParticles){
            for (int i = 0; i < Settings.numParticles; i++) {
                billboardDecalBatch.add(particles.get(i).point);
            }
            billboardDecalBatch.flush();
        } else {
            for (int i = 0; i < Settings.numParticles; i++) {
                decalBatch.add(particles.get(i).decal);
            }
            decalBatch.flush();
        }
    }

    public void setInputAdapterResolution (){
        float resolution = Gdx.graphics.getPpiX() * TOUCH_RESOLUTION_INCHES;
        inputAdapterResolutionSquared = resolution * resolution;
    }

    public InputAdapter inputAdapter = new InputAdapter(){

        @Override
        public boolean touchDown (int screenX, int screenY, int pointer, int button) {
            isTouching = true;
            haveSecondPoint = false;
            newPoint.set(screenX, screenY);
            lastPoint.set(screenX, screenY);
            lastPointAge = 0;
            return true;
        }

        @Override
        public boolean touchDragged (int screenX, int screenY, int pointer) {
            newPoint.set(screenX, screenY);
            return true;
        }

        @Override
        public boolean touchUp (int screenX, int screenY, int pointer, int button) {
            isTouching = false;
            return true;
        }
    };
}
