package com.awaymeet;
import javax.swing.JFrame;

import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Point;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.OpenCVFrameConverter;
public class JUtils {
	public static OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
	public static OpenCVFrameConverter.ToMat toMat = new OpenCVFrameConverter.ToMat();
	public static int wX=300;
	public static int wH=200;
	public static int locationX;
	public static int locationY;
	public static void show(Mat mat, String name) {
		CanvasFrame canvas = new CanvasFrame(name, 1);
		canvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		canvas.setAlwaysOnTop(true);
		canvas.setCanvasSize(wX, wH);
		canvas.setLocation(locationX, locationY);
		canvas.showImage(converter.convert(mat));
	}

	public static void show(IplImage mat, String name) {
		CanvasFrame canvas = new CanvasFrame(name, 1);
		canvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		canvas.setAlwaysOnTop(true);
		canvas.setCanvasSize(wX, wH);
		canvas.setLocation(locationX, locationY);
		canvas.showImage(converter.convert(mat));
	}

	public static double getDistance(Point pointO, Point pointA) {
		double distance;
		distance = Math.pow((pointO.x() - pointA.x()), 2) + Math.pow((pointO.y() - pointA.y()), 2);
		distance = Math.sqrt(distance);

		return distance;
	}
	public static Double[] gen(double a, double b, double c) {
		double der = Math.sqrt(Math.pow(b, 2) - 4 * a * c);
		if (der < 0) {
			return null;
		}
		Double x1 = ((-1) * b + der) / 2 * a;
		Double x2 = ((-1) * b - der) / 2 * a;
		Double r[] = { x1, x2 };
		return r;
	}
	public static Float getK(Point p1,Point p2){
		if(p1.x()-p2.x()==0) return null;
		int dy=p1.y()-p2.y();
		int dx=p1.x()-p2.x();
		float f=(float) ((dy*1.0)/(dx*1.0));
		return f;
	}
	public static Float getB(Point p1,Point p2){
		Float k = getK(p1, p2);
		if(k!=null){
				return p1.y()-k*p1.x();
		}else{
			return null;
		}
	}
	public static Float[] getXY(float k1,float b1,float k2,float b2){
		if(k1==k2){
			Float[] f={null,null};
			return f;
		}
		Float x=(b2-b1)/(k1-k2);
		Float y=(b1*k2-b2*k1)/(k2-k1);
		Float[] f={x,y};
		return f;
	}
	public static void main(String[] args) {
		for(float f:getXY(1, 1, -2, 3)){
			System.out.println(f);
		}
	}

}
