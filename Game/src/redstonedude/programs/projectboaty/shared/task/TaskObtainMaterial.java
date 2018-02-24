package redstonedude.programs.projectboaty.shared.task;

import java.awt.Graphics2D;
import java.io.Serializable;

import redstonedude.programs.projectboaty.client.net.ClientPacketHandler;
import redstonedude.programs.projectboaty.shared.entity.EntityCharacter;
import redstonedude.programs.projectboaty.shared.entity.EntityResource;
import redstonedude.programs.projectboaty.shared.net.UserData;
import redstonedude.programs.projectboaty.shared.net.serverbound.PacketRequestTileState;
import redstonedude.programs.projectboaty.shared.physics.Location;
import redstonedude.programs.projectboaty.shared.raft.ResourceStorage;
import redstonedude.programs.projectboaty.shared.raft.Tile;

public class TaskObtainMaterial extends Task implements Serializable {

	private static final long serialVersionUID = 1L;

	private EntityResource resource;
	private TaskReachLocation trl;
	private String raftUUID;

	public TaskObtainMaterial(String raftUUID, EntityResource resource) {
		super("TaskObtainMaterial");
		this.raftUUID = raftUUID;
		this.resource = resource;
		trl = new TaskReachLocation(null);//no location currently
	}
	
	public EntityResource getResource() {
		return resource;
	}
	
	public Location getTarget() {
		return trl.getTarget();
	}

	private void targetReached(EntityCharacter assignedEntity) {
		// great, for now just delete the barrel and give the character the item
		UserData ud = ClientPacketHandler.getUserData(raftUUID);
		Tile t = ud.raft.getTileAt((int) trl.getTarget().getPos().x, (int) trl.getTarget().getPos().y);
		if (t != null) {
			ResourceStorage rs = t.storage;
			if (rs != null && rs.maxNumberOfStacks == 1) {
				assignedEntity.carrying = t.storage.resources.poll();
				PacketRequestTileState prts = new PacketRequestTileState(t);
				ClientPacketHandler.sendPacket(prts); // update the server on this
				assignedEntity.sendState();//update server on this
				isCompleted = true;//completed material obtain
				return;
			}
		}
		// else something went wrong, don't set to isCompleted, let update fix us
	}

	public void setLocationTarget() {
		UserData ud = ClientPacketHandler.getUserData(raftUUID);
		if (ud != null && ud.raft != null) {
			for (Tile t : ud.raft.getTiles()) {
				ResourceStorage rs = t.storage;
				if (rs != null && rs.maxNumberOfStacks == 1) {
					EntityResource er = rs.resources.peek();
					if (er != null) {
						if (resource.resourceType == er.resourceType) {
							trl.setTarget(t.getLocation(ud));
							return;// this is our target then
						}
					}
				}
			}
		}
		// else
		trl.setTarget(null);
	}

	@Override
	public void passiveUpdate() {
		super.passiveUpdate();
		setLocationTarget();
	}

	@Override
	public Priority getPriority(EntityCharacter ec) {
		if (ec.carrying == null && trl.getTarget() != null) {
			return trl.getPriority(ec);
		}
		return Priority.getIneligible();
	}

	@Override
	public void execute(EntityCharacter assignedEntity) {
		// refresh the target
		setLocationTarget();
		if (trl.getTarget() != null) {
			trl.execute(assignedEntity);
			if (trl.isCompleted) {
				targetReached(assignedEntity);
			}
		} else {
			//else error occurred, unassign
			isOnHold = true;
		}
	}

	@Override
	public void draw(Graphics2D g2d) {
		trl.draw(g2d);
	}

}
