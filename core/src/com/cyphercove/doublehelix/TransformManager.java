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

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by Darren on 9/3/2015.
 */
public class TransformManager {

    Vector3 eulers = new Vector3();
    /** For local rotations unaffected by position */
    Vector3 localEulers = new Vector3();
    Vector3 position = new Vector3();
    ModelInstance modelInstance;
    Matrix4 tmp = new Matrix4();

    public TransformManager (ModelInstance modelInstance){
        this.modelInstance = modelInstance;
    }

    public void apply (){
        modelInstance.transform.idt().translate(position).rotate(Vector3.X, eulers.x).rotate(Vector3.Y, eulers.y)
                .rotate(Vector3.Z, eulers.z);
        tmp.idt().rotate(Vector3.X, localEulers.x).rotate(Vector3.Y, localEulers.y).rotate(Vector3.Z, localEulers.z);
        modelInstance.transform.mul(tmp);
    }

}
