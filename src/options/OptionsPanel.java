package options;

import java.awt.Dimension;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

public class OptionsPanel extends JPanel{
	private static final long serialVersionUID = 2748129716144166752L;
	public Options options;
	public ButtonGroup showTimeGroup = new ButtonGroup();
	public JRadioButton showTimeOn, showTimeOff;
	public JTextField defaultNameField;
	public JTextField passwordField;
	
	public OptionsPanel(Options opt){
		options = opt;
		showTimeOn = new JRadioButton("Yes" , options.showTime);
		showTimeOff = new JRadioButton("No" , !options.showTime);
		defaultNameField = new JTextField(15);
		defaultNameField.setText(options.defaultUsername);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		JPanel pane[] = new JPanel[4];
		for(int x = 0; x < 4; x++){
			pane[x] = new JPanel();
			add(pane[x]);
			pane[x].setLayout(new BoxLayout(pane[x], BoxLayout.X_AXIS));
			pane[x].setPreferredSize(new Dimension(300,20));
			pane[x].setMaximumSize(new Dimension(300,20));
		}
		showTimeGroup.add(showTimeOn);
		showTimeGroup.add(showTimeOff);
		JLabel showTimeLabel = new JLabel("Show message time?  ");
		JLabel setDefaultUsername = new JLabel("Username:  ");
		pane[0].add(showTimeLabel);
		pane[0].add(showTimeOn);
		pane[0].add(showTimeOff);
		pane[1].add(setDefaultUsername);
		pane[1].add(defaultNameField);
		//add "*"'s for the characters in the password box for legitimacy mainly cuz its badass
		//add remember "Login Button" --radio buttons
		setPreferredSize(new Dimension(300,150));

	}
	public void saveOptions(){
		//add serializable shit here\
		//file == config.ini
		options.showTime = showTimeOn.isSelected();
		options.defaultUsername = defaultNameField.getText().trim();
		try{
    		FileOutputStream fos = new FileOutputStream(new File("config.ini"));
    		ObjectOutputStream out = new ObjectOutputStream(fos);
    		out.writeObject(options);
    		out.close();
    		fos.close();
		}
		catch(IOException ie){
			System.out.println("" + ie);
		}
		
	}
}

