#version 320 es

precision mediump float;

// Input from vertex shader
in vec2 v_texCoord;

// Uniforms
uniform sampler2D u_texture;
uniform vec4 u_color;
uniform int u_blendMode;
uniform float u_time;  // For potential animation effects

// Output color
out vec4 fragColor;

// Blend mode constants
const int BLEND_ADD = 0;
const int BLEND_MULTIPLY = 1;
const int BLEND_ALPHA = 2;
const int BLEND_XOR = 3;

void main() {
    // Sample texture
    vec4 texColor = texture(u_texture, v_texCoord);
    vec4 blendColor = u_color;
    vec4 finalColor;
    
    // Apply different blend modes
    if (u_blendMode == BLEND_ADD) {
        // Additive blending: add colors together
        finalColor = clamp(texColor + blendColor, 0.0, 1.0);
        
    } else if (u_blendMode == BLEND_MULTIPLY) {
        // Multiplicative blending: multiply colors
        finalColor = texColor * blendColor;
        
    } else if (u_blendMode == BLEND_ALPHA) {
        // Alpha blending: interpolate based on alpha
        finalColor = mix(texColor, blendColor, blendColor.a);
        
    } else if (u_blendMode == BLEND_XOR) {
        // XOR-like blending: absolute difference
        finalColor = abs(texColor - blendColor);
        
        // Enhance XOR effect with some color manipulation
        finalColor.rgb = 1.0 - finalColor.rgb;
        finalColor.a = max(texColor.a, blendColor.a);
        
    } else {
        // Default: just texture with color tint
        finalColor = texColor * blendColor;
    }
    
    // Ensure alpha is reasonable
    if (finalColor.a < 0.01) {
        discard;
    }
    
    fragColor = finalColor;
}