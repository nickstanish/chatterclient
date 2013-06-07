package contacts;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class Contact {
	public static ImageIcon icon = null;
	private ImageIcon image;
	private String name;
	public Contact(ImageIcon image, String name){
		this.image = image;
		this.name = name;
		if(icon == null){
			try {
				BufferedImage i = ImageIO.read(new File("media/icons/noimg.jpg"));
				i = resize(i, 35,35);
				icon = new ImageIcon(i);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(this.image == null && icon != null){
			this.image = icon;
		}
	}
	public String toString(){
		return name;
	}
	public String getName(){
		return name;
	}
	public ImageIcon getImage(){
		return image;
	}
	public static BufferedImage resize(BufferedImage original, int width, int height){
		BufferedImage image = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		g.drawImage(original, 0, 0, width, height, null);
		g.dispose();
		g.setComposite(AlphaComposite.Src);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		return image;
	}
}
