package redstonedude.programs.projectboaty.shared.task;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.function.Consumer;

import redstonedude.programs.projectboaty.client.net.ClientPacketHandler;
import redstonedude.programs.projectboaty.shared.entity.EntityCharacter;
import redstonedude.programs.projectboaty.shared.event.EventCharacterMountDismount;
import redstonedude.programs.projectboaty.shared.event.EventHandler;
import redstonedude.programs.projectboaty.shared.event.EventListener;
import redstonedude.programs.projectboaty.shared.event.EventTileBroken;
import redstonedude.programs.projectboaty.shared.net.UserData;
import redstonedude.programs.projectboaty.shared.physics.Location;
import redstonedude.programs.projectboaty.shared.physics.PhysicsHandler;
import redstonedude.programs.projectboaty.shared.physics.VectorDouble;
import redstonedude.programs.projectboaty.shared.raft.Tile;
import redstonedude.programs.projectboaty.shared.task.Priority.PriorityType;

public class TaskWander extends Task implements Serializable, EventListener {

	private static final long serialVersionUID = 1L;

	private TaskReachLocation trl;

	public TaskWander() {
		super("TaskWander"); // no target - assigned in init
	}

	// @Override
	// public void targetReached() {
	// isCompleted = true;
	// }

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
		if (assignedEntity.isAbsolute() || !assignedEntity.getLoc().raftUUID.equals(assignedEntity.ownerUUID)) {
			// currently off home boat, for now navigate to origin of ship
			Location target = new Location();
			target.setPos(new VectorDouble(0, 0));// nagivate to the origin of the ship
			target.isAbsolute = false;
			target.raftUUID = assignedEntity.ownerUUID;
			trl.setTarget(target);
		} else {
			// wander

			// VectorDouble ePos = assignedEntity.loc.getPos();
			// int ex = (int) ePos.x;
			// int ey = (int) ePos.y;
			// ArrayList<VectorDouble> validSquares = new ArrayList<VectorDouble>();
			// validSquares.add(new VectorDouble(ex + 1, ey));
			// validSquares.add(new VectorDouble(ex - 1, ey));
			// validSquares.add(new VectorDouble(ex, ey + 1));
			// validSquares.add(new VectorDouble(ex, ey - 1));
			// ArrayList<VectorDouble> invalidSquares = new ArrayList<VectorDouble>();
			// for (VectorDouble vd : validSquares) {
			// UserData ud = ClientPacketHandler.getUserData(assignedEntity.ownerUUID);
			// if (ud != null && ud.raft != null) {
			// if (ud.raft.getTileAt((int) vd.x, (int) vd.y) != null) {
			// continue;// keep this one in
			// }
			// }
			// invalidSquares.add(vd);
			// }
			// for (VectorDouble vd : invalidSquares) {
			// validSquares.remove(vd);
			// }
			// if (validSquares.isEmpty()) {
			// Location target = new Location();
			// target.setPos(new VectorDouble(0, 0));// nagivate to the origin of the ship
			// target.isAbsolute = false;
			// target.raftUUID = assignedEntity.ownerUUID;
			// trl.setTarget(target);
			// return;
			// }
			// int size = validSquares.size();
			// int index = TaskHandler.rand.nextInt(size);
			// Location target = new Location();
			// target.setPos(validSquares.get(index));// nagivate to the origin of the ship
			// target.isAbsolute = false;
			// target.raftUUID = assignedEntity.ownerUUID;
			// trl.setTarget(target);
			UserData ud = ClientPacketHandler.getUserData(assignedEntity.ownerUUID);
			if (ud != null && ud.raft != null) {
				// generate boundary lines, this is a list of all walls that can be walked towards
				Collection<Line2D> boundaryLines = ud.raft.getBoundaryLines();
				Random rand = new Random();
				double angle = rand.nextDouble()*Math.PI*2;//random angle
				double maxLength = 10;//start with maximum length
				VectorDouble start = assignedEntity.getLoc().getPos().add(new VectorDouble(0.5,0.5));
				Point2D angularLineOrigin = new  Point2D.Double(start.x,start.y);
				maxLength = PhysicsHandler.getLengthOfACollidedAngularLineWithBoundaryLines(angularLineOrigin, angle, maxLength, boundaryLines);
				double minLength = 0.5;//must walk a minimum distance
				maxLength -= 0.1;//subtract a little so you don't go right to the edges
				if (maxLength < 0) {//if they would walk a negative distance then make them be stationary
					maxLength = 0;
				}
				if (maxLength < minLength) {//if the maximum distance is less than the minimum (walking right into a wall)
					minLength = maxLength;//the max and min are the same - walk to the wall
				}
				double length = rand.nextDouble()*(maxLength-minLength)+minLength;
				VectorDouble endPos = new VectorDouble();
				endPos.x = start.x+Math.sin(angle)*length;
				endPos.y = start.y+Math.cos(angle)*length;
				Location target = new Location();
				target.setPos(endPos.subtract(new VectorDouble(0.5,0.5)));// nagivate to the target
				target.isAbsolute = false;
				target.raftUUID = assignedEntity.ownerUUID;
				trl.setTarget(target);
				trl.speedModifier = 0.5;//just wandering, so can go slower
				//if length is 0 then they are stationary but it still a valid path.
				return;
			}
			// not set properly, some error occurred, navigate to origin
			System.out.println("Error finding wander path");
			Location target = new Location();
			target.setPos(new VectorDouble(0, 0));// nagivate to the origin of the ship
			target.isAbsolute = false;
			target.raftUUID = assignedEntity.ownerUUID;
			trl.setTarget(target);
		}
	}
	
	@EventHandler
	public static void onMountDismount(EventCharacterMountDismount e) {
		// make it so each task is removed if the tiles line up
		if (e.entityCharacter.currentTask != null) {
			if (e.entityCharacter.currentTask instanceof TaskWander) {
				if (e.entityCharacter.isAbsolute()) {//dismounted
					e.entityCharacter.currentTask.isCompleted = true;//cancel task
				}
			}
		}
	}

	@Override
	public Priority getPriority(EntityCharacter e) {
		// anyone can wander, this value is never even checked.
		return new Priority(PriorityType.NORMAL, 0);
	}

}
