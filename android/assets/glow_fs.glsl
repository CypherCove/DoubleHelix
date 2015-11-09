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
#define MED mediump
#define HIGH highp
precision mediump float;
#else
#define MED
#define LOWP
#define HIGH
#endif

varying MED vec2 v_texCoords;
 
uniform sampler2D u_texture;
uniform LOWP vec3 u_color;
uniform LOWP float u_ambient;

void main()
{
    LOWP vec4 texture = texture2D (u_texture, v_texCoords);
    gl_FragColor = vec4(texture.r + (texture.g + u_ambient) * u_color, 1.0);
}