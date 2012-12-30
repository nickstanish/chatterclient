package style;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;


public class LoadingPanel extends JPanel{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8456414234660190336L;

	@Override
	public void paintComponent(Graphics g0){
		Graphics2D g = (Graphics2D)g0;
		g.setColor(Color.black);
		g.fillRect(0, 0, getWidth(), getHeight());
		g.setColor(Color.gray);
		g.setFont(new Font("serif", Font.BOLD, 36));
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.drawString("Loading", (int)(0.3 * getWidth()), (int)(0.5 * getHeight()));
	}
}
