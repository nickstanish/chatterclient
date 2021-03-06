package net.vizbits.chatterclient.notifications;
import java.awt.Color;
import java.awt.GraphicsDevice;
import java.awt.GraphicsDevice.WindowTranslucency;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JWindow;


public class NotificationWindow extends JWindow {
	/**
	 * window needs to be united but declared separate so we put it in a different class
	 * there will only be one, but it needs an actionlistener that opens chatterclient window
	 */
	private static final long serialVersionUID = -3228619876556206647L;
	private JLabel titleLabel, messageLabel;
	private String title, message;
	public static final float TRANSPARENCY = 0.85f;
	public NotificationWindow(){
		JComponent comp = (JComponent)getContentPane();
		title = "";
		message = "";
		titleLabel = new JLabel(title);
		messageLabel = new JLabel(message);
		comp.setLayout(new GridBagLayout());
		comp.setBorder(BorderFactory.createLineBorder(Color.black));
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.5;
		c.weighty = 0.0;
		c.anchor = GridBagConstraints.PAGE_START;
		comp.add(titleLabel,c);
		c.gridy = 1;
		c.anchor = GridBagConstraints.CENTER;
		c.weighty = 0.35;
		comp.add(messageLabel,c);
		getContentPane().setBackground(new Color(227, 227, 227));	
		if (transparencySupported()){
			this.setOpacity(TRANSPARENCY);
		}
		
		pack();
	}
	public static boolean transparencySupported(){
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		return gd.isWindowTranslucencySupported(WindowTranslucency.TRANSLUCENT);
	}
	public void setMessage(String message){
		this.message = message;
		messageLabel.setText(message);
		
	}
	public void setTitle(String title){
		this.title = title;
		titleLabel.setText(title);
	}
}
