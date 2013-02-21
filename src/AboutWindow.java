import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;


public class AboutWindow extends JDialog implements ActionListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = -769006692300090479L;
	private JPanel mainPanel;
	public AboutWindow(JFrame f){
		super(f,"About", true);
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.Y_AXIS));
		JPanel p[] = new JPanel[3];
		for(int i = 0; i < 3; i++){
			p[i] = new JPanel();
		}
		JLabel titleLabel = new JLabel("ChatterBox");
		titleLabel.setFont(new Font("serif",Font.BOLD,24));
		p[0].add(titleLabel);
		JTextArea description = new JTextArea(40,20);
		description.setBackground(mainPanel.getBackground());
		description.setWrapStyleWord(true);
		description.setLineWrap(true);
		description.setText("Java instant messenger built on socket connections.\n\nAuthors:\nJoey Imburgia and Nick Stanish");
		description.setEditable(false);
		p[1].add(description);
		JButton okButton = new JButton("OK");
		okButton.addActionListener(this);
		p[2].add(okButton);
		for(int i = 0; i < 3; i++){
			mainPanel.add(p[i]);
		}
		getContentPane().add(mainPanel);
		mainPanel.setPreferredSize(new Dimension(250,200));
		setResizable(false);
		setLocationRelativeTo(f);
		pack();
		setVisible(true);
		
;	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new AboutWindow(null);

	}
	@Override
	public void actionPerformed(ActionEvent e) {
		setVisible(false);
		
	}

}
