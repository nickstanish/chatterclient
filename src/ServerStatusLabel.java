import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;


public class ServerStatusLabel extends JLabel{
	private String text = "";
	private Socket socket;
	private ObjectInputStream sInput;
	private ObjectOutputStream sOutput;	
	private boolean up = false;
	private BufferedImage bi;
	private BufferedImage red, green;
	/**
	 * technically this isn't the right way to do this, but we will go ahead
	 * with it for now... this should be an invisible client, id should be -1, and shouldn't actually
	 * be turned into a thread or anything. we don't want to waste any memory from the server
	 */
	public ServerStatusLabel(String server, int port){
		super("---");
		bi = new BufferedImage(25,25,BufferedImage.TYPE_INT_ARGB);
		try {
			red = ImageIO.read(new File("media/icons/red_light.png"));
			red = resize(red, 25,25);
			green = ImageIO.read(new File("media/icons/green_light.png"));
			green = resize(green, 25, 25);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		redraw();
		ServerStatusThread t = new ServerStatusThread(server, port);
		t.start();
	}
	public void redraw(){
		//Graphics2D g = bi.createGraphics();
		//g.setColor(Color.black);
		//g.fillRect(0,0,bi.getWidth(), bi.getHeight());
		if(!up){
			//g.setColor(Color.red);
			bi = red;
			this.setText("Server is down");
		}
		else{
			//g.setColor(Color.green);
			bi = green;
			this.setText("Server is up");
		}
		//g.fillOval(3,3,bi.getWidth()-5, bi.getHeight()-5);
		this.setIcon(new ImageIcon(bi));
		this.revalidate();
	}
	public BufferedImage resize(BufferedImage original, int width, int height){
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
		g.drawImage(original, 0, 0, width, height, null);
		g.dispose();
		g.setComposite(AlphaComposite.Src);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		return image;
	}
	class ServerStatusThread extends Thread{
		String host;
		int port;
		ServerStatusThread(String server, int port){
			this.host = server;
			this.port = port;
		}
		@Override
		public void run(){
			while(true){
				try {
				socket = new Socket(host, port);
				up = true;
				try{
					sInput  = new ObjectInputStream(socket.getInputStream());
					sOutput = new ObjectOutputStream(socket.getOutputStream());
				}
				catch (IOException eIO) {
					up = false;
				}
				} 
				catch(Exception ec) {
					up = false;
				}
				redraw();
			}
			
		}
		class ListenFromServer extends Thread {

			public void run() {
				while(true) {
					try {
						ChatMessage message = (ChatMessage)sInput.readObject();
						if(message.getType() == ChatMessage.MESSAGE){
						}
		
					}
					catch(IOException e) {
						up = false;
						break;
					}
					catch(ClassNotFoundException e) {}
				}
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JFrame window = new JFrame("ServerLabel");
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.getContentPane().add(new ServerStatusLabel("localhost", 1500));
		window.pack();
		window.setVisible(true);
		window.setSize(300,300);

	}

}
