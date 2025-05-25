package info.khyh.sinkerglwallpaper;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;

public class TextBox_Pref extends DialogPreference{

	public TextBox_Pref(Context context, AttributeSet attrs) {
		super(context, attrs);
		setDialogLayoutResource(R.layout.textboxpref);
	}

}
