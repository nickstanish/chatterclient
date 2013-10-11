package net.vizbits.chatterclient.contacts;

/**
 * 
 * @author Nick
 * 10/11/13: removed support for image
 */
public class Contact {
	private String name;
	
	public Contact(String name){
		this.name = name;
	}
	public String toString(){
		return name;
	}
	public String getName(){
		return name;
	}
}
