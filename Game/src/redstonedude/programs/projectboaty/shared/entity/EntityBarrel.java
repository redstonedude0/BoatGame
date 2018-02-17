package redstonedude.programs.projectboaty.shared.entity;

import java.io.Serializable;

import redstonedude.programs.projectboaty.shared.physics.VectorDouble;

public class EntityBarrel extends Entity implements Serializable{
	
	private static final long serialVersionUID = 2L;
	
	private VectorDouble vel = new VectorDouble();
	
	public EntityBarrel() {
		super();
		entityTypeID = "EntityBarrel";
	}
	
	public void setVel(VectorDouble v) {
		this.vel = v;
	}
	
	public VectorDouble getVel() {
		return new VectorDouble(vel);
	}
	
}
