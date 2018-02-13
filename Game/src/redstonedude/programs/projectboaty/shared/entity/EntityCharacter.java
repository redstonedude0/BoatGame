package redstonedude.programs.projectboaty.shared.entity;

import java.io.Serializable;

import redstonedude.programs.projectboaty.shared.task.Task;

public class EntityCharacter extends Entity implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	public String ownerUUID = "";
	public Task currentTask = null;
	public boolean carryingBarrel = false;
	
	public EntityCharacter() {
		super();
		entityTypeID = "EntityCharacter";
	}
	
}
