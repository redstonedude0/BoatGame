package redstonedude.programs.projectboaty.shared.raft;

import java.util.ArrayList;

import redstonedude.programs.projectboaty.server.physics.VectorDouble;

public class Raft {
	
	private VectorDouble pos = new VectorDouble(); //absolute
	public double theta = 0;
	private VectorDouble velocity = new VectorDouble(); //absolute
	public double dtheta = 0;
	
	public double sin = 0;
	public double cos = 1;
	private VectorDouble COMPos = new VectorDouble(); //relative 
	
	public ArrayList<Tile> tiles = new ArrayList<Tile>();
	
	public VectorDouble getUnitX() {
		VectorDouble v = new VectorDouble();
		v.x = cos;
		v.y = sin;
		return v;
	}
	
	public VectorDouble getUnitY() {
		VectorDouble v = new VectorDouble();
		v.x = sin;
		v.y = -cos;
		return v;
	}
	
	public void setPos(VectorDouble pos) {
		this.pos = pos;
	}
	
	public VectorDouble getPos() {
		return new VectorDouble(pos);
	}
	
	public void setVelocity(VectorDouble velocity) {
		this.velocity = velocity;
	}
	
	public VectorDouble getVelocity() {
		return new VectorDouble(velocity);
	}
	
	public void setCOMPos(VectorDouble COMPos) {
		this.COMPos = COMPos;
	}
	
	public VectorDouble getCOMPos() {
		return new VectorDouble(COMPos);
	}
	
}
