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
import net.t106.sinkerglwallpaper.rendering.objects.CenterGarland;
import net.t106.sinkerglwallpaper.rendering.objects.BackgroundGarland;
import net.t106.sinkerglwallpaper.rendering.filters.LeftFilter;
import net.t106.sinkerglwallpaper.rendering.filters.RightFilter;
import net.t106.sinkerglwallpaper.opengl.utils.MatrixUtils;
import net.t106.sinkerglwallpaper.opengl.utils.TextureUtils;

public class AThingLeftBehindService extends GLWallpaperServiceES32{
	public static int[] textures = new int[2];
	private static Context context = null;
	
	// Static method to provide context to other classes
	public static Context getContext() {
		return context;
	}
	
	public class AThingLeftBehindEngine extends GLWallpaperServiceES32.GLEngine{
		
		@Override
	    public void onCreate(SurfaceHolder surfaceHolder) {
	        super.onCreate(surfaceHolder);
	        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
	        setRenderer(new MyRenderer());
		}       
		
	}
	
	public class MyRenderer implements GLWallpaperServiceES32.Renderer {
		private CenterGarland cgy;
		private BackgroundGarland bgy;
		private RightFilter rf;
		private LeftFilter lf;
		
		// OpenGL ES 3.2 matrices
		private float[] projectionMatrix;
		private float[] viewMatrix;
		private long lastTime;
		
		public MyRenderer()
		{   
			cgy = new CenterGarland();
			bgy = new BackgroundGarland();
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
			
			// Draw objects - filters first, then garlands
			bgy.Draw(viewMatrix, projectionMatrix);
			cgy.Draw(viewMatrix, projectionMatrix);
			lf.Draw(viewMatrix, projectionMatrix);
			rf.Draw(viewMatrix, projectionMatrix);
		}
		
		private void resetOpenGLState() {
			// Reset only binding states, not capabilities that objects need to control
			GLES32.glUseProgram(0);
			GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, 0);
			GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, 0);
			GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, 0);
			
			// Don't reset blend, depth test, or cull face as objects may need them
			// Reset vertex attribute arrays
			for (int i = 0; i < 8; i++) {
				GLES32.glDisableVertexAttribArray(i);
			}
		}
		
		private void drawWithStateIsolation(Runnable drawCommand) {
			// Save current OpenGL binding state (not capabilities)
			int[] currentProgram = new int[1];
			int[] currentTexture = new int[1];
			int[] currentArrayBuffer = new int[1];
			
			GLES32.glGetIntegerv(GLES32.GL_CURRENT_PROGRAM, currentProgram, 0);
			GLES32.glGetIntegerv(GLES32.GL_TEXTURE_BINDING_2D, currentTexture, 0);
			GLES32.glGetIntegerv(GLES32.GL_ARRAY_BUFFER_BINDING, currentArrayBuffer, 0);
			
			try {
				// Execute draw command
				drawCommand.run();
			} finally {
				// Restore only binding states, let objects control their own capabilities
				GLES32.glUseProgram(currentProgram[0]);
				GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, currentTexture[0]);
				GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, currentArrayBuffer[0]);
				
				// Reset vertex attribute arrays
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
				// Filter size is fixed to small (true)
				lf.sizechange(true);
				rf.sizechange(true);
				// Garland size is fixed to 4.0
				float cameraZ = 4.0f;
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
				android.util.Log.d("AThingLeftBehindService", "Textures loaded: " + textures[0] + ", " + textures[1]);
			} else {
				android.util.Log.e("AThingLeftBehindService", "Failed to load textures!");
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
		return new AThingLeftBehindEngine();
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
