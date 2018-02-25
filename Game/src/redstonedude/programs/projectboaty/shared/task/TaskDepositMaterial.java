package redstonedude.programs.projectboaty.shared.task;

import java.awt.Graphics2D;
import java.io.Serializable;

import redstonedude.programs.projectboaty.client.net.ClientPacketHandler;
import redstonedude.programs.projectboaty.shared.entity.EntityCharacter;
import redstonedude.programs.projectboaty.shared.entity.EntityResource;
import redstonedude.programs.projectboaty.shared.net.UserData;
import redstonedude.programs.projectboaty.shared.net.serverbound.PacketRequestTileState;
import redstonedude.programs.projectboaty.shared.physics.Location;
import redstonedude.programs.projectboaty.shared.physics.VectorDouble;
import redstonedude.programs.projectboaty.shared.raft.ResourceStorage;
import redstonedude.programs.projectboaty.shared.raft.Tile;

public class TaskDepositMaterial extends Task implements Serializable {

	private static final long serialVersionUID = 1L;

	private TaskReachLocation trl;
	private String raftUUID;

	public TaskDepositMaterial(String raftUUID) {
		super("TaskDepositMaterial");
		this.raftUUID = raftUUID;
		trl = new TaskReachLocation(null);//no target loation initially
	}
	
	public Location getTarget() {
		return trl.getTarget();
	}

	private void targetReached(EntityCharacter assignedEntity) {
		UserData ud = ClientPacketHandler.getUserData(raftUUID);
		Tile t = ud.raft.getTileAt((int) trl.getTarget().getPos().x, (int) trl.getTarget().getPos().y);
		if (t != null) {
			ResourceStorage rs = t.storage;
			if (rs != null && rs.maxNumberOfStacks == 1 && rs.resources.isEmpty()) {
				t.storage.resources.add(assignedEntity.carrying);
				PacketRequestTileState prts = new PacketRequestTileState(t);
				ClientPacketHandler.sendPacket(prts); // update the server on this
				assignedEntity.carrying = null;
				assignedEntity.sendState();//update server on this
				isCompleted = true;// completed material deposit
				return;
			}
		}
		// else something went wrong
	}

	public void setLocationTarget(EntityCharacter ec) {
		UserData ud = ClientPacketHandler.getUserData(raftUUID);
		if (ud != null && ud.raft != null) {
			for (Tile t : ud.raft.getTiles()) {
				ResourceStorage rs = t.storage;
				if (rs != null && rs.maxNumberOfStacks == 1) {
					EntityResource er = rs.resources.peek();
					if (er == null) { //space here
						trl.setTarget(t.getLocation(ud));
						return;// this is our target then
					}
				}
			}
		}
		// else
		trl.setTarget(null);
	}

	@Override
	public Priority getPriority(EntityCharacter ec) {
		if (ec.carrying != null) {
			setLocationTarget(ec);
			if (trl.getTarget() != null) {	
				return trl.getPriority(ec);
			}
		}
		return Priority.getIneligible();
	}

	@Override
	public void execute(EntityCharacter assignedEntity) {
		// refresh the target
		setLocationTarget(assignedEntity);
		if (trl.getTarget() != null) {
			trl.execute(assignedEntity);
			if (trl.isCompleted) {
				targetReached(assignedEntity);
			}
		} else {
			//else error occurred, delete task
			isCompleted = true;
		}
	}

	@Override
	public void draw(Graphics2D g2d) {
		trl.draw(g2d);
	}

}
