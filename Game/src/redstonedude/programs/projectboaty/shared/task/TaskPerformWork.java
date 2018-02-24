package redstonedude.programs.projectboaty.shared.task;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.io.Serializable;

import redstonedude.programs.projectboaty.client.net.ClientPacketHandler;
import redstonedude.programs.projectboaty.shared.entity.EntityCharacter;
import redstonedude.programs.projectboaty.shared.net.UserData;
import redstonedude.programs.projectboaty.shared.physics.Location;
import redstonedude.programs.projectboaty.shared.physics.VectorDouble;

public class TaskPerformWork extends Task implements Serializable {

	private static final long serialVersionUID = 1L;

	public int workRemaining = 0;
	public int maximumWork = 100;

	public TaskPerformWork(int work) {
		super("TaskPerformWork");
		maximumWork = work;
		workRemaining = work;
	}

	@Override
	public void execute(EntityCharacter assignedEntity) {
		workRemaining--;
		if (workRemaining <= 0) {
			isCompleted = true;
		}
	}
	
	public void draw(Graphics2D g2d, Location target) {
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
			double workDone = (maximumWork - workRemaining);
			double proportionDone = workDone / ((double) maximumWork);
			double angleDone = proportionDone * 360;
			g2d.setColor(Color.LIGHT_GRAY);
			g2d.fillArc(25, -75, 50, 50, 90, (int) -angleDone);
			try {
				g2d.transform(rotator.createInverse());
			} catch (NoninvertibleTransformException e) {
				e.printStackTrace();
			}
		}
	}

}
