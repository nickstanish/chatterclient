import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;


public class ChatterClient extends JFrame{
	/**
	 *
	 */
	private static final long serialVersionUID = -7269347532987537692L;
	private JPanel mainPanel, topPanel, bottomPanel,cardsPanel, loginScreen, chatScreen, advancedPanel;
	private JTextField loginBox, messageBox, serverBox, portBox;
	private File file;
	private JButton loginButton, advancedButton, sendButton, resetAdvancedButton;
	private CTextArea chatArea;
	private boolean loggedIn = false;
	JMenuItem logoutMenu;
	private static final String LOGIN_SCREEN = "Login";
	private static final String CHAT_SCREEN = "Chat";
	private static final String DEFAULT_HOST = "data.cs.purdue.edu";
	private static final int DEFAULT_PORT = 1500;
	private String defaultUsername = "username";
	private String username;
	private int port;
	private String hostname;
	private boolean showTime = true;

	// for I/O
	private ObjectInputStream sInput;		// to read from the socket
	private ObjectOutputStream sOutput;		// to write on the socket
	private Socket socket;

	public boolean start() {
		// try to connect to the server
		try {
			socket = new Socket(hostname, port);
		} 
		catch(Exception ec) {
			display("Error connectiong to server:" + ec);
			//TODO: change show display on login screen
			return false;
		}
		/* Creating both Data Stream */
		try{
			sInput  = new ObjectInputStream(socket.getInputStream());
			sOutput = new ObjectOutputStream(socket.getOutputStream());
		}
		catch (IOException eIO) {
			display("Exception creating new Input/output Streams: " + eIO);
			return false;
		}
		// creates the Thread to listen from the server 
		new ListenFromServer().start();
		// Send our username to the server this is the only message that we
		// will send as a String. All other messages will be ChatMessage objects
		try{
			sOutput.writeObject(username);
		}
		catch (IOException eIO) {
			disconnect();
			return false;
		}
		// success we inform the caller that it worked
		return true;
	}

	/*
	 * To send a message to the console or the GUI
	 */
	private void display(String msg) {
		if(!showTime){
			//fix whoisin time parse bug with a start of line regex - 9/2/12
			msg = msg.replaceFirst("^(\\d{1,2}:\\d{1,2}:\\d{1,2}[:\\s]{1,2})", "");
		}
		chatArea.append(msg + "\n");		// append to the ClientGUI JTextArea (or whatever)
	}
	
	/*
	 * To send a message to the server
	 */
	void sendMessage(ChatMessage msg) {
		try {
			sOutput.writeObject(msg);
		}
		catch(IOException e) {
			display("Exception writing to server: " + e);
		}
	}
	/*
	 * When something goes wrong
	 * Close the Input/Output streams and disconnect not much to do in the catch clause
	 */
	private void disconnect() {
		try { 
			if(sInput != null) sInput.close();
		}
		catch(Exception e) {} // not much else I can do
		try {
			if(sOutput != null) sOutput.close();
		}
		catch(Exception e) {} // not much else I can do
        try{
			if(socket != null) socket.close();
		}
		catch(Exception e) {} // not much else I can do
			
	}
	public void connectionFailed() {
		JOptionPane.showMessageDialog(null, "Connection Failed");
	}
	/*
	 * a class that waits for the message from the server and append them to the JTextArea
	 * if we have a GUI or simply System.out.println() it in console mode
	 */
	class ListenFromServer extends Thread {

		public void run() {
			while(true) {
				try {
					String msg = (String) sInput.readObject();
					display(msg);
				}
				catch(IOException e) {
					display("Server has close the connection: " + e);
					connectionFailed();
					break;
				}
				// can't happen with a String object but need the catch anyhow
				catch(ClassNotFoundException e2) {
				}
			}
		}
	}
	ChatterClient(String hostname, int portnumber){
		mainPanel = new JPanel();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		createMenu();
		setMinimumSize(new Dimension(300,500));
		JLabel titleLabel = new JLabel("ChatterBox");
		titleLabel.setFont(new Font("sansserif", Font.BOLD, 48));
		titleLabel.setForeground(new Color(75,103,161));
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setPreferredSize(new Dimension(300,500));
		topPanel = new JPanel();
		mainPanel.add(topPanel);
		topPanel.add(titleLabel);
		cardsPanel = new JPanel(new CardLayout());
		mainPanel.add(cardsPanel);
		bottomPanel = new JPanel();
		mainPanel.add(bottomPanel);
		createLoginScreen();
		createChatScreen();
		add(mainPanel);
	}

	/**
	 * @param args
	 */
	/*
	 * use methods to organize everything thats going on since most of it
	 * initializing the GUI
	 */
	
