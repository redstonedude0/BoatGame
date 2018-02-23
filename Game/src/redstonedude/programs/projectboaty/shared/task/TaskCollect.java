package redstonedude.programs.projectboaty.shared.task;

import java.io.Serializable;

import redstonedude.programs.projectboaty.client.net.ClientPacketHandler;
import redstonedude.programs.projectboaty.client.physics.ClientPhysicsHandler;
import redstonedude.programs.projectboaty.shared.entity.EntityBarrel;
import redstonedude.programs.projectboaty.shared.entity.EntityCharacter;
import redstonedude.programs.projectboaty.shared.entity.EntityResource;
import redstonedude.programs.projectboaty.shared.net.UserData;
import redstonedude.programs.projectboaty.shared.net.serverbound.PacketRequestDelEntity;
import redstonedude.programs.projectboaty.shared.physics.Location;
import redstonedude.programs.projectboaty.shared.physics.VectorDouble;
import redstonedude.programs.projectboaty.shared.task.Priority.PriorityType;

public class TaskCollect extends TaskReachEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	public TaskCollect() {
		taskTypeID = "TaskCollect";
	}

	@Override
	public void targetReached() {
		// great, for now just delete the barrel and give the character the item
		String entityUUID = targetEntity.entity.uuid;
		EntityResource resource = null;
		if (targetEntity.entity instanceof EntityBarrel) {
			resource = ((EntityBarrel) targetEntity.entity).resource;
		}
		if (ClientPhysicsHandler.removeEntity(entityUUID)) {
			//assignedEntity.carryingBarrel = true;
			if (resource != null) {
				assignedEntity.carrying = resource;
			}
			assignedEntity.sendState();
			PacketRequestDelEntity prde = new PacketRequestDelEntity(entityUUID);
			ClientPacketHandler.sendPacket(prde);
		}
		isCompleted = true;//let wander bring us back
	}

	@Override
	public void init() {
		// no initialisation needed
	}

	@Override
	public Priority getPriority(EntityCharacter ec) {
		if (ec.carrying == null) {
			return new Priority(PriorityType.NORMAL,getDistanceToTarget(ec));
		}
		return Priority.getIneligible();
	}

}
