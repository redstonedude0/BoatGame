package redstonedude.programs.projectboaty.raft;

public class Tile {
	
	public double x = 0;
	public double y = 0;
	public double mass = 10;
	
	public double getAbsoluteX(Raft parent) {
		return parent.x + x*parent.getUnitX().get(0)+y*parent.getUnitY().get(0);
	}
	
	public double getAbsoluteY(Raft parent) {
		return parent.y + x*parent.getUnitX().get(1)+y*parent.getUnitY().get(1);
	}
	
}
