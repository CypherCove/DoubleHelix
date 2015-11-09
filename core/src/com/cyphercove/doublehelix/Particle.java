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

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.cyphercove.doublehelix.points.BillboardDecal;

import java.util.Random;

/**
 * Created by Darren on 9/7/2015.
 */
public class Particle {
    public final Vector3 center = new Vector3();
    public final Vector3 originalCenter = new Vector3();
    public float phase;
    public float frequency;
    public float age;
    private float ageFade;
    public float angle2d;
    final Vector3 baseVelocity = new Vector3();
    final Vector3 velocity = new Vector3();

    final Vector3 touchRotationVelocity = new Vector3();
    final Vector3 touchRotationAxis = new Vector3();
    float touchMagnitude; //if magnitude of new point is bigger, replace these
    float touchAge;

    public final DecalPlus decal;
    public final BillboardDecal point;

    private static final Random RAND = new Random();
    private static final Vector3 TMPV = new Vector3();
    private static final Vector3 TMPV2 = new Vector3();

    private static final float PI4 = MathUtils.PI2 * 2f;

    private static final float BASE_SIZE = 0.015f;
    private static final float BASE_SIZE_SQUARED = BASE_SIZE * BASE_SIZE;
    private static final float POINT_BASE_SIZE = 0.004f;
    private static final float POINT_BASE_SIZE_SQUARED = POINT_BASE_SIZE * POINT_BASE_SIZE;
    private static final float POINT_BASE_SIZE_NO_DOF = 0.004f;
    private static final float BASE_BRIGHTNESS = 1.0f;
    private static final float POINT_BASE_BRIGHTNESS = 2.0f;
    private static final float SPECULAR_POWER = 3f;
    private static final float SPECULAR_SCALE = 30f;
    private static final float SPECULAR_PHASE_SHIFT = MathUtils.PI / 2f;

    private static final float RADIAL_ACCELERATION_MAX = 0.05f;
    private static final float RADIAL_ACCELERATION_EFFECT_EXTENT = 1.3f;
    private static final float RADIAL_ACCELERATION_MAX_CENTER = 1.5f; //radius of the helix
    private static final float DEGREES_ACCLERATION_PER_DIST = 180f;
    private static final float DAMPENING = 6f;

    private static final float LIFE_TIME = 8f;
    private static final float LIFE_FADE_TIME = 0.5f;
    private static final float LIFE_FADE_OUT_TIME = LIFE_TIME - LIFE_FADE_TIME;

    private static final float TOUCH_INFLUENCE = 1F;
    private static final float TOUCH_SPEED_RATIO = 0.15F;
    private static final float TOUCH_DAMPEN_TIME = 1.5F;
    private static final float TOUCH_CHECK_DISTANCE = Particles.TOUCH_DISTANCE + TOUCH_INFLUENCE;


    private static class Power {
        static final int COUNT = 16000;

        final static float[] table = new float[COUNT];

        static {
            float indexToDistance = 1f / COUNT;
            for (int i = 0; i < COUNT; i++) {
                table[i] = (float)Math.pow((i + 0.5f) * indexToDistance, SPECULAR_POWER);
            }
        }
    }

