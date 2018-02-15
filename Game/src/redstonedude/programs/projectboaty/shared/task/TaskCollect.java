package redstonedude.programs.projectboaty.shared.task;

import java.io.Serializable;

import redstonedude.programs.projectboaty.client.net.ClientPacketHandler;
import redstonedude.programs.projectboaty.client.physics.ClientPhysicsHandler;
import redstonedude.programs.projectboaty.server.physics.VectorDouble;
import redstonedude.programs.projectboaty.shared.entity.Entity;
import redstonedude.programs.projectboaty.shared.entity.EntityCharacter;
import redstonedude.programs.projectboaty.shared.net.UserData;

public class TaskCollect extends TaskLocationMovingTarget implements Serializable {

	private static final long serialVersionUID = 1L;

	public String collectionUUID = "";
	public boolean collected = false;

	public TaskCollect() {
		taskTypeID = "TaskCollect";
	}

	@Override
	public void targetReached() {
		if (!collected) {
			// great, for now just delete the barrel and give the character the item
			EntityCharacter ce = (EntityCharacter) ClientPhysicsHandler.getEntity(assignedEntityID);
			if (ClientPhysicsHandler.removeEntity(collectionUUID)){
				ce.carryingBarrel = true;
				ce.sendState();
			}
			// now relocate the target to the ships current origin
			UserData ud = ClientPacketHandler.getUserData(ce.ownerUUID);
			targetLoc = new VectorDouble(0, 0);// nagivate to the origin of the ship
			targetLoc_absolute = false;
			targetLoc_raftuuid = ud.uuid;
			collected = true;
		} else {
			//reached boat origin
			completed = true;
		}
	}

	@Override
	public void init() {
		// no initialisation needed
	}

	@Override
	public boolean isEligible(Entity e) {
		if (e instanceof EntityCharacter) {
			EntityCharacter ec = (EntityCharacter) e;
			if (!ec.carryingBarrel) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void updateLocation() {
		Entity target = ClientPhysicsHandler.getEntity(collectionUUID);
		if (target != null) {
			targetLoc = target.getPos();
		}
	}

}
