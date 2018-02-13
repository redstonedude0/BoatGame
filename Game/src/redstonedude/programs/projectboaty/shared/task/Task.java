package redstonedude.programs.projectboaty.shared.task;

import java.io.Serializable;

public abstract class Task implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public String taskTypeID = "Null";
	public String assignedEntityID = "";
	
	public abstract void execute();
	
	
}
