package network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class Client implements Connection {
	public int port;
	public String host;
	private ObjectInputStream in;		// to read from the socket
	private ObjectOutputStream out;		// to write on the socket
	private Socket socket;
	public boolean connected = false;
	public Client(String host, int port){
		this.host = host;
		this.port = port;
	}

	@Override
	public boolean connect() {
		try {
			socket = new Socket(host, port);
		} 
		catch(Exception e) {
			System.err.println("Error connecting to server:" + e);
			return false;
		}
		try{
			in  = new ObjectInputStream(socket.getInputStream());
			out = new ObjectOutputStream(socket.getOutputStream());
		}
		catch (IOException e) {
			System.err.println("Exception creating new input/output Streams: " + e);
			return false;
		}
		// creates the Thread to listen from the server 
		//new ListenFromServer().start();
		// Send our username to the server this is the only message that we
		// will send as a String. All other messages will be ChatMessage objects
		try{
			out.writeObject("");
		}
		catch (IOException eIO) {
			disconnect();
			return false;
		}
		// success we inform the caller that it worked
		return true;
	}

	@Override
	public void disconnect() {
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

	@Override
	public boolean send(int type, Object c) {
		if(connected){
			
		}
		return false;

	}

	@Override
	public boolean reconnect(int timeout) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean connected() {
		// TODO Auto-generated method stub
		return connected;
	}

}
