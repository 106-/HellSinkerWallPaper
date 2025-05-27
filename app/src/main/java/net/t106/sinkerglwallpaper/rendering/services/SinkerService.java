package net.t106.sinkerglwallpaper.rendering.services;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.opengl.GLES32;
import android.opengl.GLUtils;
import androidx.preference.PreferenceManager;
import android.view.SurfaceHolder;
import net.rbgrn.android.glwallpaperservice.GLWallpaperServiceES32;
import net.t106.sinkerglwallpaper.R;
import net.t106.sinkerglwallpaper.rendering.objects.CenterGraveyard;
import net.t106.sinkerglwallpaper.rendering.objects.BackgroundGraveyard;
import net.t106.sinkerglwallpaper.rendering.filters.LeftFilter;
import net.t106.sinkerglwallpaper.rendering.filters.RightFilter;
import net.t106.sinkerglwallpaper.opengl.utils.MatrixUtils;
import net.t106.sinkerglwallpaper.opengl.utils.TextureUtils;

public class SinkerService extends GLWallpaperServiceES32{
	public static int[] textures = new int[2];
	public static int blend_type;
	public static int[] col = new int[4];
	private static Context context = null;
	
	// Static method to provide context to other classes
	public static Context getContext() {
		return context;
	}
	
	public class SinkerEngine extends GLWallpaperServiceES32.GLEngine{
		
		@Override
	    public void onCreate(SurfaceHolder surfaceHolder) {
	        super.onCreate(surfaceHolder);
	        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
	        setRenderer(new MyRenderer());
		}       
		
	}
	
	public class MyRenderer implements GLWallpaperServiceES32.Renderer {
		private CenterGraveyard cgy;
		private BackgroundGraveyard bgy;
		private RightFilter rf;
		private LeftFilter lf;
		
		// OpenGL ES 3.2 matrices
		private float[] projectionMatrix;
		private float[] viewMatrix;
		private long lastTime;
		
		public MyRenderer()
		{   
			cgy = new CenterGraveyard();
			bgy = new BackgroundGraveyard();
			rf = new RightFilter();
			lf = new LeftFilter();
			
			projectionMatrix = new float[16];
			viewMatrix = new float[16];
			lastTime = System.currentTimeMillis();
		}
		
		@Override
		public void onDrawFrame(javax.microedition.khronos.opengles.GL10 gl) {			
			// Reset OpenGL state to known values
			resetOpenGLState();
			
			// Clear screen
			GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT);
			
			// Calculate delta time for animations
			long currentTime = System.currentTimeMillis();
			float deltaTime = (currentTime - lastTime) / 1000.0f;
			lastTime = currentTime;
			
			// Update objects
			bgy.Update(deltaTime);
			cgy.Update(deltaTime);
			lf.Update(deltaTime);
			rf.Update(deltaTime);
			
			// Draw objects with state isolation
			drawWithStateIsolation(() -> bgy.Draw(viewMatrix, projectionMatrix));
			drawWithStateIsolation(() -> cgy.Draw(viewMatrix, projectionMatrix));
			drawWithStateIsolation(() -> lf.Draw(viewMatrix, projectionMatrix));
			drawWithStateIsolation(() -> rf.Draw(viewMatrix, projectionMatrix));
		}
		
