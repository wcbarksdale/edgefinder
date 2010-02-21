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

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.view.View;

public class EdgeView extends View implements PreviewCallback {
	
	static {
		System.loadLibrary("edgefinder");
	}
	
	private byte[] cameraPreview = null;
	private boolean cameraPreviewValid = false;
	private final Lock cameraPreviewLock = new ReentrantLock();
	private final Paint edgePaint = new Paint();
	private int width, height;

	private boolean first = true;
	
	public EdgeView(Context context) {
		super(context);
		edgePaint.setColor(Color.WHITE);
	}

	/**
	 * Native method for calculating the edge image.
	 * 
	 * @param source - source image
	 * @param width - width of the source image
	 * @param height - height of the source image
	 * @param canvas - canvas to draw on
	 * @param paint - paint used to draw edges
	 */
	private native void findEdges(byte[] source, int width, int height, Canvas canvas, Paint paint);

	@Override
	protected void onDraw(Canvas canvas) {
		if(first) {
			canvas.drawColor(Color.BLACK);
			first = false;
		} else if (cameraPreviewLock.tryLock()) {
			try {
				if (cameraPreview != null && cameraPreviewValid) {
					canvas.drawColor(Color.BLACK);
					findEdges(cameraPreview, width, height, canvas, edgePaint);
					cameraPreviewValid = false;
				}
			} finally {
				cameraPreviewLock.unlock();
			}
		} else {
			canvas.drawColor(Color.BLACK);
		}
	}
	
	public void onPreviewFrame(byte[] data, Camera camera) {
		if (cameraPreviewLock.tryLock()) {
			try {
				if(!cameraPreviewValid) {
					cameraPreviewValid = true;

					Size s = camera.getParameters().getPreviewSize();
					
					width = s.width;
					height = s.height;
					int length = width * height;
					
					if(cameraPreview == null || cameraPreview.length != length) {
						cameraPreview = new byte[length];
					}
					
					System.arraycopy(data, 0, cameraPreview, 0, length);
					
					postInvalidate();
				}
			} finally {
				cameraPreviewLock.unlock();
			}
		}
	}
}
