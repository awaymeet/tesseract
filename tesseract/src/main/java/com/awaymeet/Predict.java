package com.awaymeet;

import static org.bytedeco.javacpp.opencv_core.FONT_HERSHEY_DUPLEX;
import static org.bytedeco.javacpp.opencv_imgproc.rectangle;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.swing.JFrame;

import org.bytedeco.javacpp.opencv_imgproc;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Point;
import org.bytedeco.javacpp.opencv_core.Scalar;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter.ToIplImage;
import org.bytedeco.javacv.OpenCVFrameConverter.ToMat;
import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.layers.objdetect.DetectedObject;
import org.deeplearning4j.nn.layers.objdetect.Yolo2OutputLayer;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;

public class Predict {
	public static ComputationGraph preTrained;
	public static Yolo2OutputLayer outputLayer;
	public HashMap<Integer, String> map;
	public static ToIplImage toIplImage = new OpenCVFrameConverter.ToIplImage();
	public static ToMat toMat = new OpenCVFrameConverter.ToMat();
	
	public Predict() {
		System.out.println("Predict()————————————————————————————————————————————————————————————初始化");
		File cachedFile = new File(System.getProperty("user.dir") +File.separator+"src"+File.separator+"main"+File.separator+"resources"+File.separator+"model.zip");
		try {
            preTrained =ModelSerializer.restoreComputationGraph(cachedFile);
            prepareLabels();
            outputLayer = (Yolo2OutputLayer) preTrained.getOutputLayer(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
	}
	public static void main(String[] args) throws Exception {
		Predict jpredict = new Predict();
		CanvasFrame canvas = new CanvasFrame("平凡世界" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), 1);
		canvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		canvas.setAlwaysOnTop(true);
		for(int i=1;i<=13;i++){
			String filePath =System.getProperty("user.dir") +File.separator+"src"+File.separator+"main"+File.separator+"resources"+File.separator+"car"+File.separator+"1 ("+i+").jpg";
			FFmpegFrameGrabber grabber=new FFmpegFrameGrabber(filePath);
			grabber.start();
			Frame frame = grabber.grab();
			if(null!=frame){
				canvas.showImage(jpredict.detect(frame, jpredict));
			}
			grabber.close();
		}
	}
	public Frame detect(Frame jframe,Predict jpredict){
        int iwidth = 416;
        int owidth = 13;
        double detectionThreshold = 0.9;
    	Frame frame = jframe;
    	if (null!=frame) {
    		int w = frame.imageWidth;
    		int h = frame.imageHeight;
    		IplImage iplImage=toIplImage.convert(frame);
    		System.out.println(iplImage);
    		Mat mat = toMat.convert(frame);
    		System.out.println(mat);
    	    if(null!=mat){
    	        INDArray indArray = null;
				try {
					indArray = jpredict.prepareImage(mat, iwidth, iwidth);
				} catch (IOException e) {
					System.out.println("indArray异常");
					e.printStackTrace();
				}
    	        INDArray results = preTrained.outputSingle(indArray);
    	        List<DetectedObject> predictedObjects = outputLayer.getPredictedObjects(results, detectionThreshold);
    	        for (int j = 0; j <predictedObjects.size(); j++) {
    	        	double lx=(predictedObjects.get(j).getCenterX()-0.5*predictedObjects.get(j).getWidth())*w/owidth;
    	        	double ly=(predictedObjects.get(j).getCenterY()-0.5*predictedObjects.get(j).getHeight())*h/owidth;
    	        	double bx=(predictedObjects.get(j).getCenterX()+0.5*predictedObjects.get(j).getWidth())*w/owidth;
    	        	double by=(predictedObjects.get(j).getCenterY()+0.5*predictedObjects.get(j).getHeight())*h/owidth;
					rectangle(mat, new Point((int) Math.round(lx),(int) Math.round(ly)), new Point((int) Math.round(bx),(int) Math.round(by)), Scalar.YELLOW);
					opencv_imgproc.putText(mat,jpredict.map.get(predictedObjects.get(j).getPredictedClass()),new Point((int) Math.round(bx),(int) Math.round(by)), FONT_HERSHEY_DUPLEX, 1, Scalar.YELLOW);
				}
    	    }
		}
    	return frame;
    }
    private INDArray prepareImage(Mat file, int width, int height) throws IOException {
        NativeImageLoader loader = new NativeImageLoader(height, width, 3);
        ImagePreProcessingScaler imagePreProcessingScaler = new ImagePreProcessingScaler(0, 1);
        INDArray indArray = loader.asMatrix(file);
        imagePreProcessingScaler.transform(indArray);
        return indArray;
    }

    private void prepareLabels() {
        if (map == null) {
        	String s = "plate\n" + "chetou\n" + "sanjiaojia\n";
            String[] split = s.split("\\n");
            int i = 0;
            map = new HashMap<Integer, String>();
            for (String s1 : split) {
                map.put(i++, s1);
            }
        }
    }
}