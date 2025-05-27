package net.t106.sinkerglwallpaper.rendering.objects;

import java.nio.FloatBuffer;
import android.opengl.GLES32;
import net.t106.sinkerglwallpaper.opengl.utils.MatrixUtils;
import net.t106.sinkerglwallpaper.opengl.utils.ShaderUtils;
import net.t106.sinkerglwallpaper.opengl.utils.BufferUtils;
import net.t106.sinkerglwallpaper.opengl.shaders.ShaderLoader;
import net.t106.sinkerglwallpaper.rendering.services.SinkerService;

/**
 * Abstract base class for OpenGL ES 3.2 rendering objects
 * Migrated from OpenGL ES 1.0 fixed pipeline to modern programmable pipeline
 */
public abstract class Graveyard {
	protected float apex[], coords[];
	protected FloatBuffer ab, cb;
	protected int cnt;
	
	// OpenGL ES 3.2 resources
	protected int vao;            // Vertex Array Object
	protected int vertexVBO;      // Vertex Buffer Object for positions
	protected int texCoordVBO;    // Vertex Buffer Object for texture coordinates
	protected int shaderProgram;  // Shader program handle
	
	// Shader uniform locations
	protected int mvpMatrixLocation;
	protected int textureLocation;
	protected int colorLocation;
	protected int blendModeLocation;
	
	// Matrix for transformations
	protected float[] modelMatrix;
	protected float[] mvpMatrix;
	
	public Graveyard() {
		modelMatrix = MatrixUtils.identity();
		mvpMatrix = new float[16];
	}
	
	/**
	 * Initialize OpenGL ES 3.2 resources
	 * Must be called after OpenGL context is created
	 */
	public void initGL() {
		// Create shader program
		createShaderProgram();
		
		// Get uniform locations
		mvpMatrixLocation = ShaderUtils.getUniformLocation(shaderProgram, "u_mvpMatrix");
		textureLocation = ShaderUtils.getUniformLocation(shaderProgram, "u_texture");
		colorLocation = ShaderUtils.getUniformLocation(shaderProgram, "u_color");
		blendModeLocation = ShaderUtils.getUniformLocation(shaderProgram, "u_blendMode");
		
		// Create VAO and VBOs
		createBuffers();
	}
	
	/**
	 * Create shader program - to be overridden by subclasses if needed
	 */
	protected void createShaderProgram() {
		// Default implementation uses basic shader program
		// Subclasses can override to use different shaders
		shaderProgram = ShaderLoader.Programs.createBasicProgram(SinkerService.getContext());
	}
	
	/**
	 * Create VAO and VBOs for vertex data
	 */
	protected void createBuffers() {
		if (apex != null && coords != null) {
			vao = BufferUtils.createQuadVAO(apex, coords, 0, 1);
		}
	}
	
	/**
	 * Update MVP matrix with view and projection matrices
	 */
	public void updateMVP(float[] viewMatrix, float[] projectionMatrix) {
		float[] temp = new float[16];
		android.opengl.Matrix.multiplyMM(temp, 0, viewMatrix, 0, modelMatrix, 0);
		android.opengl.Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, temp, 0);
	}
	
	/**
	 * Bind shader program and set common uniforms
	 */
	protected void bindShader() {
		GLES32.glUseProgram(shaderProgram);
		ShaderUtils.setUniformMatrix4fv(mvpMatrixLocation, mvpMatrix);
		ShaderUtils.setUniform1i(textureLocation, 0); // Use texture unit 0
	}
	
	/**
	 * Clean up OpenGL resources
	 */
	public void cleanup() {
		if (vao != 0) {
			BufferUtils.deleteVAO(vao);
			vao = 0;
		}
		if (vertexVBO != 0) {
			BufferUtils.deleteVBO(vertexVBO);
			vertexVBO = 0;
		}
		if (texCoordVBO != 0) {
			BufferUtils.deleteVBO(texCoordVBO);
			texCoordVBO = 0;
		}
		if (shaderProgram != 0) {
			GLES32.glDeleteProgram(shaderProgram);
			shaderProgram = 0;
		}
	}
	
	// Abstract methods - subclasses must implement these
	public abstract void Draw(float[] viewMatrix, float[] projectionMatrix);
	public abstract void Update(float deltaTime);
	
	// Optional method for size changes
	public void sizechange(boolean smollflg) {
		// Default implementation does nothing
		// Subclasses can override if needed
	}
}
