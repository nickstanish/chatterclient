import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;


public class AboutWindow extends JFrame {
	private JPanel mainPanel;
	AboutWindow(){
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		mainPanel = new JPanel(new GridBagLayout());
		add(mainPanel);
		mainPanel.setPreferredSize(new Dimension(300,400));
		setResizable(false);
		GridBagConstraints c = new GridBagConstraints();
		int row = 0;
		c.gridx = 0;
		c.weightx = 0.0;
		c.weighty = 0.05;
		c.anchor = GridBagConstraints.PAGE_START;
		c.gridy = row++;
		JLabel title = new JLabel("ChatterBox");
		title.setFont(new Font("serif", Font.BOLD, 30));
		mainPanel.add(title,c);
		c.gridy = row++;
		c.weighty = 0.01;
		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		mainPanel.add(new JTextArea("100% Java based instant messenger."),c);
		c.gridx = 0;
		c.gridy = row++;
		mainPanel.add(new JLabel("Version: 1.0"),c);
		c.gridy = row++;
		mainPanel.add(new JLabel("Authors: Joey Imburgia and Nick Stanish"),c);
		c.gridy = row++;
		//mainPanel.add(new JLabel("Authors: Joey Imburgia and Nick Stanish"),c);
		c.anchor = GridBagConstraints.CENTER;
		c.weighty = 0.05;
		
		//display
		pack();
		setVisible(true);
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new AboutWindow();

	}

}
