package redstonedude.programs.projectboaty.shared.task;

import java.awt.Graphics2D;
import java.io.Serializable;

import redstonedude.programs.projectboaty.client.net.ClientPacketHandler;
import redstonedude.programs.projectboaty.shared.entity.EntityCharacter;
import redstonedude.programs.projectboaty.shared.entity.EntityResource.ResourceType;
import redstonedude.programs.projectboaty.shared.net.UserData;
import redstonedude.programs.projectboaty.shared.net.serverbound.PacketRequestRaftTiles;
import redstonedude.programs.projectboaty.shared.physics.Location;
import redstonedude.programs.projectboaty.shared.raft.Tile;

public class TaskConstruct extends Task implements Serializable {

	private static final long serialVersionUID = 1L;
	private TaskObtainMaterial tom;
	private TaskReachLocation trl;
	private TaskPerformWork tpw;

	private Tile resultantTile;

	public TaskConstruct(Tile resultantTile, UserData raftUD) {
		super("TaskConstruct");
		this.resultantTile = resultantTile;
		trl = new TaskReachLocation(resultantTile.getLocation(raftUD));
		tpw = new TaskPerformWork(100);//2 seconds build time at best
		//tom = new TaskObtainMaterial(raftUD.uuid, null);//don't request any material currently
	}
	
	public Location getTarget() {
		return trl.getTarget();
	}

	@Override
	public Priority getPriority(EntityCharacter ec) {
		if (ec.carrying != null && ec.carrying.resourceType == ResourceType.Wood) {
			return trl.getPriority(ec);
		}
		return Priority.getIneligible();
	}
	
	public void workComplete(EntityCharacter assignedEntity) {
		// great, for now just actually build the thing
		UserData ud = ClientPacketHandler.getUserData(assignedEntity.ownerUUID);
		ud.raft.addTile(resultantTile);
		PacketRequestRaftTiles prrt = new PacketRequestRaftTiles();
		prrt.tiles = ud.raft.getTiles();
		ClientPacketHandler.sendPacket(prrt); // update the server on this
		assignedEntity.carrying = null;
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
