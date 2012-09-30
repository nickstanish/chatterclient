import java.util.ArrayList;

import javax.swing.JWindow;


public class NotificationQueue extends Thread{
	private ArrayList<Note> list;
	private boolean on = false;
	private Notification notification;
	private JWindow window;
	public NotificationQueue() {
		list = new ArrayList<Note>();
		window = new JWindow();
		notification = new Notification(window, "", "", 3000);
		
	}
	public void start(){
		while(!list.isEmpty()){
			on = true;
			String total = "";
			if(list.size()>1){
				total = "(" + list.size() + ")";
			}
			notification.setMessage(list.get(0).message);
			notification.setTitle(list.get(0).title + total );
			notification.display();
			while(notification.isShowing()){
				//wait
			}
			list.remove(0);
		}
		on = false;
	}
	public void add(Note n){
		list.add(n);
		if(!on){
			start();
		}
		
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		NotificationQueue queue = new NotificationQueue();
		queue.add(new Note("title here", "ok message body here"));
		queue.add(new Note("", "no title"));
		queue.add(new Note("title here", " message body here"));
		queue.add(new Note("title here", "ok  body here"));
	}

}
class Note{
	public String title, message;
	public Note(String title, String message){
		this.title = title;
		this.message = message;
	}
}
