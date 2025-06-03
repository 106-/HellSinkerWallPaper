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
	private static final float ROTATION_SPEED = -0.125f;
	private static final int MAX_COUNT = 2881;

	public CenterGarland()
	{
		super();
		// Define quad vertices (same as original)
		apex = new float[] { -1f, -1f, 1f, -1f, -1f, 1f, 1f, 1f, };
		coords = new float[] {0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, };
		
		// Keep legacy buffer creation for compatibility
		ab = AThingLeftBehindService.makeFloatBuffer(apex);
		cb = AThingLeftBehindService.makeFloatBuffer(coords);
	}
	
	@Override
	protected void createShaderProgram() {
		// Use blend shader program for additive blending
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
		
		// Set blend mode to additive (0) for beautiful color effects
		ShaderUtils.setUniform1i(blendModeLocation, 0);
		
		// Set color (white for no tinting)
		ShaderUtils.setUniform4f(colorLocation, 0.37f, 1.0f, 1.0f, 1.0f);
		
		// Use additive blending for glowing effects
		GLES32.glEnable(GLES32.GL_BLEND);
		GLES32.glBlendFunc(GLES32.GL_ONE, GLES32.GL_ONE);
//		GLES32.glBlendFunc(GLES32.GL_SRC_ALPHA, GLES32.GL_ONE_MINUS_SRC_ALPHA);

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
		
		// Calculate rotation angle
		rotation = ROTATION_SPEED * cnt;
		
		// Update model matrix with rotation
		modelMatrix = MatrixUtils.rotateZ(rotation);
	}
}
