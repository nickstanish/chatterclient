import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;


public class StyleToolbar extends JPanel{
	public JToggleButton boldButton, italicsButton, underlineButton;
	StyleToolbar(){
		setLayout(new FlowLayout(FlowLayout.LEADING));
		boldButton = new JToggleButton("Bold", false);
		add(boldButton);
		italicsButton = new JToggleButton("Italics", false);
		add(italicsButton);
		underlineButton = new JToggleButton("Underline", false);
		add(underlineButton);
	}
	public SimpleAttributeSet style;
	public SimpleAttributeSet getStyle(){
		SimpleAttributeSet style = new SimpleAttributeSet();
		StyleConstants.setUnderline(style, underlineButton.isSelected());
	    StyleConstants.setItalic(style, italicsButton.isSelected());
	    StyleConstants.setBold(style, boldButton.isSelected());
	    // Font family
	    StyleConstants.setFontFamily(style, "SansSerif");
	    // Font size
	    StyleConstants.setFontSize(style, 12);
	    // Background color
	    //StyleConstants.setBackground(style, Color.white);
	    // Foreground color
	    StyleConstants.setForeground(style, Color.black);
	    return style;
	}
}
