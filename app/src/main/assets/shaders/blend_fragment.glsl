#version 320 es

precision mediump float;

// Input from vertex shader
in vec2 v_texCoord;

uniform sampler2D u_texture;
uniform vec4 u_color;
uniform int u_blendMode;
uniform float u_time;

out vec4 fragColor;

void main() {
    // gr.png is the original m_car3_al mask. Its grayscale value is the
    // texture alpha; the matching m_car3 RGB image is uniformly white.
    float mask = texture(u_texture, v_texCoord).r;
    fragColor = vec4(u_color.rgb, mask * u_color.a);
}
