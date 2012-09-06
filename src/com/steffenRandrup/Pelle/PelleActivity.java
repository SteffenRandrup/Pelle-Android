package com.steffenRandrup.Pelle;

import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class PelleActivity extends Activity implements OnClickListener, OnSharedPreferenceChangeListener {
	/** Called when the activity is first created. */

	TextView tid, spredning, forekomster, slutTid, mindste;
	Button start;
	String time, std, fore, minTid;
	int lyd = 0, sound, antal = 0;
	SoundPool sp;
	SharedPreferences getPrefs;
	boolean go = false, vibration = false, lydCheck = true;
	double TidMin;
	long tidStart = 0, tidSlut = 0;
	Thread timer;
	
	
	
	
	// slet måske og wake lock permission
	
	WakeLock wl;
	
	// Der er også wl længere nede
	
	//

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		getPrefs = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		getPrefs.registerOnSharedPreferenceChangeListener(this);
		tid = (TextView) findViewById(R.id.tvTidInt);
		spredning = (TextView) findViewById(R.id.tvSpred);
		forekomster = (TextView) findViewById(R.id.tvForekomster);
		slutTid = (TextView) findViewById(R.id.tvTid);
		mindste = (TextView) findViewById(R.id.tvMin);
		start = (Button) findViewById(R.id.bStart);
		start.setOnClickListener(this);
		
		sp = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
		getSound();
		time = getPrefs.getString("tid", "3");
		std = getPrefs.getString("spred", "1");
		tid.setText("Tidsinterval: " + time + " sekunder");
		spredning.setText("Standardafvigelse: "+std);
		vibration = getPrefs.getBoolean("checkbox", false);
		lydCheck = getPrefs.getBoolean("lydCheck", true);
		minTid = getPrefs.getString("minTid", "0.3");
		TidMin = Double.parseDouble(minTid); 
		mindste.setText("Mindste tid: "+minTid);
		
		
		
		//
		
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "whatever");
		
		//
	}


	private void getSound() {
		int lol = 0;
		String bum = getPrefs.getString("list", "2");
		if (bum.equals("1")) {
			lol = (int) R.raw.punch;
		} else if (bum.equals("2")) {
			lol = (int) R.raw.beep;
		} else if (bum.equals("3")) {
			lol = (int) R.raw.explosion;
		} else if (bum.equals("4")) {
			lol = (int) R.raw.pew;
		} else if (bum.equals("5")) {
			lol = (int) R.raw.swush;
		}

		lyd = sp.load(this, lol, 1);
	}

	@Override
	public void onClick(View v) {

		
		
		if (go == false) {
			lavLyd();
			go = true;
			tidStart = System.currentTimeMillis();
			
			//
			wl.acquire();
			//
		} else if (go == true) {

			go = false;
			tidSlut = System.currentTimeMillis();
			
			long result = tidSlut - tidStart;
			int millis = (int) result;
			int seconds = (int) (millis/1000);
			int minutes = (int) (seconds/60);
			seconds = seconds % 60;
			
			fore = Integer.toString(antal);
			antal = 0;
			forekomster.setText(""+fore);
			slutTid.setText(String.format("%d:%02d", minutes, seconds));
			//
			wl.release();
			//
		}			
	}

	private void lavLyd() {

		timer = new Thread() {
			public void run() {
				try {
					sleep(1000);
					Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
					
					while (go == true) {
						Random generator = new Random(System.currentTimeMillis());
						double roll = generator.nextGaussian();
						int bum = (int) ((roll*Double.parseDouble(std) +  Double.parseDouble(time))* 1000);
						
						if (bum > (TidMin*1000)) {
							antal++;
							if(lydCheck){
								sp.play(lyd, 1, 1, 0, 0, 1);
							}
							if(vibration){
								vib.vibrate(200);
							}
							sleep(bum);
						}	
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};
		timer.start();

	}

	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater blowUp = getMenuInflater();
		blowUp.inflate(R.menu.cool_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.aboutUs:
			Intent i = new Intent("com.steffenRandrup.Pelle.ABOUT");
			startActivity(i);
			break;
		case R.id.preferences:
			Intent p = new Intent("com.steffenRandrup.Pelle.PREFS");
			startActivity(p);
			break;
		}
		return false;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if(key.equals("tid")){
			time = getPrefs.getString("tid", "3");
			tid.setText("Tidsinterval: " + time + " sekunder");
		}else if(key.equals("list")){
			getSound();
		}else if(key.equals("checkbox")){
			vibration = getPrefs.getBoolean("checkbox", false);
		}else if(key.equals("lydCheck")){
			lydCheck = getPrefs.getBoolean("lydCheck", true);
		}else if(key.equals("spred")){
			std = getPrefs.getString("spred", "1");
			spredning.setText("Standardafvigelse: " + std);
		}else if(key.equals("minTid")){
			minTid = getPrefs.getString("minTid", "0.3");
			TidMin = Double.parseDouble(minTid);
			mindste.setText("Mindste tid: "+minTid);
		}
	}


	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		go = false;
		
		
		//
		if (wl.isHeld())
		wl.release();
		//
	}	
	
	
}





