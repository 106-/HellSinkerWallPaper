package info.khyh.sinkerglwallpaper;
import javax.microedition.khronos.opengles.GL10;

public class left_filter extends graveyard{

	public left_filter()
	{
		super();
		apex = new float[] { -0.7f, -2.5f, 0.7f, -2.5f, -0.7f, 2.5f, 0.7f, 2.5f, };
		ab = SinkerService.makeFloatBuffer(apex);
	}
	
	@Override
	public void Draw(GL10 gl) {
		int[] col = SinkerService.col;
		gl.glColor4f((float)(col[0]/100.0),(float)(col[1]/100.0),(float)(col[2]/100.0),(float)(col[3]/100.0));
		//í∏ì_ÇóLå¯âª
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, ab);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		
		switch(SinkerService.blend_type)
		{
		//â¡éZ
		case 0:gl.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE);break;
		//èÊéZ
		case 1:gl.glBlendFunc(GL10.GL_ZERO, GL10.GL_SRC_COLOR);break;
		//ÉAÉãÉtÉ@
		case 2:gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE);break;
		//îrëºìIò_óùòa
		case 3:gl.glBlendFunc(GL10.GL_ONE_MINUS_DST_COLOR, GL10.GL_ONE_MINUS_SRC_COLOR);break;
		}
		
		gl.glEnable(GL10.GL_BLEND);
		//í∏ì_ÇÃï`âÊ
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
		//ñ≥å¯âª
	   	gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisable(GL10.GL_BLEND);
	   	gl.glColor4f(1, 1, 1, 1);
	}

	@Override
	public void Update(GL10 gl) {}
	
	@Override
	public void sizechange(boolean smollflg)
	{
		if(smollflg)ab = SinkerService.makeFloatBuffer(new float[] { -0.5f, -1.5f, 0.5f, -1.5f, -0.5f, 1.5f, 0.5f, 1.5f, });
		else ab = SinkerService.makeFloatBuffer(new float[] { -0.7f, -1.5f, 0.7f, -1.5f, -0.7f, 1.5f, 0.7f, 1.5f, });
	}

}
