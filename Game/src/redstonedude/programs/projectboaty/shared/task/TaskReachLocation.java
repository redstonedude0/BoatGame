package redstonedude.programs.projectboaty.shared.task;

import java.io.Serializable;

import redstonedude.programs.projectboaty.client.net.ClientPacketHandler;
import redstonedude.programs.projectboaty.shared.entity.EntityCharacter;
import redstonedude.programs.projectboaty.shared.net.UserData;
import redstonedude.programs.projectboaty.shared.physics.Location;
import redstonedude.programs.projectboaty.shared.physics.VectorDouble;

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
	
	public int getDistanceToTarget(EntityCharacter ec) {
		if (target == null) {
			return 0;
		}
		VectorDouble absoluteTargetCOM = target.getPos().add(new VectorDouble(0.5,0.5));
		VectorDouble absoluteCOM = ec.loc.getPos().add(new VectorDouble(0.5,0.5));
		if (!target.isAbsolute) {
			UserData targetUD = ClientPacketHandler.getUserData(target.raftUUID);
			absoluteTargetCOM = absoluteTargetCOM.getAbsolute(targetUD.raft.getUnitX(),targetUD.raft.getUnitY()).add(targetUD.raft.getPos());
		}
		if (!ec.loc.isAbsolute) {
			UserData ecUD = ClientPacketHandler.getUserData(ec.loc.raftUUID);
			absoluteCOM = absoluteCOM.getAbsolute(ecUD.raft.getUnitX(),ecUD.raft.getUnitY()).add(ecUD.raft.getPos());
		}
		VectorDouble change = absoluteTargetCOM.subtract(absoluteCOM);
		return (int) Math.sqrt(change.getSquaredLength());
	}
	
}
