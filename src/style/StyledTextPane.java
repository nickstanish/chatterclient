package style;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;



public class StyledTextPane extends StylePane{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1645679932047019540L;
	public StyledTextPane(){
		super();
		setEditable(false);
		/*
		 * Random r = new Random();
		 */
		/*
		 * for(int x = 0; x < 12; x++){
			SimpleAttributeSet style = constructStyle(new Color(r.nextInt(256), r.nextInt(256), r.nextInt(256)), false, true, x + 10);
			append("Styled TEXT test! #" + x, style);
		}
		*/
		//GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		//String[] x = ge.getAvailableFontFamilyNames();
		/*
		for(int y = 0; y < x.length; y ++){
			append(x[y], getStyle(x[y],0));
		}
		*/
	}	
	/**
	 * @param args
	 */
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		JDialog loadingDialog = new JDialog(new JFrame(), "Loading ", false);
		loadingDialog.setPreferredSize(new Dimension(300,300));
		loadingDialog.getContentPane().add(new LoadingPanel());
		loadingDialog.pack();
		loadingDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		loadingDialog.setVisible(true);
		
		View window = new View(new StyleToolbar(), new StyledTextPane(), new StyleSendBox());
		window.pack();
		window.setVisible(true);
		loadingDialog.dispose();

	}

}
class View extends JFrame{
	private StyledTextPane pane;
	private StyleSendBox box;
	private StyleToolbar toolbar;
	public void updateStyle(){
		box.setStyle(toolbar.getStyle());
		box.requestFocusInWindow();
	}
	public void send(){
		pane.append(box.getText().replaceAll("\\n$", ""), box.style);
		box.setText("");
		box.requestFocusInWindow();
	}
	public void killExtraLine(){
		box.setText("");
		box.requestFocusInWindow();
	}
	public View(StyleToolbar toolbar, StylePane pane, StylePane box){		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel panel = new JPanel();
		this.pane = (StyledTextPane)pane;
		this.box = (StyleSendBox)box;
		JScrollPane scroller = new JScrollPane(pane);
		add(panel);
		panel.add(scroller);
		JScrollPane scroll = new JScrollPane(box);
		JButton button = new JButton("Send");
		button.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				send();
			}
			
		});
		scroller.setPreferredSize(new Dimension(300,400));
		scroll.setPreferredSize(new Dimension(300,80));
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		this.toolbar = toolbar;
		InputMap input = box.getInputMap();
		KeyStroke enter = KeyStroke.getKeyStroke("ENTER");
		input.put(enter, "send");
		ActionMap actions = box.getActionMap();
		actions.put("send", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				send();
				}
			});
		toolbar.addListeners(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent evt) {
				updateStyle();
			}

		}, 
		new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent evt) {
				updateStyle();
				
			}
			
		});
		panel.add(toolbar);
		JPanel g = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		panel.add(scroll);
		g.add(button);
		panel.add(g);
	}
}