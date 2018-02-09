package redstonedude.programs.projectboaty.raft;

import java.util.ArrayList;
import java.util.Vector;

public class Raft {
	
	public double x = 0;
	public double y = 0;
	public double theta = 0;
	public double dx = 0;
	public double dy = 0;
	public double dtheta = 0;
	
	public double sin = 0;
	public double cos = 1;
	public double comx = 0;
	public double comy = 0;
	
	public ArrayList<Tile> tiles = new ArrayList<Tile>();
	
	public Vector<Double> getUnitX() {
		Vector<Double> v = new Vector<Double>();
		v.add(cos);
		v.add(sin);
		return v;
	}
	
	public Vector<Double> getUnitY() {
		Vector<Double> v = new Vector<Double>();
		v.add(sin);
		v.add(-cos);
		return v;
	}
	
}
