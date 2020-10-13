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
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalMaterial;
import com.badlogic.gdx.graphics.g3d.decals.GroupStrategy;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.SortedIntList;

/**
 * Clone of libGDX's DecalBatch with additional vertex attributes.
 */
public class BillboardDecalBatch implements Disposable {
    public static final String SIZE_ATTRIBUTE = "a_size";
    public static final String ROTATION_ATTRIBUTE = "a_rotation";

    private static final int DEFAULT_SIZE = 1000;
    private float[] vertices;
    private Mesh mesh;

    private final SortedIntList<Array<BillboardDecal>> groupList = new SortedIntList<Array<BillboardDecal>>();
    private BillboardGroupStrategy groupStrategy;
    private final Pool<Array<BillboardDecal>> groupPool = new Pool<Array<BillboardDecal>>(16) {
        @Override
        protected Array<BillboardDecal> newObject () {
            return new Array<BillboardDecal>(false, 100);
        }
    };
    private final Array<Array<BillboardDecal>> usedGroups = new Array<Array<BillboardDecal>>(16);

    public BillboardDecalBatch (BillboardGroupStrategy groupStrategy) {
        this(DEFAULT_SIZE, groupStrategy);
    }

    public BillboardDecalBatch (int size, BillboardGroupStrategy groupStrategy) {
        initialize(size);
        setGroupStrategy(groupStrategy);
    }

    /** Sets the {@link GroupStrategy} used
     * @param groupStrategy Group strategy to use */
    public void setGroupStrategy (BillboardGroupStrategy groupStrategy) {
        this.groupStrategy = groupStrategy;
    }

    public void initialize (int size) {
        vertices = new float[size * BillboardDecal.VERTEX_SIZE];

        Mesh.VertexDataType vertexDataType = Mesh.VertexDataType.VertexArray;
        if(Gdx.gl30 != null) {
            vertexDataType = Mesh.VertexDataType.VertexBufferObjectWithVAO;
        }
        mesh = new Mesh(vertexDataType, false, size, 0, new VertexAttribute(
                VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE), new VertexAttribute(
                VertexAttributes.Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE), new VertexAttribute(
                VertexAttributes.Usage.Generic, 1, SIZE_ATTRIBUTE), new VertexAttribute(
                VertexAttributes.Usage.Generic, 1, ROTATION_ATTRIBUTE));
    }

    public int getSize () {
        return vertices.length / BillboardDecal.VERTEX_SIZE;
    }

    public void add (BillboardDecal decal) {
        int groupIndex = groupStrategy.decideBillboardGroup(decal);
        Array<BillboardDecal> targetGroup = groupList.get(groupIndex);
        if (targetGroup == null) {
            targetGroup = groupPool.obtain();
            targetGroup.clear();
            usedGroups.add(targetGroup);
            groupList.insert(groupIndex, targetGroup);
        }
        targetGroup.add(decal);
    }

    public void flush () {
        render();
        clear();
    }

    /** Renders all decals to the buffer and flushes the buffer to the GL when full/done */
    protected void render () {
        groupStrategy.beforeBillboardGroups();
        for (SortedIntList.Node<Array<BillboardDecal>> group : groupList) {
            groupStrategy.beforeBillboardGroup(group.index, group.value);
            ShaderProgram shader = groupStrategy.getBillboardGroupShader(group.index);
            render(shader, group.value);
            groupStrategy.afterBillboardGroup(group.index);
        }
        groupStrategy.afterBillboardGroups();
    }

    /** Renders a group of vertices to the buffer, flushing them to GL when done/full
     *
     * @param decals Decals to render */
    private void render (ShaderProgram shader, Array<BillboardDecal> decals) {
        // batch vertices
        BillboardDecalMaterial lastMaterial = null;
        int idx = 0;
        for (BillboardDecal decal : decals) {
            if (lastMaterial == null || !lastMaterial.equals(decal.getMaterial())) {
                if (idx > 0) {
                    flush(shader, idx);
                    idx = 0;
                }
                decal.material.set();
                lastMaterial = decal.material;
            }
            decal.update();
            System.arraycopy(decal.vertices, 0, vertices, idx, decal.vertices.length);
            idx += decal.vertices.length;
            // if our batch is full we have to flush it
            if (idx == vertices.length) {
                flush(shader, idx);
                idx = 0;
            }
        }
        // at the end if there is stuff left in the batch we render that
        if (idx > 0) {
            flush(shader, idx);
        }
    }

    /** Flushes vertices[0,verticesPosition[ to GL verticesPosition % Decal.SIZE must equal 0
     *
     * @param verticesPosition Amount of elements from the vertices array to flush */
    protected void flush (ShaderProgram shader, int verticesPosition) {
        mesh.setVertices(vertices, 0, verticesPosition);
        mesh.render(shader, GL20.GL_POINTS, 0, verticesPosition / BillboardDecal.VERTEX_SIZE);
    }

    /** Remove all decals from batch */
    protected void clear () {
        groupList.clear();
        groupPool.freeAll(usedGroups);
        usedGroups.clear();
    }

    @Override
    public void dispose() {
        clear();
        vertices = null;
        mesh.dispose();
    }
}
