package com.awaymeet;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Point;
import org.bytedeco.javacpp.opencv_core.Rect;
import org.bytedeco.javacv.Frame;
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
public class CarDetect {
	public static ComputationGraph preTrained;
	public static Yolo2OutputLayer outputLayer;
	public HashMap<Integer, String> map;
	public static ToIplImage toIplImage = new OpenCVFrameConverter.ToIplImage();
	public static ToMat toMat = new OpenCVFrameConverter.ToMat();
	public static double detectionThreshold = 0.9;

	public CarDetect() {
		System.out.println("Predict()初始化");
		File cachedFile = new File("src/main/resources/model.zip");
		try {
			preTrained = ModelSerializer.restoreComputationGraph(cachedFile);
			prepareLabels();
			outputLayer = (Yolo2OutputLayer) preTrained.getOutputLayer(0);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	public List<Rect> detect(Frame frame, CarDetect jpredict) {
		List<Rect> list=new ArrayList<Rect>();
		int iwidth = 416;
		int owidth = 13;
		double detectionThreshold = 0.9;
		if (null != frame) {
			Mat mat = toMat.convert(frame);
			if (null != mat) {
				int w = frame.imageWidth;
				int h = frame.imageHeight;
				INDArray indArray = null;
				try {
					indArray = jpredict.prepareImage(mat, iwidth, iwidth);
				} catch (IOException e) {
					System.out.println("indArray异常");
					e.printStackTrace();
				}
				INDArray results = preTrained.outputSingle(indArray);
				List<DetectedObject> predictedObjects = outputLayer.getPredictedObjects(results, detectionThreshold);
				for (int j = 0; j < predictedObjects.size(); j++) {
					double lx = (predictedObjects.get(j).getCenterX() - 0.5 * predictedObjects.get(j).getWidth()) * w
							/ owidth;
					double ly = (predictedObjects.get(j).getCenterY() - 0.5 * predictedObjects.get(j).getHeight()) * h
							/ owidth;
					double bx = (predictedObjects.get(j).getCenterX() + 0.5 * predictedObjects.get(j).getWidth()) * w
							/ owidth;
					double by = (predictedObjects.get(j).getCenterY() + 0.5 * predictedObjects.get(j).getHeight()) * h
							/ owidth;
					System.out.println(jpredict.map.get(predictedObjects.get(j).getPredictedClass()));
					if("plate".equals(jpredict.map.get(predictedObjects.get(j).getPredictedClass()))){
						list.add(new Rect(new Point((int)lx, (int)ly),new Point((int)bx,(int) by))); 
					}
				}
			}
		}
		return list;
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