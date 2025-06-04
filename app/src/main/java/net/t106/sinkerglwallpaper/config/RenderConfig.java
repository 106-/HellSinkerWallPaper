package net.t106.sinkerglwallpaper.config;

import net.t106.sinkerglwallpaper.config.BlendModeManager;

/**
 * Configuration classes for data-driven rendering
 * Eliminates hardcoded values and enables flexible object behavior
 */
public class RenderConfig {
    
    /**
     * Configuration for rotating objects
     */
    public static class RotationConfig {
        public final float rotationSpeed;
        public final int maxCount;
        public final boolean clockwise;
        
        public RotationConfig(float rotationSpeed, int maxCount, boolean clockwise) {
            this.rotationSpeed = rotationSpeed;
            this.maxCount = maxCount;
            this.clockwise = clockwise;
        }
        
        // Predefined configurations
        public static final RotationConfig CENTER = new RotationConfig(-0.125f, 2881, false);
        public static final RotationConfig BACKGROUND = new RotationConfig(0.125f, 2880, true);
    }
    
    /**
     * Configuration for colors
     */
    public static class ColorConfig {
        public final float red;
        public final float green;
        public final float blue;
        public final float alpha;
        
        public ColorConfig(float red, float green, float blue, float alpha) {
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.alpha = alpha;
        }
        
        // Predefined colors
        public static final ColorConfig WHITE = new ColorConfig(1.0f, 1.0f, 1.0f, 1.0f);
        public static final ColorConfig REDDISH_BROWN = new ColorConfig(0.375f, 0.04f, 0.09f, 0.5f);
        public static final ColorConfig PINKISH = new ColorConfig(1.0f, 0.5f, 0.5f, 0.5f);
        
    }
    
    /**
     * Configuration for textures
     */
    public static class TextureConfig {
        public final int textureIndex;
        public final boolean useTexture;
        
        public TextureConfig(int textureIndex, boolean useTexture) {
            this.textureIndex = textureIndex;
            this.useTexture = useTexture;
        }
        
        // Predefined texture configurations
        public static final TextureConfig TEXTURE_0 = new TextureConfig(0, true);
        public static final TextureConfig TEXTURE_1 = new TextureConfig(1, true);
        public static final TextureConfig NO_TEXTURE = new TextureConfig(0, false);
    }
    
    /**
     * Configuration for geometry
     */
    public static class GeometryConfig {
        public final float[] vertices;
        public final float[] texCoords;
        public final float scale;
        
        public GeometryConfig(float[] vertices, float[] texCoords, float scale) {
            this.vertices = vertices.clone();
            this.texCoords = texCoords.clone();
            this.scale = scale;
        }
        
        // Predefined geometry configurations
        public static final GeometryConfig STANDARD_QUAD = new GeometryConfig(
            new float[] { -1f, -1f, 1f, -1f, -1f, 1f, 1f, 1f },
            new float[] { 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f },
            1.0f
        );
        
        public static final GeometryConfig LARGE_QUAD = new GeometryConfig(
            new float[] { -1.5f, -1.5f, 1.5f, -1.5f, -1.5f, 1.5f, 1.5f, 1.5f },
            new float[] { 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f },
            1.5f
        );
        
        /**
         * Creates a right-side filter geometry
         */
        public static GeometryConfig createRightFilter(boolean smallSize) {
            if (smallSize) {
                return new GeometryConfig(
                    new float[] { 0f, -1.5f, 0.5f, -1.5f, 0f, 1.5f, 0.5f, 1.5f },
                    new float[] { 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f },
                    0.5f
                );
            } else {
                return new GeometryConfig(
                    new float[] { 0f, -1.5f, 0.7f, -1.5f, 0f, 1.5f, 0.7f, 1.5f },
                    new float[] { 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f },
                    0.7f
                );
            }
        }
        
        /**
         * Creates a left filter geometry (center overlay)
         */
        public static GeometryConfig createLeftFilter(boolean smallSize) {
            if (smallSize) {
                return new GeometryConfig(
                    new float[] { -0.5f, -1.5f, 0.5f, -1.5f, -0.5f, 1.5f, 0.5f, 1.5f },
                    new float[] { 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f },
                    1.0f
                );
            } else {
                return new GeometryConfig(
                    new float[] { -0.7f, -1.5f, 0.7f, -1.5f, -0.7f, 1.5f, 0.7f, 1.5f },
                    new float[] { 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f },
                    1.4f
                );
            }
        }
    }
    
    /**
     * Complete rendering configuration combining all aspects
     */
    public static class CompleteConfig {
        public final RotationConfig rotation;
        public final ColorConfig color;
        public final TextureConfig texture;
        public final GeometryConfig geometry;
        public final int blendMode;
        
        public CompleteConfig(RotationConfig rotation, ColorConfig color, 
                            TextureConfig texture, GeometryConfig geometry, int blendMode) {
            this.rotation = rotation;
            this.color = color;
            this.texture = texture;
            this.geometry = geometry;
            this.blendMode = blendMode;
        }
        
        // Predefined complete configurations
        public static final CompleteConfig CENTER_GARLAND = new CompleteConfig(
            RotationConfig.CENTER, ColorConfig.WHITE, TextureConfig.TEXTURE_0,
            GeometryConfig.STANDARD_QUAD, BlendModeManager.BLEND_ADDITIVE
        );
        
        public static final CompleteConfig BACKGROUND_GARLAND = new CompleteConfig(
            RotationConfig.BACKGROUND, ColorConfig.REDDISH_BROWN, TextureConfig.TEXTURE_1,
            GeometryConfig.LARGE_QUAD, BlendModeManager.BLEND_ADDITIVE
        );
    }
}