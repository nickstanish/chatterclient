package net.vizbits.chatterclient.contacts;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;

public class ContactsRenderer extends JPanel implements ListCellRenderer<Contact>{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3919746404500435193L;
	private JLabel label;
	private static final Color HIGHLIGHT = UIManager.getColor("Tree.selectionBackground");
	public static Font fontPlain = new Font("serif",Font.PLAIN, 14);
	public static Font fontBold = new Font("serif",Font.BOLD, 14);
	public ContactsRenderer(){
		super();
		this.setBackground(Color.white);
		label = new JLabel();
		label.setFont(fontPlain);
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	    this.add(Box.createRigidArea(new Dimension(5,20)));
	    this.add(label);
	    this.add(Box.createRigidArea(new Dimension(30,20)));
	    }

	@Override
	public Component getListCellRendererComponent(JList<? extends Contact> list, Contact value, int index, boolean isSelected, boolean cellHasFocus) {
		label.setText(value.getName());
	    setBackground(isSelected ? HIGHLIGHT : Color.white);
	    setBorder(isSelected ? BorderFactory.createEtchedBorder(EtchedBorder.RAISED) : BorderFactory.createMatteBorder(0, 0, 1, 1, Color.BLACK));
	    label.setFont(isSelected ? fontBold : fontPlain);
	    //label.setForeground(isSelected ? Color.white : Color.black);
	    return this;
	}


}
