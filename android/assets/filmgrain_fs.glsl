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

#ifdef GL_ES
	#define LOWP lowp
	precision mediump float;
#else
	#define LOWP 
#endif

varying vec2 v_texCoords;
varying vec2 v_texCoordsGrain;
varying vec2 v_texCoordsScanLines;
varying vec4 v_color;

uniform sampler2D u_texture;
uniform sampler2D u_noiseTexture;
uniform sampler2D u_scanLineTexture;
uniform float u_flicker;
     
void main()
{
	float grain = v_color.r*texture2D(u_noiseTexture,v_texCoordsGrain).a;
	float scanLine = v_color.g*texture2D(u_scanLineTexture,v_texCoordsScanLines).a;
	float vignette = v_color.b*texture2D(u_texture,v_texCoords).a;
	gl_FragColor = vec4(vec3(grain), (vignette*u_flicker + grain + scanLine));
}