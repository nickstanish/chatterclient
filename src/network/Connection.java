package network;

public interface Connection{
	public boolean connect(String username);
	public void disconnect();
	public boolean send(int type, Object c);
	public boolean connected();
	/**
	 * recursively try to reconnect with a timeout
	 * 
	 * @param timeout - timeout in seconds
	 * @return true if successful
	 */
	public boolean reconnect(int timeout);
	

}
