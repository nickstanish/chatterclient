import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
class P2PClient extends JFrame{
	/**
	 *
	 */
	private static final long serialVersionUID = -7269347532987537692L;
	private boolean loggedIn = false;
	private String username = "nick";
	private int port = 1500;
	private String hostname = "localhost";
	// for I/O
	private ObjectInputStream sInput;		// to read from the socket
	private ObjectOutputStream sOutput;		// to write on the socket
	private Socket socket;

		//constructor
	P2PClient(){

	}
	P2PClient(String host, int port){
		this.hostname = host;
		this.port = port;

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
		System.out.println(msg);
	}
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
	
				}
				catch(IOException e) {
					System.err.println("Server has close the connection: " + e);
					break;
				}
				catch(ClassNotFoundException e) {}
			}
		}
	}
	
	private void sendMessage(String s){
			if(s.equalsIgnoreCase("whoisin")){
				sendMessage(new ChatMessage(ChatMessage.WHOISIN, ""));	
			}
			else{
				sendMessage(new ChatMessage(ChatMessage.MESSAGE, s));		
			}
		
	}
	private void login(){	
		if(!start()){
			disconnect();
			connectionFailed();
			return;
		}
		loggedIn = true;
	}
	private void logout(){
		sendMessage(new ChatMessage(ChatMessage.LOGOUT, ""));
		disconnect();
		loggedIn = false;
	}
	

	
	public static void main(String[] args) {
		P2PClient c = new P2PClient();
		c.login();
		c.sendMessage("hey sup");
		Scanner s = new Scanner(System.in);
		c.sendMessage(s.nextLine());
		s.close();
		c.logout();
	}

	
}

