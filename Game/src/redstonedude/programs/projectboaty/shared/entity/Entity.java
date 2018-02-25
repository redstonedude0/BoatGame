package redstonedude.programs.projectboaty.shared.entity;

import java.io.Serializable;
import java.util.UUID;

import redstonedude.programs.projectboaty.shared.physics.Location;

public class Entity implements Serializable {
	
	private static final long serialVersionUID = 1L;
	public String entityTypeID = "Null";
	public String uuid = "";
	private Location loc = new Location();
	
	public Entity() {
		uuid = UUID.randomUUID().toString();
	}
	
	public Location getLoc() {
		return new Location(loc);
	}
	
	public void setLoc(Location loc) {
		this.loc = loc;
	}
	
	public boolean isAbsolute() {
		return loc.isAbsolute;
	}
	
}
