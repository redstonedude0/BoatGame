package redstonedude.programs.projectboaty.shared.task;

import java.io.Serializable;

import redstonedude.programs.projectboaty.client.net.ClientPacketHandler;
import redstonedude.programs.projectboaty.client.physics.ClientPhysicsHandler;
import redstonedude.programs.projectboaty.shared.entity.Entity;
import redstonedude.programs.projectboaty.shared.net.UserData;
import redstonedude.programs.projectboaty.shared.physics.Location;
import redstonedude.programs.projectboaty.shared.physics.VectorDouble;

public abstract class TaskReachLocation extends Task implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public Location target;
	public VectorDouble targetLoc;
	public boolean targetLoc_absolute;
	public String targetLoc_raftuuid;
	
	
	public abstract void targetReached();
	
	@Override
	public void execute() {
		if (assignedEntity != null) {
			if (targetLoc_absolute && !assignedEntity.loc.isAbsolute) {
				//need to jump in water, need COM to be in same pos so adjust by 0.5
				UserData ud = ClientPacketHandler.getUserData(assignedEntity.loc.raftUUID);
				assignedEntity.loc.isAbsolute = true;
				assignedEntity.loc.setPos(assignedEntity.loc.getPos().add(new VectorDouble(0.5,0.5)).getAbsolute(ud.raft.getUnitX(), ud.raft.getUnitY()).add(ud.raft.getPos()).subtract(new VectorDouble(0.5,0.5)));
				assignedEntity.loc.raftUUID="";
			}
			VectorDouble target = targetLoc;
			if (!targetLoc_absolute && assignedEntity.loc.isAbsolute) {
				//System.out.println("using different target");
				//need to navigate to ship and then climb aboard
				//overwrite target to point to absolute
				UserData ud = ClientPacketHandler.getUserData(targetLoc_raftuuid);
				target = target.add(new VectorDouble(0.5, 0.5)).getAbsolute(ud.raft.getUnitX(),ud.raft.getUnitY()).add(ud.raft.getPos()).subtract(new VectorDouble(0.5,0.5));
			}
			VectorDouble pos = assignedEntity.loc.getPos();
			VectorDouble diff = target.subtract(pos);
			if (diff.getSquaredLength() <= 0.01) {
				//less than 0.1 away
				assignedEntity.loc.setPos(targetLoc);
				assignedEntity.loc.isAbsolute = targetLoc_absolute;
				assignedEntity.loc.raftUUID = targetLoc_raftuuid;
				targetReached();
			} else {
				diff = diff.setMagnitude(0.1);
				assignedEntity.loc.setPos(pos.add(diff));
			}
		}
	}
	
}
