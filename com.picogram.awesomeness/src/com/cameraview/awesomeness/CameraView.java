package com.cameraview.awesomeness;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class CameraView extends SurfaceView implements SurfaceHolder.Callback {

	public interface OnPictureTakenListener {
		public void onPictureTaken(Bitmap bm);
	}

	private static final String TAG = "CameraView";
	private boolean isFlashEnabled;
	private OnPictureTakenListener mListener;

	public CameraView(Context context) {
		super(context);
		sharedInit(context);
	}

	public CameraView(Context context, AttributeSet attrs) {
		super(context, attrs);
		sharedInit(context);
	}

	public CameraView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		sharedInit(context);
	}

	public void setOnPictureTakenListner(OnPictureTakenListener optl) {
		mListener = optl;
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		if (previewing) {
			camera1.stopPreview();
			previewing = false;
		}

		if (camera1 != null) {
			try {
				camera1.setPreviewDisplay(surfaceHolder);
				camera1.startPreview();
				previewing = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		if (camera1 != null) {
			camera1.stopPreview();
			camera1.release();
			camera1 = null;
			previewing = false;
		}
	}

	Button btn_capture;
	Camera camera1;
	SurfaceView surfaceView = this;
	SurfaceHolder surfaceHolder;
	private boolean previewing = false;

	public void setButton(Button b) {
		btn_capture = b;
		btn_capture.setOnClickListener(new OnClickListener() {
			final ShutterCallback myShutterCallback = new ShutterCallback() {

				@Override
				public void onShutter() {
					// Called as near as possible to the moment when a photo is
					// captured from the sensor. This is a good opportunity to
					// play
					// a shutter sound or give other feedback of camera
					// operation.
					// This may be some time after the photo was triggered, but
					// some
					// time before the actual data is available.

				}
			};

			final PictureCallback myPictureCallback_RAW = new PictureCallback() {

				@Override
				public void onPictureTaken(byte[] data, Camera cam) {
					Log.d(TAG, "RAW Picture");
					// TODO: Add on picture taken.
				}
			};

			PictureCallback myPictureCallback_JPG = new PictureCallback() {
				@Override
				public void onPictureTaken(byte[] arg0, Camera arg1) {
					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inSampleSize = 6;
					options.inDither = false; // Disable Dithering mode
					options.inPurgeable = true; // Tell to gc that whether it
												// needs free
												// memory, the Bitmap can be
												// cleared
					options.inInputShareable = true; // Which kind of reference
														// will be
														// used to recover the
														// Bitmap
														// data after being
														// clear, when
														// it will be used in
														// the future
					options.inTempStorage = new byte[32 * 1024];
					options.inPreferredConfig = Bitmap.Config.RGB_565;
					Bitmap bMap = BitmapFactory.decodeByteArray(arg0, 0,
							arg0.length, options);
					int orientation;
					// others devices
					if (bMap.getHeight() < bMap.getWidth()) {
						orientation = 90;
					} else {
						orientation = 0;
					}

					Bitmap bMapRotate;
					if (orientation != 0) {
						Matrix matrix = new Matrix();
						matrix.postRotate(orientation);
						bMapRotate = Bitmap.createBitmap(bMap, 0, 0,
								bMap.getWidth(), bMap.getHeight(), matrix, true);
					} else
						bMapRotate = Bitmap.createScaledBitmap(bMap,
								bMap.getWidth(), bMap.getHeight(), true);

					mListener.onPictureTaken(bMapRotate);

					// camera1.startPreview();
				}
			};

			@Override
			public void onClick(View arg0) {
				if (camera1 != null) {
					camera1.takePicture(myShutterCallback,
							myPictureCallback_RAW, myPictureCallback_JPG);

				}

			}

		});

	}

	private void setPicture(Bitmap bitmapPicture) {
		this.picture = bitmapPicture;
	}

	private void sharedInit(Context c) {
		// getWindow().setFormat(PixelFormat.UNKNOWN);
		surfaceHolder = this.getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		btn_capture = new Button(this.getContext());

		// TODO: Get btn_capture from user.
		this.setBackgroundColor(Color.TRANSPARENT);
		// .setBackgroundResource(R.drawable.your_background_image);

		if (!previewing) {
			try {
				camera1 = Camera.open();
				if (camera1 != null) {
					try {
						Parameters p = camera1.getParameters();
						if (isFlashEnabled()) {
							p.setFlashMode(Parameters.FLASH_MODE_TORCH);
							camera1.setParameters(p);
						}
						camera1.setDisplayOrientation(90);
						camera1.setPreviewDisplay(surfaceHolder);
						camera1.startPreview();
						previewing = true;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
			}

		}

		final ShutterCallback myShutterCallback = new ShutterCallback() {

			@Override
			public void onShutter() {
				// Called as near as possible to the moment when a photo is
				// captured from the sensor. This is a good opportunity to play
				// a shutter sound or give other feedback of camera operation.
				// This may be some time after the photo was triggered, but some
				// time before the actual data is available.

			}
		};

		final PictureCallback myPictureCallback_RAW = new PictureCallback() {

			@Override
			public void onPictureTaken(byte[] data, Camera cam) {
				// TODO: Add on picture taken.
			}
		};

		final PictureCallback myPictureCallback_JPG = new PictureCallback() {

			@Override
			public void onPictureTaken(byte[] data, Camera cam) {
				Bitmap bitmapPicture = BitmapFactory.decodeByteArray(data, 0,
						data.length);
				picture = bitmapPicture;
				Bitmap correctBmp = Bitmap.createBitmap(bitmapPicture, 0, 0,
						bitmapPicture.getWidth(), bitmapPicture.getHeight(),
						null, true);
				String extr = Environment.getExternalStorageDirectory()
						.toString();
				File mFolder = new File(extr + "/MyApp");

				if (!mFolder.exists()) {
					mFolder.mkdir();
				}

				String strF = mFolder.getAbsolutePath();
				File mSubFolder = new File(strF + "/MyApp-SubFolder");

				if (!mSubFolder.exists()) {
					mSubFolder.mkdir();
				}

				String s = "myfile.png";
				File f = new File(mSubFolder.getAbsolutePath(), s);
				String strMyImagePath = f.getAbsolutePath();
				FileOutputStream fos = null;
				try {
					fos = new FileOutputStream(f);
					correctBmp.compress(Bitmap.CompressFormat.PNG, 70, fos);

					fos.flush();
					fos.close();
					// MediaStore.Images.Media.insertImage(getContentResolver(),
					// b, "Screen", "screen");
				} catch (FileNotFoundException e) {

					e.printStackTrace();
				} catch (Exception e) {

					e.printStackTrace();
				}
				Log.d(TAG, "Saved success");
			}
		};

		btn_capture.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (camera1 != null) {
					camera1.takePicture(myShutterCallback,
							myPictureCallback_RAW, myPictureCallback_JPG);

				}

			}

		});
	}

	public Bitmap picture;

	int currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;

	public void switchCameras(Activity a) {
		if (previewing) {
			camera1.stopPreview();
		}
		// NB: if you don't release the current camera before switching, you app
		// will crash
		camera1.release();

		// swap the id of the camera to be used
		if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
			currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
		} else {
			currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
		}
		camera1 = Camera.open(currentCameraId);
		// Code snippet for this method from somewhere on android developers, i
		// forget where
		setCameraDisplayOrientation(a, currentCameraId, camera1);
		try {
			// this step is critical or preview on new camera will no know where
			// to render to
			camera1.setPreviewDisplay(surfaceHolder);
		} catch (IOException e) {
			e.printStackTrace();
		}
		camera1.startPreview();
	}

	private void setCameraDisplayOrientation(Activity a, int cameraIndex,
			Camera camera) {
		// Sets the camera right Orientation.
		// IMPORTANT!! This code is available only for API Level 9 build or
		// greater.
		android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
		android.hardware.Camera.getCameraInfo(cameraIndex, info);
		int rotation = a.getWindowManager().getDefaultDisplay().getRotation();
		int degrees = 0;
		switch (rotation) {
		case Surface.ROTATION_0:
			degrees = 0;
			break;
		case Surface.ROTATION_90:
			degrees = 90;
			break;
		case Surface.ROTATION_180:
			degrees = 180;
			break;
		case Surface.ROTATION_270:
			degrees = 270;
			break;
		}

		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360; // compensate the mirror
		} else { // back-facing
			result = (info.orientation - degrees + 360) % 360;
		}
		camera.setDisplayOrientation(result);

	}

	private Camera openFrontFacingCamera() {
		int cameraCount = 0;
		Camera cam = null;
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		cameraCount = Camera.getNumberOfCameras();
		for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
			Camera.getCameraInfo(camIdx, cameraInfo);
			if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
				try {
					cam = Camera.open(camIdx);
				} catch (RuntimeException e) {
					Log.e(TAG,
							"Camera failed to open: " + e.getLocalizedMessage());
				}
			}
		}

		return cam;
	}

	public boolean isFlashEnabled() {
		return isFlashEnabled;
	}

	public void setFlashEnabled(boolean isFlashEnabled) {
		this.isFlashEnabled = isFlashEnabled;
		sharedInit(null);
	}
}
