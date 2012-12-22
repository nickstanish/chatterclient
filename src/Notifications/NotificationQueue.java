package Notifications;
import java.util.LinkedList;

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
	private LinkedList<Note> list;
	public static final int SHOW_ONE_FOR_ALL = 0;
	private NotificationWindow window;
	private int displayPreference;
	public NotificationQueue(NotificationWindow window, int displayPreference) {
		list = new LinkedList<Note>();
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
			synchronized(list){
				while(!list.isEmpty()){
				
					
					String total = "";
					if(list.size()>1){
						total = " (" + list.size() + ")";
					}
					
					window.setMessage(list.getFirst().message);
					window.setTitle(list.getFirst().title + total );
					Notification notification = new Notification(window, 3000);
										
					if(displayPreference == SHOW_ONE_FOR_ALL){
						list.clear();
					}
					else{
						while(notification.isShowing()){
							//wait
							//or better yet, check for a click boolean and break the loop
						}
						
						if(!list.isEmpty()) list.removeFirst();
						
					}
				}
			}
			
			try {
				// required to make the thread sleep/wait so that other threads can access list.
				Thread.sleep(300);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public void clear(){
		// performed on click
		// good idea would be to set a boolean false
		synchronized(list){
			list.clear();
		}
	}
	public void add(Note n){
		synchronized(list){
			list.add(n);
		}
		
		
	}

}

