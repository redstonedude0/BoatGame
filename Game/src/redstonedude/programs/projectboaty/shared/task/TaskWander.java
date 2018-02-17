package redstonedude.programs.projectboaty.shared.task;

import java.io.Serializable;
import java.util.ArrayList;

import redstonedude.programs.projectboaty.client.net.ClientPacketHandler;
import redstonedude.programs.projectboaty.client.physics.ClientPhysicsHandler;
import redstonedude.programs.projectboaty.shared.entity.Entity;
import redstonedude.programs.projectboaty.shared.entity.EntityCharacter;
import redstonedude.programs.projectboaty.shared.net.UserData;
import redstonedude.programs.projectboaty.shared.physics.VectorDouble;

public class TaskWander extends TaskLocationTarget implements Serializable {

	private static final long serialVersionUID = 1L;

	public TaskWander(EntityCharacter ent) {
		taskTypeID = "TaskWander";
		assignedEntity = ent;
	}

	@Override
	public void targetReached() {
		isCompleted = true;
	}

	@Override
	public void init() {
		// select new random tile to wander to
		if (assignedEntity.absolutePosition) {
			// currently off boat
			targetLoc = new VectorDouble(0, 0);// walk to unit square
		} else {
			// wander
			// targetLoc = new VectorDouble(1, 1);
			// get its square
			VectorDouble ePos = assignedEntity.getPos();
			int ex = (int) ePos.x;
			int ey = (int) ePos.y;
			ArrayList<VectorDouble> validSquares = new ArrayList<VectorDouble>();
			validSquares.add(new VectorDouble(ex + 1, ey));
			validSquares.add(new VectorDouble(ex - 1, ey));
			validSquares.add(new VectorDouble(ex, ey + 1));
			validSquares.add(new VectorDouble(ex, ey - 1));
			ArrayList<VectorDouble> invalidSquares = new ArrayList<VectorDouble>();
			for (VectorDouble vd : validSquares) {
				UserData ud = ClientPacketHandler.getUserData(assignedEntity.ownerUUID);
				if (ud != null && ud.raft != null) {
					if (ud.raft.getTileAt((int) vd.x, (int) vd.y) != null) {
						continue;// keep this one in
					}
				}
				invalidSquares.add(vd);
			}
			for (VectorDouble vd : invalidSquares) {
				validSquares.remove(vd);
			}
			validSquares.add(new VectorDouble(ex, ey));// stay here
			int size = validSquares.size();
			int index = TaskHandler.rand.nextInt(size);
			targetLoc = validSquares.get(index);
		}
		targetLoc_absolute = false;// on raft
		targetLoc_raftuuid = assignedEntity.ownerUUID;
	}

	@Override
	public boolean isEligible(Entity e) {
		if (e instanceof EntityCharacter) {
			return true; // anyone can wander
		}
		return false;// barrels cant wander
	}

}
