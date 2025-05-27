package net.t106.sinkerglwallpaper;

import android.opengl.GLES32;
import android.util.Log;

/**
 * Shader utility class for OpenGL ES 3.2
 * Handles shader compilation, linking, and program management
 */
public class ShaderUtils {
    private static final String TAG = "ShaderUtils";
    
    /**
     * Compiles a shader from source code
     * @param type Shader type (GLES32.GL_VERTEX_SHADER or GLES32.GL_FRAGMENT_SHADER)
     * @param source Shader source code
     * @return Compiled shader handle, or 0 if compilation failed
     */
    public static int compileShader(int type, String source) {
        int shader = GLES32.glCreateShader(type);
        if (shader == 0) {
            Log.e(TAG, "Failed to create shader");
            return 0;
        }
        
        GLES32.glShaderSource(shader, source);
        GLES32.glCompileShader(shader);
        
        int[] compiled = new int[1];
        GLES32.glGetShaderiv(shader, GLES32.GL_COMPILE_STATUS, compiled, 0);
        
        if (compiled[0] == 0) {
            String error = GLES32.glGetShaderInfoLog(shader);
            Log.e(TAG, "Shader compilation failed: " + error);
            GLES32.glDeleteShader(shader);
            return 0;
        }
        
        return shader;
    }
    
    /**
     * Creates and links a shader program
     * @param vertexSource Vertex shader source code
     * @param fragmentSource Fragment shader source code
     * @return Linked program handle, or 0 if linking failed
     */
    public static int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = compileShader(GLES32.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }
        
        int fragmentShader = compileShader(GLES32.GL_FRAGMENT_SHADER, fragmentSource);
        if (fragmentShader == 0) {
            GLES32.glDeleteShader(vertexShader);
            return 0;
        }
        
        int program = GLES32.glCreateProgram();
        if (program == 0) {
            Log.e(TAG, "Failed to create program");
            GLES32.glDeleteShader(vertexShader);
            GLES32.glDeleteShader(fragmentShader);
            return 0;
        }
        
        GLES32.glAttachShader(program, vertexShader);
        GLES32.glAttachShader(program, fragmentShader);
        GLES32.glLinkProgram(program);
        
        int[] linked = new int[1];
        GLES32.glGetProgramiv(program, GLES32.GL_LINK_STATUS, linked, 0);
        
        if (linked[0] == 0) {
            String error = GLES32.glGetProgramInfoLog(program);
            Log.e(TAG, "Program linking failed: " + error);
            GLES32.glDeleteProgram(program);
            GLES32.glDeleteShader(vertexShader);
            GLES32.glDeleteShader(fragmentShader);
            return 0;
        }
        
        // Clean up shaders (they're now part of the program)
        GLES32.glDeleteShader(vertexShader);
        GLES32.glDeleteShader(fragmentShader);
        
        return program;
    }
    
    /**
     * Gets the location of a uniform variable in a shader program
     * @param program Shader program handle
     * @param name Uniform variable name
     * @return Uniform location, or -1 if not found
     */
    public static int getUniformLocation(int program, String name) {
        int location = GLES32.glGetUniformLocation(program, name);
        if (location == -1) {
            Log.w(TAG, "Uniform '" + name + "' not found in program");
        }
        return location;
    }
    
    /**
     * Gets the location of an attribute variable in a shader program
     * @param program Shader program handle
     * @param name Attribute variable name
     * @return Attribute location, or -1 if not found
     */
    public static int getAttributeLocation(int program, String name) {
        int location = GLES32.glGetAttribLocation(program, name);
        if (location == -1) {
            Log.w(TAG, "Attribute '" + name + "' not found in program");
        }
        return location;
    }
    
    /**
     * Sets a 4x4 matrix uniform
     * @param location Uniform location
     * @param matrix 4x4 matrix array
     */
    public static void setUniformMatrix4fv(int location, float[] matrix) {
        GLES32.glUniformMatrix4fv(location, 1, false, matrix, 0);
    }
    
    /**
     * Sets a float uniform
     * @param location Uniform location
     * @param value Float value
     */
    public static void setUniform1f(int location, float value) {
        GLES32.glUniform1f(location, value);
    }
    
    /**
     * Sets an integer uniform
     * @param location Uniform location
     * @param value Integer value
     */
    public static void setUniform1i(int location, int value) {
        GLES32.glUniform1i(location, value);
    }
    
    /**
     * Sets a vec4 uniform
     * @param location Uniform location
     * @param x X component
     * @param y Y component
     * @param z Z component
     * @param w W component
     */
    public static void setUniform4f(int location, float x, float y, float z, float w) {
        GLES32.glUniform4f(location, x, y, z, w);
    }
    
    /**
     * Checks for OpenGL errors and logs them
     * @param tag Tag for logging
     * @param operation Description of the operation
     */
    public static void checkGLError(String tag, String operation) {
        int error = GLES32.glGetError();
        if (error != GLES32.GL_NO_ERROR) {
            Log.e(tag, "OpenGL error after " + operation + ": " + error);
        }
    }
}