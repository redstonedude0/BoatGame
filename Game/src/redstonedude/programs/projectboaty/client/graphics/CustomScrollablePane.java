package redstonedude.programs.projectboaty.client.graphics;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JViewport;

public class CustomScrollablePane extends JPanel{

	private static final long serialVersionUID = 1L;
	public JViewport viewport;
	public JScrollBar scrollbar;
	public Runnable update;
	
	public CustomScrollablePane() {
		super();
		//setLayout(new LayoutManagerStrictSizes());
		viewport = new JViewport();
		scrollbar = new JScrollBar(JScrollBar.VERTICAL);
		viewport.setLayout(new LayoutManagerStrictSizes());
		viewport.setView(this);
		scrollbar.setLayout(new LayoutManagerStrictSizes());
		scrollbar.addAdjustmentListener(new AdjustmentListener() {
			int y = 0;
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				for (Component c: getComponents()) {
					int change = y-e.getValue();
					c.setLocation(c.getLocation().x, c.getLocation().y+change);
				}
				y = e.getValue();
			}
		});
		scrollbar.setUnitIncrement(20);
	}
	
	
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		update.run();
	}
	
	@Override
	public boolean isVisible() {
		return viewport.isVisible();
	}
	
	@Override
	public void setVisible(boolean b) {
		viewport.setVisible(b);
		scrollbar.setVisible(b);
	}
	
	public void addAdjustmentListener(AdjustmentListener al) {
		scrollbar.addAdjustmentListener(al);
	}
	
	@Override
	public void setPreferredSize(Dimension d) {
		super.setPreferredSize(d);
		viewport.setPreferredSize(new Dimension(d.width-20,d.height));
		scrollbar.setPreferredSize(new Dimension(10,d.height));
	}
	
	@Override
	public void setLocation(int x, int y) {
		viewport.setLocation(x, y);
		scrollbar.setLocation(x+viewport.getPreferredSize().width+10, y);
	}
	
	public void setMaximum(int i) {
		scrollbar.setMaximum(i);
	}
	
	public int getValue() {
		return scrollbar.getValue();
	}
	
}
