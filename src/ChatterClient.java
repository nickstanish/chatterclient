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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.SocketException;
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
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import options.Options;
import options.OptionsPanel;
import style.StyledTextPane;

class ChatterClient extends JFrame{
	/**
	 *
	 */
	private static final long serialVersionUID = -7269347532987537692L;
	private JPanel mainPanel, topPanel, bottomPanel,cardsPanel, loginScreen, chatScreen, advancedPanel;
	private JTextField loginBox, messageBox, serverBox, portBox;
	private JPasswordField passwordBox;
	private File file;
	private JLabel isTypingLabel;
	private JButton loginButton, advancedButton, sendButton, resetAdvancedButton;
	private StyledTextPane chatArea;
	private boolean loggedIn = false;
	private JMenuItem logoutMenu;
	private static final String LOGIN_SCREEN = "Login";
	private static final String CHAT_SCREEN = "Chat";
	public static final String DEFAULT_HOST = "vizbits.net";
	private static final int DEFAULT_PORT = 1500;
	private String username;
	private int port;
	private String hostname;
	private Options options = new Options();
	private boolean isTyping = false;
	private boolean focused; //used to check if window has focus
	private TaskbarManager taskbar;
	// for I/O
	private ObjectInputStream sInput;		// to read from the socket
	private ObjectOutputStream sOutput;		// to write on the socket
	private Socket socket;
	private NotificationManager notifier;

