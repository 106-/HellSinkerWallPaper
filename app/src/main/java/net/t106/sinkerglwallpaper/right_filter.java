package net.t106.sinkerglwallpaper;
import android.opengl.GLES32;

/**
 * Right side filter for OpenGL ES 3.2
 * Renders a vertical colored strip on the right side with invert blend mode
 */
public class right_filter extends graveyard {

	private boolean isSmallSize = false;
	
	// Filter color (pinkish)
	private static final float RED = 1.0f;
	private static final float GREEN = 0.5f;
	private static final float BLUE = 0.5f;
	private static final float ALPHA = 0.5f;

	public right_filter()
	{
		super();
		// Right side vertical strip (default size)
		apex = new float[] { 0f, -2.5f, 0.7f, -2.5f, 0f, 2.5f, 0.7f, 2.5f, };
		// No texture coordinates needed for color-only rendering
		coords = new float[] { 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f };
		
		// Keep legacy buffer creation for compatibility
		ab = SinkerService.makeFloatBuffer(apex);
		cb = SinkerService.makeFloatBuffer(coords);
	}
	
	@Override
	protected void createShaderProgram() {
		// Use basic shader program without texture
		shaderProgram = ShaderLoader.Programs.createBasicProgram(SinkerService.getContext());
	}
	
	@Override
	public void Draw(float[] viewMatrix, float[] projectionMatrix) {
		// Update MVP matrix (no rotation, just basic transformation)
		updateMVP(viewMatrix, projectionMatrix);
		
		// Bind shader and set uniforms
		bindShader();
		
		// No texture binding needed for color-only rendering
		ShaderUtils.setUniform1i(textureLocation, 0);
		
		// Set blend mode to custom invert effect (can be simulated with XOR mode)
		ShaderUtils.setUniform1i(blendModeLocation, 3); // XOR mode
		
		// Set filter color (pinkish)
		ShaderUtils.setUniform4f(colorLocation, RED, GREEN, BLUE, ALPHA);
		
		// Enable special blending for invert effect
		GLES32.glEnable(GLES32.GL_BLEND);
		GLES32.glBlendFunc(GLES32.GL_ONE_MINUS_DST_COLOR, GLES32.GL_ZERO);
		
		// Bind VAO and draw
		BufferUtils.bindVAO(vao);
		BufferUtils.drawQuad();
		BufferUtils.unbindVAO();
		
		// Disable blending
		GLES32.glDisable(GLES32.GL_BLEND);
	}

	@Override
	public void Update(float deltaTime) {
		// No animation needed for static filter
	}
	
	@Override
	public void sizechange(boolean smollflg)
	{
		isSmallSize = smollflg;
		
		// Update vertex data based on size
		if(smollflg) {
			apex = new float[] { 0f, -1.5f, 0.5f, -1.5f, 0f, 1.5f, 0.5f, 1.5f, };
		} else {
			apex = new float[] { 0f, -1.5f, 0.7f, -1.5f, 0f, 1.5f, 0.7f, 1.5f, };
		}
		
		// Update buffer
		ab = SinkerService.makeFloatBuffer(apex);
		
		// Recreate VAO with new vertex data
		if (vao != 0) {
			BufferUtils.deleteVAO(vao);
			createBuffers();
		}
	}
}
