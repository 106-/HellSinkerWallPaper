package net.t106.sinkerglwallpaper.ui.preferences;

import net.t106.sinkerglwallpaper.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import androidx.preference.DialogPreference;
import androidx.preference.PreferenceManager;
import android.util.AttributeSet;
import java.util.Locale;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class SizeChangePreference extends DialogPreference {
	private Context cxt;
	private SeekBar sb;
	private TextView tv;
	private Button btn;
	private int prog,tmp_prog;
	
	public SizeChangePreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		cxt = context;
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(cxt);
		tmp_prog = sp.getInt("size", 200);
		prog = sp.getInt("size", 200);
		setDialogLayoutResource(R.layout.size_change_pref_layout);
	}
	
	public void onBindDialogView(View v)
	{
		tv = (TextView)v.findViewById(R.id.textView2);
		sb = (SeekBar)v.findViewById(R.id.seekBar1);
		btn = (Button)v.findViewById(R.id.button1);
		sb.setMax(300);
		sb.setProgress(prog);
		sb.setOnSeekBarChangeListener(new SBchange());
		tv.setText(String.format(Locale.US, "%.2f", (prog+100)/100.0));
		btn.setOnClickListener(new btnlis());
	}
	
	public void onDialogClosed(boolean res) 
	{
		if(res)
		{
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(cxt);
			Editor e = sp.edit();
			e.putInt("size", prog);
			e.apply();
		}
		else
		{
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(cxt);
			Editor e = sp.edit();
			e.putInt("size", tmp_prog);
			e.apply();
		}
	}
	
	protected void setDefault()
	{
		sb.setProgress(200);
		prog = 200;
		tv.setText(String.format(Locale.US, "%.2f", (200+100)/100.0));
	}
	
	private class SBchange implements SeekBar.OnSeekBarChangeListener
	{
		@Override
		public void onProgressChanged(SeekBar skbar, int val, boolean flg) 
		{
			tv.setText(String.format(Locale.US, "%.2f", (val+100)/100.0));
			prog = val;
		}

		@Override
		public void onStartTrackingTouch(SeekBar arg0) {}
		@Override
		public void onStopTrackingTouch(SeekBar arg0) {}
	}
	
	private class btnlis implements OnClickListener
	{
		@Override
		public void onClick(View v) {
			setDefault();
		}
	}

}
