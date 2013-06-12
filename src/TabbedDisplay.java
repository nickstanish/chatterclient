import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import tabbedchat.CloseableTab;


public class TabbedDisplay extends JTabbedPane{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1906568734746340946L;
	public TabbedDisplay(){
		super(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
	}
	public void newTab(boolean closeable, String title){
		JPanel panel = new JPanel();
		panel.add(new JLabel(title));
		this.add(title, panel);
		if(closeable){
			setTabComponentAt(getTabCount()-1,new CloseableTab(this));
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JFrame f = new JFrame("Tabbed Test");
		TabbedDisplay t  = new TabbedDisplay();
		for(int i = 0; i < 10; i++){
			t.newTab(i%2==0, "Tab " + (i+1));
		}
		f.getContentPane().add(t);
		f.pack();
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}

}
