package redstonedude.programs.projectboaty.shared.task;

import java.io.Serializable;

import redstonedude.programs.projectboaty.shared.entity.Entity;
import redstonedude.programs.projectboaty.shared.entity.EntityCharacter;

public abstract class Task implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public String taskTypeID = "Null";
	public EntityCharacter assignedEntity;
	public boolean isCompleted = false;
	
	public abstract void execute();
	
	public abstract void init();
	
	public abstract boolean isEligible(Entity e);
	
	public void passiveUpdate() {
		//do nothing passively
	}
	
	public void slowPassiveUpdate() {
		//do nothing passively
	}
	
	public void slowUpdate() {
		
	}
	
}
