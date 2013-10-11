package net.vizbits.chatterclient.tabbedchat.style;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.SimpleAttributeSet;



public class StyleViewPane extends JPanel{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1645679932047019540L;
	private StylePane messagePane;
	private JScrollPane scrollPane;
	public StyleViewPane(){
		super(new BorderLayout());
		messagePane = new StylePane();
		messagePane.setEditable(false);
		scrollPane = new JScrollPane(messagePane);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(new Dimension(150,200));
		this.add(scrollPane, BorderLayout.CENTER);
		/*
		 * Random r = new Random();
		 */
		/*
		 * for(int x = 0; x < 12; x++){
			SimpleAttributeSet net.vizbits.chatterclient.style = constructStyle(new Color(r.nextInt(256), r.nextInt(256), r.nextInt(256)), false, true, x + 10);
			append("Styled TEXT test! #" + x, net.vizbits.chatterclient.style);
		}
		*/
		//GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		//String[] x = ge.getAvailableFontFamilyNames();
	}
	public StylePane getStylePane(){
		return messagePane;
	}
	public void appendln(String text, SimpleAttributeSet style){
		messagePane.appendln(text, style);
	}
	public void append(String text, SimpleAttributeSet style){
		messagePane.append(text, style);
	}
	public static void main(String[] args) {
		View window = new View(new StyleSendPane(), new StyleViewPane());
		window.pack();
		window.setVisible(true);

	}

}
class View extends JFrame{
	private StyleViewPane pane;
	private StyleSendPane sendBox;
	
	public void send(){
		pane.append(sendBox.getStylePane().getText().replaceAll("\\n$", ""), sendBox.getStyle());
		sendBox.getStylePane().setText("");
		sendBox.getStylePane().requestFocusInWindow();
	}
	public View(StyleSendPane sendBox, StyleViewPane pane){	
		this.pane =  pane;
		this.sendBox = sendBox;
		sendBox.setSendAction(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				send();
				}
			});
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		add(pane);
		JButton button = new JButton("Send");
		button.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				send();
			}
		});
		
		
		getContentPane().add(sendBox);
		JPanel g = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		g.add(button);
		getContentPane().add(g);
	}
}