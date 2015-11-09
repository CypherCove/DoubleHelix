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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.NumberUtils;

/**
 * Created by Darren on 9/20/2015.
 */
public class BillboardDecal {
    // 3(x,y,z) + 1(color) + +1(size) + 1(angle)
    /** Size of a decal vertex in floats */
    public static final int VERTEX_SIZE = 3 + 1 + 1 + 1;

    /** Temporary vector for various calculations. */
    private static Vector3 tmp = new Vector3();
    private static Vector3 tmp2 = new Vector3();

    /** Set a multipurpose value which can be queried and used for things like group identification. */
    public int value;

    public float floatValue;

    protected float[] vertices = new float[VERTEX_SIZE];
    protected Vector3 position = new Vector3();
    protected Color color = new Color();

    /** The transformation offset can be used to change the pivot point for rotation and scaling. By default the pivot is the middle
     * of the decal. */
    public Vector2 transformationOffset = null;
    protected Vector2 dimensions = new Vector2();

    protected BillboardDecalMaterial material;
    protected boolean updated = false;

    public BillboardDecal () {
        this.material = new BillboardDecalMaterial();
    }

    public BillboardDecal (BillboardDecalMaterial material) {
        this.material = material;
    }

    /** Sets the color of all four vertices to the specified color
     *
     * @param r Red component
     * @param g Green component
     * @param b Blue component
     * @param a Alpha component */
    public void setColor (float r, float g, float b, float a) {
        color.set(r, g, b, a);
        int intBits = ((int)(255 * a) << 24) | ((int)(255 * b) << 16) | ((int)(255 * g) << 8) | ((int)(255 * r));
        float color = NumberUtils.intToFloatColor(intBits);
        vertices[C] = color;
    }

    /** Sets the color used to tint this decal. Default is {@link Color#WHITE}. */
    public void setColor (Color tint) {
        color.set(tint);
        float color = tint.toFloatBits();
        vertices[C] = color;
    }

    /** @see #setColor(Color) */
    public void setColor (float color) {
        this.color.set(NumberUtils.floatToIntColor(color));
        vertices[C] = color;
    }

    public void setAngle (float angle) {
        vertices[A] = angle;
    }

    public float getAngle (){
        return vertices[A];
    }

    /** Moves by the specified amount of units along the x axis
     *
     * @param units Units to move the decal */
    public void translateX (float units) {
        this.position.x += units;
        updated = false;
    }

    /** Sets the position on the x axis
     *
     * @param x Position to locate the decal at */
    public void setX (float x) {
        this.position.x = x;
        updated = false;
    }

    /** @return position on the x axis */
    public float getX () {
        return this.position.x;
    }

    /** Moves by the specified amount of units along the y axis
     *
     * @param units Units to move the decal */
    public void translateY (float units) {
        this.position.y += units;
        updated = false;
    }

    /** Sets the position on the y axis
     *
     * @param y Position to locate the decal at */
    public void setY (float y) {
        this.position.y = y;
        updated = false;
    }

    /** @return position on the y axis */
    public float getY () {
        return this.position.y;
    }

    /** Moves by the specified amount of units along the z axis
     *
     * @param units Units to move the decal */
    public void translateZ (float units) {
        this.position.z += units;
        updated = false;
    }

    /** Sets the position on the z axis
     *
     * @param z Position to locate the decal at */
    public void setZ (float z) {
        this.position.z = z;
        updated = false;
    }

    /** @return position on the z axis */
    public float getZ () {
        return this.position.z;
    }

    /** Translates by the specified amount of units
     *
     * @param x Units to move along the x axis
     * @param y Units to move along the y axis
     * @param z Units to move along the z axis */
    public void translate (float x, float y, float z) {
        this.position.add(x, y, z);
        updated = false;
    }

    /** @see BillboardDecal#translate(float, float, float) */
    public void translate (Vector3 trans) {
        this.position.add(trans);
        updated = false;
    }

    /** Sets the position to the given world coordinates
     *
     * @param x X position
     * @param y Y Position
     * @param z Z Position */
    public void setPosition (float x, float y, float z) {
        this.position.set(x, y, z);
        updated = false;
    }

    /** @see BillboardDecal#setPosition(float, float, float) */
    public void setPosition (Vector3 pos) {
        this.position.set(pos);
        updated = false;
    }

    /** Returns the color of this decal. The returned color should under no circumstances be modified.
     *
     * @return The color of this decal. */
    public Color getColor () {
        return color;
    }

    /** Returns the position of this decal. The returned vector should under no circumstances be modified.
     *
     * @return vector representing the position */
    public Vector3 getPosition () {
        return position;
    }

    public void setSize (float size){
        vertices[S] = size;
    }

    public float getSize (){
        return vertices[S];
    }

    /** Returns the vertices backing this sprite.<br/>
     * The returned value should under no circumstances be modified.
     *
     * @return vertex array backing the decal */
    public float[] getVertices () {
        return vertices;
    }

    /** Recalculates vertices array if it grew out of sync with the properties (position, ..) */
    protected void update () {
        if (!updated) {
            vertices[X] = position.x;
            vertices[Y] = position.y;
            vertices[Z] = position.z;
        }
    }

    /** Sets the blending parameters for this decal
     *
     * @param srcBlendFactor Source blend factor used by glBlendFunc
     * @param dstBlendFactor Destination blend factor used by glBlendFunc */
    public void setBlending (int srcBlendFactor, int dstBlendFactor) {
        material.srcBlendFactor = srcBlendFactor;
        material.dstBlendFactor = dstBlendFactor;
    }

    public void setTexture (Texture texture) {
        this.material.texture = texture;
    }

    public BillboardDecalMaterial getMaterial () {
        return material;
    }

    /**Set material
     *
     * @param material custom material
     */
    public void setMaterial (BillboardDecalMaterial material) {
        this.material = material;
    }

    // meaning of the floats in the vertices array
    public static final int X = 0;
    public static final int Y = 1;
    public static final int Z = 2;
    public static final int C = 3; //color
    public static final int S = 4; //size
    public static final int A = 5; //angle
}
