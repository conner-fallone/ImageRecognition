
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Size;

/**************************************************************
 * Conner Fallone & Matt Shrider
 * CS-365
 * Winter 2014
 * OpenCV ball/circle detection and tracking in a video.
 * This application uses the webcam to track any circle or
 * ball shapes in the frame. The circle(s) will be highlighted
 * in real time, with the center path of the ball being
 * tracked over time. The technique used for circle tracking
 * is Hough Circle Transform.
 **************************************************************/
public class BallTracking {

	public static void main(String[] args){

		// Variables
		final Scalar redColor = new Scalar(0, 0, 255);
		final Scalar greenColor = new Scalar(0, 255, 0);
		final Scalar blackColor = new Scalar(0, 0, 0);
		Size size = new Size(9,9);
		final ArrayList<Point> pointHolder = new ArrayList<Point>();

		// Lower threshold to detect circles more leniently
		int threshold = 50;

		// Load library
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		// Swing Menu
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("File");
		JMenuItem restart = new JMenuItem("Restart");
		JMenuItem exit = new JMenuItem("Exit");
		menu.add(restart);
		menu.add(exit);
		menuBar.add(menu);

		// Swing Frame
		JFrame frame = new JFrame("Object Detection");
		frame.setJMenuBar(menuBar);

		// Swing Panels
		JPanel contentPane = new JPanel(new GridLayout(1,2));
		CVPanel cameraPanel = new CVPanel();
		CVPanel drawPanel = new CVPanel();

		// Add individual panels to contentpane
		contentPane.add(cameraPanel);
		contentPane.add(drawPanel);

		// Frame Properties
		frame.setContentPane(contentPane);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(1366,600);
		frame.setVisible(true);

		// Images to use
		Mat videoImage = new Mat();
		Mat grey = new Mat();
		Mat circles = new Mat();
		final Mat permanent = new Mat(480,640,CvType.CV_8UC3);
		Mat permanentFlipped = new Mat();

		// Exit Menu Listener
		exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				System.exit(0);
			}
		});

		// Restart Menu Listener
		restart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				pointHolder.clear();
				permanent.setTo(blackColor);
			}
		});

		// Stream with webcam
		VideoCapture stream = new VideoCapture(0);

		// Enter continuous loop for our stream
		if (stream.isOpened()){
			while (true){
				stream.read(videoImage);
				if(!videoImage.empty()){

					// Convert to grayscale
					Imgproc.cvtColor(videoImage, grey, Imgproc.COLOR_RGB2GRAY);

					// Apply a Gaussian blur to reduce noise and avoid false circle detection:
					Imgproc.GaussianBlur(grey, grey, size, 2, 2 );

					// Apply Hough Circle Transform:
					Imgproc.HoughCircles(grey, circles, Imgproc.CV_HOUGH_GRADIENT,
							1, grey.rows()/8, 200, threshold, 0, 0 );

					// Draw Circles
					for (int i = 0; i < Math.min(circles.cols(), 10); i++){

						double vCircle[] = circles.get(0,  i);

						if (vCircle == null)
							break;

						// Center point and radius of the detected circle(s)
						Point center = new Point(Math.round(vCircle[0]), Math.round(vCircle[1]));
						int radius = (int)Math.round(vCircle[2]);

						// Add the center point to our array list of center points
						pointHolder.add(center);

						// Limit the number of points to prevent memory leak (clear canvases)
						if (pointHolder.size() > 10000){
							pointHolder.clear();
							permanent.setTo(blackColor);
						}

						// Draw the found circle on the webcam frame
						Core.circle(videoImage, center, radius, blackColor, 3, 8, 0);
					}

					// Loop through list of center points and draw all of them
					for (int j=0;j<pointHolder.size();j++){
						Core.circle(videoImage, pointHolder.get(j), 3, redColor, -1, 8, 0);
						Core.circle(permanent, pointHolder.get(j), 3, greenColor, -1, 8, 0);
					}

					// Paint the webcam to the camera panel
					Core.flip(videoImage, videoImage, 1);
					cameraPanel.matToBufferedImage(videoImage);
					cameraPanel.repaint();

					// Paint the draw canvas to the draw panel
					Core.flip(permanent, permanentFlipped, 1);
					drawPanel.matToBufferedImage(permanentFlipped);
					drawPanel.repaint();
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