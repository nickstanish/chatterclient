package net.vizbits.chatterclient.tabbedchat.style;

import java.awt.Color;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import net.vizbits.chatterclient.ChatterClient;

public class StylePane extends JTextPane{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5686890056874533919L;
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
	public static final SimpleAttributeSet constructStyle( Color color, boolean bold, boolean italics, boolean underline, int size){
		SimpleAttributeSet style = new SimpleAttributeSet();
	    // Italic
	    StyleConstants.setItalic(style, italics);
	    // Bold
	    StyleConstants.setBold(style, bold);
	    StyleConstants.setUnderline(style, underline);
	    // Font family
	    StyleConstants.setFontFamily(style, "SansSerif");
	    // Font size
	    StyleConstants.setFontSize(style, size);
	    // Background color
	    //StyleConstants.setBackground(net.vizbits.chatterclient.style, Color.white);
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
	    	 doc.insertString(doc.getLength(),s , style);
	    	 setCaretPosition(doc.getLength());
	    }
	    catch(BadLocationException e){
	    	System.err.println("unable to append: " + e);
	    }
	}
	public void appendln(String s, SimpleAttributeSet style){	
	    append(s + '\n', style);
	}
}
