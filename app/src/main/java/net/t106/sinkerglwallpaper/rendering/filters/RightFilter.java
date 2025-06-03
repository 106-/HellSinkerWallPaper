package net.t106.sinkerglwallpaper.rendering.filters;
import android.opengl.GLES32;
import net.t106.sinkerglwallpaper.opengl.utils.ShaderUtils;
import net.t106.sinkerglwallpaper.opengl.utils.BufferUtils;
import net.t106.sinkerglwallpaper.opengl.shaders.ShaderLoader;
import net.t106.sinkerglwallpaper.rendering.services.AThingLeftBehindService;
import net.t106.sinkerglwallpaper.rendering.objects.Garland;

/**
 * Right side filter for OpenGL ES 3.2
 * Renders a vertical colored strip on the right side with invert blend mode
 */
public class RightFilter extends Garland {

	private boolean isSmallSize = false;
	
	// Filter color (pinkish)
	private static final float RED = 1.0f;
	private static final float GREEN = 0.5f;
	private static final float BLUE = 0.5f;
	private static final float ALPHA = 0.5f;

	public RightFilter()
	{
		super();
		// Right side vertical strip (default size)
		apex = new float[] { 0f, -2.5f, 0.7f, -2.5f, 0f, 2.5f, 0.7f, 2.5f, };
		// No texture coordinates needed for color-only rendering
		coords = new float[] { 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f };
		
		// Keep legacy buffer creation for compatibility
		ab = AThingLeftBehindService.makeFloatBuffer(apex);
		cb = AThingLeftBehindService.makeFloatBuffer(coords);
	}
	
	@Override
	protected void createShaderProgram() {
		// Use color shader program for color-only rendering
		shaderProgram = ShaderLoader.Programs.createColorProgram(AThingLeftBehindService.getContext());
	}
	
	@Override
	public void Draw(float[] viewMatrix, float[] projectionMatrix) {
		// Update MVP matrix (no rotation, just basic transformation)
		updateMVP(viewMatrix, projectionMatrix);
		
		// Bind shader and set uniforms
		bindShader();

		GLES32.glEnable(GLES32.GL_BLEND);

		ShaderUtils.setUniform4f(colorLocation, 0.2f, 0.4f, 0.60f, 0.4f);
		GLES32.glBlendFunc(GLES32.GL_ZERO, GLES32.GL_SRC_COLOR);
		BufferUtils.bindVAO(vao);
		BufferUtils.drawQuad();
		BufferUtils.unbindVAO();

		ShaderUtils.setUniform4f(colorLocation, 0.85f, 0.85f, 0.85f, 1.00f);
		GLES32.glBlendFunc(GLES32.GL_ONE_MINUS_DST_COLOR, GLES32.GL_ZERO);
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
		ab = AThingLeftBehindService.makeFloatBuffer(apex);
		
		// Recreate VAO with new vertex data
		if (vao != 0) {
			BufferUtils.deleteVAO(vao);
			createBuffers();
		}
	}
}
