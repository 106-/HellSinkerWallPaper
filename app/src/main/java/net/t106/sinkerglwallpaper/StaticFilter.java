package net.t106.sinkerglwallpaper;

/**
 * Static filter class - replaces right_filter.java
 * Provides a fixed-color vertical strip with invert blending
 */
public class StaticFilter extends BaseFilter {
    
    public StaticFilter() {
        // Use pinkish color with invert blend mode
        super(RenderConfig.ColorConfig.PINKISH, BlendModeManager.BLEND_INVERT);
    }
    
    @Override
    protected RenderConfig.GeometryConfig getDefaultGeometry() {
        // Default to normal-sized right filter
        return RenderConfig.GeometryConfig.createRightFilter(false);
    }
    
    @Override
    protected RenderConfig.GeometryConfig getGeometryForSize(boolean smallSize) {
        return RenderConfig.GeometryConfig.createRightFilter(smallSize);
    }
    
    @Override
    protected void applyBlendMode() {
        // Always use invert blend mode for static filter
        BlendModeManager.applyBlendMode(BlendModeManager.BLEND_INVERT);
    }
    
    /**
     * Factory method to create with custom color
     */
    public static StaticFilter createWithColor(RenderConfig.ColorConfig color) {
        StaticFilter filter = new StaticFilter();
        filter.setColorConfig(color);
        return filter;
    }
    
    /**
     * Factory method to create a left-side static filter
     */
    public static StaticFilter createLeftSide(RenderConfig.ColorConfig color) {
        StaticFilter filter = new StaticFilter() {
            @Override
            protected RenderConfig.GeometryConfig getDefaultGeometry() {
                return RenderConfig.GeometryConfig.createLeftFilter(false);
            }
            
            @Override
            protected RenderConfig.GeometryConfig getGeometryForSize(boolean smallSize) {
                return RenderConfig.GeometryConfig.createLeftFilter(smallSize);
            }
        };
        filter.setColorConfig(color);
        return filter;
    }
}