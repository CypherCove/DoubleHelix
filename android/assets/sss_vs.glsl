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
attribute vec2 a_texCoord0;
attribute vec3 a_normal;
attribute vec3 a_tangent;
attribute vec3 a_binormal;
attribute vec4 a_color;
 
uniform mat4 u_modelViewProjTrans; 
uniform mat4 u_invWorldTrans; //world to local space

uniform vec4 u_worldLightDir;
uniform vec4 u_cameraPosition;
 
varying vec2 v_texCoords;
varying vec3 v_lightDir;
varying vec3 v_viewDir;
varying vec3 v_halfDir;
varying float v_thickness;
varying float v_fade;

const float modelHeightHalf = 22.5 / 2.0;
const float fadeLength = 2.0;
const float keep = modelHeightHalf - fadeLength;
 
void main()
{

    v_fade = 1.0 - smoothstep(keep, modelHeightHalf, abs(a_position.y - modelHeightHalf));

	gl_Position =  u_modelViewProjTrans * a_position;
	v_texCoords = a_texCoord0;
	
	mat3 localToTangentRotation = mat3 (
		a_tangent.x, a_binormal.x, a_normal.x,
		a_tangent.y, a_binormal.y, a_normal.y,
		a_tangent.z, a_binormal.z, a_normal.z);
	
	//put light direction and view direction in local space
	vec3 objLightDir = (u_invWorldTrans * u_worldLightDir).xyz;
	vec3 objCamPos = (u_invWorldTrans * u_cameraPosition).xyz;
	vec3 objViewDir = objCamPos - a_position.xyz;
	
	//rotate light direction and view direction from local space to tangent space
	v_lightDir = normalize(localToTangentRotation * objLightDir);
	v_viewDir = normalize(localToTangentRotation * objViewDir);
	v_halfDir = normalize (v_lightDir + v_viewDir);

    float axialDarken = 1.0 - 0.5 * max((dot(v_viewDir, vec3(0.0, 1.0, 0.0))), (dot(v_viewDir, vec3(0.0, -1.0, 0.0))));
	v_thickness = a_color.r * axialDarken;
}