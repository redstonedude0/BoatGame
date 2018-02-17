package redstonedude.programs.projectboaty.shared.task;

import java.io.Serializable;
import java.util.ArrayList;

import redstonedude.programs.projectboaty.client.net.ClientPacketHandler;
import redstonedude.programs.projectboaty.shared.entity.Entity;
import redstonedude.programs.projectboaty.shared.entity.EntityCharacter;
import redstonedude.programs.projectboaty.shared.net.UserData;
import redstonedude.programs.projectboaty.shared.physics.Location;
import redstonedude.programs.projectboaty.shared.physics.VectorDouble;

public class TaskWander extends TaskReachLocation implements Serializable {

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
		if (assignedEntity.loc.isAbsolute) {
			// currently off boat
			target = new Location();
			target.setPos(new VectorDouble(0, 0));// nagivate to the origin of the ship
			target.isAbsolute = false;
			target.raftUUID = assignedEntity.ownerUUID;
		} else {
			// wander
			// targetLoc = new VectorDouble(1, 1);
			// get its square
			VectorDouble ePos = assignedEntity.loc.getPos();
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
			if (validSquares.isEmpty()) {
				target = new Location();
				target.setPos(new VectorDouble(0, 0));// nagivate to the origin of the ship
				target.isAbsolute = false;
				target.raftUUID = assignedEntity.ownerUUID;
				return;
			}
			int size = validSquares.size();
			int index = TaskHandler.rand.nextInt(size);
			target = new Location();
			target.setPos(validSquares.get(index));// nagivate to the origin of the ship
			target.isAbsolute = false;
			target.raftUUID = assignedEntity.ownerUUID;
		}
	}

	@Override
	public int getPriority(EntityCharacter e) {
		return 0; // anyone can wander, this value is never even checked.
	}

}
