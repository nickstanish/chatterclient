import static java.awt.GraphicsDevice.WindowTranslucency.TRANSLUCENT;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JWindow;


public class Notification extends Thread{
	private JWindow window;
	private int duration;
	private Data desiredLocation, currentLocation, windowSize;
	private Rectangle dim;
	Notification(JWindow window, String title, String message, int duration){
		this.window = window;
		windowSize = new Data(150,80);
		JComponent comp = (JComponent)window.getContentPane();
		comp.setLayout(new GridBagLayout());
		comp.setBorder(BorderFactory.createLineBorder(Color.black));
		this.duration = duration;
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.5;
		c.weighty = 0.0;
		c.anchor = GridBagConstraints.PAGE_START;
		comp.add(new JLabel(title),c);
		c.gridy = 1;
		c.anchor = GridBagConstraints.CENTER;
		c.weighty = 0.35;
		comp.add(new JLabel(message),c);
		window.getContentPane().setBackground(new Color(227, 227, 227));	
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		if (gd.isWindowTranslucencySupported(TRANSLUCENT)){
			window.setOpacity(0.75f);
		}
		dim = ge.getMaximumWindowBounds();
		desiredLocation = new Data((int)dim.getWidth() - windowSize.x - 2, (int)dim.getHeight() - windowSize.y - 2);
		currentLocation = new Data((int)dim.getWidth(), (int)dim.getHeight() - windowSize.y - 2);
		this.start();
	}
	public void run(){
		window.setLocation(currentLocation.x, currentLocation.y); //offscreen
		window.setPreferredSize(new Dimension(windowSize.x,windowSize.y));
		window.pack();
		window.setVisible(true);
		while(!compare(desiredLocation, currentLocation)){
			//animate
			currentLocation.x -= 1;
			window.setLocation(currentLocation.x, currentLocation.y);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		window.setLocation(desiredLocation.x, desiredLocation.y);
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		desiredLocation.x = (int)dim.getWidth();
		while(!compare(desiredLocation, currentLocation)){
			//animate out
			currentLocation.x += 1;
			window.setLocation(currentLocation.x, currentLocation.y);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		window.setVisible(false);
		window.dispose();
	}

	/**
	 * @param args
	 */
	class Data{
		/**
		 * hold the x/y, height/width data
		 */
		public int x;
		public int y;
		Data(int x, int y){
			this.x = x;
			this.y = y;
			
		}
	}
	
	private boolean compare(Data a, Data b){
		return (a.x == b.x && a.y == b.y);
	}


}
