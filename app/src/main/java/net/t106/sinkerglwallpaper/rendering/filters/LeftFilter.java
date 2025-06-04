package net.t106.sinkerglwallpaper.rendering.filters;
import android.opengl.GLES32;
import net.t106.sinkerglwallpaper.opengl.utils.ShaderUtils;
import net.t106.sinkerglwallpaper.opengl.utils.BufferUtils;
import net.t106.sinkerglwallpaper.opengl.shaders.ShaderLoader;
import net.t106.sinkerglwallpaper.rendering.services.AThingLeftBehindService;
import net.t106.sinkerglwallpaper.rendering.objects.Garland;

/**
 * Left filter for OpenGL ES 3.2
 * Renders a customizable colored overlay with user-selectable blend modes
 */
public class LeftFilter extends Garland {

	private boolean isSmallSize = false;

	public LeftFilter()
	{
		super();
		// Center overlay covering most of the screen
		apex = new float[] { -0.7f, -2.5f, 0.7f, -2.5f, -0.7f, 2.5f, 0.7f, 2.5f, };
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
		GLES32.glBlendFunc(GLES32.GL_ONE, GLES32.GL_ONE);
		BufferUtils.bindVAO(vao);
		BufferUtils.drawQuad();
		BufferUtils.unbindVAO();

		GLES32.glDisable(GLES32.GL_BLEND);
	}

	@Override
	public void Update(float deltaTime) {
		// No animation needed for static filter
	}
	
	@Override
	public void sizechange(boolean smallflg)
	{
		isSmallSize = smallflg;
		
		// Update vertex data based on size
		if(smallflg) {
			apex = new float[] { -0.5f, -1.5f, 0.0f, -1.5f, -0.5f, 1.5f, 0.0f, 1.5f, };
		} else {
			apex = new float[] { -0.7f, -1.5f, 0.7f, -1.5f, -0.7f, 1.5f, 0.7f, 1.5f, };
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
