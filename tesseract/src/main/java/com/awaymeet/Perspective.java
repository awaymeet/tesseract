package com.awaymeet;

import org.bytedeco.javacpp.opencv_core.CvPoint2D32f;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Point;
import org.bytedeco.javacpp.helper.opencv_core.CvArr;
import org.bytedeco.javacv.OpenCVFrameConverter;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;


/**
 * 
 *Projection transformation
 */
public class Perspective {
	public static OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
	public static OpenCVFrameConverter.ToMat toMat = new OpenCVFrameConverter.ToMat();

	@SuppressWarnings("deprecation")
	public static Mat p(Point p0, Point p1, Point p2, Point p3, int width, int height, Mat mat) {
		CvMat map_matrix = cvCreateMat(3, 3, CV_32FC1);
		CvPoint2D32f c1 = new CvPoint2D32f(4);
		CvPoint2D32f c2 = new CvPoint2D32f(4);
		c1.position(0).put((double) p0.x(), (double) p0.y());
		c1.position(1).put((double) p1.x(), (double) p1.y());
		c1.position(2).put((double) p2.x(), (double) p2.y());
		c1.position(3).put((double) p3.x(), (double) p3.y());

		c2.position(0).put((double) 0, (double) 0);
		c2.position(1).put((double) width, (double) 0);
		c2.position(2).put((double) width, (double) height);
		c2.position(3).put((double) 0, (double) height);
		cvGetPerspectiveTransform(c1.position(0), c2.position(0), map_matrix);
		c1.close();
		c2.close();
		CvArr src = new CvMat(mat);
		Mat hmat = new Mat(height, width, CV_8UC3, new Scalar(255, 255, 255, 255));
		CvArr hm1 = new CvMat(hmat);
		cvWarpPerspective(src, hm1, map_matrix);
		return converter.convertToMat(converter.convert(new Mat(hm1)));
	}
}
