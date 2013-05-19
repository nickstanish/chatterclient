import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class ContactList extends JPanel implements ListSelectionListener{
	public DefaultListModel<String> model;
	public JList<String> list;
	public JScrollPane scroller;
	public ContactList() {
		super();
		model = new DefaultListModel<String>();
		list = new JList<String>(model);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scroller = new JScrollPane(list);
		list.setVisibleRowCount(12);
		scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		this.add(scroller);
        list.addListSelectionListener(this);
		
	}
	public void update(String[] c){
		model.clear();
		for(int i = 0; i < c.length; i++){
			model.addElement(c[i]);
		}
	}
	public static void main(String[] args){
		JFrame window = new JFrame("Contacts");
		ContactList list = new ContactList();
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