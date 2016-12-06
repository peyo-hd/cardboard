package com.google.vr.sdk.base.sensors;

import android.os.Handler;

public class RiftManager {
    static {
        System.loadLibrary("riftmanager_jni");
    }
	
	public class RiftEvent {
		public final static int TYPE_ACC = 1;
		public final static int TYPE_GYRO = 4;
	    public int type;
	    public float[] values;
	    public long timestamp;
	    public RiftEvent(int valueSize) {
	        values = new float[valueSize];
	    }
	}
	public interface RiftEventListener {
	    public void onRiftEvent(RiftEvent event);
	}
	
	RiftEventListener mRiftEventListener;
	Handler mHandler;
	long mDelayMs;

	public void registerListener(RiftEventListener listener, 
			long delayMs, Handler handler) {
		mRiftEventListener = listener;
		mDelayMs = delayMs;
		mHandler = handler;
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				nativeOpen();
				mHandler.postDelayed(mUpdateRunnable, mDelayMs);
			}
		});
	}

	public void unregisterListener(RiftEventListener mSensorEventListener) {
		mHandler.removeCallbacks(mUpdateRunnable);
		mSensorEventListener = null;
		nativeClose();
	}
	
	Runnable mUpdateRunnable = new Runnable() {
		@Override
		public void run() {
			update();
		}
	};

	public void update() {
		if (mRiftEventListener != null) {
			mHandler.postDelayed(mUpdateRunnable, mDelayMs);
		}
		nativeUpdate();
	}
	
	private native void nativeOpen();
	private native void nativeUpdate();
	private native void nativeClose();
	
	private void notifyRiftEvent(int type, long timestamp,
			float x, float y, float z) {
		if (mRiftEventListener == null) return;
		RiftEvent event = new RiftEvent(3);
		event.type = type;
		event.timestamp = timestamp;
		event.values[0] = x;
		event.values[1] = y;
		event.values[2] = z;
		mRiftEventListener.onRiftEvent(event);
	}

	private void notifyRiftEventAcc(long timestamp,
			float x, float y, float z) {
		notifyRiftEvent(RiftEvent.TYPE_ACC, timestamp, x, y, z);
	}

	private void notifyRiftEventGyro(long timestamp,
			float x, float y, float z) {
		notifyRiftEvent(RiftEvent.TYPE_GYRO, timestamp, x, y, z);
	}

}
