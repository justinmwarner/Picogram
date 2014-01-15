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

public class CameraView extends SurfaceView implements SurfaceHolder.Callback {

	public interface OnPictureTakenListener {
		public void onPictureTaken(Bitmap bm);
	}

	private static final String TAG = "CameraView";
	private boolean isFlashEnabled;
	private OnPictureTakenListener mListener;

	Button btn_capture;

	Camera camera1;

	SurfaceView surfaceView = this;

	SurfaceHolder surfaceHolder;

	private boolean previewing = false;

	public Bitmap picture;

	int currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;

	public CameraView(final Context context) {
		super(context);
		this.sharedInit(context);
	}
	public CameraView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		this.sharedInit(context);
	}
	public CameraView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		this.sharedInit(context);
	}
	public boolean isFlashEnabled() {
		return this.isFlashEnabled;
	}
	private Camera openFrontFacingCamera() {
		int cameraCount = 0;
		Camera cam = null;
		final Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		cameraCount = Camera.getNumberOfCameras();
		for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
			Camera.getCameraInfo(camIdx, cameraInfo);
			if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
				try {
					cam = Camera.open(camIdx);
				} catch (final RuntimeException e) {
					Log.e(TAG,
							"Camera failed to open: " + e.getLocalizedMessage());
				}
			}
		}

		return cam;
	}

	public void setButton(final Button b) {
		this.btn_capture = b;
		this.btn_capture.setOnClickListener(new OnClickListener() {
			final ShutterCallback myShutterCallback = new ShutterCallback() {

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

				public void onPictureTaken(final byte[] data, final Camera cam) {
					Log.d(TAG, "RAW Picture");
					// TODO: Add on picture taken.
				}
			};

			PictureCallback myPictureCallback_JPG = new PictureCallback() {

				public void onPictureTaken(final byte[] arg0, final Camera arg1) {
					final BitmapFactory.Options options = new BitmapFactory.Options();
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
					final Bitmap bMap = BitmapFactory.decodeByteArray(arg0, 0,
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
						final Matrix matrix = new Matrix();
						matrix.postRotate(orientation);
						bMapRotate = Bitmap.createBitmap(bMap, 0, 0,
								bMap.getWidth(), bMap.getHeight(), matrix, true);
					} else {
						bMapRotate = Bitmap.createScaledBitmap(bMap,
								bMap.getWidth(), bMap.getHeight(), true);
					}

					CameraView.this.mListener.onPictureTaken(bMapRotate);

					// camera1.startPreview();
				}
			};

			public void onClick(final View arg0) {
				if (CameraView.this.camera1 != null) {
					CameraView.this.camera1.takePicture(this.myShutterCallback,
							this.myPictureCallback_RAW, this.myPictureCallback_JPG);

				}

			}

		});

	}

	private void setCameraDisplayOrientation(final Activity a, final int cameraIndex,
			final Camera camera) {
		// Sets the camera right Orientation.
		// IMPORTANT!! This code is available only for API Level 9 build or
		// greater.
		final android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
		android.hardware.Camera.getCameraInfo(cameraIndex, info);
		final int rotation = a.getWindowManager().getDefaultDisplay().getRotation();
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
			result = ((info.orientation - degrees) + 360) % 360;
		}
		camera.setDisplayOrientation(result);

	}

	public void setFlashEnabled(final boolean isFlashEnabled) {
		this.isFlashEnabled = isFlashEnabled;
		this.sharedInit(null);
	}

	public void setOnPictureTakenListner(final OnPictureTakenListener optl) {
		this.mListener = optl;
	}

	private void setPicture(final Bitmap bitmapPicture) {
		this.picture = bitmapPicture;
	}

	private void sharedInit(final Context c) {
		// getWindow().setFormat(PixelFormat.UNKNOWN);
		this.surfaceHolder = this.getHolder();
		this.surfaceHolder.addCallback(this);
		this.surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		this.btn_capture = new Button(this.getContext());

		// TODO: Get btn_capture from user.
		this.setBackgroundColor(Color.TRANSPARENT);
		// .setBackgroundResource(R.drawable.your_background_image);

		if (!this.previewing) {
			try {
				this.camera1 = Camera.open();
				if (this.camera1 != null) {
					try {
						final Parameters p = this.camera1.getParameters();
						if (this.isFlashEnabled()) {
							p.setFlashMode(Parameters.FLASH_MODE_TORCH);
							this.camera1.setParameters(p);
						}
						this.camera1.setDisplayOrientation(90);
						this.camera1.setPreviewDisplay(this.surfaceHolder);
						this.camera1.startPreview();
						this.previewing = true;
					} catch (final IOException e) {
						e.printStackTrace();
					}
				}
			} catch (final Exception e) {
			}

		}

		final ShutterCallback myShutterCallback = new ShutterCallback() {


			public void onShutter() {
				// Called as near as possible to the moment when a photo is
				// captured from the sensor. This is a good opportunity to play
				// a shutter sound or give other feedback of camera operation.
				// This may be some time after the photo was triggered, but some
				// time before the actual data is available.

			}
		};

		final PictureCallback myPictureCallback_RAW = new PictureCallback() {


			public void onPictureTaken(final byte[] data, final Camera cam) {
				// TODO: Add on picture taken.
			}
		};

		final PictureCallback myPictureCallback_JPG = new PictureCallback() {


			public void onPictureTaken(final byte[] data, final Camera cam) {
				final Bitmap bitmapPicture = BitmapFactory.decodeByteArray(data, 0,
						data.length);
				CameraView.this.picture = bitmapPicture;
				final Bitmap correctBmp = Bitmap.createBitmap(bitmapPicture, 0, 0,
						bitmapPicture.getWidth(), bitmapPicture.getHeight(),
						null, true);
				final String extr = Environment.getExternalStorageDirectory()
						.toString();
				final File mFolder = new File(extr + "/MyApp");

				if (!mFolder.exists()) {
					mFolder.mkdir();
				}

				final String strF = mFolder.getAbsolutePath();
				final File mSubFolder = new File(strF + "/MyApp-SubFolder");

				if (!mSubFolder.exists()) {
					mSubFolder.mkdir();
				}

				final String s = "myfile.png";
				final File f = new File(mSubFolder.getAbsolutePath(), s);
				final String strMyImagePath = f.getAbsolutePath();
				FileOutputStream fos = null;
				try {
					fos = new FileOutputStream(f);
					correctBmp.compress(Bitmap.CompressFormat.PNG, 70, fos);

					fos.flush();
					fos.close();
					// MediaStore.Images.Media.insertImage(getContentResolver(),
					// b, "Screen", "screen");
				} catch (final FileNotFoundException e) {

					e.printStackTrace();
				} catch (final Exception e) {

					e.printStackTrace();
				}
				Log.d(TAG, "Saved success");
			}
		};

		this.btn_capture.setOnClickListener(new OnClickListener() {


			public void onClick(final View arg0) {
				if (CameraView.this.camera1 != null) {
					CameraView.this.camera1.takePicture(myShutterCallback,
							myPictureCallback_RAW, myPictureCallback_JPG);

				}

			}

		});
	}

	public void surfaceChanged(final SurfaceHolder arg0, final int arg1, final int arg2, final int arg3) {
		if (this.previewing) {
			this.camera1.stopPreview();
			this.previewing = false;
		}

		if (this.camera1 != null) {
			try {
				this.camera1.setPreviewDisplay(this.surfaceHolder);
				this.camera1.startPreview();
				this.previewing = true;
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void surfaceCreated(final SurfaceHolder arg0) {
	}

	public void surfaceDestroyed(final SurfaceHolder arg0) {
		if (this.camera1 != null) {
			this.camera1.stopPreview();
			this.camera1.release();
			this.camera1 = null;
			this.previewing = false;
		}
	}

	public void switchCameras(final Activity a) {
		if (this.previewing) {
			this.camera1.stopPreview();
		}
		// NB: if you don't release the current camera before switching, you app
		// will crash
		this.camera1.release();

		// swap the id of the camera to be used
		if (this.currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
			this.currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
		} else {
			this.currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
		}
		this.camera1 = Camera.open(this.currentCameraId);
		// Code snippet for this method from somewhere on android developers, i
		// forget where
		this.setCameraDisplayOrientation(a, this.currentCameraId, this.camera1);
		try {
			// this step is critical or preview on new camera will no know where
			// to render to
			this.camera1.setPreviewDisplay(this.surfaceHolder);
		} catch (final IOException e) {
			e.printStackTrace();
		}
		this.camera1.startPreview();
	}
}
