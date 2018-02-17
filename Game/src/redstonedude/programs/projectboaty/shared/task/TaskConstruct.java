package redstonedude.programs.projectboaty.shared.task;

import java.io.Serializable;

import redstonedude.programs.projectboaty.client.net.ClientPacketHandler;
import redstonedude.programs.projectboaty.shared.entity.Entity;
import redstonedude.programs.projectboaty.shared.entity.EntityCharacter;
import redstonedude.programs.projectboaty.shared.net.UserData;
import redstonedude.programs.projectboaty.shared.net.serverbound.PacketRequestRaftTiles;
import redstonedude.programs.projectboaty.shared.physics.Location;
import redstonedude.programs.projectboaty.shared.physics.VectorDouble;
import redstonedude.programs.projectboaty.shared.raft.Tile;

public class TaskConstruct extends TaskReachLocation implements Serializable {

	private static final long serialVersionUID = 1L;

	public Tile resultantTile;

	public TaskConstruct() {
		taskTypeID = "TaskConstruct";
	}

	@Override
	public void targetReached() {
		// great, for now just build the thing
		UserData ud = ClientPacketHandler.getUserData(assignedEntity.ownerUUID);
		ud.raft.addTile(resultantTile);
		PacketRequestRaftTiles prrt = new PacketRequestRaftTiles();
		prrt.tiles = ud.raft.getTiles();
		ClientPacketHandler.sendPacket(prrt); // update the server on this
		assignedEntity.carryingBarrel = false;
		assignedEntity.sendState();
		target = new Location();
		target.setPos(new VectorDouble(0, 0));// nagivate to the origin of the ship
		target.isAbsolute = false;
		target.raftUUID = ud.uuid;
		isCompleted = true; // let wander bring us back or take us around the boat
	}

	@Override
	public void init() {
		// no initialisation needed
	}

	@Override
	public int getPriority(EntityCharacter ec) {
		if (ec.carryingBarrel) {
			return getDistanceToTarget(ec);
		}
		return INELIGIBLE;
	}

}
