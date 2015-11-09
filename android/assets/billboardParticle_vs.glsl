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
attribute float a_size;
attribute float a_angle;

varying vec4 v_color;
varying vec4 v_channelStrength;

uniform mat4 u_projTrans;
uniform vec4 u_baseColor;

void main()
{
	vec4 data = a_color;
	data.a = data.a * (255.0/254.0);

	v_color = vec4(mix(u_baseColor.rgb, vec3(1.0), data.r) * data.a, data.a);

    v_channelStrength = max(vec4(0.0), 1.0 - 3.0 * abs(vec4(0.0, 0.333333, 0.666667, 1.0) - data.b));

	gl_Position =  u_projTrans * a_position;

	gl_PointSize = a_size;
}
