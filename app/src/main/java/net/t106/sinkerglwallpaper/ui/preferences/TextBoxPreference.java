package net.t106.sinkerglwallpaper.ui.preferences;

import android.content.Context;
import androidx.preference.DialogPreference;
import android.util.AttributeSet;
import net.t106.sinkerglwallpaper.R;

public class TextBoxPreference extends DialogPreference{

	public TextBoxPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setDialogLayoutResource(R.layout.textboxpref);
	}
	
	public TextBoxPreference(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setDialogLayoutResource(R.layout.textboxpref);
	}
	
	public void onBindDialogView(android.view.View view) {
	}
	
	public void onDialogClosed(boolean positiveResult) {
	}

}
