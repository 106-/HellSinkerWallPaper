package net.t106.sinkerglwallpaper;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (savedInstanceState == null) {
			getSupportFragmentManager()
				.beginTransaction()
				.replace(android.R.id.content, new SettingsFragment())
				.commit();
		}
	}
	
	public static class SettingsFragment extends PreferenceFragmentCompat {
		@Override
		public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
			setPreferencesFromResource(R.xml.pref, rootKey);
		}
		
		@Override
		public void onDisplayPreferenceDialog(androidx.preference.Preference preference) {
			if (preference instanceof TextBox_Pref) {
				androidx.preference.PreferenceDialogFragmentCompat dialogFragment = 
					new TextBoxPreferenceDialogFragmentCompat();
				Bundle bundle = new Bundle();
				bundle.putString("key", preference.getKey());
				dialogFragment.setArguments(bundle);
				dialogFragment.setTargetFragment(this, 0);
				dialogFragment.show(getParentFragmentManager(), "androidx.preference.PreferenceFragment.DIALOG");
			} else if (preference instanceof seekbar_pref) {
				androidx.preference.PreferenceDialogFragmentCompat dialogFragment = 
					new SeekBarPreferenceDialogFragmentCompat();
				Bundle bundle = new Bundle();
				bundle.putString("key", preference.getKey());
				dialogFragment.setArguments(bundle);
				dialogFragment.setTargetFragment(this, 0);
				dialogFragment.show(getParentFragmentManager(), "androidx.preference.PreferenceFragment.DIALOG");
			} else if (preference instanceof size_change_pref) {
				androidx.preference.PreferenceDialogFragmentCompat dialogFragment = 
					new SizeChangePreferenceDialogFragmentCompat();
				Bundle bundle = new Bundle();
				bundle.putString("key", preference.getKey());
				dialogFragment.setArguments(bundle);
				dialogFragment.setTargetFragment(this, 0);
				dialogFragment.show(getParentFragmentManager(), "androidx.preference.PreferenceFragment.DIALOG");
			} else {
				super.onDisplayPreferenceDialog(preference);
			}
		}
	}
	
	public static class TextBoxPreferenceDialogFragmentCompat extends androidx.preference.PreferenceDialogFragmentCompat {
		@Override
		protected void onBindDialogView(android.view.View view) {
			super.onBindDialogView(view);
			TextBox_Pref preference = (TextBox_Pref) getPreference();
			preference.onBindDialogView(view);
		}
		
		@Override
		public void onDialogClosed(boolean positiveResult) {
			TextBox_Pref preference = (TextBox_Pref) getPreference();
			preference.onDialogClosed(positiveResult);
		}
	}
	
	public static class SeekBarPreferenceDialogFragmentCompat extends androidx.preference.PreferenceDialogFragmentCompat {
		@Override
		protected void onBindDialogView(android.view.View view) {
			super.onBindDialogView(view);
			seekbar_pref preference = (seekbar_pref) getPreference();
			preference.onBindDialogView(view);
		}
		
		@Override
		public void onDialogClosed(boolean positiveResult) {
			seekbar_pref preference = (seekbar_pref) getPreference();
			preference.onDialogClosed(positiveResult);
		}
	}
	
	public static class SizeChangePreferenceDialogFragmentCompat extends androidx.preference.PreferenceDialogFragmentCompat {
		@Override
		protected void onBindDialogView(android.view.View view) {
			super.onBindDialogView(view);
			size_change_pref preference = (size_change_pref) getPreference();
			preference.onBindDialogView(view);
		}
		
		@Override
		public void onDialogClosed(boolean positiveResult) {
			size_change_pref preference = (size_change_pref) getPreference();
			preference.onDialogClosed(positiveResult);
		}
	}
}
