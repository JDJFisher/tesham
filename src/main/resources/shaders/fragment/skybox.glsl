#version 330

in vec2 outTextureCoord;

layout (location = 0) out vec4 fragmentColour;

uniform float ambientLightBrightness;
uniform sampler2D texture_Sampler;

void main()
{
    fragmentColour = vec4(ambientLightBrightness * texture(texture_Sampler, outTextureCoord).rgb, 1);
}