package redstonedude.programs.projectboaty.shared.task;

import java.io.Serializable;

public abstract class TaskReachLocationAndWork extends TaskReachLocation implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public int workRemaining = 0;
	public int maximumWork = 100;
	
	public TaskReachLocationAndWork(int work) {
		super();
		maximumWork = work;
		workRemaining = work;
	}
	
	public abstract void workComplete();
	
	@Override
	public void execute() {
		if (assignedEntity != null && target != null) {
			if (assignedEntity.moveToward(target)) { //always move towards target, if the target is also reached then do work
				targetReached();
			}
		}
	}
	
	@Override
	public void targetReached() {
		workRemaining--;
		if (workRemaining <= 0) {
			workComplete();
		}
	}
	
}
