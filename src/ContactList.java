import java.awt.Dimension;
import java.awt.Rectangle;
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

import contacts.Contact;
import contacts.ContactsRenderer;

public class ContactList extends JPanel implements ListSelectionListener, Runnable{
	public DefaultListModel<Contact> model;
	public ChatterClient client;
	public JList<Contact> list;
	public JScrollPane scroller;
	public int delay = 5;
	private boolean keepgoing = true;
	JPopupMenu menu;
	public ContactList(ChatterClient client) {
		super();
		this.client = client;
		model = new DefaultListModel<Contact>();
		list = new JList<Contact>(model);
		list.setCellRenderer(new ContactsRenderer());
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scroller = new JScrollPane(list);
		//list.setVisibleRowCount(12);
		scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		this.add(scroller);
        list.addListSelectionListener(this);
        menu = new JPopupMenu();
        menu.add(new JMenuItem("Message"));
        list.setPrototypeCellValue(new Contact(null, "Index 1234567890")); 
        list.addMouseListener(new MouseAdapter() {
        	public void mousePressed(MouseEvent e)  {
        		if (e.getButton() == MouseEvent.BUTTON3) { //if the event shows the menu
        	        list.setSelectedIndex(list.locationToIndex(e.getPoint())); //select the item
        	        Rectangle r = list.getBounds();
        	        menu.show(list, r.x + r.width - menu.getWidth(), list.indexToLocation(list.getSelectedIndex()).y); //and show the menu
        	    }
        	}
        	public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = list.locationToIndex(e.getPoint());
                    System.out.println("Double clicked on Item " + (index + 1));
                 }
            }
        });
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
			model.addElement(new Contact(null,c[i]));
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
		String[] s = {"Nick Stanish","Joe", "Jake","Marco Polo", "Tester_"};
		list.update(s);
	}
	@Override
	public void valueChanged(ListSelectionEvent e) {
		
	}
}