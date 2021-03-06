package net.vizbits.chatterclient;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import net.vizbits.chatterclient.notifications.Note;
import net.vizbits.chatterclient.notifications.NotificationQueue;
import net.vizbits.chatterclient.notifications.NotificationWindow;


public class NotificationManager {
	private ChatterClient client;
	private MouseListener notificationListener;
	private NotificationWindow notificationWindow;
	private NotificationQueue queue;
	
	public NotificationManager(ChatterClient c){
		this.client = c;
		notificationListener = new MouseListener(){
			@Override
			public void mouseClicked(MouseEvent m) {}
			@Override
			public void mouseEntered(MouseEvent m) {
				if(NotificationWindow.transparencySupported()) notificationWindow.setOpacity(1.0f);
			}
			@Override
			public void mouseExited(MouseEvent m) {
				if(NotificationWindow.transparencySupported()) notificationWindow.setOpacity(NotificationWindow.TRANSPARENCY);
			}
			@Override
			public void mousePressed(MouseEvent m) {
					notificationWindow.setVisible(false);
					queue.clear();
					if(client != null){
						client.bringToFront();
					}
			}
			@Override
			public void mouseReleased(MouseEvent arg0) {}
		};
		notificationWindow = new NotificationWindow();
		notificationWindow.addMouseListener(notificationListener);
		queue = new NotificationQueue(notificationWindow, NotificationQueue.SHOW_ONE_FOR_ALL);
	}
	public void notify(String title, String message){
		queue.add(new Note(title, message));
	}
	public static void main(String[] args){
		NotificationManager nm = new NotificationManager(null);
		nm.notify("Title", "This is a message");
		nm.notify("Title", "This is a message");
		try {
			Thread.sleep(2000);
			nm.notify("Notification", "This is a message");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
