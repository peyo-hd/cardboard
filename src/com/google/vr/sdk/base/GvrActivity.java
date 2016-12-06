/*
 * Copyright 2014 Google Inc. All Rights Reserved.

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.vr.sdk.base;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

import com.google.vr.sdk.base.sensors.MagnetSensor;
import com.google.vr.sdk.base.sensors.NfcSensor;

/**
 * Base activity that provides easy integration with Cardboard devices.
 * 
 * Exposes events to interact with Cardboards and handles many of the details commonly required when creating an Activity for VR rendering.
 */
public class GvrActivity extends Activity
implements MagnetSensor.OnCardboardTriggerListener, NfcSensor.OnCardboardNfcListener
{
	private static final int NAVIGATION_BAR_TIMEOUT_MS = 2000;
	private GvrView mGvrView;
	private MagnetSensor mMagnetSensor;
	private NfcSensor mNfcSensor;
	private int mVolumeKeysMode;

	public void setGvrView(GvrView cardboardView)
	{
		mGvrView = cardboardView;

		if (cardboardView != null) {
			CardboardDeviceParams cardboardDeviceParams = null;
			if (cardboardDeviceParams == null) {
				cardboardDeviceParams = new CardboardDeviceParams();
			}

			cardboardView.updateCardboardDeviceParams(cardboardDeviceParams);
		}
	}

	public GvrView getGvrView()
	{
		return mGvrView;
	}

	public void setVolumeKeysMode(int mode)
	{
		mVolumeKeysMode = mode;
	}

	public int getVolumeKeysMode()
	{
		return mVolumeKeysMode;
	}

	public boolean areVolumeKeysDisabled()
	{
		switch (mVolumeKeysMode) {
		case 0:
			return false;
		case 2:
			return isDeviceInCardboard();
		case 1:
			return true;
		}

		throw new IllegalStateException("Invalid volume keys mode " + mVolumeKeysMode);
	}

	public boolean isDeviceInCardboard()
	{
		return mNfcSensor.isDeviceInCardboard();
	}

	public void onInsertedIntoCardboard(CardboardDeviceParams deviceParams)
	{
		if (mGvrView != null)
			mGvrView.updateCardboardDeviceParams(deviceParams);
	}

	public void onRemovedFromCardboard()
	{
	}

	public void onCardboardTrigger()
	{
	}

	protected void onNfcIntent(Intent intent)
	{
		mNfcSensor.onNfcIntent(intent);
	}

	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		requestWindowFeature(1);

		getWindow().addFlags(128);

		if (Build.VERSION.SDK_INT < 19) {
			final Handler handler = new Handler();
			getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener()
			{
				public void onSystemUiVisibilityChange(int visibility)
				{
					if ((visibility & 0x2) == 0)
						handler.postDelayed(new Runnable()
						{
							public void run() {
								GvrActivity.this.setFullscreenMode();
							}
						}
						, 2000L);
				}
			});
		}
	}

	protected void onResume()
	{
		super.onResume();

		if (mGvrView != null) {
			mGvrView.onResume();
		}
	}

	protected void onPause()
	{
		super.onPause();

		if (mGvrView != null) {
			mGvrView.onPause();
		}
	}

	protected void onDestroy()
	{
		super.onDestroy();
	}

	public void setContentView(View view)
	{
		if ((view instanceof GvrView)) {
			setGvrView((GvrView)view);
		}

		super.setContentView(view);
	}

	public void setContentView(View view, ViewGroup.LayoutParams params)
	{
		if ((view instanceof GvrView)) {
			setGvrView((GvrView)view);
		}

		super.setContentView(view, params);
	}

	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (((keyCode == 24) || (keyCode == 25)) && (areVolumeKeysDisabled()))
		{
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	public boolean onKeyUp(int keyCode, KeyEvent event)
	{
		if (((keyCode == 24) || (keyCode == 25)) && (areVolumeKeysDisabled()))
		{
			return true;
		}

		return super.onKeyUp(keyCode, event);
	}

	public void onWindowFocusChanged(boolean hasFocus)
	{
		super.onWindowFocusChanged(hasFocus);

		if (hasFocus)
			setFullscreenMode();
	}

	private void setFullscreenMode()
	{
		getWindow().getDecorView().setSystemUiVisibility(5894);
	}

	/** Defines the constants with options for managing the volume keys.  */
	public static class VolumeKeys
	{
		public static final int NOT_DISABLED = 0;
		public static final int DISABLED = 1;
		public static final int DISABLED_WHILE_IN_CARDBOARD = 2;
	}
}