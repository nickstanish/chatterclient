package tabbedchat;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 * 
 * @author Nick
 * closeable tab component
 */
public class CloseableTab extends JPanel {
    private final JTabbedPane pane;
 
    public CloseableTab(final JTabbedPane pane) {
        //unset default FlowLayout' gaps
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        if (pane == null) {
            throw new NullPointerException("TabbedPane is null");
        }
        this.pane = pane;
        setOpaque(false);
         
        //make JLabel read titles from JTabbedPane
        JLabel label = new JLabel() {
            public String getText() {
                int i = pane.indexOfTabComponent(CloseableTab.this);
                if (i != -1) {
                    return pane.getTitleAt(i);
                }
                return null;
            }
        };
         
        add(label);
        //add more space between the label and the button
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));
        //tab button
        JButton button = new TabButton();
        add(button);
        //add more space to the top of the component
        setBorder(BorderFactory.createEmptyBorder(2, 0, 1, 0));
    }
 
    private class TabButton extends JButton implements ActionListener {
        /**
		 * 
		 */
		private static final long serialVersionUID = 2862828523076044286L;
		public TabButton() {
        	setText("X");
            //Make the button looks the same for all Laf's
            setUI(new BasicButtonUI());
            //Make it transparent
            setContentAreaFilled(false);
            setFocusable(false);
            setBorder(BorderFactory.createEtchedBorder());
            setBorderPainted(false);
            setRolloverEnabled(true);
            //Close the proper tab by clicking the button
            addActionListener(this);
            setFont(new Font(getFont().getFontName(), getFont().getStyle(), 10));
        }
 
        public void actionPerformed(ActionEvent e) {
            int i = pane.indexOfTabComponent(CloseableTab.this);
            if (i != -1) {
                pane.remove(i);
            }
        }
        //paint fx
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getModel().isPressed()) {
            	setFont(new Font(getFont().getFontName(), getFont().getStyle(), 12));
            }
            else{
            	setFont(new Font(getFont().getFontName(), getFont().getStyle(), 10));
            }
            this.setForeground(Color.BLACK);
            if (getModel().isRollover()) {
                this.setForeground(Color.RED);
            }
        }
    }
}