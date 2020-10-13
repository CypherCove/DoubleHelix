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
varying LOWP vec3 v_lightDir;
varying vec3 v_viewDir;
varying LOWP vec3 v_halfDir;
varying float v_thickness;
varying LOWP float v_fade;
 
uniform sampler2D u_texture;
uniform sampler2D u_specularPowerLUTTexture;
uniform LOWP vec3 u_color;
uniform LOWP vec3 u_internalColor;

const float ambientBrightness = 0.125;
const float wrapFactor = 0.1;
const float normalDistortionFactor = 0.02;
const float subsurfaceAmbient = 0.15;
const float subsurfaceAttenuation = 3.0;
const float diffuseAttenuation = 0.8;

void main()
{
    vec3 lightDir = normalize(v_lightDir);
	vec3 viewDir = normalize(v_viewDir);

	LOWP vec4 texture = texture2D (u_texture, v_texCoords);
	LOWP vec3 normal = texture.rgb * 2.0 - 1.0;

	LOWP float diff =  max(0.0, dot(normal, lightDir));
	diff = max(0.0, (diff + wrapFactor) / (1.0 + wrapFactor)) * diffuseAttenuation + ambientBrightness*texture.a*texture.a;
	LOWP float nh = max(0.0, dot(normal, v_halfDir));

	vec3 distortedLight = lightDir + normal * normalDistortionFactor;
	float subSurfaceDirect = max(0.0, min(1.0, dot(viewDir, -distortedLight)));

	vec2 specAndSSDirect = texture2D (u_specularPowerLUTTexture, vec2(nh,subSurfaceDirect)).rg;

	float subSurface = subsurfaceAttenuation * (specAndSSDirect.g * v_thickness + subsurfaceAmbient);

	gl_FragColor = vec4((diff + subSurface) * u_color + specAndSSDirect.r , v_fade);

}