		//constructor
	public ChatterClient(String hostname, int portnumber){

		loadOptions();
		taskbar = new TaskbarManager(this);
		notifier = new NotificationManager(this);
		Container contentPane = getContentPane();
		mainPanel = new JPanel();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		createMenu();
		setMinimumSize(new Dimension(300,500));
		//font
		Font font1 = new Font("sansserif", Font.BOLD, 48);
		Font font;
		try{
			File fontFile = new File("media/fonts/Sansation/Sansation_Light_Italic.ttf");
			font = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(Font.PLAIN,48);
		}
		catch(IOException ie){
			System.err.println(ie);
			font = font1;
		}
		catch(FontFormatException ffe){
			System.err.println(ffe);
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
		
		addWindowFocusListener(new WindowAdapter(){
			public void windowLostFocus(WindowEvent e){
				changeFocus(false);
			}
			public void windowGainedFocus(WindowEvent e){
				changeFocus(true);
			}
		});
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
	private void changeFocus(boolean x){
		focused = x;
	}
	public void bringToFront(){
		setVisible(true);
		setState(JFrame.NORMAL);
		if(loggedIn){
			messageBox.requestFocusInWindow();
		}
		else{
			loginBox.requestFocusInWindow();
		}
		
	}
	public boolean start() {
		// try to connect to the server
		try {
			socket = new Socket(hostname, port);
		} 
		catch(Exception ec) {
			System.err.println("Error connecting to server:" + ec.getLocalizedMessage());
			//TODO: change show display on login screen
			return false;
		}
		try{
			socket.setKeepAlive(true);
		}
		catch(SocketException e){
			System.err.println(e);
			System.err.println("Unable to set keepalive true");
		}
		/* Creating both Data Stream */
		try{
			sInput  = new ObjectInputStream(socket.getInputStream());
			sOutput = new ObjectOutputStream(socket.getOutputStream());
		}
		catch (IOException eIO) {
			System.err.println("Exception creating new Input/output Streams: " + eIO);
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
		if(!options.showTime){
			//fix whoisin time parse bug with a start of line regex - 9/2/12
			msg = msg.replaceFirst("^(\\d{1,2}:\\d{1,2}:\\d{1,2}[:\\s]{1,2})", "");
		}
		chatArea.append(msg, null);		// append to the ClientGUI JTextArea (or whatever)
		chatArea.setCaretPosition(chatArea.getDocument().getLength());
		/*
		 * seems like the best way to implement notifications for now
		 */
		//SystemTray.isSupported() for tray
		if(this.getState() == JFrame.ICONIFIED || !this.isVisible() || !focused){
			//trayIcon.displayMessage("New ChatterBox Message", "Yeah you got a message...", TrayIcon.MessageType.NONE);
			notifier.notify("ChatterBox: ", "New Message");
			//System.out.println("should have gotten a notification around now");
		}
		

	}
	
	/*
	 * To send a message to the server
	 */
	void sendMessage(ChatMessage msg) {
		try {
			sOutput.writeObject(msg);
		}
		catch(IOException e) {
			System.err.println("Exception writing to server: " + e);
		}
	}
	/*
	 * When something goes wrong
	 * Close the Input/Output streams and disconnect not much to do in the catch clause
	 */
	private void disconnect() {
		try{
			if(socket != null) socket.setKeepAlive(false);
		}
		catch(SocketException e){
			System.err.println(e);
			System.err.println("Unable to set keepalive false");
		}
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
	private void connectionFailed() {
		JOptionPane.showMessageDialog(this, "Connection Failed");
	}
	/*
	 * a class that waits for the message from the server and append them to the JTextArea
	 * if we have a GUI or simply System.out.println() it in console mode
	 */
	class ListenFromServer extends Thread {

		public void run() {
			while(true) {
				try {
					ChatMessage message = (ChatMessage)sInput.readObject();
					switch(message.getType()){
						case MESSAGE:
							display(message.getMessage());
							break;
						case TYPING:
							if(message.getTyping()){
								isTypingLabel.setText(message.getFrom() + ": " + message.getMessage());
							}
							else{
								isTypingLabel.setText("");
							}
							break;
					default:
						break;
					
					}
				}
				catch(IOException e) {
					System.err.println("Server has close the connection: " + e);
					break;
				}
				// can't happen with a String object but need the catch anyhow
				catch(ClassNotFoundException e2) {}
			}
		}
	}
	

	/**
	 * @param args
	 */
	/*
	 * use methods to organize everything thats going on since most of it
	 * initializing the GUI
	 */
	private void showOptions(){
		// TODO: pretty much done, optimize at bottom
		// Maybe refresh fields if any
		OptionsPanel optionsPanel = new OptionsPanel(options);
		int answer = JOptionPane.showConfirmDialog(this, optionsPanel, "Options", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);  
		if (answer == JOptionPane.OK_OPTION){
			optionsPanel.saveOptions();
			loadOptions();
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
		setJMenuBar(menubar);
	}
	public void showAboutWindow(){
		new AboutWindow(this);
	}
	public void exit(){
		if(loggedIn){
			logout();
		}
		dispose();
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
		labelPanel.add(new JLabel("Login to start chatting"));
		panel.add(labelPanel);
		panel.add(Box.createRigidArea(new Dimension(0,50)));
		JPanel usernamePanel = new JPanel(new GridLayout(0,1));
		usernamePanel.setMaximumSize(new Dimension(200,100));
		//usernamePanel.add(new JLabel("Username:"));
		usernamePanel.add(loginBox);
		usernamePanel.add(Box.createRigidArea(new Dimension(0,3)));
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
		 * show login screen without advanced options
		 * require shortcut to show them
		 */
		advancedPanel.setVisible(false);
		advancedButton.setVisible(false);
		cardsPanel.add(loginScreen, LOGIN_SCREEN);
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
	private void sendMessage(){
		String s = messageBox.getText().trim();
		if(!s.equals("")){
			int len = s.length();
			if(len >= 2 && s.substring(0,2).equals("./")){
				display("#: " + s);
				boolean cmd = false;
				// @commands
				if(s.substring(2).equalsIgnoreCase("help")){
					display("No help for you!");
					cmd = true;
				}
				if(s.substring(2).equalsIgnoreCase("whoisin")){
					sendMessage(new ChatMessage(ChatMessage.Type.WHOISIN, ""));
					cmd = true;
				}
				if(s.substring(2).equalsIgnoreCase("logout")){
					logout();
					cmd = true;
				}
				
				if(!cmd){
					display("'"+ s.substring(2) + "' command not found");
				}
				
			}
			
			else{
				sendMessage(new ChatMessage(ChatMessage.Type.MESSAGE, s));		
			}	
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
			port = Integer.valueOf(portBox.getText());
			hostname = serverBox.getText();
		}
		else{
			port = DEFAULT_PORT;
			hostname = DEFAULT_HOST;
		}		
		if(!start()){
			disconnect();
			connectionFailed();
			return;
		}
		switchView(1);
		loggedIn = true;
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		logoutMenu.setEnabled(true);
		messageBox.requestFocusInWindow();
	}
	public void logout(){
		switchView(0);
		sendMessage(new ChatMessage(ChatMessage.Type.LOGOUT, ""));
		disconnect();
		logoutMenu.setEnabled(false);
		loggedIn = false;
		loginBox.requestFocusInWindow();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	private void createChatScreen(){
		chatScreen = new JPanel();
		chatScreen.setBackground(Color.white);
		chatScreen.setLayout(new BoxLayout(chatScreen, BoxLayout.PAGE_AXIS));
		isTypingLabel = new JLabel(" ");
		file = new File("media/background.png");
		chatArea = new StyledTextPane();
		//chatArea.setLineWrap(true);
		JScrollPane scrollingChatPanel = new JScrollPane(chatArea);
		scrollingChatPanel.setPreferredSize(new Dimension(300,200));
		messageBox = new JTextField("");
		messageBox.getDocument().addDocumentListener(new DocumentListener(){
			@Override
			public void changedUpdate(DocumentEvent arg0) {
			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				isTyping();
			}
			@Override
			public void removeUpdate(DocumentEvent arg0) {
				isTyping();
			}
		});
		messageBox.setMinimumSize(new Dimension(100,20));
		messageBox.setPreferredSize(new Dimension(200,30));
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
		chatScreen.add(isTypingLabel);
		chatScreen.add(panel);
		
		cardsPanel.add(chatScreen, CHAT_SCREEN);
	}
	private void isTyping(){
		if(loggedIn){
			String s = messageBox.getText().trim();
			int len = s.length();
			if(len == 1 && s.substring(0,1).equals(".")) s = "";
			if(len >= 2 && s.substring(0,2).equals("./")) s = "";
			isTyping = !s.equals("");
			sendMessage(new ChatMessage(ChatMessage.Type.TYPING,isTyping, s, username));	
		}
		
	}
	private static void createAndShowGUI() {
        // Create and set up the window.
		// Avoid statics
        ChatterClient window = new ChatterClient("ChatterClient.DEFAULT_HOST", 1500);
        window.setTitle("ChatterBox");
        window.setSize(new Dimension(500,600));
        window.pack();
        window.setVisible(true);
        /*
		 * server isn't accessible
		 */
		if(!isInternetReachable()){
			JOptionPane.showMessageDialog(window, "Unable to connect to server.\n1. check your internet connection\n2. visit vizbits.net to see if server is available");
		}
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
			options = (options.Options)in.readObject();
			in.close();
			fis.close();
			return;
		}
		catch(IOException ie){
			System.err.println(ie + "" );
		}
		catch(ClassNotFoundException e){
			System.err.println(e);
		}
		catch(Exception e){
			System.err.println(e);
		}
		options = new Options();
	}
}