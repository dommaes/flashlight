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
public class MainActivity extends Activity {
	// variable declaration
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
		init(hasFlash());
		setContentView(R.layout.activity_main);
		mainView = findViewById(R.id.main);
		imageButton = (ImageButton) findViewById(R.id.toggle);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (camera != null) {
			camera = Camera.open();
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if (camera != null) {
			camera.release();
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (camera != null) {
			camera.release();
		}
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
	
	private boolean isFirstStart() {
		SharedPreferences prefs = getSharedPreferences("preferences", MODE_PRIVATE);
		return prefs.getBoolean("isFirstStart", true);
	}
	
	private void init(boolean hasFlash) {
		if(isFirstStart()) {
			PreferenceManager.setDefaultValues(this, "preferences", MODE_PRIVATE, R.xml.preferences, false);
			SharedPreferences prefs = getSharedPreferences("preferences", MODE_PRIVATE);
			Editor prefsEditor = prefs.edit();
			prefsEditor.putBoolean("isFirstStart", false);
			prefsEditor.putBoolean("hasFlash", hasFlash);
			prefsEditor.putBoolean("useFlash", hasFlash);
			prefsEditor.commit();
		}
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
		SharedPreferences prefs = getSharedPreferences("preferences", MODE_PRIVATE);
		return prefs.getBoolean("useFlash", false);
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
			imageButton.setImageResource(R.drawable.button_on);
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
			imageButton.setImageResource(R.drawable.button_off);
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
			Editor prefsEditor = getSharedPreferences("preferences", MODE_PRIVATE).edit();
			prefsEditor.putInt("brightnessMode", Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE));
			prefsEditor.commit();
		} catch(SettingNotFoundException e) {
			System.out.println("SettingNotFoundException");
		}
		SharedPreferences prefs = getSharedPreferences("preferences", MODE_PRIVATE);
		if(prefs.getInt("brightnessMode", Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL) == Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL) {
			try {
				Editor prefsEditor = getSharedPreferences("preferences", MODE_PRIVATE).edit();
				prefsEditor.putInt("brightness", Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS));
				prefsEditor.commit();
			} catch(SettingNotFoundException e) {
				System.out.println("SettingNotFoundException");
			}
		}
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
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		if(camera != null) {
			camera.release();
			camera = null;
		}
	}
}