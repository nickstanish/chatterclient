import java.awt.AWTException;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.RenderingHints;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;



public class TaskbarManager {
	private SystemTray tray;
	private TrayIcon trayIcon;
	private PopupMenu trayMenu;
	private MenuItem logoutTrayItem, exitTrayItem;
	private ChatterClient client;
	
	public TaskbarManager(ChatterClient c){
		this.client = c;
		if(SystemTray.isSupported()){
			tray = SystemTray.getSystemTray();
			BufferedImage image;
			try{
				//message_box_icon
				BufferedImage original = ImageIO.read(new File("media/icons/logo.png"));
				image = resize(original, 20,20);
			}
			catch(IOException ie){
				System.err.println("error: " + ie);
				image = backupImage();
			}
			trayMenu = new PopupMenu();
			logoutTrayItem = new MenuItem("Logout");
			logoutTrayItem.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					client.logout();
				}
			});
			exitTrayItem = new MenuItem("Exit");
			exitTrayItem.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					client.exit();
				}
			});
			trayMenu.add(logoutTrayItem);
			trayMenu.add(exitTrayItem);
			try {
				trayIcon = new TrayIcon(image, "ChatterBox", trayMenu);
				trayIcon.addMouseListener(new MouseListener(){
					@Override
					public void mouseClicked(MouseEvent arg0) {
						client.bringToFront();
					}
					@Override
					public void mouseEntered(MouseEvent arg0) {}
					@Override
					public void mouseExited(MouseEvent arg0) {}
					@Override
					public void mousePressed(MouseEvent arg0) {}
					@Override
					public void mouseReleased(MouseEvent arg0) {}
				});
				trayIcon.addActionListener(new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e) {
						client.bringToFront();		
					}					
				});
				tray.add(trayIcon);
			} catch (AWTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		else{
			System.err.println("System tray not supported");
		}
		
	}
	public BufferedImage resize(BufferedImage original, int width, int height){
		BufferedImage image = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		g.drawImage(original, 0, 0, 16, 16, null);
		g.dispose();
		g.setComposite(AlphaComposite.Src);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		return image;
	}
	private BufferedImage backupImage(){
		BufferedImage image = new BufferedImage(16,16,BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		g.setColor(Color.black);
		g.fill(new Rectangle2D.Double(0,0,16,16));
		g.setColor(Color.white);
		g.drawString("?", 4,13);
		g.dispose();
		g.setComposite(AlphaComposite.Src);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		return image;
	}
	public static void main(String[] args){
		ChatterClient c = new ChatterClient("" , 100);
		new TaskbarManager(c);
	}
	
}
