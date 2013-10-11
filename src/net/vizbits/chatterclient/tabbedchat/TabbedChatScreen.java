package net.vizbits.chatterclient.tabbedchat;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.text.SimpleAttributeSet;

import net.vizbits.chatterclient.ChatterClient;
import net.vizbits.chatterclient.tabbedchat.style.StyleChatScreen;


public class TabbedChatScreen extends JTabbedPane{
	private ChatterClient client;
	/**
	 * 
	 */
	public static final String HOME_TAB = "HOME";
	private static final long serialVersionUID = -1906568734746340946L;
	public TabbedChatScreen(ChatterClient c){
		super(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
		this.client = c;
		TabbedPanel panel = new TabbedPanel(null, new StyleChatScreen(this.client));
		this.newTab(false, panel);
	}
	private void newTab(boolean closeable, TabbedPanel c){
		this.add(toTitle(c.contacts), c);
		if(closeable){
			setTabComponentAt(getTabCount()-1,new CloseableTab(this));
		}
	}
	public TabbedPanel findConversation(String[] usernames){
		for(int i = 0; i<getTabCount(); i++){
			TabbedPanel p = (TabbedPanel)getComponentAt(i);
			if(p.contacts == null){
				if(usernames == null) return p;
			}
			else{
				if(usernames != null && Arrays.equals(p.contacts, usernames)) return p;
			}
		}
		return newConversation(usernames);
	}
	public TabbedPanel newConversation(String[] usernames){
		TabbedPanel panel = new TabbedPanel(usernames, new StyleChatScreen(this.client));
		newTab(true,panel);
		return panel;
	}
	private static String toTitle(String[] contacts){
		if(contacts == null) return "All";
		String s = "";
		if(contacts.length <= 0) return "Tab";
		int i = 0;
		for(String contact : contacts){
			if(i < 2) s += contact + ( (i == 0) ? ", " : "");
			i++;
		}
		if( i > 2) s += " + " + ( i - 2) + " other" + ( (i- 2 > 1) ? "s" : "");
			
		return s;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JFrame f = new JFrame("Tabbed Test");
		TabbedChatScreen t  = new TabbedChatScreen(null);
		String[] s = {"Nick","Joey"};
		for(int i = 0; i < 10; i++){
			t.newConversation(s);
		}
		f.getContentPane().add(t);
		f.pack();
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}
	public void requestSendBoxFocus(String[] _contacts) {
		findConversation(_contacts).requestSendBoxFocus();
		
	}
	public void clearChat(String[] _contacts) {
		findConversation(_contacts).clearChat();
		
	}
	public void clearChat(int i) {
		( (TabbedPanel) getComponentAt(i) ).clearChat();
		
	}
	public void displayMessage(String[] contacts, String msg,
			SimpleAttributeSet style) {
		findConversation(contacts).styleChatScreen.appendln(msg,style);
		
	}
}
