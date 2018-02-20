package redstonedude.programs.projectboaty.shared.physics;

import java.io.Serializable;

public class Location implements Serializable {

	private static final long serialVersionUID = 1L;

	private VectorDouble pos = new VectorDouble();
	public boolean isAbsolute = true;
	public String raftUUID = ""; // if not absolute
	
	public Location() {
	}
	
	public Location(Location l) {
		pos = new VectorDouble(l.pos);
		isAbsolute = l.isAbsolute;
		raftUUID = l.raftUUID;
	}
	
	public VectorDouble getPos() {
		return new VectorDouble(pos);
	}
	
	public void setPos(VectorDouble v) {
		pos = v;
	}
}