	private void createMenu(){
		JMenuBar menubar = new JMenuBar();
		JMenu filemenu = new JMenu("File");
		JMenuItem optionsMenu = new JMenuItem("Options");
		filemenu.add(optionsMenu);
		logoutMenu = new JMenuItem("Logout");
		logoutMenu.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				logout();
			}
		});
		logoutMenu.setEnabled(false);
		filemenu.add(logoutMenu);
		JMenuItem exitMenu = new JMenuItem("Exit");
		exitMenu.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if(loggedIn){
					logout();
				}
				dispose();
				System.exit(0);
			}
		});
		filemenu.add(exitMenu);
		menubar.add(filemenu);
		setJMenuBar(menubar);
	}
	private void createLoginScreen(){
		loginScreen = new JPanel();
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		loginBox = new JTextField(defaultUsername,20);
		loginBox.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				login();
			}
		});
		loginButton = new JButton("Login");
		advancedButton = new JButton("Advanced");
		advancedButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				showAdvancedOptions();
			}
		});
		loginButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				login();
			}
		});
		JPanel labelPanel = new JPanel();
		labelPanel.add(new JLabel("Login to start chatting"));
		panel.add(labelPanel);
		panel.add(Box.createRigidArea(new Dimension(0,20)));
		JPanel usernamePanel = new JPanel(new GridLayout(0,1));
		usernamePanel.setMaximumSize(new Dimension(200,100));
		usernamePanel.add(new JLabel("Username:"));
		usernamePanel.add(loginBox);
		panel.add(usernamePanel);
		panel.add(Box.createRigidArea(new Dimension(0,20)));
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(loginButton);
		buttonPanel.add(advancedButton);
		panel.add(buttonPanel);
		advancedPanel = new JPanel(new GridLayout(0,1));
		advancedPanel.setMaximumSize(new Dimension(200,100));
		serverBox = new JTextField(DEFAULT_HOST);
		portBox = new JTextField(DEFAULT_PORT + "");
		advancedPanel.add(new JLabel("Host address"));
		advancedPanel.add(serverBox);
		advancedPanel.add(new JLabel("Port Number"));
		advancedPanel.add(portBox);
		resetAdvancedButton = new JButton("Reset");
		resetAdvancedButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				resetAdvancedOptions();
			}
		});
		advancedPanel.add(Box.createRigidArea(new Dimension(0,10)));
		advancedPanel.add(resetAdvancedButton);
		panel.add(advancedPanel);
		advancedPanel.setVisible(false);
		panel.add(Box.createRigidArea(new Dimension(60,300)));
		loginScreen.add(panel);
		cardsPanel.add(loginScreen, LOGIN_SCREEN);
	}
	private void resetAdvancedOptions(){
		serverBox.setText(DEFAULT_HOST + "");
		portBox.setText(DEFAULT_PORT + "");
	}
	private void showAdvancedOptions(){
		if (advancedButton.getText().equalsIgnoreCase("Hide Advanced")){
			advancedButton.setText("Advanced");
			advancedPanel.setVisible(false);
		}
		else{
			advancedButton.setText("Hide Advanced");
			advancedPanel.setVisible(true);
		}
		
		
	}
	private void sendMessage(){
		if(!messageBox.getText().trim().equals("")){
			sendMessage(new ChatMessage(ChatMessage.MESSAGE, messageBox.getText()));				
			messageBox.setText("");
		}
		
	}
	private void login(){
		username = loginBox.getText().trim();
		if(username.equals("") || username == null){
			JOptionPane.showMessageDialog(null, "Invalid username");
			return;
			
		}
		if (advancedButton.getText().equalsIgnoreCase("Hide Advanced")){
			port = Integer.parseInt(portBox.getText());
			hostname = serverBox.getText();
		}
		else{
			port = DEFAULT_PORT;
			hostname = DEFAULT_HOST;
		}
		JOptionPane.showMessageDialog(null, "Logging in as " + username + "\non " + hostname + ":" + port);
		
		if(!start()){
			disconnect();
			connectionFailed();
			return;
		}
		switchView(1);
		loggedIn = true;
		logoutMenu.setEnabled(true);
	}
	private void logout(){
		switchView(0);
		sendMessage(new ChatMessage(ChatMessage.LOGOUT, ""));
		disconnect();
		logoutMenu.setEnabled(false);
		loggedIn = false;
	}
	private void createChatScreen(){
		chatScreen = new JPanel();
		chatScreen.setBackground(Color.white);
		chatScreen.setLayout(new BoxLayout(chatScreen, BoxLayout.PAGE_AXIS));
		
		file = new File("media/background.png");
		chatArea = new CTextArea(file);
		chatArea.setEditable(false);
		chatArea.setLineWrap(true);
		JScrollPane scrollingChatPanel = new JScrollPane(chatArea);
		scrollingChatPanel.setPreferredSize(new Dimension(300,200));
		messageBox = new JTextField("");
		messageBox.setMinimumSize(new Dimension(100,20));
		messageBox.setPreferredSize(new Dimension(200,20));
		messageBox.setMaximumSize(new Dimension(500,20));
		messageBox.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				sendMessage();
			}
		});
		sendButton = new JButton("Send");
		sendButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				sendMessage();
			}
		});
		JPanel panel = new JPanel();
		panel.add(messageBox);
		panel.add(sendButton);
		chatScreen.add(scrollingChatPanel);
		chatScreen.add(panel);
		
		cardsPanel.add(chatScreen, CHAT_SCREEN);
	}
	private static void createAndShowGUI() {
        // Create and set up the window.
		// Avoid statics within game
        ChatterClient window = new ChatterClient("data.cs.purdue.edu", 1500);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setTitle("ChatterBox");
        window.setSize(new Dimension(500,600));
        // TODO auto-save before exiting
        window.pack();
        window.setVisible(true);
    }
	private void switchView(int screen){
		CardLayout cl = (CardLayout)(cardsPanel.getLayout());
        switch(screen){
        case 0:
        	cl.show(cardsPanel, LOGIN_SCREEN);
        	break;
        case 1:
        	cl.show(cardsPanel, CHAT_SCREEN);
        	break;        	
        }
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
            //UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });

	}
	private void loadOptions(){
		try{
			
            File file = new File("config.ini");
            FileInputStream fis = new FileInputStream(file);
    		ObjectInputStream in = new ObjectInputStream(fis);
			showTime = (Boolean)in.readObject();
			//defaultusername = (String)in.readObject();
			in.close();
			fis.close();
			
		}
		catch(IOException ie){
			System.out.println(ie + "" );
		}
		catch(ClassNotFoundException e){
			System.out.println(e + "" );
		}
		catch(Exception e){
			System.out.println(e);
			
		}
	}
}

