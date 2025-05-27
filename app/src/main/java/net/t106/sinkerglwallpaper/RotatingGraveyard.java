package net.t106.sinkerglwallpaper;

import android.opengl.GLES32;

/**
 * Unified rotating graveyard object for OpenGL ES 3.2
 * Replaces both center_gy and back_gy with configuration-driven behavior
 */
public class RotatingGraveyard extends graveyard {
    
    private final RenderConfig.CompleteConfig config;
    private float rotation = 0.0f;
    
    public RotatingGraveyard(RenderConfig.CompleteConfig config) {
        super();
        this.config = config;
        
        // Set up geometry from configuration
        apex = config.geometry.vertices;
        coords = config.geometry.texCoords;
        
        // Keep legacy buffer creation for compatibility
        ab = SinkerService.makeFloatBuffer(apex);
        cb = SinkerService.makeFloatBuffer(coords);
    }
    
    /**
     * Factory methods for common configurations
     */
    public static RotatingGraveyard createCenter() {
        return new RotatingGraveyard(RenderConfig.CompleteConfig.CENTER_GRAVEYARD);
    }
    
    public static RotatingGraveyard createBackground() {
        return new RotatingGraveyard(RenderConfig.CompleteConfig.BACKGROUND_GRAVEYARD);
    }
    
    @Override
    protected void createShaderProgram() {
        // Use blend shader program for texture rendering with color tinting
        shaderProgram = ShaderLoader.Programs.createBlendProgram(SinkerService.getContext());
    }
    
    @Override
    public void Draw(float[] viewMatrix, float[] projectionMatrix) {
        // Update MVP matrix with current rotation
        updateMVP(viewMatrix, projectionMatrix);
        
        // Bind shader and set uniforms
        bindShader();
        
        // Set texture if enabled
        if (config.texture.useTexture) {
            TextureUtils.bindTexture(0, SinkerService.textures[config.texture.textureIndex]);
        }
        ShaderUtils.setUniform1i(textureLocation, 0);
        
        // Set blend mode
        ShaderUtils.setUniform1i(blendModeLocation, config.blendMode);
        
        // Set color from configuration
        ShaderUtils.setUniform4f(colorLocation, 
            config.color.red, config.color.green, 
            config.color.blue, config.color.alpha);
        
        // Apply blending
        BlendModeManager.applyBlendMode(config.blendMode);
        
        // Render the object
        BufferUtils.bindVAO(vao);
        BufferUtils.drawQuad();
        BufferUtils.unbindVAO();
        
        // Clean up
        BlendModeManager.disableBlending();
        
        if (config.texture.useTexture) {
            TextureUtils.unbindTexture(0);
        }
    }
    
    @Override
    public void Update(float deltaTime) {
        // Update rotation counter
        cnt++;
        if (cnt >= config.rotation.maxCount) {
            cnt = 0;
        }
        
        // Calculate rotation angle based on configuration
        float speed = config.rotation.clockwise ? config.rotation.rotationSpeed : -config.rotation.rotationSpeed;
        rotation = speed * cnt;
        
        // Update model matrix with rotation
        modelMatrix = MatrixUtils.rotateZ(rotation);
    }
    
    /**
     * Gets the current rotation angle in degrees
     */
    public float getCurrentRotation() {
        return rotation;
    }
    
    /**
     * Gets the configuration used by this object
     */
    public RenderConfig.CompleteConfig getConfig() {
        return config;
    }
    
    /**
     * Creates a custom rotating graveyard with specific parameters
     */
    public static RotatingGraveyard createCustom(
            float rotationSpeed, 
            int maxCount, 
            boolean clockwise,
            RenderConfig.ColorConfig color,
            int textureIndex,
            float scale) {
        
        RenderConfig.RotationConfig rotation = new RenderConfig.RotationConfig(
            rotationSpeed, maxCount, clockwise);
        
        RenderConfig.TextureConfig texture = new RenderConfig.TextureConfig(
            textureIndex, true);
        
        RenderConfig.GeometryConfig geometry = scale == 1.0f ? 
            RenderConfig.GeometryConfig.STANDARD_QUAD : 
            RenderConfig.GeometryConfig.LARGE_QUAD;
            
        RenderConfig.CompleteConfig config = new RenderConfig.CompleteConfig(
            rotation, color, texture, geometry, BlendModeManager.BLEND_ADDITIVE);
            
        return new RotatingGraveyard(config);
    }
}