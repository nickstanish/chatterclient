package net.vizbits.chatterclient.tabbedchat;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

import net.vizbits.chatterclient.tabbedchat.style.StyleChatScreen;

/*
 * @author: nick stanish
 */

public class TabbedPanel extends JPanel {
	public String[] contacts;
	public StyleChatScreen styleChatScreen;
	
	public TabbedPanel(String[] s, StyleChatScreen screen){
		this.contacts = s;
		this.styleChatScreen = screen;
		if(screen == null) this.styleChatScreen = new StyleChatScreen(null);
		else this.styleChatScreen = screen;
		this.setLayout(new BorderLayout());
		this.add(styleChatScreen, BorderLayout.CENTER);
	}
	public void clearChat(){
		styleChatScreen.clearChat();
	}
	public static void main(String[] args){
		JFrame f = new JFrame("Test TabbedPanel");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		String[] s = {"Nick","Joey"};
		f.getContentPane().add(new TabbedPanel(s,null));
		f.pack();
		f.setVisible(true);
		f.setSize(400,400);
	}
	public void requestSendBoxFocus() {
		styleChatScreen.requestSendBoxFocus();
		
	}
}
