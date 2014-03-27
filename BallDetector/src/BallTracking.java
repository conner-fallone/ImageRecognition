import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Size;

/************************************************************
 * Conner Fallone & Matt Shrider
 * CS-365
 * Winter 2014
 * OpenCV circle detection and tracking in a video
 ************************************************************/
public class BallTracking {
	
	public static void main(String[] args){
		
		// Variables
		String videoPath = "C:/Users/Conner Fallone/Documents/GitHub/ImageRecognition/BallDetector/ball.avi";
		Scalar redColor = new Scalar(255, 0, 0);
		Size size = new Size(9,9);
		
		// Load library
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		// Swing Frame
		JFrame frame = new JFrame("Object Detection");
		
		
		// Swing Panel
		CVPanel panel = new CVPanel();
		
		// Frame Properties
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
		frame.setSize(400,400);
		frame.setContentPane(panel);
		frame.setVisible(true);
		
		// Images to use
		Mat videoImage = new Mat();
		Mat grey = new Mat();
		Mat circles = new Mat();
		
		// Stream from .avi
		// VideoCapture stream = new VideoCapture(videoPath);
		
		// Stream with webcam
		VideoCapture stream = new VideoCapture(0);
		
		if (stream.isOpened()){
			while (true){
				stream.read(videoImage);		
				if(!videoImage.empty()){
		           frame.setSize(grey.width()+40,grey.height()+60);
		           
		           // Convert to grayscale
		           Imgproc.cvtColor(videoImage, grey, Imgproc.COLOR_RGB2GRAY);
		           
		           // Apply a Gaussian blur to reduce noise and avoid false circle detection:
		           Imgproc.GaussianBlur(grey, grey, size, 2, 2 );
		           
		           // Apply Hough Circle Transform:
		           Imgproc.HoughCircles(grey, circles, Imgproc.CV_HOUGH_GRADIENT, 1, grey.rows()/8, 200, 100, 0, 0 );
		           
		           // Draw Circles
		           for (int i = 0; i < Math.min(circles.cols(), 10); i++){
		        	   double vCircle[] = circles.get(0,  i);
		        	   
		        	   if (vCircle == null)
		        		   break;
		        	   
		        	   Point center = new Point(Math.round(vCircle[0]), Math.round(vCircle[1]));
		        	   int radius = (int)Math.round(vCircle[2]);
		        	   
		        	   // Draw the found circle
		        	   Core.circle(grey, center, radius, redColor, 2);
		           }
		           
		           // Paint the image to the panel
		           panel.matToBufferedImage(grey);
		           //panel.matToBufferedImage(videoImage);
		           panel.repaint();
		         }  
			}
		}
	}
}

// Converts each frame to buffered images for analysis
// Source: http://cell0907.blogspot.com/2013/06/creating-windows-and-capturing-webcam.html
class CVPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private BufferedImage image;

	public BufferedImage getImage() {
		return image;
	}

	public void setImage(BufferedImage image) {
		this.image = image;
	}

	/**
	 * Converts/writes a Mat into a BufferedImage.
	 * 
	 * @param matrix
	 *            Mat of type CV_8UC3 or CV_8UC1
	 * @return BufferedImage of type TYPE_3BYTE_BGR or TYPE_BYTE_GRAY
	 */
	public boolean matToBufferedImage(Mat matBGR) {
		int width = matBGR.width(), height = matBGR.height(), channels = matBGR
				.channels();
		byte[] sourcePixels = new byte[width * height * channels];
		matBGR.get(0, 0, sourcePixels);
		// create new image and get reference to backing data
		image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		final byte[] targetPixels = ((DataBufferByte) image.getRaster()
				.getDataBuffer()).getData();
		System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.length);
		return true;
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (this.image == null)
			return;
		g.drawImage(this.image, 10, 10, this.image.getWidth(),
				this.image.getHeight(), null);
	}
}
