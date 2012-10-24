import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeListener;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;


public class StyleToolbar extends JPanel{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5073819039743726973L;
	private JToggleButton boldButton, italicsButton, underlineButton;
	private JComboBox colorCombo, fontCombo;
	private JSpinner fontSizeSpinner;
	StyleToolbar(){
		setLayout(new FlowLayout(FlowLayout.LEADING));
		boldButton = new JToggleButton("Bold", false);
		add(boldButton);
		italicsButton = new JToggleButton("Italics", false);
		add(italicsButton);
		underlineButton = new JToggleButton("Underline", false);
		add(underlineButton);
		SpinnerNumberModel fontSizeModel = new SpinnerNumberModel(12, 8, 48, 2);
		fontSizeSpinner = new JSpinner(fontSizeModel);
		add(fontSizeSpinner);
		String[] colors = new String[]{"black","gray","blue","green","red","yellow"};
		colorCombo = new JComboBox(colors);
		add(colorCombo);
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
	    //StyleConstants.setBackground(style, Color.white);
	    // Foreground color
	    StyleConstants.setForeground(style, Color.black);
	    return style;
	}
	public void addListeners(ItemListener l, ChangeListener cl){
		boldButton.addItemListener(l);
		italicsButton.addItemListener(l);
		underlineButton.addItemListener(l);
		fontSizeSpinner.addChangeListener(cl);
	}
}
