attribute vec4 a_position;

varying vec2 v_texCoordsR;
varying vec2 v_texCoordsG;
varying vec2 v_texCoordsB;

const float MAX_SPREAD = 0.008;
 
void main()
{
	v_texCoordsG = 0.5 * (a_position.xy + 1.0);
	vec2 rbOffset = a_position.xy * MAX_SPREAD;
	v_texCoordsR = v_texCoordsG + rbOffset;
	v_texCoordsB = v_texCoordsG -rbOffset;
	gl_Position = a_position;
}