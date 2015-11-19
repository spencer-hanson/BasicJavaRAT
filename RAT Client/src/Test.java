import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.util.Date;
import java.util.Scanner;

import javax.swing.*;
public class Test {
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		String str = sc.next();
		str = str.replaceAll("\\\\s", " ");
		str = str.replaceAll("\\\\n", "\n");
		System.out.println(str);
	}
}
/*
public class Test extends JFrame {
	BufferedImage image;
	AffineTransform aff;
	double scalex = 1;
	double scaley = 1;
	Dimension dim;
	
	public void initGui(BufferedImage image) {
		dim = new Dimension((int)(image.getWidth()/scalex), (int)(image.getHeight()/scaley));
		aff = new AffineTransform();
		aff.scale(scalex, scaley);
		
		setSize(640, 512);
		setLocationRelativeTo(null);
		//setResizable(false);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setVisible(true);
	}
	
	public Test() {
		Toolkit tk = Toolkit.getDefaultToolkit();
		BufferedImage buf = new BufferedImage(tk.getScreenSize().width, tk.getScreenSize().height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = buf.createGraphics();
		g2d.setColor(Color.red);
		g2d.drawRect(277, 898, 1234-277, 941-898);
		g2d.drawRect(9, 49, 23-9, 70-49);
		//9,49<->23,70
		this.image = buf;
		initGui(image);
		t();
	}
	
	public Test(BufferedImage image) {
		super("Image Viewer");
		this.image = image;
		initGui(image);
	}
	
	public void t() {
		try {
		Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
		BufferedImage newScreen = new Robot().createScreenCapture(screenRect);
		BufferedImage oldScreen = newScreen;
		int[] newPixels;
		int[] oldPixels;
		PixelGrabber grabber;
        
        int w = newScreen.getWidth();
        int h = newScreen.getHeight();
        
		while(true) {
				 
				 repaint();
		        newPixels = new int[w * h];
		        oldPixels = new int[w * h];
		        long start = new Date().getTime();
		        grabber = new PixelGrabber(oldScreen, 0, 0, w, h, oldPixels, 0, w);
		        grabber.grabPixels();
		        grabber = new PixelGrabber(newScreen, 0, 0, w, h, newPixels, 0, w);
		        grabber.grabPixels();
				
		      boolean[][] cellChanged = new boolean[w >> 3][h >> 3];
				int index = 0;
				for (int x = 0; x < w; x++) {
					for (int y = 0; y < h; y++) {
						if (newPixels[index] != oldPixels[index]) {
							cellChanged[x >> 3][y >> 3] = true;
						}
						index++;
					}
				}
				int count = 0;
				for (int y = 0; y < (h >> 3); y++) {
					for (int x = 0; x < (w >> 3); x++) {
						count = count + (cellChanged[x][y]?1:0);
						//System.out.print(cellChanged[x][y] ? "1" : "0");
					}
					if(count > 16) {
						this.image = newScreen;
						//send this part because more than 1/8 of the screen changed
						//System.out.println(y + ". Meow " + count);
					}
					count = 0;
					//System.out.print("\n");
				}
		        
		       long end = new Date().getTime();
		        
		        oldScreen = newScreen;
				newScreen = new Robot().createScreenCapture(screenRect);
		        System.out.println((end-start));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		new Test();
	}
	
	@Override
	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		g2d.setTransform(aff);
		g2d.drawImage(image, 0, 0, dim.width, dim.height, this);
	}
	
	
	public static void update() {
		 try {
		Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
		BufferedImage bmp1 = new Robot().createScreenCapture(screenRect);
		BufferedImage bmp2 = new Robot().createScreenCapture(screenRect);
		int[] pixels1;
		int[] pixels2;
		boolean first_x = false;
        PixelGrabber pg1;
        PixelGrabber pg2;
        int x1 = -2, x2 = -2,y1,y2;
        int minx1=-1,maxx2=-1,miny1=-1,maxy2=-1;
        int w = bmp1.getWidth();
        int h = bmp1.getHeight();
        pixels1 = new int[w * h];
        pixels2 = new int[w * h];
        long start = new Date().getTime();
        pg1 = new PixelGrabber(bmp1, 0, 0, w, h, pixels1, 0, w);
        pg2 = new PixelGrabber(bmp2, 0, 0, w, h, pixels2, 0, w);
       
       
			pg1.grabPixels();
		
        pg2.grabPixels();
        System.out.println("//start//");
        
        int count = 0;
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                if (pixels2[count] != pixels1[count]) {
                    if(!first_x){ minx1=i; miny1=j;  maxx2=i; maxy2=j;  first_x = true; }
                    if(minx1>i) minx1 = i;
                    if(miny1>j) miny1 = j;
                    if(maxx2<i) maxx2 = i;
                    if(maxy2<j) maxy2 = j;
                    count++;
                   // System.out.println(i +"x"+ j);
                }
            }
        }
       
        if(minx1==maxx2 && maxy2==miny1) System.out.println("Un singur pixel modificat");
        else{
         System.out.println("Modified part (rectangle): "+minx1+","+miny1+"<->"+maxx2+","+maxy2);
        }
        long end = new Date().getTime();
        
        System.out.println("//finish//:"+(end-start)+"ms elapsed");
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	}
}


/*import java.io.*;
public class Test {

	
	public static void main(String[] args) {
		
		  try{
			  String str = "pause";
		  // 	Create file 
			  FileWriter fstream = new FileWriter("out.bat");
		  	BufferedWriter out = new BufferedWriter(fstream);
		  	out.write(str);
		  	//Close the output stream
		  	out.close();
		  }catch (Exception e){//Catch exception if any
			  System.err.println("Error: " + e.getMessage());
		  }
	}
}
*/