package redstonedude.programs.projectboaty.shared.task;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.io.Serializable;
import java.util.concurrent.ConcurrentLinkedQueue;

import redstonedude.programs.projectboaty.client.graphics.TextureHandler;
import redstonedude.programs.projectboaty.client.net.ClientPacketHandler;
import redstonedude.programs.projectboaty.shared.entity.EntityCharacter;
import redstonedude.programs.projectboaty.shared.entity.EntityResource;
import redstonedude.programs.projectboaty.shared.net.UserData;
import redstonedude.programs.projectboaty.shared.net.serverbound.PacketRequestRaftTiles;
import redstonedude.programs.projectboaty.shared.physics.Location;
import redstonedude.programs.projectboaty.shared.physics.VectorDouble;
import redstonedude.programs.projectboaty.shared.raft.Tile;

public class TaskConstruct extends Task implements Serializable {

	private static final long serialVersionUID = 1L;
	private TaskObtainMaterial tom;
	private TaskReachLocation trl;
	private TaskPerformWork tpw;
	private ConcurrentLinkedQueue<EntityResource> requiredResources = new ConcurrentLinkedQueue<EntityResource>();;
	private ConcurrentLinkedQueue<EntityResource> arrivedResources = new ConcurrentLinkedQueue<EntityResource>();;

	private String raftUUID;
	private Tile resultantTile;

	public TaskConstruct(Tile resultantTile, UserData raftUD) {
		super("TaskConstruct");
		this.resultantTile = resultantTile;
		trl = new TaskReachLocation(resultantTile.getLocation(raftUD));
		tpw = new TaskPerformWork(100);// 2 seconds build time at best
		raftUUID = raftUD.uuid;
		// tom = new TaskObtainMaterial(raftUUID, null);//don't request any material
		// currently
		requiredResources.addAll(resultantTile.tileType.requiredResources);
	}

	public Location getTarget() {
		return trl.getTarget();
	}

	@Override
	public Priority getPriority(EntityCharacter ec) {
		if (!requiredResources.isEmpty()) {
			TaskObtainMaterial newtom = new TaskObtainMaterial(raftUUID, (EntityResource) requiredResources.peek());
			newtom.setLocationTarget();
			if (newtom.getTarget() == null) {
				//the next resource cannot be obtained
				return Priority.getIneligible();
			}
		}
		//if (isOnHold) {
		//	return Priority.getIneligible();
		//}
		
		
		if (ec.carrying == null) {
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
		if (!requiredResources.isEmpty()) {
			// need to work on hauling the next resource, are we currently carrying one?
			if (assignedEntity.carrying != null) {
				// really ought to be carrying the required resource. Take it to the work site
				trl.execute(assignedEntity);
				if (trl.isCompleted) {
					for (EntityResource er : requiredResources) {
						if (er.resourceType == assignedEntity.carrying.resourceType) {
							requiredResources.remove(er);
							// great! the resource is here.
							// deposit this material here; (honestly if this isnt the correct material then
							// lol)
							arrivedResources.add(assignedEntity.carrying);
							// PacketRequestTileState prts = new PacketRequestTileState();
							// ClientPacketHandler.sendPacket(prts); // update the server on this
							assignedEntity.carrying = null;
							assignedEntity.sendState();// update server on this
							return;// can return now, away next tick
						}
					}
					// erm. This resource shouldn't be here.
					isOnHold = true;// place on hold to try to bug fix
					System.out.println("A weird material arrived at a construction site. This shouldn't happen");
				}
			} else {
				// carrying null, start or continue getting next one?
				if (tom == null) {
					tom = new TaskObtainMaterial(raftUUID, (EntityResource) requiredResources.peek());
				}
				tom.execute(assignedEntity);
				if (tom.isCompleted) {// great! we have the next material. carrying won't be null and we can do stuff,
					tom = null;// reset tom
				}
				if (tom != null && tom.isOnHold) {
					isOnHold = true;//tom cannot find resoruces, unassign until we find some
					tom.isOnHold = false;
				}
			}
		} else {
			// Resources empty, build the thing
			trl.execute(assignedEntity);
			if (trl.isCompleted) {
				tpw.execute(assignedEntity);
				if (tpw.isCompleted) {
					workComplete(assignedEntity);
				}
			}
		}
	}

	@Override
	public void draw(Graphics2D g2d) {
		Location target = trl.getTarget();
		trl.draw(g2d);
		tpw.draw(g2d, target);

		VectorDouble pos = target.getPos();
		AffineTransform rotator = new AffineTransform();
		if (!target.isAbsolute) {
			UserData ud = ClientPacketHandler.getUserData(target.raftUUID);
			if (ud == null) {
				return;
			}
			pos = pos.getAbsolute(ud.raft.getUnitX(), ud.raft.getUnitY()).add(ud.raft.getPos());
			rotator.translate(100 * pos.x, 100 * pos.y);
			rotator.rotate(ud.raft.theta);
		} else {
			rotator.translate(100 * pos.x, 100 * (pos.y + 1)); // correction because of height of tile?
		}
		g2d.transform(rotator);
		int index = 0;
		int maxSize = arrivedResources.size() + requiredResources.size();
		int indexWidth = 1;
		while (indexWidth*indexWidth < maxSize) {
			indexWidth++;
		}
		int width = 100/indexWidth;
		g2d.setColor(Color.GREEN);
		for (EntityResource er : arrivedResources) {
			int x = index%indexWidth;
			int y = (index-x)/indexWidth;
			x *= width;
			y *= width;
			g2d.drawImage(TextureHandler.getTexture("Resource_"+er.resourceType.textureName), x, y-100, x+width, y+width-100, 0, 0, 32, 32, null);
			g2d.drawRect(x, y-100, width, width);
			index++;
		}
		g2d.setColor(Color.RED);
		for (EntityResource er : requiredResources) {
			int x = index%indexWidth;
			int y = (index-x)/indexWidth;
			x *= width;
			y *= width;
			g2d.drawImage(TextureHandler.getTexture("Resource_"+er.resourceType.textureName), x, y-100, x+width, y+width-100, 0, 0, 32, 32, null);
			g2d.drawRect(x, y-100, width, width);
			index++;
		}
		try {
			g2d.transform(rotator.createInverse());
		} catch (NoninvertibleTransformException e) {
			e.printStackTrace();
		}

	}

}
