import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.GeneralPath;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class ChatterPaint extends JPanel{
	public ChatterPaint(){
		super();
		ChatterPaintPanel panel = new ChatterPaintPanel();
		panel.setPreferredSize(new Dimension(500,500));
		add(panel);
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JFrame window = new JFrame("ChatterPaint");
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ChatterPaint mainPanel = new ChatterPaint();
		window.getContentPane().add(mainPanel);
		window.pack();
		window.setVisible(true);
		window.setSize(500,500);

	}
}
class ChatterPaintPanel extends JPanel implements MouseListener, MouseMotionListener{
	private GeneralPath path = new GeneralPath();
	private Color color = Color.black;
	public ChatterPaintPanel(){
		super();
		addMouseListener(this);
		addMouseMotionListener(this);
		setBackground(Color.red);
	}
	@Override
	public void paintComponent(Graphics g1){
		Graphics2D g = (Graphics2D)g1;
		g.setColor(Color.red);
		g.setStroke(new BasicStroke(3.0f,BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
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
	public void mouseReleased(MouseEvent arg0) {
		
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
