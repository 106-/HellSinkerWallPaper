package net.t106.sinkerglwallpaper.opengl.utils;

import android.opengl.GLES32;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Buffer utility class for OpenGL ES 3.2
 * Manages VBO (Vertex Buffer Objects) and VAO (Vertex Array Objects)
 */
public class BufferUtils {
    
    /**
     * Creates a FloatBuffer from a float array with native byte order
     * @param data Float array data
     * @return FloatBuffer ready for OpenGL use
     */
    public static FloatBuffer createFloatBuffer(float[] data) {
        ByteBuffer bb = ByteBuffer.allocateDirect(data.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer buffer = bb.asFloatBuffer();
        buffer.put(data);
        buffer.position(0);
        return buffer;
    }
    
    /**
     * Creates a VBO (Vertex Buffer Object) and uploads data to GPU
     * @param data Float array containing vertex data
     * @param usage Buffer usage pattern (e.g., GLES32.GL_STATIC_DRAW)
     * @return VBO handle
     */
    public static int createVBO(float[] data, int usage) {
        int[] buffers = new int[1];
        GLES32.glGenBuffers(1, buffers, 0);
        
        int vbo = buffers[0];
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vbo);
        
        FloatBuffer buffer = createFloatBuffer(data);
        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, data.length * 4, buffer, usage);
        
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, 0);
        return vbo;
    }
    
    /**
     * Creates a VAO (Vertex Array Object)
     * @return VAO handle
     */
    public static int createVAO() {
        int[] arrays = new int[1];
        GLES32.glGenVertexArrays(1, arrays, 0);
        return arrays[0];
    }
    
    /**
     * Configures a vertex attribute for a VBO
     * @param attributeLocation Attribute location in shader
     * @param size Number of components per vertex (1, 2, 3, or 4)
     * @param stride Byte offset between consecutive vertices
     * @param offset Byte offset of the first component
     */
    public static void setVertexAttribute(int attributeLocation, int size, int stride, int offset) {
        GLES32.glVertexAttribPointer(attributeLocation, size, GLES32.GL_FLOAT, false, stride, offset);
        GLES32.glEnableVertexAttribArray(attributeLocation);
    }
    
    /**
     * Creates a complete VAO with vertex and texture coordinate attributes
     * @param vertices Vertex position data (x, y for each vertex)
     * @param texCoords Texture coordinate data (u, v for each vertex)
     * @param positionLocation Position attribute location in shader
     * @param texCoordLocation Texture coordinate attribute location in shader
     * @return VAO handle
     */
    public static int createQuadVAO(float[] vertices, float[] texCoords, 
                                   int positionLocation, int texCoordLocation) {
        int vao = createVAO();
        GLES32.glBindVertexArray(vao);
        
        // Create and bind vertex position VBO
        int vertexVBO = createVBO(vertices, GLES32.GL_STATIC_DRAW);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vertexVBO);
        setVertexAttribute(positionLocation, 2, 0, 0);
        
        // Create and bind texture coordinate VBO
        int texCoordVBO = createVBO(texCoords, GLES32.GL_STATIC_DRAW);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, texCoordVBO);
        setVertexAttribute(texCoordLocation, 2, 0, 0);
        
        GLES32.glBindVertexArray(0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, 0);
        
        return vao;
    }
    
    /**
     * Standard quad vertices for a rectangle from -1 to 1
     */
    public static final float[] QUAD_VERTICES = {
        -1.0f, -1.0f,  // Bottom left
         1.0f, -1.0f,  // Bottom right
        -1.0f,  1.0f,  // Top left
         1.0f,  1.0f   // Top right
    };
    
    /**
     * Standard texture coordinates for a quad
     */
    public static final float[] QUAD_TEX_COORDS = {
        0.0f, 1.0f,  // Bottom left
        1.0f, 1.0f,  // Bottom right
        0.0f, 0.0f,  // Top left
        1.0f, 0.0f   // Top right
    };
    
    /**
     * Deletes a VBO
     * @param vbo VBO handle to delete
     */
    public static void deleteVBO(int vbo) {
        int[] buffers = {vbo};
        GLES32.glDeleteBuffers(1, buffers, 0);
    }
    
    /**
     * Deletes a VAO
     * @param vao VAO handle to delete
     */
    public static void deleteVAO(int vao) {
        int[] arrays = {vao};
        GLES32.glDeleteVertexArrays(1, arrays, 0);
    }
    
    /**
     * Binds a VAO for rendering
     * @param vao VAO handle
     */
    public static void bindVAO(int vao) {
        GLES32.glBindVertexArray(vao);
    }
    
    /**
     * Unbinds the current VAO
     */
    public static void unbindVAO() {
        GLES32.glBindVertexArray(0);
    }
    
    /**
     * Draws a quad using triangle strip
     */
    public static void drawQuad() {
        GLES32.glDrawArrays(GLES32.GL_TRIANGLE_STRIP, 0, 4);
    }
}