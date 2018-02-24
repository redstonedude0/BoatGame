package redstonedude.programs.projectboaty.shared.task;

import java.util.concurrent.ConcurrentLinkedQueue;

import redstonedude.programs.projectboaty.shared.entity.EntityResource;

public interface TaskMaterialComponent {
	
	public ConcurrentLinkedQueue<EntityResource> requirements = new ConcurrentLinkedQueue<EntityResource>();
	
}
