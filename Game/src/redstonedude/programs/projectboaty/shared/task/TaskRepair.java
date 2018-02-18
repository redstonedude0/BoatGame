package redstonedude.programs.projectboaty.shared.task;

import java.io.Serializable;

import redstonedude.programs.projectboaty.client.net.ClientPacketHandler;
import redstonedude.programs.projectboaty.shared.entity.EntityCharacter;
import redstonedude.programs.projectboaty.shared.net.UserData;
import redstonedude.programs.projectboaty.shared.net.serverbound.PacketRequestTileState;
import redstonedude.programs.projectboaty.shared.raft.Tile;
import redstonedude.programs.projectboaty.shared.task.Priority.PriorityType;

public class TaskRepair extends TaskReachLocationAndWork implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public TaskRepair() {
		super(50);//1 second build time at best
		taskTypeID = "TaskConstruct";
	}

	@Override
	public void init() {
		// no initialisation needed
	}

	@Override
	public Priority getPriority(EntityCharacter ec) {
		if (ec.carryingBarrel) {
			return new Priority(PriorityType.CRITICAL,getDistanceToTarget(ec));
		}
		return Priority.getIneligible();
	}

	@Override
	public void workComplete() {
		// great, for now just actually repair the thing
		UserData ud = ClientPacketHandler.getUserData(assignedEntity.ownerUUID);
		Tile t = ud.raft.getTileAt((int) target.getPos().x,(int) target.getPos().y);
		if (t != null) {
			t.hp = 100;//maximise HP
			PacketRequestTileState prts = new PacketRequestTileState(t);
			ClientPacketHandler.sendPacket(prts); // update the server on this
			assignedEntity.carryingBarrel = false;
			assignedEntity.sendState();
		}
		isCompleted = true; // let wander bring us back or take us around the boat
	}

}
