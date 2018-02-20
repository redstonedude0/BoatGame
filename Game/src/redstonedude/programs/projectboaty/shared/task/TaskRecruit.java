package redstonedude.programs.projectboaty.shared.task;

import java.io.Serializable;

import redstonedude.programs.projectboaty.client.net.ClientPacketHandler;
import redstonedude.programs.projectboaty.shared.entity.EntityCharacter;
import redstonedude.programs.projectboaty.shared.event.EventListener;
import redstonedude.programs.projectboaty.shared.net.serverbound.PacketRequestEntityState;
import redstonedude.programs.projectboaty.shared.physics.VectorDouble;
import redstonedude.programs.projectboaty.shared.task.Priority.PriorityType;

public class TaskRecruit extends TaskReachEntityAndWork implements Serializable, EventListener {

	private static final long serialVersionUID = 1L;
	
	public TaskRecruit() {
		super(50);// 1 second recruit time at best
		taskTypeID = "TaskRecruit";
	}

	@Override
	public void init() {
		// no initialisation needed
	}

	@Override
	public Priority getPriority(EntityCharacter ec) {
		return new Priority(PriorityType.NORMAL, getDistanceToTarget(ec));
	}

	@Override
	public void workComplete() {
//		// great, for now just actually recruit the thing
		if (targetEntity.entity != null && targetEntity.entity instanceof EntityCharacter) {
			EntityCharacter ec = (EntityCharacter) targetEntity.entity;
			if (ec.ownerUUID.equals("")) {
				ec.loc.setPos(ec.loc.getPos().add(new VectorDouble(0,1)));
				ec.ownerUUID = assignedEntity.ownerUUID;
				PacketRequestEntityState pres = new PacketRequestEntityState(ec);
				ClientPacketHandler.sendPacket(pres);
			}
		}
		isCompleted = true; // let wander bring us back or take us around the boat
	}

}
