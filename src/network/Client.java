package network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;


public abstract class Client implements Connection, Runnable {
	private BufferedReader in;		// to read from the socket
	private PrintWriter out;		// to write on the socket
	private Socket socket;
	private boolean keepGoing = true;
	public enum MessageType{
		MESSAGE, NOTICE, ERROR, TYPING, CONTACTS, DISCONNECTED;
	}
	@Override
	public final boolean connect(String username, String host, int port) throws IOException,UsernameTakenException{
		socket = new Socket(host, port);
		socket.setKeepAlive(true);
		in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);
		out.println(username);
		String validation = in.readLine();
		if(validation.charAt(0) == '0'){
			throw new UsernameTakenException(username);
		}
		new Thread(this).start();
		// success we inform the caller that it worked
		return true;
	}
	@Override
	public final void disconnect() {
		try {
			send('1',"");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		keepGoing = false;
		try {
			socket.setKeepAlive(false);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		try { 
			if(in != null) in.close();
		} catch(Exception e) {} // not much else I can do
		
		try {
			if(out != null) out.close();
		} catch(Exception e) {} // not much else I can do
		
        try{
			if(socket != null) socket.close();
		} catch(Exception e) {} // not much else I can do
	}

	/**
	 * Transfer Strings to the server via PrintWriter
	 * for transferring data, use a(n Buffered)OutputStream
	 * Xfer is threaded to reduce delays in UI, but could potentially
	 * screw up order of sending..
	 */
	@Override
	public final boolean send(char code, String s) throws IOException {
		if(connected()){
			//send in thread
			final String message = code + s;	// concat as final variable so runnable doesnt complain
			Thread t = new Thread(new Runnable() {           
				public void run() { 
					synchronized(out){
						out.println(message);
					}	
			    }
			});
			t.start();
			return true;
		}
		// disconnected
		return false;

	}
	/**
	 * unimplemented
	 */
	@Override
	public boolean reconnect(int timeout) {
		// TODO Auto-generated method stub
		return false;
	}
	/**
	 * returns boolean if connection is active
	 */
	@Override
	public final boolean connected() {
		if(socket != null){
			return socket.isConnected();
		}
		return false;
		
	}
	/**
	 * Run method for the listening thread of the client
	 * called in the connect method
	 * should run after started until told to stop through disconnect
	 */
	@Override
	public final void run(){
		keepGoing = true;
		while(keepGoing){
			String line = null;
			try {
				line = in.readLine();
			} catch (IOException e) {
				messageHandler(MessageType.DISCONNECTED, "IO Error in thread " + Thread.currentThread().getName());
			}
			if(line != null){
				switch(line.charAt(0)){
				case '0': // message
					messageHandler(MessageType.MESSAGE, line.substring(1));
					break;
				case '2': // istyping
					if(line.charAt(1) == '0') messageHandler(MessageType.TYPING, " ");
					else messageHandler(MessageType.TYPING, line.substring(2));
					
					break;
				case 'c': //contacts list
					messageHandler(MessageType.CONTACTS, line.substring(1).split(","));
					break;
					
				case 'E': // Error
					messageHandler(MessageType.ERROR, line.substring(1));
					break;
				default:
					messageHandler(MessageType.ERROR, "Error:\n " + line);
					break;
				}
			}
		}
	}
	public abstract void messageHandler(MessageType type, Object s);
	

}
