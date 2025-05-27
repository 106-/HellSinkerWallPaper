package net.t106.sinkerglwallpaper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.opengl.GLES32;
import android.opengl.GLUtils;
import android.util.Log;

/**
 * Texture utility class for OpenGL ES 3.2
 * Handles texture loading, creation, and management
 */
public class TextureUtils {
    private static final String TAG = "TextureUtils";
    
    /**
     * Loads a texture from resources and creates an OpenGL texture
     * @param context Application context
     * @param resourceId Resource ID of the image
     * @return OpenGL texture handle, or 0 if loading failed
     */
    public static int loadTexture(Context context, int resourceId) {
        int[] textures = new int[1];
        GLES32.glGenTextures(1, textures, 0);
        
        if (textures[0] == 0) {
            Log.e(TAG, "Failed to generate texture");
            return 0;
        }
        
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId);
        if (bitmap == null) {
            Log.e(TAG, "Failed to decode resource " + resourceId);
            GLES32.glDeleteTextures(1, textures, 0);
            return 0;
        }
        
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, textures[0]);
        
        // Set texture parameters
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MIN_FILTER, GLES32.GL_LINEAR);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MAG_FILTER, GLES32.GL_LINEAR);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_WRAP_S, GLES32.GL_CLAMP_TO_EDGE);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_WRAP_T, GLES32.GL_CLAMP_TO_EDGE);
        
        // Upload bitmap to GPU
        GLUtils.texImage2D(GLES32.GL_TEXTURE_2D, 0, bitmap, 0);
        
        bitmap.recycle();
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, 0);
        
        return textures[0];
    }
    
    /**
     * Loads a texture and its horizontally flipped version
     * @param context Application context
     * @param resourceId Resource ID of the image
     * @return Array containing [original texture, flipped texture], or null if loading failed
     */
    public static int[] loadTextureWithFlipped(Context context, int resourceId) {
        int[] textures = new int[2];
        GLES32.glGenTextures(2, textures, 0);
        
        if (textures[0] == 0 || textures[1] == 0) {
            Log.e(TAG, "Failed to generate textures");
            return null;
        }
        
        Bitmap originalBitmap = BitmapFactory.decodeResource(context.getResources(), resourceId);
        if (originalBitmap == null) {
            Log.e(TAG, "Failed to decode resource " + resourceId);
            GLES32.glDeleteTextures(2, textures, 0);
            return null;
        }
        
        // Create flipped bitmap
        Matrix flipMatrix = new Matrix();
        flipMatrix.preScale(-1, 1);
        Bitmap flippedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, 
                                                  originalBitmap.getWidth(), 
                                                  originalBitmap.getHeight(), 
                                                  flipMatrix, false);
        
        // Upload original texture
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, textures[0]);
        setDefaultTextureParameters();
        GLUtils.texImage2D(GLES32.GL_TEXTURE_2D, 0, originalBitmap, 0);
        
        // Upload flipped texture
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, textures[1]);
        setDefaultTextureParameters();
        GLUtils.texImage2D(GLES32.GL_TEXTURE_2D, 0, flippedBitmap, 0);
        
        // Cleanup
        originalBitmap.recycle();
        flippedBitmap.recycle();
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, 0);
        
        return textures;
    }
    
    /**
     * Creates an empty texture with specified dimensions
     * @param width Texture width
     * @param height Texture height
     * @param format Internal format (e.g., GLES32.GL_RGBA8)
     * @return Texture handle, or 0 if creation failed
     */
    public static int createEmptyTexture(int width, int height, int format) {
        int[] textures = new int[1];
        GLES32.glGenTextures(1, textures, 0);
        
        if (textures[0] == 0) {
            Log.e(TAG, "Failed to generate empty texture");
            return 0;
        }
        
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, textures[0]);
        setDefaultTextureParameters();
        
        GLES32.glTexImage2D(GLES32.GL_TEXTURE_2D, 0, format, width, height, 0, 
                           GLES32.GL_RGBA, GLES32.GL_UNSIGNED_BYTE, null);
        
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, 0);
        
        return textures[0];
    }
    
    /**
     * Sets default texture parameters for 2D textures
     */
    private static void setDefaultTextureParameters() {
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MIN_FILTER, GLES32.GL_LINEAR);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MAG_FILTER, GLES32.GL_LINEAR);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_WRAP_S, GLES32.GL_CLAMP_TO_EDGE);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_WRAP_T, GLES32.GL_CLAMP_TO_EDGE);
    }
    
    /**
     * Binds a texture to a specific texture unit
     * @param textureUnit Texture unit (0-31)
     * @param textureHandle Texture handle
     */
    public static void bindTexture(int textureUnit, int textureHandle) {
        GLES32.glActiveTexture(GLES32.GL_TEXTURE0 + textureUnit);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, textureHandle);
    }
    
    /**
     * Unbinds all textures from a specific texture unit
     * @param textureUnit Texture unit (0-31)
     */
    public static void unbindTexture(int textureUnit) {
        GLES32.glActiveTexture(GLES32.GL_TEXTURE0 + textureUnit);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, 0);
    }
    
    /**
     * Deletes a texture
     * @param textureHandle Texture handle to delete
     */
    public static void deleteTexture(int textureHandle) {
        int[] textures = {textureHandle};
        GLES32.glDeleteTextures(1, textures, 0);
    }
    
    /**
     * Deletes multiple textures
     * @param textureHandles Array of texture handles to delete
     */
    public static void deleteTextures(int[] textureHandles) {
        GLES32.glDeleteTextures(textureHandles.length, textureHandles, 0);
    }
    
    /**
     * Checks if a texture handle is valid
     * @param textureHandle Texture handle to check
     * @return True if texture is valid, false otherwise
     */
    public static boolean isValidTexture(int textureHandle) {
        return textureHandle != 0 && GLES32.glIsTexture(textureHandle);
    }
}