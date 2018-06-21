package com.awaymeet;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class ImagePanel extends JPanel  {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1783093098384956552L;
	private BufferedImage image;
	
    public ImagePanel(BufferedImage image) {
		super();
		this.image = image;
	}

	@Override  
    public void paintComponent(Graphics g) {  
        g.drawImage(image, 0, 0, null);   
    }
}
