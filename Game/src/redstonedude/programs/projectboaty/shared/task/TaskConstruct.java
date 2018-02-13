package redstonedude.programs.projectboaty.shared.task;

import java.io.Serializable;

import redstonedude.programs.projectboaty.client.net.ClientPacketHandler;
import redstonedude.programs.projectboaty.client.physics.ClientPhysicsHandler;
import redstonedude.programs.projectboaty.server.physics.VectorDouble;
import redstonedude.programs.projectboaty.shared.entity.Entity;
import redstonedude.programs.projectboaty.shared.entity.EntityCharacter;
import redstonedude.programs.projectboaty.shared.net.UserData;
import redstonedude.programs.projectboaty.shared.raft.Tile;

public class TaskConstruct extends TaskLocationTarget implements Serializable {

	private static final long serialVersionUID = 1L;

	public boolean constructed = false;
	public Tile resultantTile;
	
	public TaskConstruct() {
		taskTypeID = "TaskConstruct";
	}

	@Override
	public void targetReached() {
		if (!constructed) {
			// great, for now just build the thing
			EntityCharacter ce = (EntityCharacter) ClientPhysicsHandler.getEntity(assignedEntityID);
			UserData ud = ClientPacketHandler.getUserData(ce.ownerUUID);
			ud.raft.addTile(resultantTile);
			ce.carryingBarrel = false;
			// now relocate the target to the ships current origin
			targetLoc = new VectorDouble(0, 0);// nagivate to the origin of the ship
			targetLoc_absolute = false;
			targetLoc_raftuuid = ud.uuid;
			constructed = true;
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
			if (ec.carryingBarrel) {
				return true;
			}
		}
		return false;
	}

}
