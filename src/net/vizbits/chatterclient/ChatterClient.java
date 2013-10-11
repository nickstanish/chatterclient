package net.vizbits.chatterclient;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.text.SimpleAttributeSet;

import net.vizbits.chatterclient.network.Client;
import net.vizbits.chatterclient.network.UsernameTakenException;
import net.vizbits.chatterclient.options.Options;
import net.vizbits.chatterclient.options.OptionsPanel;
import net.vizbits.chatterclient.tabbedchat.TabbedChatScreen;
import net.vizbits.chatterclient.tabbedchat.TabbedPanel;
import net.vizbits.chatterclient.tabbedchat.style.StylePane;
import net.vizbits.chatterlib.ExpandedStyle;
import net.vizbits.chatterlib.Message;

public class ChatterClient extends Client{
	/**
	 * GUI based client for vizbits chatterbox instant messenger
	 * Use JDK and JRE 7!
	 * @author: Nick Stanish
	 * @author: Joey Imburgia
	 */
	/*
	 * TODO: notify tab of new messages, only show other contacts in tab title, notify of client offline, fix duplicate convos.
	 * ie. {nick, joey} is different then {joey,nick} according to Arrays.equal
	 * if array1.contains(array2) and length = length, then same
	 */
	public static final long serialVersionUID = -7269347532987537692L;
	private JFrame window;
	private JPanel mainPanel, topPanel, bottomPanel,cardsPanel, loginScreen, advancedPanel;
	private JTextField loginBox, serverBox, portBox;
	private JPasswordField passwordBox;
	private ContactList contactsList;
	private JLabel isTypingLabel, loginErrorLabel;
	private JButton loginButton, advancedButton, resetAdvancedButton;
	private boolean loggedIn = false, realtime = false, isTyping = false;
	private JMenuItem logoutMenu;
	private TabbedChatScreen chatScreen;
	/*
	 * properties
	 */
	private static final String LOGIN_SCREEN = "Login";
	private static final String CHAT_SCREEN = "Chat";
	public static final String DEFAULT_HOST = "vizbits.net";
	private static final int DEFAULT_PORT = 1500;
	
	private String host = DEFAULT_HOST;
	private int port = DEFAULT_PORT;
	public String username;
	private String oldRealtime;
	private Options options = new Options();
	private TaskbarManager taskbar;
	
	public enum DisplayStyle{
		Message, Notice, Error, Command
	}

