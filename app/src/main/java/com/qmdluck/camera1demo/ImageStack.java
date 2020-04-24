package com.qmdluck.camera1demo;

import android.util.Log;

import java.nio.ByteBuffer;

public class ImageStack {
	private final static String TAG = ImageStack.class.getSimpleName();
	private static Object mLock = new Object();
	private ImageInfo imageOne = null;
	private ImageInfo imageTwo = null;

	public ImageStack(int width, int height) {
		imageOne = new ImageInfo(width, height);
		imageTwo = new ImageInfo(width, height);
	}

	/**
	 * 出堆
	 * @return
	 */
	public ImageInfo pullImageInfo() {
		synchronized (mLock) {
			if (imageOne.isNew()) {
				imageTwo.setImage(imageOne.getData());
				imageTwo.setTime(imageOne.getTime());
				imageTwo.setNew(true);
				imageOne.setNew(false);
			} else {
				imageTwo.setNew(false);
			}
			return imageTwo;
		}
	}

	/**
	 * 入堆
	 * @param img
	 */
	public void pushImageInfo(byte[] imgData, long time) {
		//log("pushImageInfo(byte[]) isReading:" + isReading);
		synchronized (mLock) {
			imageOne.setImage(imgData);
			imageOne.setTime(time);
			imageOne.setNew(true);
		}
	}

	/**
	 * 入堆
	 * @param img
	 */
	public void pushImageInfo(ByteBuffer buffer, long time) {
		//log("pushImageInfo(ByteBuffer) isReading:" + isReading);
		synchronized (mLock) {
			imageOne.setImage(buffer);
			imageOne.setTime(time);
			imageOne.setNew(true);
			//log("pullImageInfo() imageOne.setNew(true)");
		}
	}

	public void clearAll() {
		synchronized (mLock) {
			imageOne.setNew(false);
			imageTwo.setNew(false);
		}
	}

	// 打印log
	public static void log(String msg) {
		Log.e(TAG, msg);
	}
}
