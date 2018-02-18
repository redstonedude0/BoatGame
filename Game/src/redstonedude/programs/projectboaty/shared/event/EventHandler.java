package redstonedude.programs.projectboaty.shared.event;

import redstonedude.programs.projectboaty.shared.task.Priority;

public @interface EventHandler {
	
	Priority priority = Priority.getIneligible();
	
}
