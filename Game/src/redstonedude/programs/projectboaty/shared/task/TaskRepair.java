package redstonedude.programs.projectboaty.shared.task;

import java.awt.Graphics2D;
import java.io.Serializable;
import java.util.function.Consumer;

import redstonedude.programs.projectboaty.client.net.ClientPacketHandler;
import redstonedude.programs.projectboaty.shared.entity.EntityCharacter;
import redstonedude.programs.projectboaty.shared.entity.EntityResource.ResourceType;
import redstonedude.programs.projectboaty.shared.event.EventHandler;
import redstonedude.programs.projectboaty.shared.event.EventListener;
import redstonedude.programs.projectboaty.shared.event.EventTileBroken;
import redstonedude.programs.projectboaty.shared.net.UserData;
import redstonedude.programs.projectboaty.shared.net.serverbound.PacketRequestTileState;
import redstonedude.programs.projectboaty.shared.physics.Location;
import redstonedude.programs.projectboaty.shared.physics.VectorDouble;
import redstonedude.programs.projectboaty.shared.raft.Tile;
import redstonedude.programs.projectboaty.shared.task.Priority.PriorityType;

public class TaskRepair extends Task implements Serializable, EventListener {

	private static final long serialVersionUID = 1L;

	private TaskReachLocation trl;
	private TaskPerformWork tpw;
	
	public TaskRepair(Location target) {
		super("TaskRepair");
		trl = new TaskReachLocation(target);
		tpw = new TaskPerformWork(50);
	}
	
	public Location getTarget() {
		return trl.getTarget();
	}
	
	@Override
	public Priority getPriority(EntityCharacter ec) {
		if (ec.carrying != null && ec.carrying.resourceType == ResourceType.Wood) {
			return new Priority(PriorityType.CRITICAL, TaskHandler.getDistanceToTarget(ec, trl.getTarget()));
		}
		return Priority.getIneligible();
	}

	public void workComplete(EntityCharacter assignedEntity) {
		// great, for now just actually repair the thing
		UserData ud = ClientPacketHandler.getUserData(assignedEntity.ownerUUID);
		Tile t = ud.raft.getTileAt((int) trl.getTarget().getPos().x, (int) trl.getTarget().getPos().y);
		if (t != null) {
			t.hp = 100;// maximise HP
			PacketRequestTileState prts = new PacketRequestTileState(t);
			ClientPacketHandler.sendPacket(prts); // update the server on this
			assignedEntity.carrying = null;
			assignedEntity.sendState();
		}
		isCompleted = true; // let wander bring us back or take us around the boat
	}

	@EventHandler
	public static void onTileBreak(EventTileBroken e) {
		// make it so each task is removed if the tiles line up
		e.parent.getAllTasks().forEach(new Consumer<Task>() {
			@Override
			public void accept(Task t) {
				if (t instanceof TaskRepair) {
					TaskRepair tr = (TaskRepair) t;
					if (tr.trl.getTarget().getPos().equals(e.tile.getPos())) {
						//its a repair task on this raft, it's going to be correct
						tr.isCompleted = true; //'cancel' it
					}
				}
			}
		});
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

	@Override
	public boolean shouldCancel(VectorDouble absoluteClickedPos) {
		return trl.shouldCancel(absoluteClickedPos);
	}
	
}
