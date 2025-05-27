package net.t106.sinkerglwallpaper.ui.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;
import net.t106.sinkerglwallpaper.R;
import net.t106.sinkerglwallpaper.ui.preferences.TextBoxPreference;
import net.t106.sinkerglwallpaper.ui.preferences.SeekBarPreference;
import net.t106.sinkerglwallpaper.ui.preferences.SizeChangePreference;

public class SettingsActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(R.string.title_activity_settings);
		
		if (savedInstanceState == null) {
			getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.settings_container, new SettingsFragment())
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
			if (preference instanceof TextBoxPreference) {
				androidx.preference.PreferenceDialogFragmentCompat dialogFragment = 
					new TextBoxPreferenceDialogFragmentCompat();
				Bundle bundle = new Bundle();
				bundle.putString("key", preference.getKey());
				dialogFragment.setArguments(bundle);
				dialogFragment.setTargetFragment(this, 0);
				dialogFragment.show(getParentFragmentManager(), "androidx.preference.PreferenceFragment.DIALOG");
			} else if (preference instanceof SeekBarPreference) {
				androidx.preference.PreferenceDialogFragmentCompat dialogFragment = 
					new SeekBarPreferenceDialogFragmentCompat();
				Bundle bundle = new Bundle();
				bundle.putString("key", preference.getKey());
				dialogFragment.setArguments(bundle);
				dialogFragment.setTargetFragment(this, 0);
				dialogFragment.show(getParentFragmentManager(), "androidx.preference.PreferenceFragment.DIALOG");
			} else if (preference instanceof SizeChangePreference) {
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
			TextBoxPreference preference = (TextBoxPreference) getPreference();
			preference.onBindDialogView(view);
		}
		
		@Override
		public void onDialogClosed(boolean positiveResult) {
			TextBoxPreference preference = (TextBoxPreference) getPreference();
			preference.onDialogClosed(positiveResult);
		}
	}
	
	public static class SeekBarPreferenceDialogFragmentCompat extends androidx.preference.PreferenceDialogFragmentCompat {
		@Override
		protected void onBindDialogView(android.view.View view) {
			super.onBindDialogView(view);
			SeekBarPreference preference = (SeekBarPreference) getPreference();
			preference.onBindDialogView(view);
		}
		
		@Override
		public void onDialogClosed(boolean positiveResult) {
			SeekBarPreference preference = (SeekBarPreference) getPreference();
			preference.onDialogClosed(positiveResult);
		}
	}
	
	public static class SizeChangePreferenceDialogFragmentCompat extends androidx.preference.PreferenceDialogFragmentCompat {
		@Override
		protected void onBindDialogView(android.view.View view) {
			super.onBindDialogView(view);
			SizeChangePreference preference = (SizeChangePreference) getPreference();
			preference.onBindDialogView(view);
		}
		
		@Override
		public void onDialogClosed(boolean positiveResult) {
			SizeChangePreference preference = (SizeChangePreference) getPreference();
			preference.onDialogClosed(positiveResult);
		}
	}
}
