package redstonedude.programs.projectboaty.shared.task;

import java.io.Serializable;
import java.util.ArrayList;

import redstonedude.programs.projectboaty.client.net.ClientPacketHandler;
import redstonedude.programs.projectboaty.shared.entity.EntityCharacter;
import redstonedude.programs.projectboaty.shared.net.UserData;
import redstonedude.programs.projectboaty.shared.physics.Location;
import redstonedude.programs.projectboaty.shared.physics.VectorDouble;
import redstonedude.programs.projectboaty.shared.task.Priority.PriorityType;

public class TaskWander extends Task implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private TaskReachLocation trl;

	public TaskWander() {
		super("TaskWander"); //no target - assigned in init
	}

	//@Override
	//public void targetReached() {
	//	isCompleted = true;
	//}
	
	@Override
	public void execute(EntityCharacter assignedEntity) {
		trl.execute(assignedEntity);
		if (trl.isCompleted) {
			isCompleted = true;
		}
	}

	@Override
	public void init(EntityCharacter assignedEntity) {
		super.init(assignedEntity);
		trl = new TaskReachLocation(null);
		// select new random tile to wander to
		if (assignedEntity.loc.isAbsolute) {
			// currently off boat
			Location target = new Location();
			target.setPos(new VectorDouble(0, 0));// nagivate to the origin of the ship
			target.isAbsolute = false;
			target.raftUUID = assignedEntity.ownerUUID;
			trl.setTarget(target);
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
				Location target = new Location();
				target.setPos(new VectorDouble(0, 0));// nagivate to the origin of the ship
				target.isAbsolute = false;
				target.raftUUID = assignedEntity.ownerUUID;
				trl.setTarget(target);
				return;
			}
			int size = validSquares.size();
			int index = TaskHandler.rand.nextInt(size);
			Location target = new Location();
			target.setPos(validSquares.get(index));// nagivate to the origin of the ship
			target.isAbsolute = false;
			target.raftUUID = assignedEntity.ownerUUID;
			trl.setTarget(target);
		}
	}

	@Override
	public Priority getPriority(EntityCharacter e) {
		// anyone can wander, this value is never even checked.
		return new Priority(PriorityType.NORMAL,0);
	}

}
