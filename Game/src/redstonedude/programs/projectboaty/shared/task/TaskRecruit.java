package redstonedude.programs.projectboaty.shared.task;

import java.awt.Graphics2D;
import java.io.Serializable;

import redstonedude.programs.projectboaty.client.net.ClientPacketHandler;
import redstonedude.programs.projectboaty.shared.entity.EntityCharacter;
import redstonedude.programs.projectboaty.shared.entity.WrappedEntity;
import redstonedude.programs.projectboaty.shared.event.EventListener;
import redstonedude.programs.projectboaty.shared.net.serverbound.PacketRequestEntityState;
import redstonedude.programs.projectboaty.shared.physics.VectorDouble;

public class TaskRecruit extends Task implements Serializable, EventListener {

	private static final long serialVersionUID = 1L;
	private TaskReachEntity tre;
	private TaskPerformWork tpw;
	
	public TaskRecruit(WrappedEntity targetEntity) {
		super("TaskRecruit");
		tre = new TaskReachEntity(targetEntity);
		tpw = new TaskPerformWork(50);
	}
	
	public WrappedEntity getTarget() {
		return tre.getTarget();
	}

	@Override
	public Priority getPriority(EntityCharacter ec) {
		return tre.getPriority(ec);
	}
	
	public void workComplete(EntityCharacter assignedEntity) {
//		// great, for now just actually recruit the thing
		if (tre.getTarget().entity != null && tre.getTarget().entity instanceof EntityCharacter) {
			EntityCharacter ec = (EntityCharacter) tre.getTarget().entity;
			if (ec.ownerUUID.equals("")) {
				ec.loc.setPos(ec.loc.getPos().add(new VectorDouble(0,1)));
				ec.ownerUUID = assignedEntity.ownerUUID;
				PacketRequestEntityState pres = new PacketRequestEntityState(ec);
				ClientPacketHandler.sendPacket(pres);
			}
		}
		isCompleted = true; // let wander bring us back or take us around the boat
	}
	
	@Override
	public void execute(EntityCharacter assignedEntity) {
		tre.execute(assignedEntity);
		if (tre.isCompleted) {
			tpw.execute(assignedEntity);
			if (tpw.isCompleted) {
				workComplete(assignedEntity);
			}
		}
	}
	
	@Override
	public void draw(Graphics2D g2d) {
		tre.draw(g2d);
		tpw.draw(g2d, tre.getTarget().entity.loc);
	}

}
