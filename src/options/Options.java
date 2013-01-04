package options;

import java.io.Serializable;

public class Options implements Serializable{
	/**
	 * options contained in here
	 */
	private static final long serialVersionUID = 1L;
	public String defaultUsername;
	public boolean showTime;
	public Options(){
		defaultUsername = "username";
		showTime = true;
	}
}
