package com.awaymeet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Point;
import org.bytedeco.javacpp.opencv_core.Scalar;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber.Exception;
import net.sourceforge.tess4j.TesseractException;
import static org.bytedeco.javacpp.opencv_core.CV_PI;
import tess4j.Ocr;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.*;

/**
 * @author Viayie
 * @company awaymeet 
 * Image correction core class
 */
public class BussinessCore {

	private static int thresholdDegree = 10;// Predicted tilt angle threshold
	private static double dmax = Math.tan(Math.toRadians(0 + thresholdDegree))
			* Math.tan(Math.toRadians(180 - thresholdDegree));// tan max
	private static double dmin = Math.tan(Math.toRadians(90 - thresholdDegree))
			* Math.tan(Math.toRadians(90 + thresholdDegree));// tan min
	
	public static Boolean yesOrNoDebug = Boolean.FALSE;
	private static int HoughLinesPReduceValue = 5;
	private static int HoughLinesPReduceMaxValueScale = 2;
	private static int HoughLinesPReduceMinValueScale = 15;
	private static int threshold = 15;
	private static double maxLineGap = 5;
	public static int canvasLocationX=0;
	public static int canvasLocationY=0;
	public static int showScale = 3;// Debug display scaling

	// Get the result of the correction
	public static Mat imageCorrention(Frame frame) {
		Mat rmat = JUtils.toMat.convert(frame);
		if (rmat != null) {
			if (yesOrNoDebug) {
				show(rmat, "原图");
			}
			Mat edges = new Mat();
			Canny(rmat, edges, 80, 130);
			int w = rmat.cols();
			int h = rmat.rows();
			int minWOrH = w < h ? w : h;
			for (int i = w / HoughLinesPReduceMaxValueScale; i > minWOrH
					/ HoughLinesPReduceMinValueScale; i -= HoughLinesPReduceValue) {
				Mat templines = new Mat();
				HoughLinesP(edges, templines, 1, CV_PI / 180, threshold, i, maxLineGap);
				int r = templines.cols() * templines.rows();
				if (r > 1) {
					BooleanAndBytePoint vertify = vertify(templines, w, h);
					if (vertify.getB()) {
						Point startP = new Point(vertify.getBytePoint1().position(0));
						Point endP = new Point(vertify.getBytePoint1().position(1));
						Float k1 = JUtils.getK(startP, endP);

						Point startP2 = new Point(vertify.getBytePoint2().position(0));
						Point endP2 = new Point(vertify.getBytePoint2().position(1));
						Float k2 = JUtils.getK(startP2, endP2);

						if (yesOrNoDebug) {
							line(rmat, startP, endP, Scalar.RED, 2, 8, 0);
							line(rmat, startP2, endP2, Scalar.GREEN, 2, 8, 0);
						}
						if (null == k1 || k1.floatValue() == 0) {
							if (yesOrNoDebug) {
								show(rmat, "不需要矫正rmat");
							}
							break;
						}
						if (null == k2 || k2.floatValue() == 0) {
							if (yesOrNoDebug) {
								show(rmat, "不需要矫正rmat");
							}
							break;
						}

						Float kBig, kSmall;
						if (k1.floatValue() > k2.floatValue()) {
							kBig = k1;
							kSmall = k2;
						} else {
							kBig = k2;
							kSmall = k1;
						}

						Point po = new Point(0, 0);
						Point pp = new Point(w, 0);
						Point pq = new Point(w, h);
						Point pr = new Point(0, h);
						if (yesOrNoDebug) {
							circle(rmat, po, 15, Scalar.RED);
							circle(rmat, pp, 15, Scalar.GREEN);
							circle(rmat, pq, 15, Scalar.BLUE);
							circle(rmat, pr, 15, Scalar.YELLOW);
						}
						Float t1 = pr.y() - kBig * pr.x();
						Float t2 = pp.y() - kBig * pp.x();
						Float t3 = pq.y() - kSmall * pq.x();
						Float t4 = po.y() - kSmall * po.x();
						po.close();
						pp.close();
						pq.close();
						pr.close();

						Float[] xypa = JUtils.getXY(kBig, t1, kSmall, t4);
						Point pa = new Point(xypa[0].intValue(), xypa[1].intValue());
						Float[] xypb = JUtils.getXY(kBig, t2, kSmall, t4);
						Point pb = new Point(xypb[0].intValue(), xypb[1].intValue());
						Float[] xypc = JUtils.getXY(kBig, t2, kSmall, t3);
						Point pc = new Point(xypc[0].intValue(), xypc[1].intValue());
						Float[] xypd = JUtils.getXY(kBig, t1, kSmall, t3);
						Point pd = new Point(xypd[0].intValue(), xypd[1].intValue());
						if (kBig.floatValue() > 1) {
							rmat = Perspective.p(pa, pb, pc, pd, w, h, rmat);
						} else {
							rmat = Perspective.p(pb, pc, pd, pa, w, h, rmat);
						}
						if (yesOrNoDebug) {
							show(rmat,"矫正结果图"+JUtils.getK(pa, pb).floatValue() + "," + pa.x() + "," + pa.y() + "——" + pb.x()
									+ "," + pb.y() + "rmat");
						}
						break;
					}
				}
			}
		}
		return rmat;
	}

