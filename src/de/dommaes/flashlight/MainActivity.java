package de.dommaes.flashlight;

import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;

//Flashlight v1.0
public class MainActivity extends Activity {
	// variable declaration
	private boolean hasFlash = false;
	private Camera camera = null;
	private boolean isFlashlightOn = false;
	private boolean isDisplayFlashlightOn = false;
	private static View mainView = null;
	private int savedScreenBrightness = 0;
	private int savedScreenBrightnessMode = 0;
	private ImageButton imageButton = null;
	private static final int MAX_SCREEN_BRIGHTNESS = 255;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences preferences = getPreferences(MODE_PRIVATE);
		if(preferences.contains("isFirstStart") == false) {
			Editor preferencesEditor = preferences.edit();
			preferencesEditor.putBoolean("isFirstStart", false);
			hasFlash = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
			preferencesEditor.putBoolean("hasFlash", hasFlash);
			preferencesEditor.commit();
			preferencesEditor = null;
		} else {
			hasFlash = preferences.getBoolean("hasFlash", false);
		}
		preferences = null;
		setContentView(R.layout.activity_main);
		mainView = findViewById(R.id.main);
		imageButton = (ImageButton) findViewById(R.id.toggle);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	public void toggleTorchLight(View view) {
		if(hasFlash) {
			toggleFlashlight();
		} else {
			toggleDisplayFlashlight();
		}
	}
	
	private void toggleFlashlight() {
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
	
	private void toggleDisplayFlashlight() {
		if(!isDisplayFlashlightOn) {
			saveScreenBrightnessSettings();
			Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
			Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, MAX_SCREEN_BRIGHTNESS);
			mainView.setBackgroundColor(Color.argb(255, 255, 255, 255));
			isDisplayFlashlightOn = true;
		} else {
			mainView.setBackgroundColor(Color.argb(255, 0, 0, 0));
			restoreScreenBrightnessSettings();
			isDisplayFlashlightOn = false;
		}
	}
	
	public void saveScreenBrightnessSettings() {
		try {
			savedScreenBrightnessMode = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
		} catch(SettingNotFoundException e) {
			
		}
		if(savedScreenBrightnessMode == Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL) {
			try {
				savedScreenBrightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
			} catch(SettingNotFoundException e) {
				
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
