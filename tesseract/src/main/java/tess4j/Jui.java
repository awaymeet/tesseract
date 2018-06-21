package tess4j;

import java.awt.Container;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;

import com.awaymeet.BussinessCore;
import com.awaymeet.JUtils;

/**
 * GUI页面
 *
 */
public class Jui implements ActionListener {
	public static JFrame frame = new JFrame("我愿是一阵三月的春风，吹拂过每一个可爱的脸庞...");// 框架布局
	public static JTabbedPane tabPane = new JTabbedPane();// 选项卡布局
	public static Container con = new Container();//
	public static JLabel label1 = new JLabel("文件目录");
	public static JLabel label2 = new JLabel("选择文件");
	public static JTextField text1 = new JTextField();// TextField 目录的路径
	public static JTextField text2 = new JTextField();// 文件的路径
	public static JTextArea textResultShow = new JTextArea();// 文件的路径
	public static JButton button1 = new JButton("...");// 选择
	public static JButton button2 = new JButton("...");// 选择
	public static JFileChooser jfc = new JFileChooser();// 文件选择器
	public static JButton button3 = new JButton("开始");//
	public static double lx = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
	public static double ly = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
	public static String filePath = "";

	Jui() {
		jfc.setCurrentDirectory(new File("d://"));// 文件选择器的初始目录定为d盘
		frame.setLocation(new Point(0, 0));// 设定窗口出现位置
		frame.setSize((int) lx, (int) ly - 50);// 设定窗口大小
		frame.setContentPane(tabPane);// 设置布局
		label1.setBounds(10, 10, 70, 20);
		text1.setBounds(75, 10, 120, 20);
		button1.setBounds(210, 10, 50, 20);
		label2.setBounds(10, 35, 70, 20);
		text2.setBounds(75, 35, 120, 20);
		textResultShow.setBounds((int) lx - 250, 0, 250, (int) (ly - 50));
		textResultShow.setLineWrap(true);
		textResultShow.setWrapStyleWord(true);
		textResultShow.setText("结果是:");
		button2.setBounds(210, 35, 50, 20);
		button3.setBounds(30, 60, 120, 20);
		button1.addActionListener(this); // 添加事件处理
		button2.addActionListener(this); // 添加事件处理
		button3.addActionListener(this); // 添加事件处理
		con.add(label1);
		con.add(text1);
		con.add(button1);
		con.add(label2);
		con.add(text2);
		con.add(textResultShow);
		con.add(button2);
		con.add(button3);
		frame.setVisible(true);// 窗口可见
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);// 使能关闭窗口，结束程序
		tabPane.add("通用图像矫正及文字识别", con);// 添加布局1
	}

	/**
	 * 时间监听的方法
	 */
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if (e.getSource().equals(button1)) {// 判断触发方法的按钮是哪个
			jfc.setFileSelectionMode(1);// 设定只能选择到文件夹
			int state = jfc.showOpenDialog(null);// 此句是打开文件选择器界面的触发语句
			if (state == 1) {
				return;
			} else {
				File f = jfc.getSelectedFile();// f为选择到的目录
				text1.setText(f.getAbsolutePath());
			}
		}
		// 绑定到选择文件，先择文件事件
		if (e.getSource().equals(button2)) {
			jfc.setFileSelectionMode(0);// 设定只能选择到文件
			int state = jfc.showOpenDialog(null);// 此句是打开文件选择器界面的触发语句
			if (state == 1) {
				return;// 撤销则返回
			} else {
				File f = jfc.getSelectedFile();// f为选择到的文件
				text2.setText(f.getAbsolutePath());
				filePath = f.getAbsolutePath();
				System.out.println(f.getAbsolutePath());
			}
		}
		if (e.getSource().equals(button3)) {
			// 弹出对话框可以改变里面的参数具体得靠大家自己去看，时间很短
			// JOptionPane.showMessageDialog(null, "弹出对话框的实例，欢迎您-漆艾琳！", "提示",
			// 2);
			if ("".equals(filePath) || null == filePath)
				JOptionPane.showMessageDialog(null, "请选择文件", "提示", 2);
			try {

				File f = new File(filePath);
				FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(f);
				grabber.start();
				int lengthInFrames = grabber.getLengthInFrames();
				@SuppressWarnings("resource")
				Mat rmat = new Mat();
				for (int fi = 0; fi < lengthInFrames; fi++) {
					Frame frame = grabber.grab();
					rmat = JUtils.toMat.convert(frame);
					if (rmat != null) {
						BussinessCore.yesOrNoDebug = true;
						BussinessCore.showScale=3;
						BussinessCore.canvasLocationX=400;
						BussinessCore.canvasLocationY=50;
						Mat imageCorrention = BussinessCore.imageCorrention(frame);
						String tempPath = System.getProperty("user.dir") + File.separator + "temp.jpg";
						System.out.println(tempPath);
						BussinessCore.saveImage(imageCorrention, tempPath);
						String ocrRes = Ocr.ocr(tempPath);
						String ocrPre = Ocr.ocr(filePath);
						textResultShow.append("矫正后："+ocrRes+"\n");
						textResultShow.append("矫正前："+ocrPre+"\n");
					}
				}
				grabber.close();
			} catch (Exception e2) {
				textResultShow.setText(e2.getMessage());
			}
		}
	}
	public static void main(String[] args) {
		new Jui();
	}
}
