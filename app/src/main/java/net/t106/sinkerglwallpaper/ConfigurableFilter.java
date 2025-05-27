package net.t106.sinkerglwallpaper;

/**
 * Configurable filter class - replaces left_filter.java
 * Supports user-customizable colors and blend modes
 */
public class ConfigurableFilter extends BaseFilter {
    
    public ConfigurableFilter() {
        // Start with white color, user settings will override
        super(RenderConfig.ColorConfig.WHITE, BlendModeManager.BLEND_ADDITIVE);
    }
    
    @Override
    protected RenderConfig.GeometryConfig getDefaultGeometry() {
        // Default to large center overlay
        return RenderConfig.GeometryConfig.createLeftFilter(false);
    }
    
    @Override
    protected RenderConfig.GeometryConfig getGeometryForSize(boolean smallSize) {
        return RenderConfig.GeometryConfig.createLeftFilter(smallSize);
    }
    
    @Override
    public void Draw(float[] viewMatrix, float[] projectionMatrix) {
        // Update color and blend mode from user settings before drawing
        updateFromUserSettings();
        super.Draw(viewMatrix, projectionMatrix);
    }
    
    /**
     * Updates configuration from user settings in SinkerService
     */
    private void updateFromUserSettings() {
        // Update color from user settings
        colorConfig = RenderConfig.ColorConfig.fromUserSettings(SinkerService.col);
        
        // Update blend mode from user settings
        blendMode = SinkerService.blend_type;
    }
    
    @Override
    protected void applyBlendMode() {
        // Use the user-configured blend mode
        BlendModeManager.applyBlendMode(blendMode);
    }
    
    /**
     * Factory method to create with specific settings
     */
    public static ConfigurableFilter createWithSettings(
            RenderConfig.ColorConfig color, int blendMode) {
        ConfigurableFilter filter = new ConfigurableFilter();
        filter.setColorConfig(color);
        filter.setBlendMode(blendMode);
        return filter;
    }
}