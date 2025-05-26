package net.t106.sinkerglwallpaper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class seekbar_pref extends DialogPreference{
	private TextView[] tv = new TextView[4];
	private SeekBar[] sb = new SeekBar[4];
	private Button btn;
	private Context cxt;
	private int[] col = new int[4];
	private int[] col_tmp = new int[4];
	private int[] col_default = {50,50,100,50};

	public seekbar_pref(Context context, AttributeSet attrs) {
		super(context, attrs);
		cxt = context;
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(cxt);
		col[0] = sp.getInt("col_R", 50);
		col[1] = sp.getInt("col_G", 50);
		col[2] = sp.getInt("col_B", 100);
		col[3] = sp.getInt("col_Alpha", 50);
		col_tmp = (int[])col.clone();
		setDialogLayoutResource(R.layout.pref_seekbar_pref);
	}
	
	@Override
	protected void onBindDialogView(View v)
	{
		super.onBindDialogView(v);
		tv[0] = (TextView)v.findViewById(R.id.textView1);
		tv[1] = (TextView)v.findViewById(R.id.textView2);
		tv[2] = (TextView)v.findViewById(R.id.textView3);
		tv[3] = (TextView)v.findViewById(R.id.textView5);
		sb[0] = (SeekBar)v.findViewById(R.id.seekBar1);
		sb[1] = (SeekBar)v.findViewById(R.id.seekBar2);
		sb[2] = (SeekBar)v.findViewById(R.id.seekBar3);
		sb[3] = (SeekBar)v.findViewById(R.id.seekBar4);
		btn = (Button)v.findViewById(R.id.button1);
		btn.setOnClickListener(new btnlis());
		SBchange sbc = new SBchange(sb,tv);
		for(int i=0;i<4;i++)sb[i].setProgress(col[i]);
		for(int i=0;i<4;i++)sb[i].setOnSeekBarChangeListener(sbc);
		for(int i=0;i<4;i++)tv[i].setText(String.format("%.2f", col[i]/100.0));
	}
	
	@Override
	protected void onDialogClosed(boolean res) 
	{
		super.onDialogClosed(res);
		if(res)
		{
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(cxt);
			Editor e = sp.edit();
			e.putInt("col_R", col[0]);
			e.putInt("col_G", col[1]);
			e.putInt("col_B", col[2]);
			e.putInt("col_Alpha", col[3]);
			e.commit();
		}
		else
		{
			col = (int[])col_tmp.clone();
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(cxt);
			Editor e = sp.edit();
			e.putInt("col_R", col[0]);
			e.putInt("col_G", col[1]);
			e.putInt("col_B", col[2]);
			e.putInt("col_Alpha", col[3]);
			e.commit();
		}
	}
	
	protected void setColor(int n,int val)
	{
		col[n] = val;
	}
	
	protected void setDefault()
	{
		col = (int[])col_default.clone();
		for(int i=0;i<4;i++)sb[i].setProgress(col[i]);
		for(int i=0;i<4;i++)tv[i].setText(String.format("%.2f", col[i]/100.0));
	}
	
	private class SBchange implements SeekBar.OnSeekBarChangeListener
	{	
		private SeekBar[] sb_arr = new SeekBar[4];
		private TextView[] tv_arr = new TextView[4];
		
		public SBchange(SeekBar[] sb_arr_arg, TextView[] tv_arr_arg)
		{
			this.sb_arr = sb_arr_arg;
			this.tv_arr = tv_arr_arg;
		}
		
		@Override
		public void onProgressChanged(SeekBar sb, int val, boolean fromUser) {
			int id = sb.getId();
			for(int i=0;i<4;i++)
				if(sb_arr[i].getId() == id)
				{
					setColor(i, val);
					tv[i].setText(String.format("%.2f", val/100.0));
				}
		}
		
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {}
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {}
	}

	private class btnlis implements OnClickListener
	{
		@Override
		public void onClick(View v) {
			setDefault();
		}
	}
}
