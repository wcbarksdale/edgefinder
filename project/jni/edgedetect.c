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
#include <string.h>
#include <jni.h>
#include <math.h>

#define THRESHOLD 30

void Java_bwr_edgefinder_EdgeView_findEdges(JNIEnv* env, jobject thiz, jbyteArray source, 
	jint width, jint height, jobject canvas, jobject paint)
{
	int px, cx, nx, ly, val, y, x, y_width, num_points = 0;

	jclass Canvas = (*env)->GetObjectClass(env, canvas);
	jbyte *sb = (*env)->GetByteArrayElements(env, source, NULL);
	jmethodID drawPoint = (*env)->GetMethodID(env, Canvas, "drawPoint", "(FFLandroid/graphics/Paint;)V");

	for (y = 1; y < height-1; y++) {

		y_width = y*width;

		px = sb[y_width]/2; // init previous x
		cx = sb[y_width+1]/2; // init current x

		for (x = 1; x < width-1; x++) {
			nx = sb[y_width+x+1]/2; // next x

			ly = (sb[(y-1)*width+x]/2) - (sb[(y+1)*width+x]/2);
			val = abs(px - nx) + abs(ly);

			if(val > THRESHOLD) {
				(*env)->CallVoidMethod(env, canvas, drawPoint, (jfloat)x, (jfloat)y, paint);
			}

			// previous x becomes current x and current x becomes next x
			px = cx;
			cx = nx;
		}
	}

	(*env)->ReleaseByteArrayElements(env, source, sb, JNI_COMMIT);
}
