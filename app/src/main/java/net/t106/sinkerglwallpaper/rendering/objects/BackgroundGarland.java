package net.t106.sinkerglwallpaper.rendering.objects;

import android.opengl.GLES32;
import net.t106.sinkerglwallpaper.opengl.utils.MatrixUtils;
import net.t106.sinkerglwallpaper.opengl.utils.ShaderUtils;
import net.t106.sinkerglwallpaper.opengl.utils.BufferUtils;
import net.t106.sinkerglwallpaper.opengl.utils.TextureUtils;
import net.t106.sinkerglwallpaper.opengl.shaders.ShaderLoader;
import net.t106.sinkerglwallpaper.rendering.services.AThingLeftBehindService;

/**
 * Background rotating garland object for OpenGL ES 3.2
 * Larger than CenterGarland and rotates in opposite direction with color tint
 */
public class BackgroundGarland extends Garland {
	
	private float rotation = 0.0f;
	private float tickRemainder = 0.0f;
	private static final int PERIOD_TICKS = 1920;
	private static final float DISPLAY_SCALE = 0.566f;
	
	public BackgroundGarland()
	{
		super();
		// 512 x 512 source at 1.5 scale.
		apex = new float[] { -384f, -384f, 384f, -384f, -384f, 384f, 384f, 384f, };
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
		// Update MVP matrix with current rotation
		updateMVP(viewMatrix, projectionMatrix);
		
		// Bind shader and set uniforms
		bindShader();
		
		TextureUtils.bindTexture(0, AThingLeftBehindService.textures[0]);
		
		ShaderUtils.setUniform1i(blendModeLocation, 2);
		
		// Original ARGB tint: 0xff400810.
		ShaderUtils.setUniform4f(colorLocation, 64.0f / 255.0f,
			8.0f / 255.0f, 16.0f / 255.0f, 1.0f);
		
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
		
		// Equivalent to -(1919 - (t mod 1920)) / 1920 D3D turns,
		// modulo one turn, then vertically mirrored as scaleY = -1.5.
		rotation = 360.0f * (cnt + 1) / PERIOD_TICKS;
		modelMatrix = MatrixUtils.multiply(
			MatrixUtils.rotateZ(rotation),
			MatrixUtils.scale(DISPLAY_SCALE, -DISPLAY_SCALE, 1.0f));
	}
}
