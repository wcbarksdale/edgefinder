/*
 * Copyright (C) 2010 William Robert Beene
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package bwr.edgefinder;

import java.io.IOException;
import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.FrameLayout;

public class EdgeFinder extends Activity implements SurfaceHolder.Callback {

	private Camera camera;
	private EdgeView edgeView;
	private SurfaceView cameraView;
	private FrameLayout frameLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Get the entire screen
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		edgeView = new EdgeView(this);

		// Create the surface for the camera to draw its preview on
		cameraView = new SurfaceView(this);
		cameraView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		cameraView.getHolder().addCallback(this);

		// Setup the layout where the cameraView is completely obscured by the edgeView
		frameLayout = new FrameLayout(this);
		frameLayout.addView(cameraView);
		frameLayout.addView(edgeView);
		setContentView(frameLayout);
		
		// Prevent camera preview from showing up first
		edgeView.postInvalidate();
	}

	@Override
	protected void onPause() {
		super.onPause();
		stopCameraPreview();
	}

	private void stopCameraPreview() {
		if (camera != null) {
			camera.setPreviewCallback(null);
			camera.stopPreview();
			camera.release();
			camera = null;
		}
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Camera.Parameters parameters = camera.getParameters();
		parameters.setPreviewSize(width, height); // TODO: check that width, height are a valid camera preview size
		camera.setParameters(parameters);
		camera.startPreview();
	}

	public void surfaceCreated(SurfaceHolder holder) {
		camera = Camera.open();
		try {
			camera.setPreviewDisplay(cameraView.getHolder());
		} catch (IOException e) {
			e.printStackTrace();
		}
		camera.setPreviewCallback(edgeView);
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		stopCameraPreview();
	}
}