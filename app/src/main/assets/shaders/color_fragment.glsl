#version 320 es

precision mediump float;

// Input from vertex shader
in vec2 v_texCoord;

// Uniforms
uniform vec4 u_color;
uniform int u_blendMode;

// Output color
out vec4 fragColor;

// Blend mode constants
const int BLEND_ADD = 0;
const int BLEND_MULTIPLY = 1;
const int BLEND_ALPHA = 2;
const int BLEND_XOR = 3;

void main() {
    // Use solid color (no texture)
    vec4 finalColor = u_color;
    
    // For XOR mode, apply some color manipulation
    if (u_blendMode == BLEND_XOR) {
        // Enhance color for invert effect
        finalColor.rgb = 1.0 - finalColor.rgb;
    }
    
    // Discard completely transparent pixels
    if (finalColor.a < 0.01) {
        discard;
    }
    
    fragColor = finalColor;
}