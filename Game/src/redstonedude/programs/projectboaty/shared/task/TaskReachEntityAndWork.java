package redstonedude.programs.projectboaty.shared.task;

import java.io.Serializable;

import redstonedude.programs.projectboaty.shared.entity.WrappedEntity;
import redstonedude.programs.projectboaty.shared.physics.Location;

public abstract class TaskReachEntityAndWork extends TaskReachLocationAndWork implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public WrappedEntity targetEntity;
	
	public TaskReachEntityAndWork(int work) {
		super(work);
		
	}
	
	public void updateLocation() {
		if (targetEntity != null && targetEntity.entity != null) {
			target = new Location(targetEntity.entity.loc);
		} else {
			isCompleted = true; //target must have been despawned or destroyed, oh well, what a shame :(
		}
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
