#version 320 es

// Vertex attributes
layout(location = 0) in vec2 a_position;
layout(location = 1) in vec2 a_texCoord;

// Uniforms
uniform mat4 u_mvpMatrix;

// Output to fragment shader
out vec2 v_texCoord;

void main() {
    // Transform vertex position using MVP matrix
    gl_Position = u_mvpMatrix * vec4(a_position, 0.0, 1.0);
    
    // Pass texture coordinates to fragment shader
    v_texCoord = a_texCoord;
}