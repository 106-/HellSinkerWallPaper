package net.t106.sinkerglwallpaper;

import javax.microedition.khronos.opengles.GL10;

public class center_gy extends graveyard {

	public center_gy()
	{
		super();
		apex = new float[] { -1f, -1f, 1f, -1f, -1f, 1f, 1f, 1f, };
		coords = new float[] {0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, };
		
		ab = SinkerService.makeFloatBuffer(apex);
		cb = SinkerService.makeFloatBuffer(coords);
	}
	
	@Override
	public void Draw(GL10 gl) {
		cnt++;
		if(cnt == 2881)cnt = 0;
		//座標系の保存
		gl.glPushMatrix();
		//回転
		gl.glRotatef(-0.125f*cnt, 0.0f, 0.0f, 1.0f);
		//頂点を有効化
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, ab);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		//頂点にテクスチャを付ける
		gl.glBindTexture(GL10.GL_TEXTURE_2D, SinkerService.textures[0]);
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, cb);
		//OpenGLを2Dモードに
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		//加算合成を有効にする
		gl.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE);
		gl.glEnable(GL10.GL_BLEND);
		//頂点の描画
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
		//無効化
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	   	gl.glDisable(GL10.GL_TEXTURE_2D);
	   	gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisable(GL10.GL_BLEND);
		
	   	//保存内容の復元
	   	gl.glPopMatrix();
	}

	@Override
	public void Update(GL10 gl) {
		// TODO 自動生成されたメソッド・スタブ
		
	}

}
