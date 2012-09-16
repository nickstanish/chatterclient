
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

/* cli server */
public class Server {
	private static int connectionId;
	// an ArrayList to keep the list of the Client
	private ArrayList<ClientThread> al;
	private double version = 1.1;
	// to display time
	private SimpleDateFormat dateFormat;
	private int port;
	// the boolean that will be turned of to stop the server
	private boolean keepGoing;

	/*
	 *  server constructor that receive the port to listen to for connection as parameter
	 *  in console
	 */
	public Server(int port) {
		this.port = port;
		dateFormat = new SimpleDateFormat("h:mm:ss:");
		// ArrayList for the Client list
		al = new ArrayList<ClientThread>();
	}
	
	public void start() {
		keepGoing = true;
		System.out.println("Starting ChatterBox Server " + version);
		/* create socket server and wait for connection requests */
		try {
			// the socket used by the server
			ServerSocket serverSocket = new ServerSocket(port);
			// infinite loop to wait for connections
			while(keepGoing) 
			{
				// format message saying we are waiting
				display("Server waiting for Clients on port " + port + ".");
				Socket socket = serverSocket.accept();  	// accept connection
				// if I was asked to stop
				if(!keepGoing)
					break;
				ClientThread t = new ClientThread(socket);  // make a thread of it
				al.add(t);									// save it in the ArrayList
				t.start();
			}
			// I was asked to stop
			try {
				serverSocket.close();
				for(int i = 0; i < al.size(); ++i) {
					ClientThread tc = al.get(i);
					try {
					tc.sInput.close();
					tc.sOutput.close();
					tc.socket.close();
					}
					catch(IOException ioE) {
						// not much I can do
					}
				}
			}
			catch(Exception e) {
				display("Exception closing the server and clients: " + e);
			}
		}
		// something went bad
		catch (IOException e) {
			display("Exception on new ServerSocket: " + e + "\n");
		}
	}		
	/*
	 * Display an event (not a message) to the console or the GUI
	 */
	private void display(String msg) {
		String event = dateFormat.format(new Date()) + " " + msg;
		System.out.println(event);
	}
	/*
	 *  to broadcast a message to all Clients
	 */
	private synchronized void broadcast(String message) {
		// add HH:mm:ss and \n to the message
		String time = dateFormat.format(new Date());
		String messageOut = time + " " + message + "\n";
		//display(message);
		// we loop in reverse order in case we would have to remove a Client
		// because it has disconnected
		for(int i = al.size(); --i >= 0;) {
			ClientThread ct = al.get(i);
			// try to write to the Client if it fails remove it from the list
			if(!ct.writeMsg(new ChatMessage(ChatMessage.MESSAGE, messageOut))) {
				al.remove(i);
				display("Disconnected Client " + ct.username + " removed from list.");
			}
		}
	}
	private synchronized void broadcastExceptFor(int id, ChatMessage cm) {
		
		for(int i = al.size(); --i >= 0;) {
			if(al.get(i).id == id){
				cm.setUsername(al.get(i).username);
			}
		}
		for(int i = al.size(); --i >= 0;) {
			
			if(al.get(i).id != id){
				ClientThread ct = al.get(i);
			
				
				// try to write to the Client if it fails remove it from the list
				if(!ct.writeMsg(cm)) {
					al.remove(i);
					display("Disconnected Client " + ct.username + " removed from list.");
				}
			}
			
		}
	}

	// for a client who logoff using the LOGOUT message
	synchronized void remove(int id) {
		// scan the array list until we found the Id
		for(int i = 0; i < al.size(); ++i) {
			ClientThread ct = al.get(i);
			// found it
			if(ct.id == id) {
				al.remove(i);
				return;
			}
		}
	}
	
