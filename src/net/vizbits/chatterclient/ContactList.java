package net.vizbits.chatterclient;
import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.vizbits.chatterclient.contacts.Contact;
import net.vizbits.chatterclient.contacts.ContactsRenderer;

public class ContactList extends JPanel implements ListSelectionListener, Runnable{
	public DefaultListModel<Contact> model;
	public ChatterClient client;
	public JList<Contact> list;
	private int x = 0, y = 0;
	public JScrollPane scroller;
	public int delay = 5;
	private boolean keepgoing = true;
	private String[] contacts = null;
	JPopupMenu menu;
	public ContactList(ChatterClient client) {
		super();
		this.setLayout(new BorderLayout());
		this.client = client;
		model = new DefaultListModel<Contact>();
		list = new JList<Contact>(model);
		list.setCellRenderer(new ContactsRenderer());
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		scroller = new JScrollPane(list);
		list.setVisibleRowCount(10);
		scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		this.add(scroller, BorderLayout.CENTER);
        list.addListSelectionListener(this);
        menu = new JPopupMenu();
        
        JMenuItem messageItem = new JMenuItem("Message");
        messageItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				handleSendMessage();
			}
		});
        menu.add(messageItem);
        list.setPrototypeCellValue(new Contact("Index 1234567890")); 
        list.addMouseListener(new MouseAdapter() {
        	public void mousePressed(MouseEvent e)  {
        		x = e.getX();
        		y = e.getY();
        		if (e.getButton() == MouseEvent.BUTTON3) { //if the event shows the menu
        	       // list.setSelectedIndex(list.locationToIndex(e.getPoint())); //select the item
        	        Rectangle r = list.getBounds();
        	        menu.setVisible(true);
        	        menu.show(list, r.x + r.width - menu.getWidth(), list.indexToLocation(list.getSelectedIndex()).y); //and show the menu
        	    }
        	}
        	public void mouseReleased(MouseEvent e){}
        	
        	public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                	handleSendMessage();
                 }
            }
        });

		this.setVisible(client == null);
	}
	/*
	 * TODO press enter to send to multiple yup yup
	 */
	private void handleSendMessage(){
		int[] indices = list.getSelectedIndices();
    	String[] s = new String[indices.length];
    	for(int i = 0; i < indices.length; i++){
    		System.out.println(indices[i]);
    		System.out.println(model.get(indices[i]));
    		s[i] = model.get(indices[i]).getName();
    	}
        sendMessage(s);
	}

	public void startPolling(){
		new Thread(this).start();
		setVisible(true);
		
	}
	private String[] addSelf(String[] _usernames){
		String[] all = new String[_usernames.length + 1];
		for(int i = 0; i < _usernames.length; i++){
			all[i] = _usernames[i];
		}
		if(client != null)	all[all.length - 1] = client.username;
		return all;
	}
	private void sendMessage(String[] usernames){
		if (client != null){
			client.newConversation(addSelf(usernames));
			//client.getSendMessagePane().setText("./to " + username + ":");
		}
		else{
			for(String s: usernames) System.out.print(s + ". ");
			System.out.println();
		}
	}
	public void setDelay(int seconds){
		delay = seconds;
	}
	public void update(String[] c){
		model.clear();
		for(int i = 0; i < c.length; i++){
			if(client != null && client.username.equals(c[i])) continue;
			model.addElement(new Contact(c[i]));
		}
		contacts = c;
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
	public String[] getContacts(){
		if(contacts != null) return contacts;
		String[] c = {};
		return c;
	}
	public void kill(){
		keepgoing = false;
	}
	public static void main(String[] args){
		JFrame p = new JFrame("Contacts List");
		
		ContactList list = new ContactList(null);
		
		p.getContentPane().add(list);
		String[] s = {"Nick Stanish","Joe", "Jake","Marco Polo", "Tester_"};
		list.update(s);
		p.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		p.pack();
		
		p.setVisible(true);
	}
	@Override
	public void valueChanged(ListSelectionEvent e) {
		
	}
}