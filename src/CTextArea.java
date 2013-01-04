import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JTextArea;


public class CTextArea extends JTextArea{
	/**
	 * DEPRECATED BY STYLEDTEXTPANE
	 */

	private static final long serialVersionUID = -217191299156683782L;
	private BufferedImage bufferedImage;
	private TexturePaint texturePaint;
	public CTextArea(File file){
		super();
		try{
			bufferedImage = ImageIO.read(file);
		    Rectangle rect = new Rectangle(0, 0, bufferedImage.getWidth(null), bufferedImage.getHeight(null));
		    texturePaint = new TexturePaint(bufferedImage, rect);
		    setOpaque(false);
		}
		catch(IOException ie){
			System.out.println(ie);
			append("Background Media Not Found: " + file + "\n");
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
}
