package redstonedude.programs.projectboaty.shared.task;

import java.awt.Graphics2D;
import java.io.Serializable;
import java.util.Arrays;

import redstonedude.programs.projectboaty.client.net.ClientPacketHandler;
import redstonedude.programs.projectboaty.shared.entity.EntityCharacter;
import redstonedude.programs.projectboaty.shared.entity.EntityResource;
import redstonedude.programs.projectboaty.shared.entity.EntityResource.ResourceType;
import redstonedude.programs.projectboaty.shared.net.UserData;
import redstonedude.programs.projectboaty.shared.net.serverbound.PacketRequestRaftTiles;
import redstonedude.programs.projectboaty.shared.physics.Location;
import redstonedude.programs.projectboaty.shared.raft.Tile;
import redstonedude.programs.projectboaty.shared.task.Priority.PriorityType;

public class TaskDeconstruct extends Task implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private TaskReachLocation trl;
	private TaskPerformWork tpw;

	public TaskDeconstruct(Tile targetTile, UserData raftUD) {
		super("TaskDeconstruct");
		trl = new TaskReachLocation(targetTile.getLocation(raftUD));
		tpw = new TaskPerformWork(100);
	}

	public Location getTarget() {
		return trl.getTarget();
	}
	
	@Override
	public Priority getPriority(EntityCharacter ec) {
		if (ec.carrying == null) {
			return new Priority(PriorityType.NORMAL,TaskHandler.getDistanceToTarget(ec,trl.getTarget()));
		}
		return Priority.getIneligible();
	}
	
	public void workComplete(EntityCharacter assignedEntity) {
		// great, for now just actually build the thing
		UserData ud = ClientPacketHandler.getUserData(assignedEntity.ownerUUID);
		Tile t = ud.raft.getTileAt((int) trl.getTarget().getPos().x, (int) trl.getTarget().getPos().y);
		ud.raft.removeAllTiles(Arrays.asList(t));
		PacketRequestRaftTiles prrt = new PacketRequestRaftTiles();
		prrt.tiles = ud.raft.getTiles();
		ClientPacketHandler.sendPacket(prrt); // update the server on this
		assignedEntity.carrying = new EntityResource(ResourceType.Wood);
		assignedEntity.sendState();
		isCompleted = true; // let wander bring us back or take us around the boat
	}

	@Override
	public void execute(EntityCharacter assignedEntity) {
		trl.execute(assignedEntity);
		if (trl.isCompleted) {
			tpw.execute(assignedEntity);
			if (tpw.isCompleted) {
				workComplete(assignedEntity);
			}
		}
	}
	
	@Override
	public void draw(Graphics2D g2d) {
		trl.draw(g2d);
		tpw.draw(g2d, trl.getTarget());
	}

}
