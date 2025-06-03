package net.t106.sinkerglwallpaper.rendering.filters;

import android.opengl.GLES32;
import net.t106.sinkerglwallpaper.config.RenderConfig;
import net.t106.sinkerglwallpaper.config.BlendModeManager;
import net.t106.sinkerglwallpaper.opengl.utils.ShaderUtils;
import net.t106.sinkerglwallpaper.opengl.utils.BufferUtils;
import net.t106.sinkerglwallpaper.opengl.shaders.ShaderLoader;
import net.t106.sinkerglwallpaper.rendering.services.AThingLeftBehindService;
import net.t106.sinkerglwallpaper.rendering.objects.Garland;

/**
 * Base class for all filter objects
 * Eliminates code duplication between LeftFilter and RightFilter
 */
public abstract class BaseFilter extends Garland {
    
    protected RenderConfig.ColorConfig colorConfig;
    protected RenderConfig.GeometryConfig geometryConfig;
    protected int blendMode;
    protected boolean isSmallSize = false;
    
    public BaseFilter(RenderConfig.ColorConfig colorConfig, int blendMode) {
        super();
        this.colorConfig = colorConfig;
        this.blendMode = blendMode;
        this.geometryConfig = getDefaultGeometry();
        
        setupGeometry();
    }
    
    /**
     * Subclasses must provide their default geometry configuration
     */
    protected abstract RenderConfig.GeometryConfig getDefaultGeometry();
    
    /**
     * Subclasses can override to customize geometry for size changes
     */
    protected abstract RenderConfig.GeometryConfig getGeometryForSize(boolean smallSize);
    
    /**
     * Sets up geometry data
     */
    private void setupGeometry() {
        apex = geometryConfig.vertices;
        coords = geometryConfig.texCoords;
        
        // Keep legacy buffer creation for compatibility
        ab = AThingLeftBehindService.makeFloatBuffer(apex);
        cb = AThingLeftBehindService.makeFloatBuffer(coords);
    }
    
    @Override
    protected void createShaderProgram() {
        // Use basic shader program for filters (no texture needed typically)
        shaderProgram = ShaderLoader.Programs.createBasicProgram(AThingLeftBehindService.getContext());
    }
    
    @Override
    public void Draw(float[] viewMatrix, float[] projectionMatrix) {
        // Update MVP matrix (no rotation for filters)
        updateMVP(viewMatrix, projectionMatrix);
        
        // Bind shader and set uniforms
        bindShader();
        
        // Set texture (usually not needed for filters, but set anyway)
        ShaderUtils.setUniform1i(textureLocation, 0);
        
        // Apply custom blend mode
        applyBlendMode();
        
        // Set color from configuration
        ShaderUtils.setUniform4f(colorLocation, 
            colorConfig.red, colorConfig.green, 
            colorConfig.blue, colorConfig.alpha);
        
        // Set blend mode for shader
        ShaderUtils.setUniform1i(blendModeLocation, blendMode);
        
        // Render the filter
        BufferUtils.bindVAO(vao);
        BufferUtils.drawQuad();
        BufferUtils.unbindVAO();
        
        // Clean up blending
        BlendModeManager.disableBlending();
    }
    
    /**
     * Applies the appropriate blend mode for this filter
     * Subclasses can override for custom blending
     */
    protected void applyBlendMode() {
        BlendModeManager.applyBlendMode(blendMode);
    }
    
    @Override
    public void Update(float deltaTime) {
        // Filters are typically static, so no update needed
        // Subclasses can override if animation is needed
    }
    
    @Override
    public void sizechange(boolean smollflg) {
        isSmallSize = smollflg;
        
        // Update geometry based on size
        geometryConfig = getGeometryForSize(smollflg);
        apex = geometryConfig.vertices;
        
        // Update buffer
        ab = AThingLeftBehindService.makeFloatBuffer(apex);
        
        // Recreate VAO with new vertex data
        if (vao != 0) {
            BufferUtils.deleteVAO(vao);
            createBuffers();
        }
    }
    
    /**
     * Updates the color configuration
     */
    public void setColorConfig(RenderConfig.ColorConfig newColorConfig) {
        this.colorConfig = newColorConfig;
    }
    
    /**
     * Updates the blend mode
     */
    public void setBlendMode(int newBlendMode) {
        this.blendMode = newBlendMode;
    }
    
    /**
     * Gets the current color configuration
     */
    public RenderConfig.ColorConfig getColorConfig() {
        return colorConfig;
    }
    
    /**
     * Gets the current blend mode
     */
    public int getBlendMode() {
        return blendMode;
    }
}