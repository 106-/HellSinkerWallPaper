package net.t106.sinkerglwallpaper.config;

import android.opengl.GLES32;

/**
 * Centralized blend mode management for OpenGL ES 3.2
 * Eliminates duplicated blend function code across filter classes
 */
public class BlendModeManager {
    
    // Blend mode constants matching AThingLeftBehindService.blend_type
    public static final int BLEND_ADDITIVE = 0;
    public static final int BLEND_MULTIPLICATIVE = 1;
    public static final int BLEND_ALPHA = 2;
    public static final int BLEND_XOR = 3;
    public static final int BLEND_INVERT = 4;  // Special mode for right filter
    
    /**
     * Applies the specified blend mode
     * @param blendMode The blend mode to apply
     */
    public static void applyBlendMode(int blendMode) {
        GLES32.glEnable(GLES32.GL_BLEND);
        
        switch(blendMode) {
            case BLEND_ADDITIVE:
                // Additive blending: add colors together
                GLES32.glBlendFunc(GLES32.GL_ONE, GLES32.GL_ONE);
                break;
                
            case BLEND_MULTIPLICATIVE:
                // Multiplicative blending: multiply colors
                GLES32.glBlendFunc(GLES32.GL_ZERO, GLES32.GL_SRC_COLOR);
                break;
                
            case BLEND_ALPHA:
                // Alpha blending with additive component
                GLES32.glBlendFunc(GLES32.GL_SRC_ALPHA, GLES32.GL_ONE);
                break;
                
            case BLEND_XOR:
                // XOR-like blending: exclusive or effect
                GLES32.glBlendFunc(GLES32.GL_ONE_MINUS_DST_COLOR, GLES32.GL_ONE_MINUS_SRC_COLOR);
                break;
                
            case BLEND_INVERT:
                // Invert blending: invert destination color
                GLES32.glBlendFunc(GLES32.GL_ONE_MINUS_DST_COLOR, GLES32.GL_ZERO);
                break;
                
            default:
                // Default to standard alpha blending
                GLES32.glBlendFunc(GLES32.GL_SRC_ALPHA, GLES32.GL_ONE_MINUS_SRC_ALPHA);
                break;
        }
    }
    
    /**
     * Disables blending
     */
    public static void disableBlending() {
        GLES32.glDisable(GLES32.GL_BLEND);
    }
    
    /**
     * Gets a human-readable name for a blend mode
     * @param blendMode The blend mode
     * @return Descriptive name
     */
    public static String getBlendModeName(int blendMode) {
        switch(blendMode) {
            case BLEND_ADDITIVE: return "Additive";
            case BLEND_MULTIPLICATIVE: return "Multiplicative";
            case BLEND_ALPHA: return "Alpha";
            case BLEND_XOR: return "XOR";
            case BLEND_INVERT: return "Invert";
            default: return "Default";
        }
    }
    
    /**
     * Checks if a blend mode is valid
     * @param blendMode The blend mode to check
     * @return True if valid
     */
    public static boolean isValidBlendMode(int blendMode) {
        return blendMode >= BLEND_ADDITIVE && blendMode <= BLEND_INVERT;
    }
}