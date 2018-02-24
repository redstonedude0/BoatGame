package redstonedude.programs.projectboaty.shared.task;

import java.awt.Graphics2D;
import java.io.Serializable;

import redstonedude.programs.projectboaty.shared.entity.EntityCharacter;
import redstonedude.programs.projectboaty.shared.entity.WrappedEntity;
import redstonedude.programs.projectboaty.shared.physics.Location;

public class TaskReachEntity extends Task implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private WrappedEntity targetEntity;
	private TaskReachLocation trl;
	
	public TaskReachEntity(WrappedEntity targetEntity) {
		super("TaskReachEntity");
		this.targetEntity = targetEntity;
		trl = new TaskReachLocation(targetEntity.entity.loc);
	}
	
	public WrappedEntity getTarget() {
		return targetEntity;
	}
	
	public void updateLocation() {
		if (targetEntity != null && targetEntity.entity != null) {
			trl.setTarget(new Location(targetEntity.entity.loc));
		} else {
			isCompleted = true; //target must have been despawned or destroyed, oh well, what a shame :(
		}
	}
	
	@Override
	public void execute(EntityCharacter assignedEntity) {
		updateLocation();
		trl.execute(assignedEntity);
		if (trl.isCompleted) {
			isCompleted = true;
		}
	}
	
	@Override
	public void passiveUpdate() {
		updateLocation();
	}
	
	public int getDistanceToTarget(EntityCharacter ec) {
		return TaskHandler.getDistanceToTarget(ec, trl.getTarget());
	}
	
	@Override
	public void draw(Graphics2D g2d) {
		trl.draw(g2d);
	}
	
	@Override
	public Priority getPriority(EntityCharacter ec) {
		return trl.getPriority(ec);
	}
	
}
