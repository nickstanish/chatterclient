package net.vizbits.chatterclient.notifications;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

public class Notification{
	private NotificationWindow window;
	private int duration;
	private boolean showing = true;
	private Data desiredLocation, currentLocation, windowSize;
	private Rectangle dim;
	public Notification(NotificationWindow window, int duration){
		this.window = window;
		windowSize = new Data(150,80);
		this.duration = duration;	
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		dim = ge.getMaximumWindowBounds();
		desiredLocation = new Data((int)dim.getWidth() - windowSize.x - 2, (int)dim.getHeight() - windowSize.y - 2);
		currentLocation = new Data((int)dim.getWidth(), (int)dim.getHeight() - windowSize.y - 2);
		window.setPreferredSize(new Dimension(windowSize.x,windowSize.y));
		window.pack();
		display();
	}
	public boolean isShowing(){
		return showing;
	}
	public void display(){
		window.setVisible(true);
		window.setLocation(currentLocation.x, currentLocation.y); //offscreen
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
		showing = false;
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
