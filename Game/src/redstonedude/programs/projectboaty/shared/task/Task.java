package redstonedude.programs.projectboaty.shared.task;

import java.io.Serializable;

import redstonedude.programs.projectboaty.shared.entity.Entity;

public abstract class Task implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public String taskTypeID = "Null";
	public String assignedEntityID = "";
	public boolean completed = false;
	
	public abstract void execute();
	
	public abstract void init();
	
	public abstract boolean isEligible(Entity e);
	
	public void passiveUpdate() {
		//do nothing passively
	}
	
}
