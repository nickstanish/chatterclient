package network;

import java.io.IOException;

public interface Connection{
	public boolean connect(String username, String host, int port) throws IOException, UsernameTakenException;
	public void disconnect();
	public boolean send(char code, String s) throws IOException;
	public boolean connected();
	/**
	 * recursively try to reconnect with a timeout
	 * 
	 * @param timeout - timeout in seconds
	 * @return true if successful
	 */
	public boolean reconnect(int timeout);
	

}
