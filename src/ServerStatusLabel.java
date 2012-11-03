import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

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
	public ServerStatusLabel(String server, int port){
		super("---");
		bi = new BufferedImage(25,25,BufferedImage.TYPE_INT_ARGB);
		redraw();
		ServerStatusThread t = new ServerStatusThread(server, port);
		t.start();
	}
	public void redraw(){
		Graphics2D g = bi.createGraphics();
		g.setColor(Color.black);
		g.fillRect(0,0,bi.getWidth(), bi.getHeight());
		if(!up){
			g.setColor(Color.red);
			this.setText("Server is down");
		}
		else{
			g.setColor(Color.green);
			this.setText("Server is up");
		}
		g.fillOval(0,0,bi.getWidth(), bi.getHeight());
		this.setIcon(new ImageIcon(bi));
		this.revalidate();
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
