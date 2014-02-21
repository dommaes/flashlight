package de.dommaes.flashlight;

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
public class MainActivity extends Activity {
	// variable declaration
	private boolean isFirstStart = true;
	private boolean hasFlash = false;
	private boolean useFlash = true;
	private Camera camera = null;
	private boolean isFlashlightOn = false;
	private boolean isDisplayFlashlightOn = false;
	private static View mainView = null;
	private int savedScreenBrightness = 0;
	private int savedScreenBrightnessMode = 0;
	private ImageButton imageButton = null;
	
	//constant declaration
	private static final int MAX_SCREEN_BRIGHTNESS = 255;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PreferenceManager.setDefaultValues(this, "preferences", MODE_PRIVATE, R.xml.preferences, false);
		SharedPreferences prefs = getSharedPreferences("preferences", MODE_PRIVATE);
		isFirstStart = prefs.getBoolean("isFirstStart", true);
		if(isFirstStart) {
			Editor prefsEditor = prefs.edit();
			prefsEditor.putBoolean("isFirstStart", false);
			hasFlash = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
			prefsEditor.putBoolean("hasFlash", hasFlash);
			prefsEditor.putBoolean("useFlash", hasFlash);
			prefsEditor.commit();
			prefsEditor = null;
		}
		hasFlash = prefs.getBoolean("hasFlash", false);
		prefs = null;
		setContentView(R.layout.activity_main);
		mainView = findViewById(R.id.main);
		imageButton = (ImageButton) findViewById(R.id.toggle);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		SharedPreferences prefs = getSharedPreferences("preferences", MODE_PRIVATE);
		useFlash = prefs.getBoolean("useFlash", false);
		prefs = null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if(itemId == R.id.menu_settings) {
			Intent settingsIntent = new Intent(this, SettingsActivity.class);
			startActivity(settingsIntent);
			return true;
		} else {
		return super.onOptionsItemSelected(item);
		}
	}
	
	public void toggleTorchLight(View view) {
		if(useFlash) {
			toggleFlash();
		} else {
			toggleDisplay();
		}
	}
	
	private void toggleFlash() {
		if(!isFlashlightOn) {
			imageButton.setImageResource(R.drawable.button_on);
			camera = Camera.open();
			Parameters parameters = camera.getParameters();
			parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
			camera.setParameters(parameters);
			camera.startPreview();
			isFlashlightOn = true;
			parameters = null;
		} else {
			imageButton.setImageResource(R.drawable.button_off);
			camera.stopPreview();
			camera.release();
			camera = null;
			isFlashlightOn = false;
		}
	}
	
	private void toggleDisplay() {
		if(!isDisplayFlashlightOn) {
			imageButton.setImageResource(R.drawable.button_on);
			saveScreenBrightnessSettings();
			mainView.setBackgroundColor(Color.argb(255, 255, 255, 255));
			Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
			Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, MAX_SCREEN_BRIGHTNESS);
			isDisplayFlashlightOn = true;
		} else {
			imageButton.setImageResource(R.drawable.button_off);
			mainView.setBackgroundColor(Color.argb(255, 0, 0, 0));
			restoreScreenBrightnessSettings();
			isDisplayFlashlightOn = false;
		}
	}
	
	public void saveScreenBrightnessSettings() {
		try {
			savedScreenBrightnessMode = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
		} catch(SettingNotFoundException e) {
			System.out.println("SettingNotFoundException");
		}
		if(savedScreenBrightnessMode == Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL) {
			try {
				savedScreenBrightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
			} catch(SettingNotFoundException e) {
				System.out.println("SettingNotFoundException");
			}
		}
	}
	
	public void restoreScreenBrightnessSettings() {
		Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, savedScreenBrightnessMode);
		if(savedScreenBrightnessMode == Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL) {
			Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, savedScreenBrightness);
		}
	}
}