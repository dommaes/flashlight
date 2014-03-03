package de.dommaes.flashlight;

import android.content.SharedPreferences;

public interface PreferenceHandler {

	public SharedPreferences openPreferences();
	public boolean getPreference(String key, boolean defaultValue);
	public int getPreference(String key, int defaultValue);
	public void setPreference(String key, boolean value);
	public void setPreference(String key, int value);
}
