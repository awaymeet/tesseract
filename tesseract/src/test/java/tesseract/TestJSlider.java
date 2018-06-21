package tesseract;

import java.awt.BorderLayout;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
public class TestJSlider {
	JFrame mainWin = new JFrame("滑动条示范");
	Box sliderBox = new Box(BoxLayout.Y_AXIS);
	JTextField showVal = new JTextField();
	JTextField showVal2 = new JTextField();
	ChangeListener listener;
	ChangeListener listener2;
	public static int L=0;
	public static int H=200;

	public void init() {
		// 定义一个监听器，用于监听所有滑动条
		listener = new ChangeListener() {
			public void stateChanged(ChangeEvent event) {
				// 取出滑动条的值，并在文本中显示出来
				JSlider source = (JSlider) event.getSource();
				L=source.getValue();
				showVal.setText("低值是：" + source.getValue());
				JT jt = new JT(L,H);
				jt.start();
				jt = null;
			}
		};
		listener2 = new ChangeListener() {
			public void stateChanged(ChangeEvent event) {
				// 取出滑动条的值，并在文本中显示出来
				JSlider source = (JSlider) event.getSource();
				H=source.getValue();
				showVal2.setText("高值是：" + source.getValue());
				JT jt = new JT(L,H);
				jt.start();
				jt = null;
			}
		};
		JSlider slider = new JSlider();
		// 设置绘制刻度
		slider.setPaintTicks(true);
		// 设置主、次刻度的间距
		slider.setMajorTickSpacing(20);
		slider.setMinorTickSpacing(1);
		slider.setMaximum(1000);
		// 设置绘制刻度标签，默认绘制数值刻度标签
		slider.setPaintLabels(true);
		addSlider(slider, "数值刻度标签",listener);
		mainWin.add(sliderBox, BorderLayout.CENTER);
		mainWin.add(showVal, BorderLayout.NORTH);
		mainWin.pack();
		mainWin.setVisible(true);

		JSlider slider2 = new JSlider();
		// 设置绘制刻度
		slider2.setPaintTicks(true);
		// 设置主、次刻度的间距
		slider2.setMajorTickSpacing(20);
		slider2.setMinorTickSpacing(1);
		slider2.setMaximum(1000);
		// 设置绘制刻度标签，默认绘制数值刻度标签
		slider2.setPaintLabels(true);
		addSlider(slider2, "数值刻度标签",listener2);
		mainWin.add(sliderBox, BorderLayout.CENTER);
		mainWin.add(showVal2, BorderLayout.SOUTH);
		mainWin.pack();
		mainWin.setVisible(true);

	}

	// 定义一个方法，用于将滑动条添加到容器中
	public void addSlider(JSlider slider, String description,ChangeListener jlistener) {
		slider.addChangeListener(jlistener);
		Box box = new Box(BoxLayout.X_AXIS);
		box.add(new JLabel(description + "："));
		box.add(slider);
		sliderBox.add(box);
	}

	public static void main(String[] args) {
		new TestJSlider().init();
	}
}

class JT extends Thread {
	public int L;
	public int H;

	public JT(int l, int h) {
		super();
		L = l;
		H = h;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		//BussinessCore.t(L, H);
	}

}