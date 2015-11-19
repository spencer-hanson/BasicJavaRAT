package ratclient;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.swing.*;

@SuppressWarnings("serial")
public class ImageWindow extends JFrame { //Simple image window to show screenshot
	BufferedImage image;
	AffineTransform aff;
	double scalex = 1;
	double scaley = 1;
	Dimension dim;
	
	public void initGui(BufferedImage image) {
		dim = new Dimension((int)(image.getWidth()/scalex), (int)(image.getHeight()/scaley));
		aff = new AffineTransform();
		aff.scale(scalex, scaley);
		setSize(dim); //fix window to image
		setLocationRelativeTo(null); //centered
		//setResizable(false);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); //don't close entire program
		setVisible(true);
	}
	
	public ImageWindow(BufferedImage image) {
		super("Image Viewer");
		this.image = image;
		initGui(image);
	}
	
	@Override
	public void paint(Graphics g) { //render window, only the image though
		Graphics2D g2d = (Graphics2D)g;
		g2d.setTransform(aff);
		g2d.drawImage(image, 0, 0, getWidth(), getHeight(), this);
	}
}
