import java.awt.Dimension;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class ContactList extends JPanel implements ListSelectionListener, Runnable{
	public DefaultListModel<String> model;
	public ChatterClient client;
	public JList<String> list;
	public JScrollPane scroller;
	public int delay = 5;
	private boolean keepgoing = true;
	public ContactList(ChatterClient client) {
		super();
		this.client = client;
		model = new DefaultListModel<String>();
		list = new JList<String>(model);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scroller = new JScrollPane(list);
		list.setVisibleRowCount(12);
		scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		this.add(scroller);
        list.addListSelectionListener(this);
	}
	public void startPolling(){
		new Thread(this).start();
	}
	public void setDelay(int seconds){
		delay = seconds;
	}
	public void update(String[] c){
		model.clear();
		for(int i = 0; i < c.length; i++){
			model.addElement(c[i]);
		}
		revalidate();
	}
	public void run(){
		while(keepgoing){
			try {
				client.requestContacts();
				Thread.sleep(delay*1000);
				} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public void kill(){
		keepgoing = false;
	}
	public static void main(String[] args){
		JFrame window = new JFrame("Contacts");
		ContactList list = new ContactList(null);
		window.getContentPane().add(list);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.pack();
		window.setVisible(true);
		window.setSize(new Dimension(100,100));
	}
	@Override
	public void valueChanged(ListSelectionEvent e) {
		// TODO Auto-generated method stub
		
	}
}