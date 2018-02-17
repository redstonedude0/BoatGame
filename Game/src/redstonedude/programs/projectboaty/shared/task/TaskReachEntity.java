package redstonedude.programs.projectboaty.shared.task;

import java.io.Serializable;

import redstonedude.programs.projectboaty.shared.entity.WrappedEntity;

public abstract class TaskReachEntity extends TaskReachLocation implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public WrappedEntity targetEntity;
	
	public void updateLocation() {
		target = targetEntity.entity.loc;
	}
	
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
