import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;


public class StyledTextPane extends JTextPane{
	public Document doc;
	private BufferedImage bufferedImage;
	private TexturePaint texturePaint;
	StyledTextPane(){
		File file = new File("media/background.png");
		try{
			bufferedImage = ImageIO.read(file);
		    Rectangle rect = new Rectangle(0, 0, bufferedImage.getWidth(null), bufferedImage.getHeight(null));
		    texturePaint = new TexturePaint(bufferedImage, rect);
		    setOpaque(false);
		}
		catch(IOException ie){
			System.err.println(ie);
		}
		init();
		Random r = new Random();
		for(int x = 0; x < 12; x++){
			SimpleAttributeSet style = getStyle(new Color(r.nextInt(256), r.nextInt(256), r.nextInt(256)), false, true, x + 10);
			append("Styled TEXT test! #" + x, style);
		}
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		String[] x = ge.getAvailableFontFamilyNames();
		for(int y = 0; y < x.length; y ++){
			append(x[y], getStyle(x[y],0));
		}
	}
	StyledTextPane(int x){
		init();
	}
	public SimpleAttributeSet getStyle( String font, int x){
		SimpleAttributeSet style = new SimpleAttributeSet();
	    StyleConstants.setFontFamily(style, font);
	    return style;
	}
	public SimpleAttributeSet style;
	public void setStyle(SimpleAttributeSet s){
		style = s;
		setParagraphAttributes(s, true);
	}
	private void init(){
		doc = new DefaultStyledDocument();
		setDocument(doc);
	}
	public SimpleAttributeSet getStyle( Color color, boolean bold, boolean italics, int size){
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
	public void append(String s, SimpleAttributeSet style){	
	    try{
	    	 doc.insertString(doc.getLength(),s + "\n", style);
	    	 setCaretPosition(doc.getLength());
	    }
	    catch(BadLocationException e){
	    	System.err.println("unable to append: " + e);
	    }
	   
	}
	 public void paintComponent(Graphics g)
	  {
		 if(bufferedImage!= null){
			 Graphics2D g2 = (Graphics2D) g;
			 g2.setPaint(texturePaint);
		 }
	    
	    g.fillRect(0, 0, getWidth(), getHeight());
	    super.paintComponent(g);
	  }

	/**
	 * @param args
	 */
	public static void updateStyle(){
		box.setStyle(toolbar.getStyle());
		box.requestFocusInWindow();
	}
	public static void send(){
		pane.append(box.getText().replaceAll("\\n$", ""), box.style);
		box.setText("");
		box.requestFocusInWindow();
	}
	public static StyleToolbar toolbar;
	public static StyledTextPane box;
	public static StyledTextPane pane;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		JDialog loadingDialog = new JDialog(new JFrame(), "Loading ", false);
		loadingDialog.setPreferredSize(new Dimension(300,300));
		JLabel label = new JLabel("Loading");
		label.setFont(new Font("serif",Font.BOLD,32));
		label.setForeground(Color.white);
		loadingDialog.getContentPane().add(label);
		loadingDialog.getContentPane().setBackground(Color.black);
		loadingDialog.pack();
		loadingDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		loadingDialog.setVisible(true);
		JFrame window = new JFrame("Styled Text");
		
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel panel = new JPanel();
		pane = new StyledTextPane();
		JScrollPane scroller = new JScrollPane(pane);
		
		window.getContentPane().add(panel);
		panel.add(scroller);
		box = new StyledTextPane(0);
		JScrollPane scroll = new JScrollPane(box);
		JButton button = new JButton("Send");
		button.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				send();
			}
			
		});
		scroller.setPreferredSize(new Dimension(300,400));
		scroll.setPreferredSize(new Dimension(300,40));
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		toolbar = new StyleToolbar();
		ItemListener listener = new ItemListener(){
			public void itemStateChanged(ItemEvent e){
				updateStyle();
			}
		};
		box.addKeyListener(new KeyListener(){

			@Override
			public void keyPressed(KeyEvent k) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void keyReleased(KeyEvent k) {
				// TODO Auto-generated method stub
				if(k.getKeyCode() == KeyEvent.VK_ENTER){
					send();
				}
			}

			@Override
			public void keyTyped(KeyEvent k) {
				// TODO Auto-generated method stub
				
			}
			
		});
		toolbar.boldButton.addItemListener(listener);
		toolbar.italicsButton.addItemListener(listener);
		toolbar.underlineButton.addItemListener(listener);
		panel.add(toolbar);
		JPanel g = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		g.add(scroll);
		g.add(button);
		panel.add(g);
		window.pack();
		window.setVisible(true);
		loadingDialog.dispose();

	}

}
