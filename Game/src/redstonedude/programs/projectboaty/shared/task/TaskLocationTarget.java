package redstonedude.programs.projectboaty.shared.task;

import java.io.Serializable;

import redstonedude.programs.projectboaty.client.net.ClientPacketHandler;
import redstonedude.programs.projectboaty.client.physics.ClientPhysicsHandler;
import redstonedude.programs.projectboaty.server.physics.VectorDouble;
import redstonedude.programs.projectboaty.shared.entity.Entity;
import redstonedude.programs.projectboaty.shared.net.UserData;

public abstract class TaskLocationTarget extends Task implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public VectorDouble targetLoc;
	public boolean targetLoc_absolute;
	public String targetLoc_raftuuid;
	
	
	public abstract void targetReached();
	
	@Override
	public void execute() {
		Entity e = ClientPhysicsHandler.getEntity(assignedEntityID);
		if (e != null) {
			if (targetLoc_absolute && !e.absolutePosition) {
				//need to jump in water
				UserData ud = ClientPacketHandler.getUserData(e.raftUUID);
				e.absolutePosition = true;
				e.setPos(e.getPos().getAbsolute(ud.raft.getUnitX(), ud.raft.getUnitY()).add(ud.raft.getPos()));
				e.raftUUID="";
			}
			if (!targetLoc_absolute && e.absolutePosition) {
				//need to climb aboard
				//oh well, screw it
			}
			VectorDouble pos = e.getPos();
			VectorDouble diff = targetLoc.subtract(pos);
			if (diff.getSquaredLength() <= 0.01) {
				//less than 0.1 away
				e.setPos(targetLoc);
				targetReached();
			} else {
				diff = diff.setMagnitude(0.1);
				e.setPos(pos.add(diff));
			}
		}
	}
	
}
