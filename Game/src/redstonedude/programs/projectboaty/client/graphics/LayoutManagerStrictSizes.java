package redstonedude.programs.projectboaty.client.graphics;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

public class LayoutManagerStrictSizes implements LayoutManager {
	@Override
	public void removeLayoutComponent(Component comp) {
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		return null;
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		return null;
	}

	@Override
	public void layoutContainer(Container parent) {
		parent.setSize(parent.getPreferredSize());// keep elements at their preferred size
	}

	@Override
	public void addLayoutComponent(String name, Component comp) {
	}

}
