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

attribute vec4 a_position;
attribute vec4 a_color;
attribute vec2 a_texCoord0;

varying vec2 v_texCoords;
varying vec2 v_texCoordsGrain;
varying vec2 v_texCoordsScanLines;
varying vec4 v_color;

uniform mat4 u_projTrans;
uniform vec2 u_grainInverse;
uniform vec2 u_scanLineInverse;
uniform vec2 u_grainOffset;

void main()
{
	v_texCoords = a_texCoord0;
	v_texCoordsGrain = a_texCoord0*u_grainInverse+u_grainOffset;
	v_texCoordsScanLines = a_texCoord0*u_scanLineInverse;
	v_color = a_color;
	gl_Position =  u_projTrans * a_position;
}
