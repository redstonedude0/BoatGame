package redstonedude.programs.projectboaty.shared.entity;

import java.io.Serializable;
import java.util.UUID;

import redstonedude.programs.projectboaty.server.physics.VectorDouble;

public class Entity implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private VectorDouble pos = new VectorDouble();
	public String entityTypeID = "Null";
	public String uuid = "";
	public boolean absolutePosition = false;
	public String raftUUID = "";
	
	public Entity() {
		uuid = UUID.randomUUID().toString();
	}
	
	public void setPos(VectorDouble pos) {
		this.pos = pos;
	}
	
	public VectorDouble getPos() {
		return new VectorDouble(pos);
	}
	
}
