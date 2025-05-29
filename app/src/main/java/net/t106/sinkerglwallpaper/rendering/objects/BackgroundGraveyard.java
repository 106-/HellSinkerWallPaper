package net.t106.sinkerglwallpaper.rendering.objects;

import android.opengl.GLES32;
import net.t106.sinkerglwallpaper.opengl.utils.MatrixUtils;
import net.t106.sinkerglwallpaper.opengl.utils.ShaderUtils;
import net.t106.sinkerglwallpaper.opengl.utils.BufferUtils;
import net.t106.sinkerglwallpaper.opengl.utils.TextureUtils;
import net.t106.sinkerglwallpaper.opengl.shaders.ShaderLoader;
import net.t106.sinkerglwallpaper.rendering.services.SinkerService;

/**
 * Background rotating graveyard object for OpenGL ES 3.2
 * Larger than CenterGraveyard and rotates in opposite direction with color tint
 */
public class BackgroundGraveyard extends Graveyard {
	
	private float rotation = 0.0f;
	private static final float ROTATION_SPEED = 0.125f;  // Positive rotation (opposite to center)
	private static final int MAX_COUNT = 2880;
	
	public BackgroundGraveyard()
	{
		super();
		// Larger quad vertices (1.5x scale compared to CenterGraveyard)
		apex = new float[] { -1.5f, -1.5f, 1.5f, -1.5f, -1.5f, 1.5f, 1.5f, 1.5f, };
		coords = new float[] {0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, };
		
		// Keep legacy buffer creation for compatibility
		ab = SinkerService.makeFloatBuffer(apex);
		cb = SinkerService.makeFloatBuffer(coords);
	}

	@Override
	protected void createShaderProgram() {
		// Use blend shader program for additive blending with color tint
		shaderProgram = ShaderLoader.Programs.createBlendProgram(SinkerService.getContext());
	}

	@Override
	public void Draw(float[] viewMatrix, float[] projectionMatrix) {
		// Update MVP matrix with current rotation
		updateMVP(viewMatrix, projectionMatrix);
		
		// Bind shader and set uniforms
		bindShader();
		
		// Set texture (using flipped texture)
		TextureUtils.bindTexture(0, SinkerService.textures[1]);
		
		// Set blend mode to additive (0)
		ShaderUtils.setUniform1i(blendModeLocation, 0);
		
		// Set color tint (reddish-brown tint)
		ShaderUtils.setUniform4f(colorLocation, 0.375f, 0.04f, 0.09f, 1.0f);
		
		// Enable blending for additive effect
		GLES32.glEnable(GLES32.GL_BLEND);
		GLES32.glBlendFunc(GLES32.GL_ONE, GLES32.GL_ONE);
		
		// Bind VAO and draw
		BufferUtils.bindVAO(vao);
		BufferUtils.drawQuad();
		BufferUtils.unbindVAO();
		
		// Disable blending
		GLES32.glDisable(GLES32.GL_BLEND);
		
		// Unbind texture
		TextureUtils.unbindTexture(0);
	}

	@Override
	public void Update(float deltaTime) {
		// Update rotation counter
		cnt++;
		if(cnt >= MAX_COUNT) cnt = 0;
		
		// Calculate rotation angle (positive direction)
		rotation = ROTATION_SPEED * cnt;
		
		// Update model matrix with rotation
		modelMatrix = MatrixUtils.rotateZ(rotation);
	}
}
