package net.videosc2.processing;

import android.os.Bundle;

import net.videosc2.R;

import processing.core.PApplet;

/**
 * Created by stefan on 04.03.17.
 */

public class TestApplet extends PApplet {
	static final String TAG = "TestApplet";

	int sLength = 200;

	public void settings() {
//		size(500, 500);
//		fullScreen();
//		background(0, 0);
	}

	public void setup() {
		rectMode(CENTER);
	}

	public void draw() {
		strokeWeight(10);
		stroke(random(255), random(255), random(0xFF));
		rect(0, 0, sLength, sLength);
	}

	@Override
	public void onCreate(Bundle saveInstanceState) {
		super.onCreate(saveInstanceState);
//		fullScreen();
//		rectMode(CENTER);
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}
