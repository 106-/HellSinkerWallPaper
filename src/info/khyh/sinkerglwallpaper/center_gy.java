package info.khyh.sinkerglwallpaper;

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
		//���W�n�̕ۑ�
		gl.glPushMatrix();
		//��]
		gl.glRotatef(-0.125f*cnt, 0.0f, 0.0f, 1.0f);
		//���_��L����
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, ab);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		//���_�Ƀe�N�X�`����t����
		gl.glBindTexture(GL10.GL_TEXTURE_2D, SinkerService.textures[0]);
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, cb);
		//OpenGL��2D���[�h��
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		//���Z������L���ɂ���
		gl.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE);
		gl.glEnable(GL10.GL_BLEND);
		//���_�̕`��
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
		//������
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	   	gl.glDisable(GL10.GL_TEXTURE_2D);
	   	gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisable(GL10.GL_BLEND);
		
	   	//�ۑ����e�̕���
	   	gl.glPopMatrix();
	}

	@Override
	public void Update(GL10 gl) {
		// TODO �����������ꂽ���\�b�h�E�X�^�u
		
	}

}
