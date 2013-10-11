package net.vizbits.chatterclient.tabbedchat.style;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.SimpleAttributeSet;

import net.vizbits.chatterclient.ChatterClient;

public class StyleChatScreen extends JPanel{
	private StyleViewPane viewPane;
	private StyleSendPane sendMessagePane;
	private JLabel isTypingLabel;
	private ChatterClient client;
	private JButton sendButton;
	
	public StyleChatScreen(ChatterClient c){
		this.client = c;
		viewPane = new StyleViewPane();
		sendMessagePane = new StyleSendPane();
		StylePane sendBox = sendMessagePane.getStylePane();
		sendBox.setAutoscrolls(true);
		sendBox.setEditable(true);
		sendBox.setDragEnabled(true);
		sendBox.setDropMode(DropMode.INSERT);
		sendMessagePane.setSendAction(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				send();
			}
		});
		sendBox.getDocument().addDocumentListener(new DocumentListener(){
			@Override
			public void changedUpdate(DocumentEvent e) {}
			@Override
			public void insertUpdate(DocumentEvent e) {
				isTyping();
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				isTyping();
			}
		});
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		JPanel isTypingPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		isTypingLabel = new JLabel(" ");
		isTypingPanel.add(isTypingLabel);
		sendButton = new JButton("Send");
		sendButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				send();
			}
		});
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		panel.add(sendButton);
		this.add(viewPane);
		this.add(isTypingPanel);
		this.add(sendMessagePane);
		this.add(panel);
	}
	public void send(){
		String msg = sendMessagePane.getStylePane().getText().trim();
		SimpleAttributeSet style = sendMessagePane.getStyle();
		if(client == null) appendln("Null Client... " + msg, style);
		if(client != null && client.connected()){
			client.interpretMessage(msg,style);
		}
		sendMessagePane.getStylePane().setText("");
		
	}
	public void isTyping(){
		if(client != null && client.connected()){
			String s = sendMessagePane.getStylePane().getText().trim();
			int len = s.length();
			if(len == 1 && s.substring(0,1).equals(".")) s = "";
			if(len >= 2 && s.substring(0,2).equals("./")) s = "";
			boolean isTyping = !s.equals("");
			/*if(realtime){
				try {
					send('2',booleanToBit(isTyping) + fauxDiff(oldRealtime, s));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else
				try {
					send('2',booleanToBit(isTyping) + "");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				*/
			//oldRealtime = s;
		}
	}
	public void requestSendBoxFocus(){
		sendMessagePane.requestFocusInWindow();
	}
	public void appendln(String msg, SimpleAttributeSet style){
		viewPane.appendln(msg, style);
	}
	public void clearChat(){
		viewPane.getStylePane().setText("");
	}
	public static void main(String[] args){
		JFrame f = new JFrame("Test StyleChatScreen");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.getContentPane().add(new StyleChatScreen(null));
		f.pack();
		f.setVisible(true);
		f.setSize(400,400);
	}
}
