import java.awt.AWTException;
import java.awt.AlphaComposite;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.SystemTray;
import java.awt.TexturePaint;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
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
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import Notifications.Note;
import Notifications.NotificationQueue;
import Notifications.NotificationWindow;
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
	private CTextArea chatArea;
	private boolean loggedIn = false;
	JMenuItem logoutMenu;
	private static final String LOGIN_SCREEN = "Login";
	private static final String CHAT_SCREEN = "Chat";
	private static final String DEFAULT_HOST = "data.cs.purdue.edu";
	private static final int DEFAULT_PORT = 1500;
	private String username;
	private int port;
	private String hostname;
	private Options options = new Options();
	private boolean isTyping = false;
	private boolean focused; //used to check if window has focus

	// for I/O
	private ObjectInputStream sInput;		// to read from the socket
	private ObjectOutputStream sOutput;		// to write on the socket
	private Socket socket;
	//system tray
	private SystemTray tray;
	private TrayIcon trayIcon;
	private PopupMenu trayMenu;
	private MenuItem logoutTrayItem, exitTrayItem;
	//notifications
	private MouseListener notificationListener;
	private NotificationWindow notificationWindow;
	private NotificationQueue queue;

		//constructor
	public ChatterClient(String hostname, int portnumber){

		loadOptions();
		notificationListener = new MouseListener(){
			@Override
			public void mouseClicked(MouseEvent arg0) {}
			@Override
			public void mouseEntered(MouseEvent arg0) {}
			@Override
			public void mouseExited(MouseEvent arg0) {}
			@Override
			public void mousePressed(MouseEvent arg0) {
				//open client window
				notificationClicked();
			}
			@Override
			public void mouseReleased(MouseEvent arg0) {}
		};
		notificationWindow = new NotificationWindow();
		notificationWindow.addMouseListener(notificationListener);
		queue = new NotificationQueue(notificationWindow, NotificationQueue.SHOW_ONE_FOR_ALL);
		if(SystemTray.isSupported()){
			tray = SystemTray.getSystemTray();
			BufferedImage image;
			try{
				//message_box_icon
				BufferedImage original = ImageIO.read(new File("media/icons/logo.png"));
				image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
				Graphics2D g = image.createGraphics();
				g.drawImage(original, 0, 0, 16, 16, null);
				g.dispose();
				g.setComposite(AlphaComposite.Src);
				g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			}
			catch(IOException ie){
				System.err.println("error: " + ie);
				image = new BufferedImage(15,15,BufferedImage.TYPE_INT_ARGB);
				Graphics2D g = image.createGraphics();
				g.setColor(Color.blue);
				g.fill(new Rectangle2D.Double(0,0,15,15));
				g.setColor(Color.white);
				g.fill(new Rectangle2D.Double(3,3,9,9));
				g.dispose();
				g.setComposite(AlphaComposite.Src);
				g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			}
			trayMenu = new PopupMenu();
			logoutTrayItem = new MenuItem("Logout");
			logoutTrayItem.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					logout();
				}
			});
			exitTrayItem = new MenuItem("Exit");
			exitTrayItem.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					exit();
				}
			});
			trayMenu.add(logoutTrayItem);
			trayMenu.add(exitTrayItem);
			try {
				trayIcon = new TrayIcon(image, "ChatterBox", trayMenu);
				trayIcon.addMouseListener(new MouseListener(){
					@Override
					public void mouseClicked(MouseEvent arg0) {
						bringToFront();
					}
					@Override
					public void mouseEntered(MouseEvent arg0) {}
					@Override
					public void mouseExited(MouseEvent arg0) {}
					@Override
					public void mousePressed(MouseEvent arg0) {}
					@Override
					public void mouseReleased(MouseEvent arg0) {}
				});
				trayIcon.addActionListener(new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e) {
						bringToFront();		
					}					
				});
				tray.add(trayIcon);
			} catch (AWTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
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
	private void notificationClicked(){
		notificationWindow.setVisible(false);
		queue.clear();
		bringToFront();
	}
	private void changeFocus(boolean x){
		focused = x;
	}
	private void bringToFront(){
		this.setVisible(true);
		this.setState(JFrame.NORMAL);
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
			System.err.println("Error connectiong to server:" + ec);
			//TODO: change show display on login screen
			return false;
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
		chatArea.append(msg + "\n");		// append to the ClientGUI JTextArea (or whatever)
		chatArea.setCaretPosition(chatArea.getDocument().getLength());
		/*
		 * seems like the best way to implement notifications for now
		 */
		//SystemTray.isSupported() for tray
		if(this.getState() == JFrame.ICONIFIED || !this.isVisible() || !focused){
			//trayIcon.displayMessage("New ChatterBox Message", "Yeah you got a message...", TrayIcon.MessageType.NONE);
			queue.add(new Note("ChatterBox: ", "New Message"));
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
					ChatMessage message = (ChatMessage)sInput.readObject();
					if(message.getType() == ChatMessage.MESSAGE){
						display(message.getMessage());
					}
					else if(message.getType() == ChatMessage.TYPING){
						//TODO: display that the person is typing
						if(message.getTyping()){
							isTypingLabel.setText(message.getUsername() + " is typing");
						}
						else{
							isTypingLabel.setText("");
						}
						
					}
				}
				catch(IOException e) {
					System.err.println("Server has close the connection: " + e);
					break;
				}
				// can't happen with a String object but need the catch anyhow
				catch(ClassNotFoundException e2) {
				}
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
		filemenu.add(exitMenu);
		menubar.add(filemenu);
		setJMenuBar(menubar);
	}
	private void exit(){
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
	private void sendMessage(){
		if(!messageBox.getText().trim().equals("")){
			String s = messageBox.getText().trim();
			if(s.equalsIgnoreCase("whoisin")){
				sendMessage(new ChatMessage(ChatMessage.WHOISIN, ""));	
			}
			else{
				sendMessage(new ChatMessage(ChatMessage.MESSAGE, s));		
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
	private void logout(){
		switchView(0);
		sendMessage(new ChatMessage(ChatMessage.LOGOUT, ""));
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
		chatArea = new CTextArea(file);
		chatArea.setEditable(false);
		chatArea.setLineWrap(true);
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
			isTyping = !messageBox.getText().equals("");
			sendMessage(new ChatMessage(ChatMessage.TYPING, isTyping));	
		}
		
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
			options = (Options)in.readObject();
			in.close();
			fis.close();
			return;
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
		options = new Options();
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
class OptionsPanel extends JPanel{
	private static final long serialVersionUID = 2748129716144166752L;
	public Options options;
	public ButtonGroup showTimeGroup = new ButtonGroup();
	public JRadioButton showTimeOn, showTimeOff;
	public JTextField defaultNameField;
	public JTextField passwordField;
	
	OptionsPanel(Options opt){
		options = opt;
		showTimeOn = new JRadioButton("Yes" , options.showTime);
		showTimeOff = new JRadioButton("No" , !options.showTime);
		defaultNameField = new JTextField(15);
		defaultNameField.setText(options.defaultUsername);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		JPanel pane[] = new JPanel[4];
		for(int x = 0; x < 4; x++){
			pane[x] = new JPanel();
			add(pane[x]);
			pane[x].setLayout(new BoxLayout(pane[x], BoxLayout.X_AXIS));
			pane[x].setPreferredSize(new Dimension(300,20));
			pane[x].setMaximumSize(new Dimension(300,20));
		}
		showTimeGroup.add(showTimeOn);
		showTimeGroup.add(showTimeOff);
		JLabel showTimeLabel = new JLabel("Show message time?  ");
		JLabel setDefaultUsername = new JLabel("Username:  ");
		pane[0].add(showTimeLabel);
		pane[0].add(showTimeOn);
		pane[0].add(showTimeOff);
		pane[1].add(setDefaultUsername);
		pane[1].add(defaultNameField);
		//add "*"'s for the characters in the password box for legitimacy mainly cuz its badass
		//add remember "Login Button" --radio buttons
		setPreferredSize(new Dimension(300,150));

	}
	public void saveOptions(){
		//add serializable shit here\
		//file == config.ini
		options.showTime = showTimeOn.isSelected();
		options.defaultUsername = defaultNameField.getText().trim();
		try{
    		FileOutputStream fos = new FileOutputStream(new File("config.ini"));
    		ObjectOutputStream out = new ObjectOutputStream(fos);
    		out.writeObject(options);
    		out.close();
    		fos.close();
		}
		catch(IOException ie){
			System.out.println("" + ie);
		}
		
	}
}
class Options implements Serializable{
	/**
	 * options contained in here
	 */
	private static final long serialVersionUID = 1L;
	public String defaultUsername;
	public boolean showTime;
	Options(){
		defaultUsername = "username";
		showTime = true;
	}
}