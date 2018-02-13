package redstonedude.programs.projectboaty.shared.entity;

import java.io.Serializable;

public class EntityCharacter extends Entity implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	public String ownerUUID = "";
	
	public EntityCharacter() {
		entityTypeID = "EntityCharacter";
	}
	
}
