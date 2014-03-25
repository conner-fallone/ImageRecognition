import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.VideoCapture;


public class ObjectDetectionFrame {
	
	public static void main(String[] args){
		String videoPath = "C:/Users/Conner Fallone/Documents/GitHub/ImageRecognition/BallDetector/ball.avi";
		
		// Load library
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		// Swing Frame
		JFrame frame = new JFrame("Object Detection");
		
		// Frame Properties
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
		frame.setSize(400,400);
		
		// Swing Panel
		CVPanel panel = new CVPanel();
		frame.setContentPane(panel);
		frame.setVisible(true);
		
		// Initialize Video
		Mat videoImage = new Mat();
		// VideoCapture stream = new VideoCapture(videoPath);
		
		// Stream with webcam
		VideoCapture stream = new VideoCapture(0);
		
		if (stream.isOpened()){
			while (true){
				stream.read(videoImage);
				
				if( !videoImage.empty() )  
		         {  
		           frame.setSize(videoImage.width()+40,videoImage.height()+60);
		           panel.matToBufferedImage(videoImage);
		           panel.repaint();
		         }  
			}
		}
	}
}

//converts each frame to buffered images for analysis
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
