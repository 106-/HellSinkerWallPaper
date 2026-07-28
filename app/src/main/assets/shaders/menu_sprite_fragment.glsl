#version 320 es

precision mediump float;

in vec2 v_texCoord;

uniform sampler2D u_rgbTexture;
uniform sampler2D u_alphaTexture;
uniform vec4 u_tint;

out vec4 fragColor;

void main() {
    vec3 sourceRgb = texture(u_rgbTexture, v_texCoord).rgb;
    float sourceAlpha = texture(u_alphaTexture, v_texCoord).r;
    fragColor = vec4(sourceRgb * u_tint.rgb, sourceAlpha * u_tint.a);
}
