package redstonedude.programs.projectboaty.shared.task;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.io.Serializable;

import redstonedude.programs.projectboaty.client.graphics.TextureHandler;
import redstonedude.programs.projectboaty.client.net.ClientPacketHandler;
import redstonedude.programs.projectboaty.shared.entity.EntityCharacter;
import redstonedude.programs.projectboaty.shared.net.UserData;
import redstonedude.programs.projectboaty.shared.physics.Location;
import redstonedude.programs.projectboaty.shared.physics.VectorDouble;
import redstonedude.programs.projectboaty.shared.task.Priority.PriorityType;

public class TaskReachLocation extends Task implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Location target;
	public double speedModifier = 1;
	
	public TaskReachLocation(Location target) {
		super("TaskReachLocation");
		this.target = target;
	}
	
	public Location getTarget() {
		return target;
	}
	
	public void setTarget(Location target) {
		this.target = target;
	}
	
	@Override
	public void execute(EntityCharacter assignedEntity) {
		isCompleted = false;
		if (assignedEntity != null && target != null) {
			if (assignedEntity.moveToward(target, speedModifier)) {
				isCompleted = true;
			}
		}
	}
	
	@Override
	public Priority getPriority(EntityCharacter ec) {
		return new Priority(PriorityType.NORMAL, TaskHandler.getDistanceToTarget(ec,target));
	}

	@Override
	public void draw(Graphics2D g2d) {
		if (target != null) {
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
				rotator.translate(100 * pos.x, 100 * (pos.y+1)); //correction because of height of tile?
			}
			g2d.transform(rotator);
			g2d.drawImage(TextureHandler.getTexture("TileConstruction"), 0, -100, 100, 0, 0, 0, 32, 32, null);
			try {
				g2d.transform(rotator.createInverse());
			} catch (NoninvertibleTransformException e) {
				e.printStackTrace();
			}
		}
	}
	
}
