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
		// Use color shader program for color-only rendering
		shaderProgram = ShaderLoader.Programs.createColorProgram(SinkerService.getContext());
	}
	
	@Override
	public void Draw(float[] viewMatrix, float[] projectionMatrix) {
		android.util.Log.d("LeftFilter", "Draw() called, blend_type=" + SinkerService.blend_type + 
			", col=[" + SinkerService.col[0] + "," + SinkerService.col[1] + "," + SinkerService.col[2] + "," + SinkerService.col[3] + "]");
		
		// Update MVP matrix (no rotation, just basic transformation)
		updateMVP(viewMatrix, projectionMatrix);
		
		// Bind shader and set uniforms
		bindShader();
		
		// Set blend mode to fixed additive (0)
		ShaderUtils.setUniform1i(blendModeLocation, 0);
		
		// Set fixed blue-purple color like old.png
		float red = 0.3f;   // Less red
		float green = 0.4f; // Some green  
		float blue = 1.0f;  // Full blue for blue-purple color
		float alpha = 0.6f; // Moderate transparency
		ShaderUtils.setUniform4f(colorLocation, red, green, blue, alpha);
		
		// Enable additive blending (fixed)
		GLES32.glEnable(GLES32.GL_BLEND);
		GLES32.glBlendFunc(GLES32.GL_ONE, GLES32.GL_ONE);
		
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
