package redstonedude.programs.projectboaty.shared.task;

import java.io.Serializable;

public abstract class TaskLocationMovingTarget extends TaskLocationTarget implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public abstract void updateLocation();
	
	@Override
	public void execute() {
		updateLocation();
		super.execute();
	}
	
	@Override
	public void passiveUpdate() {
		updateLocation();
	}
	
}
