package redstonedude.programs.projectboaty.shared.task;

import java.awt.Graphics2D;
import java.io.Serializable;

import redstonedude.programs.projectboaty.client.net.ClientPacketHandler;
import redstonedude.programs.projectboaty.client.physics.ClientPhysicsHandler;
import redstonedude.programs.projectboaty.shared.entity.EntityBarrel;
import redstonedude.programs.projectboaty.shared.entity.EntityCharacter;
import redstonedude.programs.projectboaty.shared.entity.EntityResource;
import redstonedude.programs.projectboaty.shared.entity.WrappedEntity;
import redstonedude.programs.projectboaty.shared.net.serverbound.PacketRequestDelEntity;
import redstonedude.programs.projectboaty.shared.physics.VectorDouble;

public class TaskCollect extends Task implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private TaskReachEntity tre;

	public TaskCollect(WrappedEntity targetEntity) {
		super("TaskCollect");
		tre = new TaskReachEntity(targetEntity);
	}
	
	public WrappedEntity getTarget() {
		return tre.getTarget();
	}
	
	
	private void targetReached(EntityCharacter assignedEntity) {
		// great, for now just delete the barrel and give the character the item
		String entityUUID = tre.getTarget().entity.uuid;
		EntityResource resource = null;
		if (tre.getTarget().entity instanceof EntityBarrel) {
			resource = ((EntityBarrel) tre.getTarget().entity).resource;
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
	public Priority getPriority(EntityCharacter ec) {
		if (ec.carrying == null) {
			return tre.getPriority(ec);
		}
		return Priority.getIneligible();
	}

	@Override
	public void execute(EntityCharacter assignedEntity) {
		tre.execute(assignedEntity);
		if (tre.isCompleted) {
			targetReached(assignedEntity);
		}
		
	}
	
	@Override
	public void draw(Graphics2D g2d) {
		tre.draw(g2d);
	}
	
	@Override
	public boolean shouldCancel(VectorDouble absoluteClickedPos) {
		return tre.shouldCancel(absoluteClickedPos);
	}

}
