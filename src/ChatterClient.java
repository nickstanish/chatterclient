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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
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
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

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
	private JLabel isTypingLabel, loginErrorLabel;
	private JButton loginButton, advancedButton, sendButton, resetAdvancedButton;
	private StyledTextPane chatArea;
	private boolean loggedIn = false, realtime = false;
	private JMenuItem logoutMenu;
	private static final String LOGIN_SCREEN = "Login";
	private static final String CHAT_SCREEN = "Chat";
	public static final String DEFAULT_HOST = "vizbits.net";
	private static final int DEFAULT_PORT = 1500;
	private String username, oldRealtime;
	private int port;
	private String hostname;
	private Options options = new Options();
	private boolean isTyping = false;
	private boolean focused; //used to check if window has focus
	private TaskbarManager taskbar;
	// for I/O
	private PrintWriter out;		// to read from the socket
	private BufferedReader in;		// to write on the socket
	private Socket socket;
	private NotificationManager notifier;
	// TODO: remove icon from taskbar at close
	// TODO: logout
	// TODO: redo all message sending operations
	// TODO: login validation check
	
	
	
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
			connectionFailed();
			System.err.println("Error connecting to server:" + ec.getLocalizedMessage());
			//TODO: change show display on login screen
			return false;
		}
		try{
			socket.setKeepAlive(true);
		}
		catch(SocketException e){
			System.err.println(e);
			connectionFailed();
			System.err.println("Unable to set keepalive true");
		}
		/* Creating both Data Stream */
		try{
			in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);
		}
		catch (IOException eIO) {
			connectionFailed();
			System.err.println("Exception creating new Input/output Streams: " + eIO);
			return false;
		}
		// creates the Thread to listen from the server 
		
		// Send our username to the server this is the only message that we
		// will send as a String. All other messages will be ChatMessage objects
		out.println(username);
		try {
			String validation = in.readLine();
			if(validation.charAt(0) == '0'){
				showLoginError("Username taken");
				return false;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			connectionFailed();
			return false;
		}
		new ListenFromServer().start();
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
	private void clearChat(){
		chatArea.setText("");
	}
	
	/**
	 * To send a string to the server to be broadcasted
	 */
	void sendMessage(String s) {
		send('0', s);
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
			if(in != null) in.close();
		}
		catch(Exception e) {} // not much else I can do
		try {
			if(out != null) out.close();
		}
		catch(Exception e) {} // not much else I can do
        try{
			if(socket != null) socket.close();
		}
		catch(Exception e) {} // not much else I can do
			
	}
	private void connectionFailed() {
		showLoginError("Connection Failed");
	}
	/*
	 * a class that waits for the message from the server and append them to the JTextArea
	 * if we have a GUI or simply System.out.println() it in console mode
	 */
	class ListenFromServer extends Thread {
		public void run() {
			while(true) {
				try {
					String line = in.readLine();
					if(line != null){
						switch(line.charAt(0)){
						case '0': // message
							display(line.substring(1));
							break;
						case '2': // istyping
							if(line.charAt(1) == '0') isTypingLabel.setText(" ");
							else isTypingLabel.setText(line.substring(2));
							
							break;
						case 'E': // Error
							display(line.substring(1));
							break;
						default:
							display("  Error:\n " + line);
							break;
						}
					}
				}
				catch(IOException e) {
					System.err.println("Server has close the connection: " + e);
					showLoginError("Server closed connection");
					switchView(0);
					break;
				}
				// can't happen with a String object but need the catch anyhow
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
		 * show login screen without advanced options
		 * require shortcut to show them
		 */
		advancedPanel.setVisible(false);
		advancedButton.setVisible(false);
		cardsPanel.add(loginScreen, LOGIN_SCREEN);
	}
	private void showLoginError(String m){
		loginErrorLabel.setText(m);
		loginErrorLabel.setVisible(true);
		revalidate();
	}
	private void hideLoginError(){
		loginErrorLabel.setVisible(false);
		revalidate();
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
	private void send(){
		String s = messageBox.getText().trim();
		if(!s.equals("")){
			int len = s.length();
			if(len >= 2 && s.substring(0,2).equals("./")){
				display("#: " + s);
				boolean cmd = false;
				// @commands
				if(s.substring(2).equalsIgnoreCase("help")){
					display("whoisin\nlogout\nclear\nrealtime\n");
					cmd = true;
				}
				else if(s.substring(2).equalsIgnoreCase("whoisin")){
					send('4', ".whoisin");
					cmd = true;
				}
				else if(s.substring(2).equalsIgnoreCase("logout")){
					logout();
					cmd = true;
				}
				else if(s.substring(2).equalsIgnoreCase("clear")){
					clearChat();
					cmd = true;
				}
				else if(s.substring(2).equalsIgnoreCase("realtime")){
					realtime = !realtime; // toggle realtime on/off
					send('3', booleanToBit(realtime) + "" );
					cmd = true;
				}
				
				if(!cmd){
					display("'"+ s.substring(2) + "' command not found");
				}
				
			}
			else{
				sendMessage(s);		
			}	
			messageBox.setText("");
		}
		
	}
	private void login(){
		hideLoginError();
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
			return;
		}
		switchView(1);
		loggedIn = true;
		taskbar.login(true);
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		logoutMenu.setEnabled(true);
		messageBox.requestFocusInWindow();
	}
	public void logout(){
		loggedIn = false;
		out.println(1 + "");
		taskbar.login(false);
		clearChat();
		disconnect();
		switchView(0);
		logoutMenu.setEnabled(false);
		loggedIn = false;
		loginBox.requestFocusInWindow();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		hideLoginError();
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
			boolean toggle = true;
			
			@Override
			public void changedUpdate(DocumentEvent e) {}

			@Override
			public void insertUpdate(DocumentEvent e) {
				isTyping();
/* send every other char, works but is unnatural
				if(toggle){
					isTyping();
				}
				toggle = !toggle;
				*/
				
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				isTyping();
/* send every other char
				if(toggle){
					isTyping();
				}
				toggle = !toggle;
				*/
			}
		});
		messageBox.setMinimumSize(new Dimension(100,20));
		messageBox.setPreferredSize(new Dimension(200,30));
		messageBox.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				send();
			}
		});
		sendButton = new JButton("Send");
		sendButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				send();
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
			if(realtime){
				send('2',booleanToBit(isTyping) + fauxDiff(oldRealtime, s));
			}
			else send('2',booleanToBit(isTyping) + "");
			oldRealtime = s;
		}
		
	}
	public static char booleanToBit(boolean b){
		if(b) return '1';
		else return '0';
	}
	private void send(char code, String s){
		out.println(code + s);
	}
	private static void createAndShowGUI() {
        // Create and set up the window.
		// Avoid statics
        ChatterClient window = new ChatterClient("ChatterClient.DEFAULT_HOST", 1500);
        window.setTitle("ChatterBox");
        window.setSize(new Dimension(500,600));
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
		File file = new File("config.ini");
		try{
            FileInputStream fis = new FileInputStream(file);
    		ObjectInputStream in = new ObjectInputStream(fis);
			options = (options.Options)in.readObject();
			in.close();
			fis.close();
			return;
		}
		catch(FileNotFoundException fnf){
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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