package redstonedude.programs.projectboaty.shared.task;

import java.io.Serializable;
import java.util.Arrays;

import redstonedude.programs.projectboaty.client.net.ClientPacketHandler;
import redstonedude.programs.projectboaty.shared.entity.EntityCharacter;
import redstonedude.programs.projectboaty.shared.net.UserData;
import redstonedude.programs.projectboaty.shared.net.serverbound.PacketRequestRaftTiles;
import redstonedude.programs.projectboaty.shared.physics.Location;
import redstonedude.programs.projectboaty.shared.physics.VectorDouble;
import redstonedude.programs.projectboaty.shared.raft.Tile;
import redstonedude.programs.projectboaty.shared.task.Priority.PriorityType;

public class TaskDeconstruct extends TaskReachLocationAndWork implements Serializable {

	private static final long serialVersionUID = 1L;

	public TaskDeconstruct() {
		super(100);//2 seconds destroy time at best
		taskTypeID = "TaskConstruct";
	}

	@Override
	public void init() {
		// no initialisation needed
	}

	@Override
	public Priority getPriority(EntityCharacter ec) {
		if (!ec.carryingBarrel) {
			return new Priority(PriorityType.NORMAL,getDistanceToTarget(ec));
		}
		return Priority.getIneligible();
	}

	@Override
	public void workComplete() {
		// great, for now just actually build the thing
		UserData ud = ClientPacketHandler.getUserData(assignedEntity.ownerUUID);
		Tile t = ud.raft.getTileAt((int) target.getPos().x, (int) target.getPos().y);
		ud.raft.removeAllTiles(Arrays.asList(t));
		PacketRequestRaftTiles prrt = new PacketRequestRaftTiles();
		prrt.tiles = ud.raft.getTiles();
		ClientPacketHandler.sendPacket(prrt); // update the server on this
		assignedEntity.carryingBarrel = true;
		assignedEntity.sendState();
		isCompleted = true; // let wander bring us back or take us around the boat
	}

}
