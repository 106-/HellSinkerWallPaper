package net.t106.sinkerglwallpaper.rendering.filters;
import android.opengl.GLES32;
import net.t106.sinkerglwallpaper.opengl.utils.ShaderUtils;
import net.t106.sinkerglwallpaper.opengl.utils.BufferUtils;
import net.t106.sinkerglwallpaper.opengl.shaders.ShaderLoader;
import net.t106.sinkerglwallpaper.rendering.services.AThingLeftBehindService;
import net.t106.sinkerglwallpaper.rendering.objects.Garland;

/**
 * Right side filter for OpenGL ES 3.2
 * Inverts the right half after both sprite layers have been drawn.
 */
public class RightFilter extends Garland {

	private static final float FILTER_WIDTH = 72.45f;

	public RightFilter()
	{
		super();
		apex = new float[] {
			0f, -240f, FILTER_WIDTH, -240f,
			0f, 240f, FILTER_WIDTH, 240f,
		};
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

		// With an opaque white source this is exactly 1 - destination RGB:
		// S * (1 - D) + D * (1 - S).
		ShaderUtils.setUniform1i(blendModeLocation, 0);
		ShaderUtils.setUniform4f(colorLocation, 1.0f, 1.0f, 1.0f, 1.0f);
		GLES32.glBlendEquation(GLES32.GL_FUNC_ADD);
		GLES32.glBlendFuncSeparate(
			GLES32.GL_ONE_MINUS_DST_COLOR, GLES32.GL_ONE_MINUS_SRC_COLOR,
			GLES32.GL_ONE, GLES32.GL_ZERO);
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
	public void sizechange(boolean smallflg)
	{
		// The viewport dimensions are supplied by setViewport().
	}

	public void setViewport(float halfWidth, float halfHeight)
	{
		float width = Math.min(FILTER_WIDTH, halfWidth);
		apex = new float[] {
			0f, -halfHeight, width, -halfHeight,
			0f, halfHeight, width, halfHeight,
		};
		// Update buffer
		ab = AThingLeftBehindService.makeFloatBuffer(apex);
		
		// Recreate VAO with new vertex data
		if (vao != 0) {
			BufferUtils.deleteVAO(vao);
			createBuffers();
		}
	}
}
