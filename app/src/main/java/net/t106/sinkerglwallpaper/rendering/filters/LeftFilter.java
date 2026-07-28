package net.t106.sinkerglwallpaper.rendering.filters;
import android.opengl.GLES32;
import net.t106.sinkerglwallpaper.opengl.utils.ShaderUtils;
import net.t106.sinkerglwallpaper.opengl.utils.BufferUtils;
import net.t106.sinkerglwallpaper.opengl.shaders.ShaderLoader;
import net.t106.sinkerglwallpaper.rendering.services.AThingLeftBehindService;
import net.t106.sinkerglwallpaper.rendering.objects.Garland;

/**
 * The full-width central band used by key 20 and key 36.
 */
public class LeftFilter extends Garland {

	static final float FILTER_WIDTH = 72.45f;

	public LeftFilter()
	{
		super();
		apex = new float[] {
			-FILTER_WIDTH, -240f, FILTER_WIDTH, -240f,
			-FILTER_WIDTH, 240f, FILTER_WIDTH, 240f,
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
	
	/**
	 * key 20: opaque #401060 plate behind both sprite layers.
	 */
	public void DrawBase(float[] viewMatrix, float[] projectionMatrix) {
		DrawBase(viewMatrix, projectionMatrix, 0xFF401060);
	}

	public void DrawBase(
		float[] viewMatrix, float[] projectionMatrix, int argb) {
		updateMVP(viewMatrix, projectionMatrix);
		bindShader();

		GLES32.glDisable(GLES32.GL_BLEND);
		ShaderUtils.setUniform1i(blendModeLocation, 0);
		setColor(argb);

		BufferUtils.bindVAO(vao);
		BufferUtils.drawQuad();
		BufferUtils.unbindVAO();
	}

	@Override
	public void Draw(float[] viewMatrix, float[] projectionMatrix) {
		DrawOverlay(viewMatrix, projectionMatrix, 0x40606060);
	}

	public void DrawOverlay(
		float[] viewMatrix, float[] projectionMatrix, int argb) {
		// Update MVP matrix (no rotation, just basic transformation)
		updateMVP(viewMatrix, projectionMatrix);

		// Bind shader and set uniforms
		bindShader();

		GLES32.glEnable(GLES32.GL_BLEND);

		ShaderUtils.setUniform1i(blendModeLocation, 0);
		setColor(argb);
		GLES32.glBlendEquation(GLES32.GL_FUNC_ADD);
		GLES32.glBlendFunc(GLES32.GL_SRC_ALPHA, GLES32.GL_ONE_MINUS_SRC_ALPHA);
		BufferUtils.bindVAO(vao);
		BufferUtils.drawQuad();
		BufferUtils.unbindVAO();

		GLES32.glDisable(GLES32.GL_BLEND);
	}

	private void setColor(int argb) {
		ShaderUtils.setUniform4f(
			colorLocation,
			((argb >> 16) & 0xFF) / 255.0f,
			((argb >> 8) & 0xFF) / 255.0f,
			(argb & 0xFF) / 255.0f,
			((argb >>> 24) & 0xFF) / 255.0f);
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
			-width, -halfHeight, width, -halfHeight,
			-width, halfHeight, width, halfHeight,
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