	/*
	 *  To run as a console application just open a console window and: 
	 * > java Server
	 * > java Server portNumber
	 * If the port number is not specified 1500 is used
	 */ 
	public static void main(String[] args) {
		// start server on default port 1500 unless a PortNumber is specified 
		int portNumber = 1500;
		switch(args.length) {
			case 1:
				try {
					portNumber = Integer.parseInt(args[0]);
				}
				catch(Exception e) {
					System.out.println("Invalid port number.");
					System.out.println("Usage is: > java Server [portNumber]");
					return;
				}
			case 0:
				break;
			default:
				System.out.println("Usage is: > java Server [portNumber]");
				return;
		}
		// create a server object and start it
		Server server = new Server(portNumber);
		server.start();
	}

	/** One instance of this thread will run for each client */
	
	/*
	 * pretty much does all the work in here
	 */
	class ClientThread extends Thread {
		// the socket where to listen/talk
		Socket socket;
		ObjectInputStream sInput;
		ObjectOutputStream sOutput;
		// my unique id (easier for disconnection)
		int id;
		// the Username of the Client
		String username;
		// the only type of message a will receive
		ChatMessage cm;
		// the date I connect
		Date date;
		SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm:ss");

		// Constructor
		ClientThread(Socket socket) {
			// give each a unique id
			id = ++connectionId;
			this.socket = socket;
			try
			{
				// create output first
				sOutput = new ObjectOutputStream(socket.getOutputStream());
				sInput  = new ObjectInputStream(socket.getInputStream());
				// read the username
				username = (String) sInput.readObject();
				display(username + " just connected.");
			}
			catch (IOException e) {
				display("Exception creating new Input/output Streams: " + e);
				return;
			}
			catch (ClassNotFoundException e) {
				//required to make java happy
			}
            date = new Date();
		}
		// run forever
		public void run() {
			// to loop until LOGOUT
			boolean keepGoing = true;
			broadcast(username + " connected");
			while(keepGoing) {
				// read a String (which is an object)
				try {
					cm = (ChatMessage) sInput.readObject();
				}
				catch (IOException e) {
					display(username + " Exception reading Streams: " + e);
					break;				
				}
				catch(ClassNotFoundException e2) {
					break;
				}
				// the messaage part of the ChatMessage
				String message = cm.getMessage();

				// Switch on the type of message receive
				switch(cm.getType()) {

				case ChatMessage.MESSAGE:
					broadcast(username + ": " + message);
					break;
				case ChatMessage.LOGOUT:
					//display(username + " disconnected with a LOGOUT message.");
					keepGoing = false;
					break;
				case ChatMessage.TYPING:
					broadcastExceptFor(id, cm);
					break;
				case ChatMessage.WHOISIN:
					writeMsg(new ChatMessage(ChatMessage.MESSAGE, "List of the users connected at " + dateFormat.format(new Date()) + "\n"));
					// scan al the users connected
					for(int i = 0; i < al.size(); ++i) {
						ClientThread ct = al.get(i);
						writeMsg(new ChatMessage(ChatMessage.MESSAGE,(i+1) + ") " + ct.username + " online for " + (int)((new Date().getTime() - ct.date.getTime())/1000) + " seconds \n"));
					}
					break;
				}
			}
			// remove self from the arrayList containing the list of the
			// connected Clients
			broadcast(username + " disconnected");
			remove(id);
			close();
		}
		
		// try to close everything
		private void close() {
			// try to close the connection
			try {
				if(sOutput != null) sOutput.close();
			}
			catch(Exception e) {}
			try {
				if(sInput != null) sInput.close();
			}
			catch(Exception e) {};
			try {
				if(socket != null) socket.close();
			}
			catch (Exception e) {}
		}

		/*
		 * Write a String to the Client output stream
		 */
		private boolean writeMsg(ChatMessage message) {
			// if Client is still connected send the message to it
			if(!socket.isConnected()) {
				close();
				return false;
			}
			// write the message to the stream
			try {
				sOutput.writeObject(message);
			}
			// if an error occurs, do not abort just inform the user
			catch(IOException e) {
				display("Error sending message to " + username);
				display(e.toString());
			}
			return true;
		}
	}
}