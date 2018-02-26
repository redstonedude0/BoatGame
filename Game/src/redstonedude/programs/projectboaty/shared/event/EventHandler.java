package redstonedude.programs.projectboaty.shared.event;

import redstonedude.programs.projectboaty.shared.task.Priority;

public @interface EventHandler {
	
	/**
	 * Note to self: use this.
	 */
	Priority priority = Priority.getIneligible();
	
}
