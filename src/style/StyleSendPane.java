package style;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.text.SimpleAttributeSet;


public class StyleSendPane extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7843551512287205797L;
	private StylePane sendBox;
	private StyleToolbar toolbar;
	private JScrollPane scrollPane;

	public StyleSendPane(){
		super();
		Dimension minim = new Dimension(300,80);
		sendBox = new StylePane();
		toolbar = new StyleToolbar(sendBox);
		scrollPane = new JScrollPane(sendBox);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(minim);
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1.0;   //request any extra hor. space
		c.anchor = GridBagConstraints.EAST;
		this.add(toolbar,c);
		c.gridy++;
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1.0;   //request any extra vertical space
		c.weightx = 1.0;   //request any extra hor. space
		this.add(scrollPane,c);
		KeyStroke enter = KeyStroke.getKeyStroke("ENTER");
		sendBox.getInputMap().put(enter, "send");
		
	}
	public void setSendAction(AbstractAction action){
		sendBox.getActionMap().put("send", action);
	}
	public SimpleAttributeSet getStyle(){
		return sendBox.style;
	}
	public StylePane getStylePane(){
		return sendBox;
	}
	public static void main(String[] args){
		JFrame w = new JFrame("Test StyleSend");
		w.getContentPane().add(new StyleSendPane());
		w.pack();
		w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		w.setVisible(true);
	}

}
