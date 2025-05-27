package net.t106.sinkerglwallpaper.rendering.filters;
import android.opengl.GLES32;
import net.t106.sinkerglwallpaper.opengl.utils.ShaderUtils;
import net.t106.sinkerglwallpaper.opengl.utils.BufferUtils;
import net.t106.sinkerglwallpaper.opengl.shaders.ShaderLoader;
import net.t106.sinkerglwallpaper.rendering.services.SinkerService;
import net.t106.sinkerglwallpaper.rendering.objects.Graveyard;

/**
 * Left filter for OpenGL ES 3.2
 * Renders a customizable colored overlay with user-selectable blend modes
 */
public class LeftFilter extends Graveyard {

	private boolean isSmallSize = false;

	public LeftFilter()
	{
		super();
		// Center overlay covering most of the screen
		apex = new float[] { -0.7f, -2.5f, 0.7f, -2.5f, -0.7f, 2.5f, 0.7f, 2.5f, };
		// No texture coordinates needed for color-only rendering
		coords = new float[] { 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f };
		
		// Keep legacy buffer creation for compatibility
		ab = SinkerService.makeFloatBuffer(apex);
		cb = SinkerService.makeFloatBuffer(coords);
	}
	
	@Override
	protected void createShaderProgram() {
		// Use blend shader program for customizable blending
		shaderProgram = ShaderLoader.Programs.createBlendProgram(SinkerService.getContext());
	}
	
	@Override
	public void Draw(float[] viewMatrix, float[] projectionMatrix) {
		// Update MVP matrix (no rotation, just basic transformation)
		updateMVP(viewMatrix, projectionMatrix);
		
		// Bind shader and set uniforms
		bindShader();
		
		// No texture binding needed for color-only rendering
		ShaderUtils.setUniform1i(textureLocation, 0);
		
		// Set blend mode from user settings
		ShaderUtils.setUniform1i(blendModeLocation, SinkerService.blend_type);
		
		// Set color from user settings (convert from 0-100 range to 0.0-1.0)
		int[] col = SinkerService.col;
		float red = col[0] / 100.0f;
		float green = col[1] / 100.0f;
		float blue = col[2] / 100.0f;
		float alpha = col[3] / 100.0f;
		ShaderUtils.setUniform4f(colorLocation, red, green, blue, alpha);
		
		// Enable blending with mode based on user settings
		GLES32.glEnable(GLES32.GL_BLEND);
		
		switch(SinkerService.blend_type)
		{
		// Additive
		case 0:
			GLES32.glBlendFunc(GLES32.GL_ONE, GLES32.GL_ONE);
			break;
		// Multiplicative  
		case 1:
			GLES32.glBlendFunc(GLES32.GL_ZERO, GLES32.GL_SRC_COLOR);
			break;
		// Alpha
		case 2:
			GLES32.glBlendFunc(GLES32.GL_SRC_ALPHA, GLES32.GL_ONE);
			break;
		// XOR (Exclusive OR)
		case 3:
			GLES32.glBlendFunc(GLES32.GL_ONE_MINUS_DST_COLOR, GLES32.GL_ONE_MINUS_SRC_COLOR);
			break;
		default:
			GLES32.glBlendFunc(GLES32.GL_SRC_ALPHA, GLES32.GL_ONE_MINUS_SRC_ALPHA);
			break;
		}
		
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
			apex = new float[] { -0.5f, -1.5f, 0.5f, -1.5f, -0.5f, 1.5f, 0.5f, 1.5f, };
		} else {
			apex = new float[] { -0.7f, -1.5f, 0.7f, -1.5f, -0.7f, 1.5f, 0.7f, 1.5f, };
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
