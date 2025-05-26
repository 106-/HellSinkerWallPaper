package net.t106.sinkerglwallpaper;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public abstract class graveyard {
	protected float apex[],coords[];
	protected FloatBuffer ab,cb;
	protected int cnt;
	
	public graveyard() 
	{
	}
	
	public abstract void Draw(GL10 gl);
	public abstract void Update(GL10 gl);
	public void sizechange(boolean smollflg){};
}
