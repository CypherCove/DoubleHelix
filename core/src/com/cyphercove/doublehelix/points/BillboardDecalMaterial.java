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

package com.cyphercove.doublehelix.points;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class BillboardDecalMaterial {
    public static final int NO_BLEND = -1;
    protected Texture texture;
    protected int srcBlendFactor;
    protected int dstBlendFactor;

    /** Binds the material's texture to the OpenGL context and changes the glBlendFunc to the values used by it. */
    public void set () {
        texture.bind();
        if (!isOpaque()) {
            Gdx.gl.glBlendFunc(srcBlendFactor, dstBlendFactor);
        }
    }

    /** @return true if the material is completely opaque, false if it is not and therefor requires blending */
    public boolean isOpaque () {
        return srcBlendFactor == NO_BLEND;
    }

    public int getSrcBlendFactor () {
        return srcBlendFactor;
    }

    public int getDstBlendFactor () {
        return dstBlendFactor;
    }

    @Override
    public boolean equals (Object o) {
        if (o == null) return false;

        BillboardDecalMaterial material = (BillboardDecalMaterial)o;

        return dstBlendFactor == material.dstBlendFactor && srcBlendFactor == material.srcBlendFactor
                && texture == material.texture;

    }

    @Override
    public int hashCode () {
        int result = texture != null ? texture.hashCode() : 0;
        result = 31 * result + srcBlendFactor;
        result = 31 * result + dstBlendFactor;
        return result;
    }
}