		private void resetOpenGLState() {
			// Reset common OpenGL states to default values
			GLES32.glUseProgram(0);
			GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, 0);
			GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, 0);
			GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, 0);
			GLES32.glDisable(GLES32.GL_BLEND);
			GLES32.glDisable(GLES32.GL_DEPTH_TEST);
			GLES32.glDisable(GLES32.GL_CULL_FACE);
			
			// Reset vertex attribute arrays
			for (int i = 0; i < 8; i++) {
				GLES32.glDisableVertexAttribArray(i);
			}
		}
		
		private void drawWithStateIsolation(Runnable drawCommand) {
			// Save current OpenGL state (basic approach)
			int[] currentProgram = new int[1];
			int[] currentTexture = new int[1];
			int[] currentArrayBuffer = new int[1];
			boolean blendEnabled = GLES32.glIsEnabled(GLES32.GL_BLEND);
			
			GLES32.glGetIntegerv(GLES32.GL_CURRENT_PROGRAM, currentProgram, 0);
			GLES32.glGetIntegerv(GLES32.GL_TEXTURE_BINDING_2D, currentTexture, 0);
			GLES32.glGetIntegerv(GLES32.GL_ARRAY_BUFFER_BINDING, currentArrayBuffer, 0);
			
			try {
				// Execute draw command
				drawCommand.run();
			} finally {
				// Restore OpenGL state
				GLES32.glUseProgram(currentProgram[0]);
				GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, currentTexture[0]);
				GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, currentArrayBuffer[0]);
				
				if (blendEnabled) {
					GLES32.glEnable(GLES32.GL_BLEND);
				} else {
					GLES32.glDisable(GLES32.GL_BLEND);
				}
				
				// Reset vertex attribute arrays again
				for (int i = 0; i < 8; i++) {
					GLES32.glDisableVertexAttribArray(i);
				}
			}
		}
		@Override
		public void onSurfaceChanged(javax.microedition.khronos.opengles.GL10 gl, int wid, int hei) {
			GLES32.glViewport(0, 0, wid, hei);
			
			// Create projection matrix using modern approach
			projectionMatrix = MatrixUtils.perspective(45f, (float)wid/(float)hei, 0.1f, 100f);
			
			if(context != null)
			{
				SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
				String type = sp.getString("blend_type", "mul");
				if(type.equals("add"))			blend_type = 0;
				else if(type.equals("mul"))		blend_type = 1;
				else if(type.equals("alpha"))	blend_type = 2;
				else if(type.equals("xor"))		blend_type = 3;
				col[0] = sp.getInt("col_R", 50);
				col[1] = sp.getInt("col_G", 50);
				col[2] = sp.getInt("col_B", 100);
				col[3] = sp.getInt("col_Alpha", 50);
				String Isvert = sp.getString("filter_size", "smoll");
				if(Isvert.equals("smoll"))
				{
					lf.sizechange(true);
					rf.sizechange(true);
				}
				else if(Isvert.equals("big"))
				{
					lf.sizechange(false);
					rf.sizechange(false);
				}
				int gr_size = sp.getInt("size", 200);
				
				// Create view matrix using modern approach
				float cameraZ = (gr_size + 100) / 100.0f;
				viewMatrix = MatrixUtils.lookAt(0, 0, cameraZ, 0, 0, 0, 0, 1, 0);
			}
		}
		
		@Override
		public void onSurfaceCreated(javax.microedition.khronos.opengles.GL10 gl, javax.microedition.khronos.egl.EGLConfig arg1) {
			// Delete old textures if they exist
			if (textures[0] != 0 || textures[1] != 0) {
				TextureUtils.deleteTextures(textures);
			}
			
			// Load textures using new utility
			int[] newTextures = TextureUtils.loadTextureWithFlipped(context, R.drawable.gr);
			if (newTextures != null) {
				textures[0] = newTextures[0]; // Original texture
				textures[1] = newTextures[1]; // Flipped texture
			}
			
			// Set background color
			GLES32.glClearColor(0, 0, 0, 0);
			
			// Initialize all rendering objects
			bgy.initGL();
			cgy.initGL();
			lf.initGL();
			rf.initGL();
		}
	}
	
	@Override
	public Engine onCreateEngine()
	{
		context = this;
		return new SinkerEngine();
	}
	
	//頂点の配列をバッファーに変換するメソッド
	 public static FloatBuffer makeFloatBuffer(float[] values) {
	  ByteBuffer bb = ByteBuffer.allocateDirect(values.length * 4);
	  bb.order(ByteOrder.nativeOrder());
	  FloatBuffer fb = bb.asFloatBuffer();
	  fb.put(values);
	  fb.position(0);
	  return fb;
	 }
}
