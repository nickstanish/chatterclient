package StyleModules;

import java.awt.Color;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public abstract class StylePane extends JTextPane{
	protected Document doc;
	protected SimpleAttributeSet style;
	public StylePane(){
		super();
		init();
	}
	public SimpleAttributeSet getStyle( String font, int x){
		SimpleAttributeSet style = new SimpleAttributeSet();
	    StyleConstants.setFontFamily(style, font);
	    return style;
	}
	protected void init(){
		doc = new DefaultStyledDocument();
		setDocument(doc);
	}
	protected SimpleAttributeSet constructStyle( Color color, boolean bold, boolean italics, int size){
		SimpleAttributeSet style = new SimpleAttributeSet();
	    // Italic
	    StyleConstants.setItalic(style, italics);
	    // Bold
	    StyleConstants.setBold(style, bold);
	    // Font family
	    StyleConstants.setFontFamily(style, "SansSerif");
	    // Font size
	    StyleConstants.setFontSize(style, size);
	    // Background color
	    //StyleConstants.setBackground(style, Color.white);
	    // Foreground color
	    StyleConstants.setForeground(style, color);
	    return style;
	}
	protected void setStyle(SimpleAttributeSet s){
		style = s;
		setParagraphAttributes(s, true);
	}
	public void append(String s, SimpleAttributeSet style){	
	    try{
	    	 doc.insertString(doc.getLength(),s + "\n", style);
	    	 setCaretPosition(doc.getLength());
	    }
	    catch(BadLocationException e){
	    	System.err.println("unable to append: " + e);
	    }
	}
}
