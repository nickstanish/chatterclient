import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ChatterPaint extends JPanel{
	private JButton colorButton;
	private JSlider sizeSlider;
	private ChatterPaintPanel panel;
	public ChatterPaint(){
		super();
		setLayout(new BorderLayout());
		panel = new ChatterPaintPanel();
		colorButton = new JButton("Color");
		colorButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				panel.setColor(JColorChooser.showDialog(null, "Color", panel.getColor()));
				
			}
		});
		
		sizeSlider = new JSlider(SwingConstants.HORIZONTAL, 1, 30, 1);
		sizeSlider.setMajorTickSpacing(10);
		sizeSlider.setMinorTickSpacing(5);
		sizeSlider.setPaintTicks(true);
		sizeSlider.setPaintLabels(true);
		sizeSlider.setValue((int)panel.getSizeValue());
		sizeSlider.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent e) {
				panel.setSizeValue(sizeSlider.getValue());
			}
			
		});
		add(panel, BorderLayout.CENTER);
		JPanel toolPanel = new JPanel();
		toolPanel.add(colorButton);
		toolPanel.add(sizeSlider);
		add(toolPanel, BorderLayout.NORTH);
	}
	 
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JFrame window = new JFrame("ChatterPaint");
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JMenuBar menu = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenuItem saveItem = new JMenuItem("Save");
		final JFileChooser fc = new JFileChooser();
		JMenuItem openItem = new JMenuItem("Open");
		fileMenu.add(saveItem);
		fileMenu.add(openItem);
		menu.add(fileMenu);
		window.setJMenuBar(menu);
		final ChatterPaint mainPanel = new ChatterPaint();
		window.getContentPane().add(mainPanel);
		openItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				File file;
				int value = fc.showDialog(null, "Open");
				if(value == JFileChooser.APPROVE_OPTION){
					file = fc.getSelectedFile();
					mainPanel.openFile(file);
					
				}
			}
		});
		saveItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				JOptionPane.showMessageDialog(null, "Sorry this feature is not implemented yet");
			}
		});
		window.pack();
		window.setVisible(true);
		window.setSize(500,500);

	}
	public void openFile(File file){
		panel.openFile(file);
	}
}
class ChatterPaintPanel extends JPanel implements MouseListener, MouseMotionListener{
	private ArrayList<ChatterPaintShape> pathList = new ArrayList<ChatterPaintShape>();
	private GeneralPath path = new GeneralPath();
	private float size = 3.0f;
	private BufferedImage paintedImage;
	private Color color = Color.black;
	private void paintToImage(){
		if(pathList.size() > 12){
			int width = getWidth();
			int height = getHeight();
			if (paintedImage.getHeight() > height){
				height = paintedImage.getHeight();
			}
			if (paintedImage.getWidth() > width){
				width = paintedImage.getWidth();
			}
			BufferedImage temp = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = (Graphics2D)temp.getGraphics();
			g.drawImage(paintedImage,0,0,null);
			g.setStroke(new BasicStroke(pathList.get(0).getSize(),BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
	    	// free up list
			g.setColor(pathList.get(0).getColor());
	    	g.draw(pathList.get(0).getPath());
	    	pathList.remove(0);
	    	g.dispose();
	    	paintedImage = temp;
		}
		
		
		
	}
	public void setColor(Color c){
		color = c;
	}
	public Color getColor(){
		return color;
	}
	public float getSizeValue(){
		return size;
	}
	public void setSizeValue(float size){
		this.size = size;
	}
	public ChatterPaintPanel(){
		super();
		setDoubleBuffered(true);
		addMouseListener(this);
		addMouseMotionListener(this);
		paintedImage = new BufferedImage(500,500,BufferedImage.TYPE_INT_ARGB);
	}
	public void openFile(File file){
		
		try {
			paintedImage = ImageIO.read(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Sorry, unable to use file.");
		}
		pathList.clear();
		repaint();
		
	}
	@Override
	public void paintComponent(Graphics g1){
		super.paintComponent(g1);
		Graphics2D g = (Graphics2D)g1;
		paintToImage();
		g.drawImage(paintedImage, 0, 0 , null);
		System.out.println(pathList.size());
	    for(int i = 0; i < pathList.size(); i++){
	    	g.setStroke(new BasicStroke(pathList.get(i).getSize(),BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
	    	g.setColor(pathList.get(i).getColor());
	    	g.draw(pathList.get(i).getPath());
	    }
	    g.setColor(color);
	    g.setStroke(new BasicStroke(size,BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
	    g.draw(path);
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {}

	@Override
	public void mouseEntered(MouseEvent arg0) {}

	@Override
	public void mouseExited(MouseEvent arg0) {}

	@Override
	public void mousePressed(MouseEvent m) {
		path.moveTo(m.getX(), m.getY());
	}

	@Override
	public void mouseReleased(MouseEvent m) {
		pathList.add(new ChatterPaintShape(path, color, size));
		path = new GeneralPath();
		repaint();
	}
	@Override
	public void mouseDragged(MouseEvent m) {
		path.lineTo(m.getX(), m.getY());
		repaint();
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	

}
class ChatterPaintShape{
	private GeneralPath path;
	private Color color;
	private float size;
	public ChatterPaintShape(GeneralPath path, Color color, float size){
		this.path = path;
		this.size = size;
		this.color = color;
	}
	public Color getColor(){
		return color;
	}
	public float getSize(){
		return size;
	}
	public GeneralPath getPath(){
		return path;
	}
}