class CTextArea extends JTextArea{
	/**
	 * 
	 */

	private static final long serialVersionUID = -217191299156683782L;
	private BufferedImage bufferedImage;
	private TexturePaint texturePaint;
	CTextArea(File file){
		super();
		try{
			bufferedImage = ImageIO.read(file);
		    Rectangle rect = new Rectangle(0, 0, bufferedImage.getWidth(null), bufferedImage.getHeight(null));
		    texturePaint = new TexturePaint(bufferedImage, rect);
		    setOpaque(false);
		}
		catch(IOException ie){
			System.out.println(ie);
			append("Background Media Not Found: " + file + "\n");
		}
	    
	}
	 public void paintComponent(Graphics g)
	  {
		 if(bufferedImage!= null){
			 Graphics2D g2 = (Graphics2D) g;
			 g2.setPaint(texturePaint);
		 }
	    
	    g.fillRect(0, 0, getWidth(), getHeight());
	    super.paintComponent(g);
	  }
}
class OptionsWindow extends JFrame implements Serializable{
	/**
	 * 
	 */

	private boolean showTime = true;
	private String defaultusername = "username";
	ButtonGroup showTimeGroup = new ButtonGroup();
	JRadioButton showTimeOn = new JRadioButton("Yes");
	JRadioButton showTimeOff = new JRadioButton("No");
	JTextField defaultnameField = new JTextField(defaultusername);
	private static final long serialVersionUID = 2748129716144166752L;
	JPanel mainpanel = new JPanel();
	OptionsWindow(){
		loadOptions();
		showTimeOn.setSelected(showTime);
		showTimeOff.setSelected(!showTime);
		defaultnameField.setText(defaultusername);
		mainpanel.setLayout(new BoxLayout(mainpanel, BoxLayout.Y_AXIS));
		JPanel pane[] = new JPanel[10];
		for(int x = 0; x < 10; x++){
			pane[x] = new JPanel();
			pane[x].setPreferredSize(new Dimension(300,20));
			mainpanel.add(pane[x]);
		}

		showTimeGroup.add(showTimeOn);
		showTimeGroup.add(showTimeOff);
		JLabel showTimeLabel = new JLabel("Show time of message? ");
		JLabel setDefaultUsername = new JLabel("Set default username: ");
		pane[0].add(new JLabel("OPTIONS"));
		pane[1].add(showTimeLabel);
		pane[1].add(showTimeOn);
		pane[1].add(showTimeOff);
		pane[2].add(setDefaultUsername);
		pane[2].add(defaultnameField);
		JButton save = new JButton("Save");
		save.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				saveOptions();
			}
		});
		pane[8].add(save);
		mainpanel.setPreferredSize(new Dimension(300,300));
		setSize(300,300);
		add(mainpanel);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		pack();

	}
	private void saveOptions(){
		//add serializable shit here\
		//file == config.ini
		showTime = showTimeOn.isSelected();
		defaultusername = defaultnameField.getText();
		try{
    		FileOutputStream fos = new FileOutputStream(new File("config.ini"));
    		ObjectOutputStream out = new ObjectOutputStream(fos);
    		out.writeObject(showTime);
    		out.writeObject(defaultusername);
    		out.close();
    		fos.close();
		}
		catch(IOException ie){
			System.out.println("" + ie);
		}
		this.dispose();
		
	}
	private void loadOptions(){
		try{
			
            File file = new File("config.ini");
            FileInputStream fis = new FileInputStream(file);
    		ObjectInputStream in = new ObjectInputStream(fis);
			showTime = (Boolean)in.readObject();
			defaultusername = (String)in.readObject();
			in.close();
			fis.close();
			
		}
		catch(IOException ie){
			System.out.println(ie + "" );
		}
		catch(ClassNotFoundException e){
			System.out.println(e + "" );
		}
		catch(Exception e){
			System.out.println(e);
			
		}
	}
}