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
    
    // Calculate luminance to detect dark areas (for black background transparency)
    float luminance = dot(texColor.rgb, vec3(0.299, 0.587, 0.114));
    
    // Apply different blend modes
    if (u_blendMode == BLEND_ADD) {
        // Additive blending: multiply texture with color first, then add in framebuffer
        finalColor.rgb = texColor.rgb * blendColor.rgb;
        finalColor.a = texColor.a * blendColor.a;
        
    } else if (u_blendMode == BLEND_MULTIPLY) {
        // Multiplicative blending: multiply colors
        finalColor = texColor * blendColor;
        
    } else if (u_blendMode == BLEND_ALPHA) {
        // Alpha blending: preserve texture transparency
        finalColor.rgb = texColor.rgb * blendColor.rgb;
        finalColor.a = texColor.a * blendColor.a;
        
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
    
    // Discard dark pixels (black background) to create transparency
    if (luminance < 0.1) {
        discard;
    }
    
    // Discard completely transparent pixels from texture alpha
    if (texColor.a < 0.01) {
        discard;
    }
    
    // Also discard if final alpha is too low
    if (finalColor.a < 0.01) {
        discard;
    }
    
    fragColor = finalColor;
}