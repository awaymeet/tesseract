package com.awaymeet;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.JFrame;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public class Server {
	public static final int PORT = 8888;// 监听的端口号
	private static Predict jpredict;
	private static CanvasFrame canvas;
	private static OpenCVFrameConverter.ToIplImage converter;
	private static ThreadPoolTaskExecutor pool;
	private static int perlDealFps=10;

	public static void main(String[] args) {
		System.out.println("服务器启动中...");
		jpredict = new Predict();
		initPool();
		converter = new OpenCVFrameConverter.ToIplImage();
		canvas = new CanvasFrame("平凡世界" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), 1);
		canvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		canvas.setAlwaysOnTop(true);
		Server server = new Server();
		server.init();
		System.out.println("服务器启动完成...");
	}

	public static void initPool() {
		pool = new ThreadPoolTaskExecutor();
		pool.setCorePoolSize(4);
		pool.setMaxPoolSize(8);
		pool.setKeepAliveSeconds(120);
		pool.setQueueCapacity(32);
		pool.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		pool.initialize();
	}

	@SuppressWarnings("resource")
	public void init() {
		try {
			ServerSocket serverSocket = new ServerSocket(PORT);
			int j=0;
			while (true) {
			//while (true) {
					Socket socket = serverSocket.accept();
					if (null != socket && j%perlDealFps==1) {
						DataInputStream input = new DataInputStream(socket.getInputStream());
						if (input != null) {
							FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(input);
							grabber.start();
							Frame kframe = grabber.grab();
							if (null != kframe) {
								kframe = converter.convert(rotate(converter.convert(kframe), 270));
								pool.execute(new JT(kframe, jpredict, canvas, input));
							}
							input.close();
							// grabber.close();
						}
					}
				j++;
			}
		} catch (Exception e) {
			System.out.println("服务器异常: " + e.getMessage());
		}
	}

	public static IplImage rotate(IplImage src, int rotate) {
		IplImage img = IplImage.create(src.height(), src.width(), src.depth(), src.nChannels());
		opencv_core.cvTranspose(src, img);
		opencv_core.cvFlip(img, img, rotate);
		return img;
	}
}
class JT implements Runnable {
	private Frame kframe;
	@SuppressWarnings("unused")
	private Predict jpredict;
	private CanvasFrame canvas;
	private DataInputStream input;
	private int showScale = 3;
	public JT(Frame kframe, Predict jpredict, CanvasFrame canvas, DataInputStream input) {
		super();
		System.out.println("JT()————————————————————————————————————————————————————————————初始化");
		this.kframe = kframe;
		this.jpredict = jpredict;
		this.canvas = canvas;
		this.input = input;
	}
	public void run() {
		try {
			//Frame detect = jpredict.detect(kframe, jpredict);
			Mat corrention = BussinessCore.imageCorrention(kframe);
			frameShow(JUtils.converter.convert(corrention));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@SuppressWarnings("unused")
	private Lock lock = new ReentrantLock();
	private void frameShow(Frame showFrame) {
		// lock.lock();
		canvas.setSize(showFrame.imageWidth / showScale, showFrame.imageHeight / showScale);
		canvas.showImage(showFrame);
		try {
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			System.out.println("线程名" + Thread.currentThread().getName() + "释放了锁");
			// lock.unlock();
		}
	}
}