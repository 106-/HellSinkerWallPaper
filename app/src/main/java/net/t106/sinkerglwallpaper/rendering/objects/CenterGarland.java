package net.t106.sinkerglwallpaper.rendering.objects;

import android.opengl.GLES32;
import net.t106.sinkerglwallpaper.opengl.utils.MatrixUtils;
import net.t106.sinkerglwallpaper.opengl.utils.ShaderUtils;
import net.t106.sinkerglwallpaper.opengl.utils.BufferUtils;
import net.t106.sinkerglwallpaper.opengl.utils.TextureUtils;
import net.t106.sinkerglwallpaper.opengl.shaders.ShaderLoader;
import net.t106.sinkerglwallpaper.rendering.services.AThingLeftBehindService;

/**
 * Center rotating garland object for OpenGL ES 3.2
 * Migrated from OpenGL ES 1.0 fixed pipeline
 */
public class CenterGarland extends Garland {

	private float rotation = 0.0f;
	private float tickRemainder = 0.0f;
	private static final int PERIOD_TICKS = 2880;
	private static final float DISPLAY_SCALE = 0.566f;

	public CenterGarland()
	{
		super();
		// The original source rectangle is 512 x 512 around (320, 240).
		apex = new float[] { -256f, -256f, 256f, -256f, -256f, 256f, 256f, 256f, };
		coords = new float[] {0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, };
		
		// Keep legacy buffer creation for compatibility
		ab = AThingLeftBehindService.makeFloatBuffer(apex);
		cb = AThingLeftBehindService.makeFloatBuffer(coords);
	}
	
	@Override
	protected void createShaderProgram() {
		// The shader turns the grayscale m_car3_al mask into sprite alpha.
		shaderProgram = ShaderLoader.Programs.createBlendProgram(AThingLeftBehindService.getContext());
	}
	
	@Override
	public void Draw(float[] viewMatrix, float[] projectionMatrix) {
		// android.util.Log.d("CenterGarland", "Draw() called");
		
		// Debug: Check if shader and texture are valid
		if (shaderProgram == 0) {
			android.util.Log.e("CenterGarland", "Shader program is 0!");
			return;
		}
		if (AThingLeftBehindService.textures[0] == 0) {
			android.util.Log.e("CenterGarland", "Texture is 0!");
			return;
		}
		if (vao == 0) {
			android.util.Log.e("CenterGarland", "VAO is 0!");
			return;
		}
		
		// Update MVP matrix with current rotation
		updateMVP(viewMatrix, projectionMatrix);
		
		// Bind shader and set uniforms
		bindShader();
		
		// Set texture
		TextureUtils.bindTexture(0, AThingLeftBehindService.textures[0]);
		
		ShaderUtils.setUniform1i(blendModeLocation, 2);
		
		// Original ARGB tint: 0xff40c0ff.
		ShaderUtils.setUniform4f(colorLocation, 64.0f / 255.0f,
			192.0f / 255.0f, 1.0f, 1.0f);
		
		// The Direct3D fixed-function path uses ordinary source-alpha blending.
		GLES32.glEnable(GLES32.GL_BLEND);
		GLES32.glBlendEquation(GLES32.GL_FUNC_ADD);
		GLES32.glBlendFunc(GLES32.GL_SRC_ALPHA, GLES32.GL_ONE_MINUS_SRC_ALPHA);

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
		tickRemainder += Math.max(0.0f, deltaTime) * 60.0f;
		int elapsedTicks = (int)tickRemainder;
		tickRemainder -= elapsedTicks;
		cnt = (cnt + elapsedTicks) % PERIOD_TICKS;
		
		// Positive D3D rotation appears clockwise because its Y axis points down.
		// OpenGL's Y axis points up, hence the negative angle here.
		rotation = -360.0f * cnt / PERIOD_TICKS;
		// Match the apparent size of the previous perspective-projection version.
		modelMatrix = MatrixUtils.multiply(
			MatrixUtils.rotateZ(rotation),
			MatrixUtils.scale(DISPLAY_SCALE, DISPLAY_SCALE, 1.0f));
	}
}
