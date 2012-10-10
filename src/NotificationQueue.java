import java.util.ArrayList;

import javax.swing.JWindow;

/**
 * 
 * NotificationQueue
 * This is an object that runs concurrently with your program. Used in combination with
 * Notification, NotificationWindow, and Note to display an animated notification to the 
 * user for a specified time. Notifications can be added to this list and queued up until
 * it is time to display them. You can also specify an option to display all notifications 
 * at once, and this may clear up problems with multiple threads accessing the list at once.
 *
 */
public class NotificationQueue extends Thread{
	private ArrayList<Note> list;
	public static final int SHOW_ONE_FOR_ALL = 0;
	private NotificationWindow window;
	private int displayPreference;
	public NotificationQueue(NotificationWindow window, int displayPreference) {
		list = new ArrayList<Note>();
		this.window = window;
		this.displayPreference = displayPreference;
		start();
	}
	/*
	 * display notification
	 * synchronization crap sucks, but seems necessary since elements are removed
	 * and being used at the same time. If errors occur (IndexOutOfBoundsException) 
	 * then use SHOW_ONE_FOR_ALL option
	 */
	public void run(){
		while(true){
			while(!list.isEmpty()){
				synchronized(list){
					
					String total = "";
					if(list.size()>1){
						total = " (" + list.size() + ")";
					}
					
					window.setMessage(list.get(0).message);
					window.setTitle(list.get(0).title + total );
					Notification notification = new Notification(window, 3000);
										
					if(displayPreference == SHOW_ONE_FOR_ALL){
						list.clear();
					}
					else{
						while(notification.isShowing()){
							//wait
						}
						
						if(!list.isEmpty()) list.remove(0);
					}
				}
			}
			
		}
	}
	public synchronized void clear(){
		list.clear();
	}
	public synchronized void add(Note n){
		list.add(n);
		
	}

}

