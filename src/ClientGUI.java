

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
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

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;



/*
 * The Client with its GUI
 */
public class ClientGUI extends JFrame implements ActionListener {
	File bgImage = new File("media/background.png");
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

	private static final long serialVersionUID = 1L;
	// show time in message!
	// 
	OptionsWindow optionsWindow = new OptionsWindow();
	private boolean showTime = false;
	// will first hold "Username:", later on "Enter message"
	private JLabel label;
	private String defaultusername = "username";
	// to hold the Username and later on the messages
	private JTextField tf;
	// to hold the server address an the port number
	private JTextField tfServer, tfPort;
	// to Logout and get the list of the users
	private JButton login, logout, whoIsIn;
	// for the chat room
	private CTextArea ta;
	// if it is for connection
	private boolean connected;
	// the Client object
	private Client client;
	// the default port number
	private int defaultPort;
	private String defaultHost;

	// Constructor connection receiving a socket number
	ClientGUI(String host, int port) {

		super("ChatterBox Client");
		defaultPort = port;
		defaultHost = host;
		JMenuBar menubar = new JMenuBar();
		JMenu filemenu = new JMenu("File");
		menubar.add(filemenu);
		loadOptions();
				//Options Button for the "File" MenuBar
		JMenuItem item1 = new JMenuItem("Options");
		item1.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				optionsWindow.setVisible(true);
			}});
		
		JMenuItem item2 = new JMenuItem("Refresh Options");
		item2.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				loadOptions();
			}});
				//Exit Button for the "File" Menu
		JMenuItem item3 = new JMenuItem("Exit");
		item3.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				dispose();
		        System.exit(0);
				}
		});
		
		
		filemenu.add(item1);
		filemenu.add(item2);
		filemenu.add(item3);
		setJMenuBar(menubar);
		// The NorthPanel with:
		JPanel northPanel = new JPanel(new GridLayout(3,1));
		// the server name anmd the port number
		JPanel serverAndPort = new JPanel(new GridLayout(1,5, 1, 3));
		// the two JTextField with default value for server address and port number
		tfServer = new JTextField(host);
		tfPort = new JTextField("" + port);
		tfPort.setHorizontalAlignment(SwingConstants.RIGHT);

		serverAndPort.add(new JLabel("Server Address:  "));
		serverAndPort.add(tfServer);
		serverAndPort.add(new JLabel("Port Number:  "));
		serverAndPort.add(tfPort);
		serverAndPort.add(new JLabel(""));
		// adds the Server an port field to the GUI
		northPanel.add(serverAndPort);

		// the Label and the TextField
		label = new JLabel("Enter your username above", SwingConstants.CENTER);
		//northPanel.add(label);
		tf = new JTextField(defaultusername);
		tf.setBackground(Color.WHITE);
		//northPanel.add(tf);
		add(northPanel, BorderLayout.NORTH);

		// The CenterPanel which is the chat room
		ta = new CTextArea(bgImage);
		ta.append("Welcome to ChatterBox\n");
		ta.setLineWrap(true);
		JPanel centerPanel = new JPanel(new GridLayout(1,1));
		centerPanel.add(new JScrollPane(ta));
		ta.setEditable(false);
		add(centerPanel, BorderLayout.CENTER);

		// the 3 buttons
		login = new JButton("Login");
		login.addActionListener(this);
		logout = new JButton("Logout");
		logout.addActionListener(this);
		logout.setEnabled(false);		// you have to login before being able to logout
		whoIsIn = new JButton("Who is in");
		whoIsIn.addActionListener(this);
		whoIsIn.setEnabled(false);		// you have to login before being able to Who is in

		JPanel southPanel = new JPanel();
		southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
		JPanel pane1 = new JPanel();
		pane1.setLayout(new BoxLayout(pane1, BoxLayout.Y_AXIS));
		pane1.add(tf);
		pane1.add(label);
		JPanel pane2 = new JPanel();
		pane2.setLayout(new BoxLayout(pane2, BoxLayout.X_AXIS));
		southPanel.add(pane1);
		pane2.add(login);
		pane2.add(logout);
		pane2.add(whoIsIn);
		southPanel.add(pane2);
		add(southPanel, BorderLayout.SOUTH);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(600, 600);
		setVisible(true);
		tf.requestFocus();

	}

	// called by the Client to append text in the TextArea 
	void append(String str) {
		if(!showTime){
			str = str.replaceFirst("\\d{1,2}:\\d{1,2}:\\d{1,2}[:\\s]{1,2}", "");
		}
		
		ta.append(str);
		ta.setCaretPosition(ta.getText().length() - 1);
	}
	// called by the GUI is the connection failed
	// we reset our buttons, label, textfield
	void connectionFailed() {
		login.setEnabled(true);
		logout.setEnabled(false);
		whoIsIn.setEnabled(false);
		label.setText("Enter your username above");
		tf.setText(defaultusername);
		// reset port number and host name as a construction time
		tfPort.setText("" + defaultPort);
		tfServer.setText(defaultHost);
		// let the user change them
		tfServer.setEditable(false);
		tfPort.setEditable(false);
		// don't react to a <CR> after the username
		tf.removeActionListener(this);
		connected = false;
	}
		
	/*
	* Button or JTextField clicked
	*/
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		// if it is the Logout button
		if(o == logout) {
			client.sendMessage(new ChatMessage(ChatMessage.LOGOUT, ""));
			return;
		}
		// if it the who is in button
		if(o == whoIsIn) {
			client.sendMessage(new ChatMessage(ChatMessage.WHOISIN, ""));				
			return;
		}

		// ok it is coming from the JTextField
		if(connected) {
			// just have to send the message
			client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, tf.getText()));				
			tf.setText("");
			return;
		}
		

		if(o == login) {
			// ok it is a connection request
			String username = tf.getText().trim();
			// empty username ignore it
			if(username.length() == 0)
				return;
			// empty serverAddress ignore it
			String server = tfServer.getText().trim();
			if(server.length() == 0)
				return;
			// empty or invalid port numer, ignore it
			String portNumber = tfPort.getText().trim();
			if(portNumber.length() == 0)
				return;
			int port = 0;
			try {
				port = Integer.parseInt(portNumber);
			}
			catch(Exception en) {
				return;   // nothing I can do if port number is not valid
			}

			// try creating a new Client with GUI
			client = new Client(server, port, username, this);
			// test if we can start the Client
			if(!client.start()) 
				return;
			tf.setText("");
			label.setText("Enter your message above");
			connected = true;
			
			// disable login button
			login.setEnabled(false);
			// enable the 2 buttons
			logout.setEnabled(true);
			whoIsIn.setEnabled(true);
			// disable the Server and Port JTextField
			tfServer.setEditable(false);
			tfPort.setEditable(false);
			// Action listener for when the user enter a message
			tf.addActionListener(this);
		}

	}

	// to start the whole thing the server
	public static void main(String[] args) {
		new ClientGUI("data.cs.purdue.edu", 1500);
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