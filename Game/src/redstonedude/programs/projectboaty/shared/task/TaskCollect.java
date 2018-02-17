package redstonedude.programs.projectboaty.shared.task;

import java.io.Serializable;

import redstonedude.programs.projectboaty.client.net.ClientPacketHandler;
import redstonedude.programs.projectboaty.client.physics.ClientPhysicsHandler;
import redstonedude.programs.projectboaty.shared.entity.EntityCharacter;
import redstonedude.programs.projectboaty.shared.net.UserData;
import redstonedude.programs.projectboaty.shared.net.serverbound.PacketRequestDelEntity;
import redstonedude.programs.projectboaty.shared.physics.Location;
import redstonedude.programs.projectboaty.shared.physics.VectorDouble;

public class TaskCollect extends TaskReachEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	// public String collectionUUID = "";
	public boolean collected = false;

	public TaskCollect() {
		taskTypeID = "TaskCollect";
	}

	@Override
	public void targetReached() {
		if (!collected) {
			// great, for now just delete the barrel and give the character the item
			String entityUUID = targetEntity.entity.uuid;
			if (ClientPhysicsHandler.removeEntity(entityUUID)) {
				assignedEntity.carryingBarrel = true;
				assignedEntity.sendState();
				PacketRequestDelEntity prde = new PacketRequestDelEntity(entityUUID);
				ClientPacketHandler.sendPacket(prde);
			}
			// now relocate the target to the ships current origin
			UserData ud = ClientPacketHandler.getUserData(assignedEntity.ownerUUID);
			target = new Location();
			target.setPos(new VectorDouble(0, 0));// nagivate to the origin of the ship
			target.isAbsolute = false;
			target.raftUUID = ud.uuid;
			collected = true;
		} else {
			// reached boat origin
			isCompleted = true;
		}
	}

	@Override
	public void init() {
		// no initialisation needed
	}

	@Override
	public int getPriority(EntityCharacter ec) {
		if (!ec.carryingBarrel) {
			return getDistanceToTarget(ec);
		}
		return INELIGIBLE;
	}

	@Override
	public void updateLocation() {
		if (!collected) {// not collected, point to entity
			super.updateLocation();
		} // else don't change target
	}

}
