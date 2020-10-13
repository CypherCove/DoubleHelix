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

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

/**
 * PerspectiveCamera with properties and methods for calculating depth of field values.
 */
public class DepthOfFieldCamera extends PerspectiveCamera {

    private float focalLength;
    private float focalPlane;
    private float aperture;
    private CircleOfConfusion circleOfConfusion;

    private final Matrix4 tmp = new Matrix4();

    @Override
    public void update (boolean updateFrustum) {
        super.update(updateFrustum);

        if (Settings.flipH || Settings.flipV){
            tmp.idt().scale(Settings.flipH ? -1f : 1f, Settings.flipV ? -1f : 1f, 1f);
            projection.mul(tmp);
            combined.set(projection).mul(view);
        }

    }

    private class CircleOfConfusion {
        static final int COUNT = 16000;
        float range;
        float distanceToIndex;

        final float[] table = new float[COUNT];

        public CircleOfConfusion (float range) {
            this.range = range;
            distanceToIndex = COUNT / range;
            float indexToDistance = range / COUNT;
            for (int i = 0; i < COUNT; i++) {
                float distance = (i + 0.5f) * indexToDistance;
                table[i] = distance * //unproject by multiplying by distance
                        Math.abs(
                                aperture * focalLength * (distance - focalPlane) /
                                        (distance * (focalPlane - focalLength))
                        );
            }
        }
    }

    public DepthOfFieldCamera (float focalLength, float focalPlane, float aperture, float range){
        this.focalLength = focalLength;
        this.focalPlane = focalPlane;
        this.aperture = aperture;
        circleOfConfusion = new CircleOfConfusion(range);
    }

    public float calculateUnprojectedCircleOfConfusion (float distance){
        return circleOfConfusion.table[(int)Math.min(CircleOfConfusion.COUNT - 1,
                distance * circleOfConfusion.distanceToIndex)];
    }
}
