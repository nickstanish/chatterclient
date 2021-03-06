package net.vizbits.chatterclient.tabbedchat.style;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;


public class StyleToolbar extends JToolBar{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2512447123182158139L;
	private JToggleButton boldButton, italicsButton, underlineButton;
	private JComboBox<String> colorCombo, fontCombo;
	private JSpinner fontSizeSpinner;
	private StylePane pane;
	public StyleToolbar(StylePane pane){
		super(JToolBar.HORIZONTAL);
		this.setFloatable(false);
		this.pane = pane;
		this.setRollover(true);
		setLayout(new FlowLayout(FlowLayout.LEADING));
		JPanel buttonPanel = new JPanel(new GridLayout(1,0));
		boldButton = new JToggleButton("B", false);
		boldButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
		buttonPanel.add(boldButton);
		italicsButton = new JToggleButton("I", false);
		italicsButton.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 10));
		buttonPanel.add(italicsButton);
		underlineButton = new JToggleButton("U", false);
		Map<TextAttribute, Integer> fontAttributes = new HashMap<TextAttribute, Integer>();
		fontAttributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
		underlineButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10).deriveFont(fontAttributes));
		buttonPanel.add(underlineButton);
		add(buttonPanel);
		SpinnerNumberModel fontSizeModel = new SpinnerNumberModel(12, 4, 72, 2);
		fontSizeSpinner = new JSpinner(fontSizeModel);
		add(fontSizeSpinner);
		String[] colors = new String[]{"black","gray","blue","green","red","yellow", "pink"};
		colorCombo = new JComboBox<String>(colors);
		add(colorCombo);
		addListeners(
			new ItemListener() {
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
				
			}
		);
	}
	public SimpleAttributeSet getStyle(){
		SimpleAttributeSet style = new SimpleAttributeSet();
		StyleConstants.setUnderline(style, underlineButton.isSelected());
	    StyleConstants.setItalic(style, italicsButton.isSelected());
	    StyleConstants.setBold(style, boldButton.isSelected());
	    // Font family
	    StyleConstants.setFontFamily(style, "SansSerif");
	    // Font size
	    StyleConstants.setFontSize(style, Integer.parseInt(fontSizeSpinner.getValue().toString()));
	    // Background color
	    //StyleConstants.setBackground(net.vizbits.chatterclient.style, Color.white);
	    // Foreground color
	    StyleConstants.setForeground(style, nameToColor((String)colorCombo.getSelectedItem()));
	    return style;
	}
	public static Color nameToColor(String s){
		switch(s){
			case "red": 	return Color.red;
			case "blue": 	return Color.blue;
			case "yellow":	return Color.yellow;
			case "pink": 	return Color.pink;
			case "gray":	return Color.gray;
			case "green": 	return Color.green;
			default: 		return Color.black;
		}
	}
	public void updateStyle(){
		if(pane != null){
			pane.setStyle(getStyle());
			pane.requestFocusInWindow();
		}
		
	}
	public void addListeners(ItemListener l, ChangeListener cl){
		boldButton.addItemListener(l);
		italicsButton.addItemListener(l);
		underlineButton.addItemListener(l);
		fontSizeSpinner.addChangeListener(cl);
		colorCombo.addItemListener(l);
	}
	public static void main(String[] args){
		JFrame frame = new JFrame("StyleToolbar");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(new StyleToolbar(null));
		frame.pack();
		frame.setVisible(true);
		frame.setSize(400, 500);
	}
}
