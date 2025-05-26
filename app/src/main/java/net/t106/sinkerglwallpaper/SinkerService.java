package net.t106.sinkerglwallpaper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.preference.PreferenceManager;
import android.view.SurfaceHolder;
import net.rbgrn.android.glwallpaperservice.GLWallpaperService;

public class SinkerService extends GLWallpaperService{
	public static int[] textures = new int[2];
	public static int blend_type;
	public static int[] col = new int[4];
	public Context cnt = null;
	
	public class SinkerEngine extends GLWallpaperService.GLEngine{
		
		@Override
	    public void onCreate(SurfaceHolder surfaceHolder) {
	        super.onCreate(surfaceHolder);
	        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
	        setRenderer(new MyRenderer());
		}       
		
	}
	
	public class MyRenderer implements GLWallpaperService.Renderer {
		private center_gy cgy;
		private back_gy bgy;
		private right_filter rf;
		private left_filter lf;
		
		public MyRenderer()
		{   
			cgy = new center_gy();
			bgy = new back_gy();
			rf = new right_filter();
			lf = new left_filter();
		}
		
		@Override
		public void onDrawFrame(GL10 gl) {			
			//画面を消す
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
			bgy.Draw(gl);
			cgy.Draw(gl);
			lf.Draw(gl);
			rf.Draw(gl);
		}
		@Override
		public void onSurfaceChanged(GL10 gl, int wid, int hei) {
			gl.glViewport(0, 0, wid, hei);
			gl.glMatrixMode(GL10.GL_PROJECTION);
			gl.glLoadIdentity();
			GLU.gluPerspective(gl, 45f, (float)wid/(float)hei, 0.1f, 100f);
			
			if(cnt!=null)
			{
				SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(cnt);
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
				GLU.gluLookAt(gl, 0, 0, ((gr_size+100)/100.0f), 0, 0, 0, 0, 1, 0);
				//GLU.gluLookAt(gl, 0, 0, 2, 0, 0, 0, 0, 1, 0);
			}
		}
		
		@Override
		public void onSurfaceCreated(GL10 gl, EGLConfig arg1) {
			gl.glDeleteTextures(2, textures, 0);
			gl.glGenTextures(2, textures, 0);
			
			gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);			
			Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.gr);
			GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bm, 0);
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
			
			gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[1]);
			Matrix mat = new Matrix();
			mat.preScale(-1, 1);
			Bitmap bm_rev = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), mat, false);
			GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bm_rev, 0);
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
			
			bm.recycle();
			bm_rev.recycle();
			
			//背景色
			gl.glClearColor(0, 0, 0, 0);
		}
	}
	
	@Override
	public Engine onCreateEngine()
	{
		cnt = this;
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
