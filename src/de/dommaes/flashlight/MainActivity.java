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
	private boolean hasFlash = false;
	private boolean useFlash = true;
	private Camera camera = null;
	private boolean isFlashOn = false;
	private boolean isDisplayOn = false;
	private static View mainView = null;
	private ImageButton imageButton = null;
	
	//constant declaration
	private static final int MAX_SCREEN_BRIGHTNESS = 255;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences prefs = getSharedPreferences("preferences", MODE_PRIVATE);
		if(prefs.getBoolean("isFirstStart", true)) {
			PreferenceManager.setDefaultValues(this, "preferences", MODE_PRIVATE, R.xml.preferences, false);
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
			startActivity(new Intent(this, SettingsActivity.class));
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
		if(!isFlashOn) {
			imageButton.setImageResource(R.drawable.button_on);
			camera = Camera.open();
			Parameters parameters = camera.getParameters();
			parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
			camera.setParameters(parameters);
			camera.startPreview();
			isFlashOn = true;
			parameters = null;
		} else {
			imageButton.setImageResource(R.drawable.button_off);
			camera.stopPreview();
			camera.release();
			camera = null;
			isFlashOn = false;
		}
	}
	
	private void toggleDisplay() {
		if(!isDisplayOn) {
			imageButton.setImageResource(R.drawable.button_on);
			saveBrightnessSettings();
			mainView.setBackgroundColor(Color.argb(255, 255, 255, 255));
			Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
			Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, MAX_SCREEN_BRIGHTNESS);
			isDisplayOn = true;
		} else {
			imageButton.setImageResource(R.drawable.button_off);
			mainView.setBackgroundColor(Color.argb(255, 0, 0, 0));
			restoreBrightnessSettings();
			isDisplayOn = false;
		}
	}
	
	public void saveBrightnessSettings() {
		try {
			//savedScreenBrightnessMode = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
			Editor prefsEditor = getSharedPreferences("preferences", MODE_PRIVATE).edit();
			prefsEditor.putInt("brightnessMode", Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE));
			prefsEditor.commit();
			prefsEditor = null;
		} catch(SettingNotFoundException e) {
			System.out.println("SettingNotFoundException");
		}
		SharedPreferences prefs = getSharedPreferences("preferences", MODE_PRIVATE);
		if(prefs.getInt("brightnessMode", Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL) == Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL) {
			try {
				//savedScreenBrightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
				Editor prefsEditor = getSharedPreferences("preferences", MODE_PRIVATE).edit();
				prefsEditor.putInt("brightness", Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS));
				prefsEditor.commit();
				prefsEditor = null;
			} catch(SettingNotFoundException e) {
				System.out.println("SettingNotFoundException");
			}
		}
		prefs = null;
	}
	
	public void restoreBrightnessSettings() {
		SharedPreferences prefs = getSharedPreferences("preferences", MODE_PRIVATE);
		Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, prefs.getInt("brightnessMode", Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL));
		if(prefs.getInt("brightnessMode", Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL) == Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL) {
			try {
				Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, prefs.getInt("brightness", Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS)));
			} catch (SettingNotFoundException e) {
				System.out.println("SettingsNotFoundException");
			}
		}
		prefs = null;
	}
}