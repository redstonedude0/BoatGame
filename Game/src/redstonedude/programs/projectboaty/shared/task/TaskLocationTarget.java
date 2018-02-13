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
				//need to jump in water, need COM to be in same pos so adjust by 0.5
				UserData ud = ClientPacketHandler.getUserData(e.raftUUID);
				e.absolutePosition = true;
				e.setPos(e.getPos().add(new VectorDouble(0.5,0.5)).getAbsolute(ud.raft.getUnitX(), ud.raft.getUnitY()).add(ud.raft.getPos()).subtract(new VectorDouble(0.5,0.5)));
				e.raftUUID="";
			}
			VectorDouble target = targetLoc;
			if (!targetLoc_absolute && e.absolutePosition) {
				//System.out.println("using different target");
				//need to navigate to ship and then climb aboard
				//overwrite target to point to absolute
				UserData ud = ClientPacketHandler.getUserData(targetLoc_raftuuid);
				target = target.add(new VectorDouble(0.5, 0.5)).getAbsolute(ud.raft.getUnitX(),ud.raft.getUnitY()).add(ud.raft.getPos()).subtract(new VectorDouble(0.5,0.5));
			}
			VectorDouble pos = e.getPos();
			VectorDouble diff = target.subtract(pos);
			if (diff.getSquaredLength() <= 0.01) {
				//less than 0.1 away
				e.setPos(targetLoc);
				e.absolutePosition = targetLoc_absolute;
				e.raftUUID = targetLoc_raftuuid;
				targetReached();
			} else {
				diff = diff.setMagnitude(0.1);
				e.setPos(pos.add(diff));
			}
		}
	}
	
}
