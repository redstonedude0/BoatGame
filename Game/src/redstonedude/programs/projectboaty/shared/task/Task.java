package redstonedude.programs.projectboaty.shared.task;

import java.awt.Graphics2D;
import java.io.Serializable;

import redstonedude.programs.projectboaty.shared.entity.EntityCharacter;

public abstract class Task implements Serializable {

	private static final long serialVersionUID = 1L;

	public final String taskTypeID;
	public boolean isCompleted = false;
	public boolean isInProgress = false;

	public Task(String taskTypeID) {
		this.taskTypeID = taskTypeID;
	}

	public abstract void execute(EntityCharacter assignedEntity);

	public void init(EntityCharacter assignedEntity) {
		// do nothing initially
		isInProgress = true;
	}

	public Priority getPriority(EntityCharacter e) {
		return Priority.getIneligible();
	}

	public void passiveUpdate() {
		// do nothing passively
	}

	public void slowPassiveUpdate() {
		// do nothing passively
	}

	public void slowUpdate() {

	}

	public void draw(Graphics2D g2d) {
		// do nothing
	}

}
