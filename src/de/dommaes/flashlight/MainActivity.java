package de.dommaes.flashlight;

import java.util.List;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

//Flashlight v1.1
public class MainActivity extends Activity implements PreferenceHandler {
	// variable declaration
	private Camera camera = null;
	private boolean isFlashOn = false;
	private boolean isDisplayOn = false;
	
	//constant declaration
	private static final String PREF_NAME = "preferences";
	private static final int PREF_MODE = MODE_PRIVATE;
	private static final int MAX_SCREEN_BRIGHTNESS = 255;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if(isFirstStart()) {
			init(hasFlash());
		}
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		if (camera != null) {
			camera = Camera.open();
		}		
	}

	@Override
	protected void onPause() {
		if (camera != null) {
			camera.release();
		}
		super.onPause();
	}
	
	@Override
	protected void onStop() {
		if (camera != null) {
			camera.release();
		}
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		if (camera != null) {
			camera.release();
			camera = null;
		}
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if(itemId == R.id.action_settings) {
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		} else {
		return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public SharedPreferences openPreferences() {
		return getSharedPreferences(PREF_NAME, PREF_MODE);
		
	}
	
	@Override
	public boolean getPreference(String key, boolean defaultValue) {
		return openPreferences().getBoolean(key, defaultValue);
	}
	
	@Override
	public int getPreference(String key, int defaultValue) {
		return openPreferences().getInt(key, defaultValue);
	}
	
	@Override
	public void setPreference(String key, boolean value) {
		Editor prefsEditor = openPreferences().edit();
		prefsEditor.putBoolean(key, value);
		prefsEditor.commit();
		prefsEditor = null;
	}
	
	@Override
	public void setPreference(String key, int value) {
		Editor prefsEditor = openPreferences().edit();
		prefsEditor.putInt(key, value);
		prefsEditor.commit();
		prefsEditor = null;
	}

	private boolean isFirstStart() {
		return getPreference("isFirstStart", true);
	}
	
	private void init(boolean hasFlash) {
		PreferenceManager.setDefaultValues(this, PREF_NAME, PREF_MODE, R.xml.preferences, false);
		setPreference("isFirstStart", false);
		setPreference("hasFlash", hasFlash);
		setPreference("useFlash", hasFlash);
	}
	
	private boolean hasFlash() {
		boolean hasFlash;
		if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
			camera = Camera.open();
			Parameters cameraParameters = camera.getParameters();
			camera.release();
			camera = null;
			List<String> flashModes = cameraParameters.getSupportedFlashModes();
			if(flashModes != null) {
				if(flashModes.get(0).equals("off") && (flashModes.size() > 1)) {
					hasFlash = true;
				} else {
					hasFlash = false;
				}
			} else {
				hasFlash = false;
			}
		} else {
			hasFlash = false;
		}
		return hasFlash;
	}
	

	private boolean useFlash() {
		return getPreference("useFlash", false);
	}
	
	public void toggleTorchLight(View view) {
		if(useFlash()) {
			toggleFlash();
		} else {
			toggleDisplay();
		}
	}
	
	private void toggleFlash() { 
		if(!isFlashOn) {
			((ImageButton) findViewById(R.id.toggle)).setImageResource(R.drawable.button_on);
			try {
				if (camera != null) {
			        camera.release();
			        camera = null;
				}
				camera = Camera.open();
				Parameters parameters = camera.getParameters();
				parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
				camera.setParameters(parameters);
				camera.startPreview();
				isFlashOn = true;
			} catch(Exception e) {
				camera.release();
				camera = null;
				isFlashOn = false;
			}
		} else {
			((ImageButton) findViewById(R.id.toggle)).setImageResource(R.drawable.button_off);
			try {
				Parameters cameraParameters = camera.getParameters();
				cameraParameters.setFlashMode(Parameters.FLASH_MODE_OFF);
				camera.setParameters(cameraParameters);
				camera.stopPreview();
				camera.release();
				camera = null;
			} catch(Exception e) {
				camera.release();
				camera = null;
				isFlashOn = false;
			}
			isFlashOn = false;
		}
	}
	
	private void toggleDisplay() {
		if(!isDisplayOn) {
			((ImageButton) findViewById(R.id.toggle)).setImageResource(R.drawable.button_on);
			saveBrightnessSettings();
			findViewById(R.id.main).setBackgroundColor(Color.argb(255, 255, 255, 255));
			Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
			Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, MAX_SCREEN_BRIGHTNESS);
			isDisplayOn = true;
		} else {
			((ImageButton) findViewById(R.id.toggle)).setImageResource(R.drawable.button_off);
			findViewById(R.id.main).setBackgroundColor(Color.argb(255, 0, 0, 0));
			restoreBrightnessSettings();
			isDisplayOn = false;
		}
	}
	
	public void saveBrightnessSettings() {
		try {
			setPreference("brightnessMode", Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE));
		} catch(SettingNotFoundException e) {
			System.out.println("SettingNotFoundException");
		}
		if(getPreference("brightnessMode", Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL) == Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL) {
			try {
				setPreference("brightness", Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS));
			} catch(SettingNotFoundException e) {
				System.out.println("SettingNotFoundException");
			}
		}
	}
	
	public void restoreBrightnessSettings() {
		Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, getPreference("brightnessMode", Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL));
		if(getPreference("brightnessMode", Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL) == Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL) {
			try {
				Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, getPreference("brightness", Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS)));
			} catch (SettingNotFoundException e) {
				System.out.println("SettingsNotFoundException");
			}
		}
	}
}