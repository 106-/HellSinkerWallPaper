#version 320 es

precision mediump float;

// Input from vertex shader
in vec2 v_texCoord;

// Uniforms
uniform sampler2D u_texture;
uniform vec4 u_color;
uniform int u_blendMode;

// Output color
out vec4 fragColor;

void main() {
    // Sample texture
    vec4 texColor = texture(u_texture, v_texCoord);
    vec4 finalColor;
    
    // Apply blend mode
    if (u_blendMode == 0) {
        // Additive blending
        finalColor = texColor + u_color;
    } else if (u_blendMode == 1) {
        // Multiplicative blending
        finalColor = texColor * u_color;
    } else if (u_blendMode == 2) {
        // Alpha blending
        finalColor = mix(texColor, u_color, u_color.a);
    } else if (u_blendMode == 3) {
        // XOR blending (simulated)
        finalColor = abs(texColor - u_color);
    } else {
        // Default: just texture
        finalColor = texColor;
    }
    
    fragColor = finalColor;
}