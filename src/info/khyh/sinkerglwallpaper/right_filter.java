package info.khyh.sinkerglwallpaper;
import javax.microedition.khronos.opengles.GL10;

public class right_filter extends graveyard {

	public right_filter()
	{
		super();
		apex = new float[] { 0f, -2.5f, 0.7f, -2.5f, 0f, 2.5f, 0.7f, 2.5f, };
		ab = SinkerService.makeFloatBuffer(apex);
	}
	
	@Override
	public void Draw(GL10 gl) {
		//gl.glColor4f(1f,0.554f,0.18f,0.5f);
		gl.glColor4f(1f,0.5f,0.5f,0.5f);
		//頂点を有効化
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, ab);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		//OpenGLを2Dモードに
		//gl.glEnable(GL10.GL_TEXTURE_2D);
		//反転を有効にする
		gl.glBlendFunc(GL10.GL_ONE_MINUS_DST_COLOR, GL10.GL_ZERO);
		//gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE);
		gl.glEnable(GL10.GL_BLEND);
		//頂点の描画
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
		//無効化
	   	//gl.glDisable(GL10.GL_TEXTURE_2D);
	   	gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisable(GL10.GL_BLEND);
	   	gl.glColor4f(1, 1, 1, 1);
	}

	@Override
	public void Update(GL10 gl) {}
	
	@Override
	public void sizechange(boolean smollflg)
	{
		if(smollflg)ab = SinkerService.makeFloatBuffer(new float[] { 0f, -1.5f, 0.5f, -1.5f, 0f, 1.5f, 0.5f, 1.5f, });
		else ab = SinkerService.makeFloatBuffer(new float[] { 0f, -1.5f, 0.7f, -1.5f, 0f, 1.5f, 0.7f, 1.5f, });
	}

}
