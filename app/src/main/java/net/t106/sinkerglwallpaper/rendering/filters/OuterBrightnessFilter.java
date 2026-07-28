package net.t106.sinkerglwallpaper.rendering.filters;

import android.opengl.GLES32;

import net.t106.sinkerglwallpaper.opengl.shaders.ShaderLoader;
import net.t106.sinkerglwallpaper.opengl.utils.BufferUtils;
import net.t106.sinkerglwallpaper.opengl.utils.ShaderUtils;
import net.t106.sinkerglwallpaper.rendering.objects.Garland;
import net.t106.sinkerglwallpaper.rendering.services.AThingLeftBehindService;

/**
 * Reproduces the menu/HUD pass that brightens the two areas outside the
 * central background band.
 */
public class OuterBrightnessFilter extends Garland {

	private int rightVao;
	private float halfWidth = 320.0f;
	private float halfHeight = 240.0f;

	public OuterBrightnessFilter() {
		super();
		coords = new float[] {
			0.0f, 0.0f, 1.0f, 0.0f,
			0.0f, 1.0f, 1.0f, 1.0f,
		};
		updateGeometry();
	}

	@Override
	protected void createShaderProgram() {
		shaderProgram = ShaderLoader.Programs.createColorProgram(
			AThingLeftBehindService.getContext());
	}

	@Override
	protected void createBuffers() {
		float boundary = Math.min(LeftFilter.FILTER_WIDTH, halfWidth);
		float[] leftVertices = new float[] {
			-halfWidth, -halfHeight, -boundary, -halfHeight,
			-halfWidth, halfHeight, -boundary, halfHeight,
		};
		float[] rightVertices = new float[] {
			boundary, -halfHeight, halfWidth, -halfHeight,
			boundary, halfHeight, halfWidth, halfHeight,
		};

		apex = leftVertices;
		vao = BufferUtils.createQuadVAO(leftVertices, coords, 0, 1);
		rightVao = BufferUtils.createQuadVAO(rightVertices, coords, 0, 1);
	}

	@Override
	public void Draw(float[] viewMatrix, float[] projectionMatrix) {
		if (vao == 0 || rightVao == 0) {
			return;
		}

		updateMVP(viewMatrix, projectionMatrix);
		bindShader();

		/*
		 * The original #80FFFFFF HUD command has the observed result
		 * dst + 0.5 * dst. Supplying 0.5 RGB with DST_COLOR, ONE
		 * reproduces that equation directly while preserving alpha.
		 */
		ShaderUtils.setUniform4f(
			colorLocation, 0.5f, 0.5f, 0.5f, 1.0f);
		ShaderUtils.setUniform1i(blendModeLocation, 0);
		GLES32.glEnable(GLES32.GL_BLEND);
		GLES32.glBlendEquation(GLES32.GL_FUNC_ADD);
		GLES32.glBlendFuncSeparate(
			GLES32.GL_DST_COLOR, GLES32.GL_ONE,
			GLES32.GL_ZERO, GLES32.GL_ONE);

		BufferUtils.bindVAO(vao);
		BufferUtils.drawQuad();
		BufferUtils.bindVAO(rightVao);
		BufferUtils.drawQuad();
		BufferUtils.unbindVAO();

		GLES32.glDisable(GLES32.GL_BLEND);
	}

	@Override
	public void Update(float deltaTime) {
		// Static post-composite pass.
	}

	public void setViewport(float halfWidth, float halfHeight) {
		this.halfWidth = halfWidth;
		this.halfHeight = halfHeight;
		updateGeometry();

		if (vao != 0) {
			BufferUtils.deleteVAO(vao);
			vao = 0;
		}
		if (rightVao != 0) {
			BufferUtils.deleteVAO(rightVao);
			rightVao = 0;
		}
		if (shaderProgram != 0) {
			createBuffers();
		}
	}

	private void updateGeometry() {
		float boundary = Math.min(LeftFilter.FILTER_WIDTH, halfWidth);
		apex = new float[] {
			-halfWidth, -halfHeight, -boundary, -halfHeight,
			-halfWidth, halfHeight, -boundary, halfHeight,
		};
		ab = AThingLeftBehindService.makeFloatBuffer(apex);
		cb = AThingLeftBehindService.makeFloatBuffer(coords);
	}

	@Override
	public void cleanup() {
		super.cleanup();
		if (rightVao != 0) {
			BufferUtils.deleteVAO(rightVao);
			rightVao = 0;
		}
	}
}