    public Particle (TextureRegion textureRegion, Texture pointTexture){
        super();
        decal = new DecalPlus();
        decal.setTextureRegion(textureRegion);
        decal.setBlending(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);

        point = new BillboardDecal();
        point.setTexture(pointTexture);
        point.setBlending(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    public void reset (){
        center.set(originalCenter);
        age = RAND.nextFloat() * LIFE_TIME;
        phase = RAND.nextFloat() * MathUtils.PI2 * 2f; //need to go up to 4pi because of doubled parameter for the every-other-wave specular cut
        frequency = 3f; //TODO
        angle2d = 360f * RAND.nextFloat();
        touchMagnitude = 0;
    }

    private void updateCommon (float delta, boolean haveTouchVelocity, Vector3 touchPosition,
                               Vector3 touchVelocity, float cameraDistance){
        age += delta;
        if (age < LIFE_FADE_TIME)
            ageFade = Interpolation.fade.apply(age/LIFE_FADE_TIME);
        else if (age > LIFE_FADE_OUT_TIME)
            ageFade = Interpolation.fade.apply((LIFE_TIME - age)/LIFE_FADE_TIME);
        else
            ageFade = 1;

        TMPV.set(center.x, center.y, 0);
        float distFromAccelerationCenter = TMPV.len()- RADIAL_ACCELERATION_MAX_CENTER;
        float radialAcceleration = RADIAL_ACCELERATION_MAX * (1F - Math.min(1f, Interpolation.fade.apply(Math.abs(distFromAccelerationCenter)/ RADIAL_ACCELERATION_EFFECT_EXTENT)));
        TMPV.nor().rotate(Vector3.Z, 90f - (distFromAccelerationCenter * DEGREES_ACCLERATION_PER_DIST)); //motion direction
        TMPV.scl(radialAcceleration); //acceleration
        baseVelocity.add(TMPV.scl(delta)); //apply acceleration


        float dampenAmount = DAMPENING * baseVelocity.len() * delta;
        if (dampenAmount >= 1f)
            baseVelocity.set(0, 0, 0);
        else
            baseVelocity.add(TMPV.set(baseVelocity).scl(-dampenAmount));

        velocity.set(baseVelocity);

        if (haveTouchVelocity && cameraDistance <= TOUCH_CHECK_DISTANCE){
            float dst = TMPV.set(center).sub(touchPosition).len();
            if (dst < TOUCH_INFLUENCE) {
                float mag = 1f - Math.min(1f, dst / TOUCH_INFLUENCE);
                if (mag > touchMagnitude) {
                    touchMagnitude = mag;
                    //float radialDst = distanceToAxis(center, touchPosition, touchVelocity);
                    touchAge = 0;

                    touchRotationVelocity.set(touchVelocity).scl(TOUCH_SPEED_RATIO * mag);
                    touchRotationAxis.set(TMPV).crs(touchVelocity).nor();
                }
            }
        }

        if (touchMagnitude > 0){
            touchAge += delta;

            if (touchAge > TOUCH_DAMPEN_TIME){
                touchMagnitude = 0;
                TMPV.set(touchRotationVelocity).scl(0.5f);
                TMPV.rotate(touchRotationAxis, -45);
                baseVelocity.add(TMPV);
                velocity.set(baseVelocity);
            } else {
                float lerp = touchAge / TOUCH_DAMPEN_TIME;
                TMPV.set(touchRotationVelocity).scl(1f - 0.5f*Interpolation.pow2Out.apply(lerp));
                TMPV.rotate(touchRotationAxis, -Interpolation.pow3In.apply(lerp)*45);
                velocity.add(TMPV);
            }
        }


        if (age >= LIFE_TIME) {
            center.set(originalCenter);
            age = 0;
        } else {
            center.add(TMPV.set(velocity).scl(delta));
        }
    }

    float distanceToAxis(Vector3 point, Vector3 pointOnAxis, Vector3 axisDirection){
        TMPV.set(point).sub(pointOnAxis);
        float c1 = TMPV.dot(axisDirection);
        float c2 = axisDirection.dot(axisDirection);
        TMPV.set(axisDirection).scl(c1 / c2).add(pointOnAxis);
        return TMPV.dst(point);
    }

    public void updateFlake (float delta, DepthOfFieldCamera camera, Vector3 projectedLight,
                             boolean haveTouchVelocity, Vector3 touchPosition, Vector3 touchVelocity){
        float cameraDistance = center.dst(camera.position);
        decal.cameraDistance = cameraDistance;
        updateCommon(delta, haveTouchVelocity, touchPosition, touchVelocity, cameraDistance);

        float circleOfConfusion = camera.calculateUnprojectedCircleOfConfusion(cameraDistance);
        float size = BASE_SIZE + circleOfConfusion;
        float focus = 1f - Math.min(1f, circleOfConfusion * 6.0f); // Focus is from 0 to 1. 0 is full bokeh. Last parameter experimentally determined (it's a factor of CoC).
        focus = focus*focus*focus;
        float unfocus = 1f - focus;

        //Fake rotation by squeezing vertically. Reduce amount of squeeze by unfocus to keep bokeh
        //blur. At full unfocus, there is no squeezing.
        float squeeze = 1f - focus * (0.5f + 0.5f * MathUtils.sin(frequency * age + phase));
        float squeezedBaseSize = BASE_SIZE * squeeze;
        decal.setDimensions(size, size * squeeze);

        camera.project(TMPV.set(center)); //screen position of cell
        TMPV.sub(projectedLight).nor(); //screen space light-to-cell vector
        TMPV2.set(0, 1f, 0).rotate(angle2d, 0, 0, -1f); //sprite angle in 2D (-1 z matches camera.direction used to rotate decal at end of this method)
        float specularAlignment = Math.abs(TMPV.x * TMPV2.x + TMPV.y * TMPV2.y); //dot product of rotation direction and vector to light
        specularAlignment = Power.table[(int)Math.min(Power.COUNT-1, specularAlignment * Power.COUNT)];//(float)Math.pow(specularAlignment, SPECULAR_POWER);

        float specularSineParameter = frequency * age + phase + SPECULAR_PHASE_SHIFT;
        float specular = (specularSineParameter-1.5f*MathUtils.PI) % PI4 < MathUtils.PI2 ? specularAlignment *
            Power.table[(int)Math.min(Power.COUNT-1, Math.max(0, 0.5f + 0.5f*MathUtils.sin(specularSineParameter)) * Power.COUNT)] : 0; //Ternary expression makes it every other wave
        float totalBrightness = BASE_BRIGHTNESS + specular * SPECULAR_SCALE;

        //alpha is only used as the decal goes unfocused. When fully unfocused, the brightness is
        //evenly spread across the larger area of the sprite. Brightness can be more than 1 before
        //clamping because of HDR specular.
        float alpha = ageFade * Math.min(1f, //Clamp color down to one
                Math.max(1, totalBrightness) * //HDR brightness
                        Interpolation.linear.apply(BASE_SIZE_SQUARED, (squeezedBaseSize * squeezedBaseSize), unfocus) / (size * size)); //size ratio of focused to unfocused sprite.
                        // ^^Squeezed base size is used for unfocused sprites because the proper base size is dependent on squeeze

        decal.setColor(Math.min(1f, specular), 0f, unfocus, alpha); //whiteness, unused, unfocus, alpha.
        decal.setPosition(center);
        decal.lookAt(camera.position, TMPV.set(camera.up).rotate(camera.direction, angle2d));
    }

    public void updateFlakeNoDOF (float delta, DepthOfFieldCamera camera, Vector3 projectedLight,
                             boolean haveTouchVelocity, Vector3 touchPosition, Vector3 touchVelocity){
        float cameraDistance = center.dst(camera.position);
        decal.cameraDistance = cameraDistance;
        updateCommon(delta, haveTouchVelocity, touchPosition, touchVelocity, cameraDistance);

        //Fake rotation by squeezing vertically. Reduce amount of squeeze by unfocus to keep bokeh
        //blur. At full unfocus, there is no squeezing.
        float squeeze = 1f - (0.5f + 0.5f * MathUtils.sin(frequency * age + phase));
        decal.setDimensions(BASE_SIZE, BASE_SIZE * squeeze);

        camera.project(TMPV.set(center)); //screen position of cell
        TMPV.sub(projectedLight).nor(); //screen space light-to-cell vector
        TMPV2.set(0, 1f, 0).rotate(angle2d, 0, 0, -1f); //sprite angle in 2D (-1 z matches camera.direction used to rotate decal at end of this method)
        float specularAlignment = Math.abs(TMPV.x * TMPV2.x + TMPV.y * TMPV2.y); //dot product of rotation direction and vector to light
        specularAlignment = Power.table[(int)Math.min(Power.COUNT-1, specularAlignment * Power.COUNT)];//(float)Math.pow(specularAlignment, SPECULAR_POWER);

        float specularSineParameter = frequency * age + phase + SPECULAR_PHASE_SHIFT;
        float specular = (specularSineParameter-1.5f*MathUtils.PI) % PI4 < MathUtils.PI2 ? specularAlignment *
                Power.table[(int)Math.min(Power.COUNT-1, Math.max(0, 0.5f + 0.5f*MathUtils.sin(specularSineParameter)) * Power.COUNT)] : 0; //Ternary expression makes it every other wave
        //float totalBrightness = BASE_BRIGHTNESS + specular * SPECULAR_SCALE;

        decal.setColor(Math.min(1f, specular), 0f, 1f, ageFade); //whiteness, unused, unfocus, alpha.
        decal.setPosition(center);
        decal.lookAt(camera.position, TMPV.set(camera.up).rotate(camera.direction, angle2d));
    }

    public void updateBillboard (float delta, DepthOfFieldCamera camera, Vector3 projectedLight,
                                 boolean haveTouchVelocity, Vector3 touchPosition, Vector3 touchVelocity, float sizeFactor){
        float cameraDistance = center.dst(camera.position);
        point.floatValue = cameraDistance;
        updateCommon(delta, haveTouchVelocity, touchPosition, touchVelocity, cameraDistance);

        float circleOfConfusion = 0.5f * camera.calculateUnprojectedCircleOfConfusion(cameraDistance);
        float size = POINT_BASE_SIZE + circleOfConfusion;
        float focus = 1f - Math.min(1f, circleOfConfusion * 6.0f); // Focus is from 0 to 1. 0 is full bokeh. Last parameter experimentally determined (it's a factor of CoC).
        focus = focus*focus*focus;
        float unfocus = 1f - focus;

        //not actually squeezing, but this is used for alpha
        float squeeze = 1f - (0.25f + 0.25f * MathUtils.sin(frequency * age + phase)); //note squeeze limited to half as much as with flakes
        float squeezedBaseSize = POINT_BASE_SIZE * squeeze;
        point.setSize(size * sizeFactor / cameraDistance); //size projected to screen

        camera.project(TMPV.set(center)); //screen position of cell
        TMPV.sub(projectedLight).nor(); //screen space light-to-cell vector
        TMPV2.set(0, 1f, 0).rotate(angle2d, 0, 0, -1f); //sprite angle in 2D (-1 z matches camera.direction used to rotate decal at end of this method)
        float specularAlignment = Math.abs(TMPV.x * TMPV2.x + TMPV.y * TMPV2.y); //dot product of rotation direction and vector to light
        specularAlignment = Power.table[(int)Math.min(Power.COUNT-1, specularAlignment * Power.COUNT)];//(float)Math.pow(specularAlignment, SPECULAR_POWER);

        float specularSineParameter = frequency * age + phase + SPECULAR_PHASE_SHIFT;
        float specular = (specularSineParameter-1.5f*MathUtils.PI) % PI4 < MathUtils.PI2 ? specularAlignment *
                Power.table[(int)Math.min(Power.COUNT-1, Math.max(0, 0.5f + 0.5f*MathUtils.sin(specularSineParameter)) * Power.COUNT)] : 0; //Ternary expression makes it every other wave
        float totalBrightness = POINT_BASE_BRIGHTNESS + specular * SPECULAR_SCALE;

        //alpha is only used as the decal goes unfocused. When fully unfocused, the brightness is
        //evenly spread across the larger area of the sprite. Brightness can be more than 1 before
        //clamping because of HDR specular.
        //TODO this should be calculated from unprojected sizes, not projected sizes.
        float alpha = ageFade * Math.min(1f, //Clamp color down to one
                Math.max(1, totalBrightness) * //HDR brightness
                squeeze * //simulate rotating spec
                Interpolation.linear.apply(POINT_BASE_SIZE_SQUARED, (squeezedBaseSize * squeezedBaseSize), unfocus) / (size * size)); //size ratio of focused to unfocused sprite.

        point.setColor(Math.min(1f, specular), 0f, unfocus, alpha); //whiteness, unused, unfocus, alpha.
        point.setPosition(center);
        point.setAngle(angle2d);
    }

    public void updateBillboardNoDOF (float delta, DepthOfFieldCamera camera, Vector3 projectedLight,
                                 boolean haveTouchVelocity, Vector3 touchPosition, Vector3 touchVelocity, float sizeFactor){
        float cameraDistance = center.dst(camera.position);
        point.floatValue = cameraDistance;
        updateCommon(delta, haveTouchVelocity, touchPosition, touchVelocity, cameraDistance);

        //not actually squeezing, but this is used for alpha
        float squeeze = 1f - (0.25f + 0.25f * MathUtils.sin(frequency * age + phase)); //note squeeze limited to half as much as with flakes
        point.setSize(POINT_BASE_SIZE_NO_DOF * sizeFactor / cameraDistance); //size projected to screen

        camera.project(TMPV.set(center)); //screen position of cell
        TMPV.sub(projectedLight).nor(); //screen space light-to-cell vector
        TMPV2.set(0, 1f, 0).rotate(angle2d, 0, 0, -1f); //sprite angle in 2D (-1 z matches camera.direction used to rotate decal at end of this method)
        float specularAlignment = Math.abs(TMPV.x * TMPV2.x + TMPV.y * TMPV2.y); //dot product of rotation direction and vector to light
        specularAlignment = Power.table[(int)Math.min(Power.COUNT-1, specularAlignment * Power.COUNT)];

        float specularSineParameter = frequency * age + phase + SPECULAR_PHASE_SHIFT;
        float specular = (specularSineParameter-1.5f*MathUtils.PI) % PI4 < MathUtils.PI2 ? specularAlignment *
                Power.table[(int)Math.min(Power.COUNT-1, Math.max(0, 0.5f + 0.5f*MathUtils.sin(specularSineParameter)) * Power.COUNT)] : 0; //Ternary expression makes it every other wave

        point.setColor(Math.min(1f, specular), 0f, 1f, ageFade * squeeze); //whiteness, unused, unfocus, alpha.
        point.setPosition(center);
        point.setAngle(angle2d);
    }
}
