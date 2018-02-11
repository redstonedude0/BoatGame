package redstonedude.programs.projectboaty.raft;

import redstonedude.programs.projectboaty.physics.VectorDouble;

public class Tile {
	
	private VectorDouble pos = new VectorDouble();
	public double mass = 10;
	
	public double getAbsoluteX(Raft parent) {
		return parent.getPos().x + pos.x*parent.getUnitX().x+pos.y*parent.getUnitY().x;
	}
	
	public double getAbsoluteY(Raft parent) {
		return parent.getPos().y + pos.x*parent.getUnitX().y+pos.y*parent.getUnitY().y;
	}
	
	public void setPos(VectorDouble pos) {
		this.pos = pos;
	}
	
	public VectorDouble getPos() {
		return new VectorDouble(pos);
	}
	
}