	@SuppressWarnings("resource")
	public static void main(String[] args) throws InterruptedException, TesseractException, Exception {
		String filePath =System.getProperty("user.dir") +File.separator+"src"+File.separator+"main"+File.separator+"resources"+File.separator+"img"+File.separator+"1 (1).jpg";
		File f = new File(filePath);
		FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(f);
		grabber.start();
		int lengthInFrames = grabber.getLengthInFrames();
		Mat rmat = new Mat();
		for (int fi = 0; fi < lengthInFrames; fi++) {
			Frame frame = grabber.grab();
			rmat = JUtils.toMat.convert(frame);
			if (rmat != null) {
				show(rmat, "rmat");
				yesOrNoDebug = true;
				Mat imageCorrention = imageCorrention(frame);
				String tempPath = System.getProperty("user.dir") +File.separator + "temp.jpg";
				saveImage(imageCorrention, tempPath);
				System.out.println("矫正后的识别结果："+Ocr.ocr(tempPath));
				System.out.println("——————————————————————————————————————————————————————————————————");
				System.out.println("矫正前的识别结果："+Ocr.ocr(filePath));
			}
		}
		grabber.close();

	}

	// Enlargement and reduction image
	public static Mat jResize(Mat rmat, int scalar) {
		if (null == rmat) {
			return rmat;
		}
		Size dsize = new Size(rmat.cols() * scalar, rmat.rows() * scalar);
		resize(rmat, rmat, dsize);
		return rmat;
	}

	// Image enhancement algorithm
	public static Mat jEqualizeHist(Mat rmat) {
		if (null == rmat) {
			return rmat;
		}
		MatVector mv = new MatVector(3);
		split(rmat, mv);
		for (int m = 0; m < mv.size(); m++) {
			equalizeHist(mv.get(m), mv.get(m));
		}
		merge(mv, rmat);
		return rmat;
	}

	// Image save
	public static void saveImage(Mat rmat, String path) {
		if (null == rmat) {
			return;
		}
		imwrite(path, rmat);
	}

	public static BooleanAndBytePoint vertifyList(List<BytePointer> list) {
		for (int i = 0; i < list.size() - 1; i++) {
			for (int j = i + 1; j < list.size(); j++) {
				Boolean compare = compare(list.get(i), list.get(j));
				if (compare) {
					return new BooleanAndBytePoint(Boolean.TRUE, list.get(i), list.get(j));
				}
			}
		}
		return new BooleanAndBytePoint(Boolean.FALSE, null, null);
	}

	public static Boolean compare(BytePointer byte1, BytePointer byte2) {
		Point p1 = new Point(byte1.position(0));
		Point p2 = new Point(byte1.position(1));
		Float k1 = JUtils.getK(p1, p2);
		Point p3 = new Point(byte2.position(0));
		Point p4 = new Point(byte2.position(1));
		Float k2 = JUtils.getK(p3, p4);
		if (null == k1 || null == k2) {
			if (k1 == null && k2 != null && k2.floatValue() == 0) {
				if (yesOrNoDebug) {
					System.out.println("compare垂直k线与平行k线");
				}
				return Boolean.TRUE;
			} else if (k1 != null && k1.floatValue() == 0 && k2 == null) {
				if (yesOrNoDebug) {
					System.out.println("compare垂直k线与平行k线");
				}
				return Boolean.TRUE;
			} else {
				if (yesOrNoDebug) {
					System.out.println("compare垂直k线");
				}
				return Boolean.FALSE;
			}
		} else {
			if (k1.floatValue() != k2.floatValue() && k1 * k2 < dmax && k1 * k2 > dmin) {
				return Boolean.TRUE;
			} else {
				if (yesOrNoDebug) {
					System.out.println("compare欲言又止的心疼");
				}
				return Boolean.FALSE;
			}
		}
	}

	public static BooleanAndBytePoint vertify(Mat templines, int w, int h) {
		List<BytePointer> list = new ArrayList<BytePointer>();
		for (int k = 0; k < templines.cols(); k++) {
			for (int L = 1; L < templines.rows(); L++) {
				list.add(templines.ptr(k, L));
			}
		}
		return vertifyList(list);
	}

	public static CanvasFrame canvas;

	public static void show(Mat mat, String title) {
		canvas = new CanvasFrame("");
		canvas.setTitle(title);
		canvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		canvas.setAlwaysOnTop(true);
		canvas.setLocation(canvasLocationX,canvasLocationY);
		canvas.setCanvasSize(mat.cols() / showScale, mat.rows() / showScale);
		canvas.showImage(JUtils.converter.convert(mat));
	}
}

class BooleanAndBytePoint {
	private Boolean b;
	private BytePointer bytePoint1;
	private BytePointer bytePoint2;

	public Boolean getB() {
		return b;
	}

	public void setB(Boolean b) {
		this.b = b;
	}

	public BytePointer getBytePoint1() {
		return bytePoint1;
	}

	public void setBytePoint1(BytePointer bytePoint1) {
		this.bytePoint1 = bytePoint1;
	}

	public BytePointer getBytePoint2() {
		return bytePoint2;
	}

	public void setBytePoint2(BytePointer bytePoint2) {
		this.bytePoint2 = bytePoint2;
	}

	public BooleanAndBytePoint(Boolean b, BytePointer bytePoint1, BytePointer bytePoint2) {
		super();
		this.b = b;
		this.bytePoint1 = bytePoint1;
		this.bytePoint2 = bytePoint2;
	}

}