attribute vec4 a_position;
attribute vec4 a_color;
attribute vec2 a_texCoord0;

varying vec2 v_texCoords;
varying vec2 v_texCoordsGrain;
varying vec2 v_texCoordsScanLines;

uniform mat4 u_projTrans;
uniform vec2 u_grainInverse;
uniform vec2 u_scanLineUVScale;
uniform vec2 u_grainOffset;

void main()
{
	v_texCoords = a_texCoord0;
	v_texCoordsGrain = a_texCoord0 * u_grainInverse + u_grainOffset;
	v_texCoordsScanLines = a_texCoord0 * u_scanLineUVScale;
	gl_Position =  u_projTrans * a_position;
}
