package redstonedude.programs.projectboaty.shared.task;

import java.io.Serializable;

import redstonedude.programs.projectboaty.shared.physics.Location;

public abstract class TaskReachLocation extends Task implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public Location target;
	
	public abstract void targetReached();
	
	@Override
	public void execute() {
		if (assignedEntity != null && target != null) {
			if (assignedEntity.moveToward(target)) {
				targetReached();
			}
		}
	}
	
}
