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

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;

public interface BillboardGroupStrategy {

    public ShaderProgram getBillboardGroupShader (int group);

    /** Assigns a group to a decal
     *
     * @param decal BillboardDecal to assign group to
     * @return group assigned */
    public int decideBillboardGroup (BillboardDecal decal);

    /** Invoked directly before rendering the contents of a group
     *
     * @param group Group that will be rendered
     * @param contents Array of entries of arrays containing all the decals in the group */
    public void beforeBillboardGroup (int group, Array<BillboardDecal> contents);

    /** Invoked directly after rendering of a group has completed
     *
     * @param group Group which completed rendering */
    public void afterBillboardGroup (int group);

    /** Invoked before rendering any group */
    public void beforeBillboardGroups ();

    /** Invoked after having rendered all groups */
    public void afterBillboardGroups ();
}
