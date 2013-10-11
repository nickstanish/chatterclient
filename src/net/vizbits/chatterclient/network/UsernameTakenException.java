package net.vizbits.chatterclient.network;

public class UsernameTakenException extends Exception {
	private String username = "";
	public UsernameTakenException(String s){
		this.username = s;
	}
	public String getUsername(){
		return username;
	}
}