	private NotificationManager notifier;
	/*
	 * bugs to fix: 
	 * threads don't synchro connectionslist
	 * slow sending/receieving
	 * error with username in use when it isnt
	 * net.vizbits.chatterclient.contacts list
	 * ui
	 * validate login for illegal chars
	 */
	
	
		//constructor
	public ChatterClient(String host, int port){
		super();
		this.host = host;
		this.port = port;
		window = new JFrame();
		loadOptions();
		taskbar = new TaskbarManager(this);
		notifier = new NotificationManager(this);
		Container contentPane = window.getContentPane();
		mainPanel = new JPanel();
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		createMenu();
		window.setMinimumSize(new Dimension(300,500));
		//font
		Font font1 = new Font("sansserif", Font.BOLD, 48);
		Font font;
		try{
			File fontFile = new File("media/fonts/Sansation/Sansation_Light_Italic.ttf");
			font = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(Font.PLAIN,48);
		}
		catch(IOException | FontFormatException ie){
			System.err.println(ie);
			font = font1;
		}
		JLabel titleLabel = new JLabel("ChatterBox");
		titleLabel.setFont(font);
		titleLabel.setForeground(new Color(0x6BE400));
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
		contentPane.add(mainPanel);
		loginBox.requestFocusInWindow();
		/*
		 * keyboard shortcuts
		 * first connect the key to an actionmap
		 * then create an action
		 * then connect actionmap to the action
		 */
		
		// CTRL + L to login advanced
		mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK), "advanced");
		AbstractAction advancedAction = new AbstractAction(){
			public void actionPerformed(ActionEvent e){
				advancedLoginShortcut();
			}
		};
		mainPanel.getActionMap().put( "advanced", advancedAction );
		window.setTitle("ChatterBox");
        window.setSize(new Dimension(500,600));
        window.pack();
        window.setVisible(true);
		if(!isInternetReachable()){
			showLoginError("Default server not available");
		}
	}
	private void advancedLoginShortcut(){
		if(showAdvancedOptions()){
			serverBox.setText("localhost");
			loginBox.setText("tester_");
		}
		else{
			resetAdvancedOptions();
			loginBox.setText(options.defaultUsername);
		}
		
		loginBox.requestFocusInWindow();
		
	}
	public void bringToFront(){
		window.setVisible(true);
		window.setState(JFrame.NORMAL);
		if(loggedIn){
			chatScreen.requestSendBoxFocus(null);
		}
		else{
			loginBox.requestFocusInWindow();
		}
		
	}
	/**
	 * connects to the server
	 * @return true on success
	 */
	public boolean start(String _username, String _host, int _port) {
		
		hideLoginError();
		try {
			return connect(_username, _host, _port);
		} catch ( IOException | UsernameTakenException e) {
			if(e instanceof UsernameTakenException){
				showLoginError("Username taken.");
			}
			System.err.println(e.toString());
			return false;
		}
	}
	/**
	 * display a message
	 */
	public void display(String[] contacts, String msg, SimpleAttributeSet style) {
		if(!options.showTime){
			//fix whoisin time parse bug with a start of line regex - 9/2/12
			msg = msg.replaceFirst("^(\\d{1,2}:\\d{1,2}:\\d{1,2}[:\\s]{1,2})", "");
		}
		System.out.println(style);
		chatScreen.displayMessage(contacts, msg, style);		
		
		/*
		 * seems like the best way to implement net.vizbits.chatterclient.notifications for now
		 */
		//SystemTray.isSupported() for tray
		if(window.getState() == JFrame.ICONIFIED || !window.isVisible()){
			//trayIcon.displayMessage("Chatterbox", "You got a message...", TrayIcon.MessageType.NONE);
			notifier.notify("ChatterBox: ", "New Message");
			//System.out.println("should have gotten a notification around now");
		}
		

	}
	private void clearChat(String[] _contacts){
		chatScreen.clearChat(_contacts);
	}
	private void sendCommand(String message){
		prepareAndSendMessage(Message.Type.Command, null, message, null, false);
	}
	public void requestContacts(){
		prepareAndSendMessage(Message.Type.Contacts, null, null, null, false);
	}
	private void typing(String[] recipients, String message, boolean bool){
		prepareAndSendMessage(Message.Type.Typing, recipients, message, null, bool);
	}
	private void sendMessage(String[] recipients, String message, SimpleAttributeSet style){
		prepareAndSendMessage(Message.Type.Message, recipients, message, style, false);
	}
	//sendMessagePane.getStylePane().requestFocusInWindow();
	public static boolean validateChars(String s){
		return s.matches("[A-Za-z0-9\\-\\_\\.\\s]+");
	}
	private void showOptions(){
		// TODO: pretty much done, optimize at bottom
		// Maybe refresh fields if any
		OptionsPanel optionsPanel = new OptionsPanel(options);
		int answer = JOptionPane.showConfirmDialog(window, optionsPanel, "Options", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);  
		if (answer == JOptionPane.OK_OPTION){
			optionsPanel.saveOptions();
			loadOptions();
			} 
		
	}
	public JFrame getFrame(){
		return window;
	}
	public void interpretMessage(String s, SimpleAttributeSet style){
		if(!s.equals("")){
			TabbedPanel tp = (TabbedPanel) chatScreen.getSelectedComponent();
			String[] convo = tp.contacts;
			int len = s.length();
			if(len >= 2 && s.substring(0,2).equals("./")){
				
				display(convo,"#: " + s, null);
				boolean cmd = false;
				// @commands
				switch(s.substring(2).toLowerCase()){
					case "help":
						display(convo,"whoisin\nlogout\nclear\nrealtime\nto [user]:[message]\n", null);
						cmd = true;
						break;
					case "logout":
						logout();
						cmd = true;
						break;
					case "clear":
						chatScreen.clearChat(chatScreen.getSelectedIndex());
						cmd = true;
						break;
					case "realtime":
						realtime = !realtime; // toggle realtime on/off
						display(convo,"Realtime " + booleanValue(realtime) , null);
						cmd = true;
						break;
					default:
							display(convo,"  Incorrect Syntax", null);
							cmd = true;
						if(!cmd){
							display(convo,"'"+ s.substring(2) + "' command not found" , null);
						}
						break;
				}
			}
			else{
				if(convo == null) sendMessage(null, s, style);
				else sendMessage(convo, s, style);
			}
		}
	}
	private void createMenu(){
		JMenuBar menubar = new JMenuBar();
		JMenu filemenu = new JMenu("File");
		JMenu helpmenu = new JMenu("Help");
		JMenuItem optionsMenu = new JMenuItem("Options");
		filemenu.add(optionsMenu);
			
		optionsMenu.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				showOptions();
			}
		});
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
				exit();
			}
		});
		JMenuItem aboutMenuItem = new JMenuItem("About");
		aboutMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				showAboutWindow();
			}
		});
		helpmenu.add(aboutMenuItem);
		filemenu.add(exitMenu);
		menubar.add(filemenu);
		menubar.add(helpmenu);
		window.setJMenuBar(menubar);
	}
	public void showAboutWindow(){
		new AboutWindow(window);
	}
	public static String fauxDiff(String a, String b){
		String similar = "";
		if(a == null || b == null) return null;
		if(a.equals(b)) return "0";
		int len = Math.min(a.length(), b.length());
		for(int i = 0; i < len; i++){
			if(a.charAt(i) == b.charAt(i)){
				similar += a.charAt(i);
				continue;
			}
			break;
		}
		if(a.equals(similar)){
			similar = "+" + b.substring(a.length());
		}
		else{
			similar = "-" + a.substring(similar.length()).length();
		}
		return similar;
	}
	
	public void exit(){
		if(loggedIn){
			logout();
		}
		taskbar.kill();
		window.dispose();
		System.exit(0);
	}
	private void createLoginScreen(){
		loginScreen = new JPanel();
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		loginBox = new JTextField(options.defaultUsername,20);
		passwordBox = new JPasswordField("titties", 20); //PasswordField
		/*
		 * use getPassword to get the password instead of getText
		 * this returns a char array
		 * this focus listener clears the password field when you click it
		 * then if you don't type anything, it will bring the password back when you
		 * remove focus
		 */
		passwordBox.addFocusListener(new FocusListener(){
			char[] text;
			@Override
			public void focusGained(FocusEvent e) {
				JPasswordField p =  (JPasswordField)e.getSource();
				text = p.getPassword();
				p.setText("");
			}
			@Override
			public void focusLost(FocusEvent e) {
				JPasswordField p =  (JPasswordField)e.getSource();
				String s = "";
				for(int i = 0; i<text.length; i++){
					s += text[i];
				}
				if(p.getPassword().length == 0){
					p.setText(s);
				}
				/*
				 * security measures
				 */
				s = null;
				Arrays.fill(text, '0');
			}
		});
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
		labelPanel.add(new JLabel("Login to start chatting") );
		JPanel errorPanel = new JPanel();
		loginErrorLabel = new JLabel("Error");
		loginErrorLabel.setForeground(Color.red);
		errorPanel.add(loginErrorLabel);
		loginErrorLabel.setVisible(false);
		panel.add(labelPanel);
		panel.add(errorPanel);
		panel.add(Box.createRigidArea(new Dimension(0,50)));
		JPanel usernamePanel = new JPanel(new GridLayout(0,1));
		usernamePanel.setMaximumSize(new Dimension(200,100));
		//usernamePanel.add(new JLabel("Username:"));
		usernamePanel.add(loginBox);
		usernamePanel.add(Box.createRigidArea(new Dimension(0,3)));
		passwordBox.setEnabled(false);
		usernamePanel.add(passwordBox);
		panel.add(usernamePanel);
		panel.add(Box.createRigidArea(new Dimension(0,10)));
		
		//Adding of the Checkbox Panel
		JPanel checkboxPanel = new JPanel(new GridLayout(0,1));
		checkboxPanel.setBorder(new EmptyBorder(5, 10, 5, 0) );
		JCheckBox rememPass = new JCheckBox("remember password (soon)");
		JCheckBox autoLog = new JCheckBox("login automatically (soon)");
		
		checkboxPanel.add(rememPass);
		checkboxPanel.add(autoLog);
		panel.add(checkboxPanel);
		panel.add(Box.createRigidArea(new Dimension(0,10)));
		
		//JPanel buttonPanel = new JPanel();
		JPanel buttonPanel = new JPanel(new GridLayout(0,1));
		buttonPanel.add(loginButton);
		buttonPanel.add(Box.createRigidArea(new Dimension(0,5)));
		buttonPanel.add(advancedButton);
		panel.add(buttonPanel);		
		advancedPanel = new JPanel(new GridLayout(0,1));
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
		advancedPanel.setMaximumSize(new Dimension(200,150));
		advancedPanel.setPreferredSize(new Dimension(200,130));
		panel.add(advancedPanel);
		panel.add(Box.createRigidArea(new Dimension(60,300)));
		loginScreen.setPreferredSize(new Dimension(250,350));
		loginScreen.add(panel);
		/*
		 * show login screen without advanced net.vizbits.chatterclient.options
		 * require shortcut to show them
		 */
		advancedPanel.setVisible(false);
		advancedButton.setVisible(false);
		cardsPanel.add(loginScreen, LOGIN_SCREEN);
	}
	private void showLoginError(String m){
		loginErrorLabel.setText(m);
		loginErrorLabel.setVisible(true);
		window.revalidate();
	}
	private void hideLoginError(){
		loginErrorLabel.setVisible(false);
		window.revalidate();
	}
	private void resetAdvancedOptions(){
		serverBox.setText(DEFAULT_HOST + "");
		portBox.setText(DEFAULT_PORT + "");
	}
	private boolean showAdvancedOptions(){
		if (advancedButton.getText().equalsIgnoreCase("Hide Advanced")){
			advancedButton.setText("Advanced");
			advancedPanel.setVisible(false);
			advancedButton.setVisible(false);
			return false;
		}
		else{
			advancedButton.setText("Hide Advanced");
			advancedPanel.setVisible(true);
			advancedButton.setVisible(true);
			return true;
		}
		
		
	}
	public static boolean isInternetReachable(){
        try {
            //make a URL to a known source
            URL url = new URL("http://vizbits.net");
            //open a connection to that source
            HttpURLConnection urlConnect = (HttpURLConnection)url.openConnection();
            urlConnect.setConnectTimeout(2000);
            //trying to retrieve data from the source. If there
            //is no connection, this line will fail
            urlConnect.getContent();

        } catch (UnknownHostException e) {
            return false;
        }
        catch (IOException e) {
            return false;
        }
        return true;
    }
	public static String booleanValue(boolean b){
		if (b) return "on";
		else   return "off";
	}
	private void login(){
		hideLoginError();
		username = loginBox.getText().trim();
		if(username.equals("") || username == null){
			showLoginError("You must enter a username.");
			return;
			
		}
		if(!validateChars(username)){
			showLoginError("Username has illegal characters.");
			return;
			
		}
		if (advancedButton.getText().equalsIgnoreCase("Hide Advanced")){
			port = Integer.valueOf(portBox.getText());
			host = serverBox.getText();
		}
		else{
			port = DEFAULT_PORT;
			host = DEFAULT_HOST;
		}		
		if(!start(username, host, port)){
			disconnect();
			return;
		}
		switchView(1);
		loggedIn = true;
		taskbar.login(true);
		window.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		logoutMenu.setEnabled(true);
		chatScreen.requestSendBoxFocus(null);
		contactsList.startPolling();
		
	}
	public void logout(){
		loggedIn = false;
		contactsList.kill();
		taskbar.login(false);
		chatScreen.removeAll();
		chatScreen.newConversation(null);
		disconnect();
		switchView(0);
		logoutMenu.setEnabled(false);
		loggedIn = false;
		loginBox.requestFocusInWindow();
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		hideLoginError();
	}

	private void createChatScreen(){
		contactsList = new ContactList(this);
		chatScreen = new TabbedChatScreen(this);
		JSplitPane container = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,contactsList, chatScreen);
		container.setOneTouchExpandable(true);
		container.setDividerLocation(0);
		cardsPanel.add(container, CHAT_SCREEN);
	}
	
	public static char booleanToBit(boolean b){
		if(b) return '1';
		else return '0';
	}
	public static boolean bitToBoolean(char c){
		return (c != '0');
	}
	private static void createAndShowGUI() {
        // Create and set up the window.
        new ChatterClient("ChatterClient.DEFAULT_HOST", 1500);
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
		try {
			UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });

	}
	private void loadOptions(){
		File file = new File("config.ini");
		try{
            FileInputStream fis = new FileInputStream(file);
    		ObjectInputStream in = new ObjectInputStream(fis);
			options = (Options)in.readObject();
			in.close();
			fis.close();
			return;
		}
		catch( IOException | ClassNotFoundException e){
			try {
				file.createNewFile();
			} catch (IOException ie) {
				ie.printStackTrace();
			}
		}
		catch(Exception e){
			System.err.println(e);
		}
		options = new Options();
	}
	@Override
	public void messageHandler(Message m ) {
		System.out.print(m.type.name());
		switch(m.type){
		case Contacts:
			contactsList.update(m.message.split(","));
			break;
		case Error:
			display(m.contacts,m.message, null);
			break;
		case Command:
			display(m.contacts,m.message, null);
			break;
		case Message:
			ExpandedStyle s = m.style;
			SimpleAttributeSet set =  StylePane.constructStyle(s.c, s.b, s.i,s.u, s.s) ;
			display(m.contacts,m.username + ": " + m.message, set);
			break;
		case Typing:
			isTypingLabel.setText(m.message);
			break;
		default:
			display(m.contacts,"  Error:\n " + m.message, null);
			break;
		}
				
	}
	/**
	 * TODO: implement
	 * @param username2
	 */
	public void newConversation(String[] username) {
		chatScreen.setSelectedComponent(chatScreen.findConversation(username));
	}
}