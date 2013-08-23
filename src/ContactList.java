import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JWindow;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import contacts.Contact;
import contacts.ContactsRenderer;

public class ContactList extends JWindow implements ListSelectionListener, Runnable{
	public DefaultListModel<Contact> model;
	public ChatterClient client;
	public JList<Contact> list;
	private int x = 0, y = 0;
	public JScrollPane scroller;
	public int delay = 5;
	private boolean snapped = true;
	private boolean drag = false;
	private boolean keepgoing = true;
	JPopupMenu menu;
	public ContactList(ChatterClient client) {
		super();
		this.setOpacity(0.9f);
		this.client = client;
		model = new DefaultListModel<Contact>();
		list = new JList<Contact>(model);
		list.setCellRenderer(new ContactsRenderer());
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scroller = new JScrollPane(list);
		list.setVisibleRowCount(10);
		scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		this.getContentPane().add(scroller);
        list.addListSelectionListener(this);
        menu = new JPopupMenu();
        
        JMenuItem messageItem = new JMenuItem("Message");
        messageItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				sendMessage(list.getSelectedValue().getName());
			}
		});
        menu.add(messageItem);
        list.setPrototypeCellValue(new Contact(null, "Index 1234567890")); 
        list.addMouseMotionListener(new MouseMotionAdapter() {
        	public void mouseDragged(MouseEvent e){
        		snapped = false;
        		drag = true;
        		setLocation(getLocation().x + (e.getX() - x ) , getLocation().y + (e.getY() - y));
        	}
        });
        list.addMouseListener(new MouseAdapter() {
        	public void mousePressed(MouseEvent e)  {
        		x = e.getX();
        		y = e.getY();
        		if (e.getButton() == MouseEvent.BUTTON3) { //if the event shows the menu
        	        list.setSelectedIndex(list.locationToIndex(e.getPoint())); //select the item
        	        Rectangle r = list.getBounds();
        	        menu.setVisible(true);
        	        menu.show(list, r.x + r.width - menu.getWidth(), list.indexToLocation(list.getSelectedIndex()).y); //and show the menu
        	    }
        	}
        	public void mouseReleased(MouseEvent e){
        		if(drag){
        			drag = false;
        			checkSnap();
        		}
        	}
        	
        	public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    sendMessage(list.getSelectedValue().getName());
                 }
            }
        });
		this.pack();

		this.setVisible(client == null);
		this.setLocation(50, 50);
		//this.setSize(new Dimension(100,100));
	}
	private boolean checkSnap() {
		if(client != null){
			JFrame w = client.getFrame();
			Rectangle f = new Rectangle(w.getX(),w.getY(),w.getWidth(),w.getHeight());
			Rectangle co = new Rectangle(getX(),getY(),getWidth(),getHeight());
			if(f.intersects(co)){
				snap();
				return true;
			}
			
		}
		return false;
	}
	public void startPolling(){
		new Thread(this).start();
		setVisible(true);
		if(client != null){
			snap();
		}
		
	}
	private void sendMessage(String username){
		if (client != null){
			client.getSendMessagePane().setText("./to " + username + ":");
		}
		else System.out.println(username);
	}
	public boolean getSnapped(){
		return snapped;
	}
	public void snap(){
		snapped = true;
		JFrame w = client.getFrame();
		setLocation(w.getX() + w.getWidth(),w.getY() + 50);
	}
	public void setDelay(int seconds){
		delay = seconds;
	}
	public void update(String[] c){
		model.clear();
		for(int i = 0; i < c.length; i++){
			model.addElement(new Contact(null,c[i]));
		}
		revalidate();
	}
	public void run(){
		while(keepgoing){
			try {
				if(client.connected()){
					client.requestContacts();
					Thread.sleep(delay*1000);
				}
				
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
		ContactList list = new ContactList(null);
		String[] s = {"Nick Stanish","Joe", "Jake","Marco Polo", "Tester_"};
		list.update(s);
	}
	@Override
	public void valueChanged(ListSelectionEvent e) {
		
	}
}