package net.t106.sinkerglwallpaper.rendering.backgrounds;

import android.content.Context;
import android.opengl.GLES32;

import net.t106.sinkerglwallpaper.opengl.shaders.ShaderLoader;
import net.t106.sinkerglwallpaper.opengl.utils.BufferUtils;
import net.t106.sinkerglwallpaper.opengl.utils.MatrixUtils;
import net.t106.sinkerglwallpaper.opengl.utils.ShaderUtils;
import net.t106.sinkerglwallpaper.opengl.utils.TextureUtils;

/**
 * Draws the original 512 x 512 menu sprites using separate RGB and alpha
 * images. Keeping them separate avoids Android bitmap alpha premultiplication.
 */
public final class MenuSpriteRenderer {
	public static final float DISPLAY_SCALE = 0.566f;

	private static final float[] VERTICES = {
		-256.0f, -256.0f,
		256.0f, -256.0f,
		-256.0f, 256.0f,
		256.0f, 256.0f,
	};
	private static final float[] TEX_COORDS = {
		0.0f, 1.0f,
		1.0f, 1.0f,
		0.0f, 0.0f,
		1.0f, 0.0f,
	};

	private int program;
	private int vao;
	private int mvpLocation;
	private int rgbTextureLocation;
	private int alphaTextureLocation;
	private int tintLocation;

	public void initGL(Context context) {
		cleanup();
		program = ShaderLoader.Programs.createMenuSpriteProgram(context);
		if (program == 0) {
			return;
		}
		mvpLocation = ShaderUtils.getUniformLocation(program, "u_mvpMatrix");
		rgbTextureLocation = ShaderUtils.getUniformLocation(program, "u_rgbTexture");
		alphaTextureLocation = ShaderUtils.getUniformLocation(program, "u_alphaTexture");
		tintLocation = ShaderUtils.getUniformLocation(program, "u_tint");
		vao = BufferUtils.createQuadVAO(VERTICES, TEX_COORDS, 0, 1);
	}

	public void draw(
		float[] viewMatrix,
		float[] projectionMatrix,
		int rgbTexture,
		int alphaTexture,
		SpriteState sprite
	) {
		if (program == 0 || vao == 0 || rgbTexture == 0 || alphaTexture == 0) {
			return;
		}

		// Convert the original 640 x 480 top-left coordinate system into the
		// centered, Y-up Android viewport while retaining the established size.
		float translateX = (sprite.centerX - 320.0f) * DISPLAY_SCALE;
		float translateY = (240.0f - sprite.centerY) * DISPLAY_SCALE;
		float rotationDegrees = -360.0f * sprite.rotationTurns;
		float[] model = MatrixUtils.multiply(
			MatrixUtils.translate(translateX, translateY, 0.0f),
			MatrixUtils.multiply(
				MatrixUtils.rotateZ(rotationDegrees),
				MatrixUtils.scale(
					DISPLAY_SCALE * sprite.scaleX,
					DISPLAY_SCALE * sprite.scaleY,
					1.0f)));
		float[] viewModel = MatrixUtils.multiply(viewMatrix, model);
		float[] mvp = MatrixUtils.multiply(projectionMatrix, viewModel);

		GLES32.glUseProgram(program);
		ShaderUtils.setUniformMatrix4fv(mvpLocation, mvp);
		ShaderUtils.setUniform1i(rgbTextureLocation, 0);
		ShaderUtils.setUniform1i(alphaTextureLocation, 1);
		setColor(tintLocation, sprite.tintColor);

		TextureUtils.bindTexture(0, rgbTexture);
		TextureUtils.bindTexture(1, alphaTexture);
		GLES32.glEnable(GLES32.GL_BLEND);
		GLES32.glBlendEquation(GLES32.GL_FUNC_ADD);
		GLES32.glBlendFunc(
			GLES32.GL_SRC_ALPHA, GLES32.GL_ONE_MINUS_SRC_ALPHA);

		BufferUtils.bindVAO(vao);
		BufferUtils.drawQuad();
		BufferUtils.unbindVAO();

		GLES32.glDisable(GLES32.GL_BLEND);
		TextureUtils.unbindTexture(1);
		TextureUtils.unbindTexture(0);
	}

	public void cleanup() {
		if (vao != 0) {
			BufferUtils.deleteVAO(vao);
			vao = 0;
		}
		if (program != 0) {
			GLES32.glDeleteProgram(program);
			program = 0;
		}
	}

	private static void setColor(int location, int argb) {
		ShaderUtils.setUniform4f(
			location,
			((argb >> 16) & 0xFF) / 255.0f,
			((argb >> 8) & 0xFF) / 255.0f,
			(argb & 0xFF) / 255.0f,
			((argb >>> 24) & 0xFF) / 255.0f);
	}

	public static final class SpriteState {
		float centerX;
		float centerY;
		float scaleX;
		float scaleY;
		float rotationTurns;
		int tintColor;

		public void set(
			float centerX,
			float centerY,
			float scaleX,
			float scaleY,
			float rotationTurns,
			int tintColor
		) {
			this.centerX = centerX;
			this.centerY = centerY;
			this.scaleX = scaleX;
			this.scaleY = scaleY;
			this.rotationTurns = rotationTurns;
			this.tintColor = tintColor;
		}
	}
}
