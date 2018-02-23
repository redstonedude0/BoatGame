package redstonedude.programs.projectboaty.shared.raft;

import java.io.Serializable;
import java.util.concurrent.ConcurrentLinkedQueue;

import redstonedude.programs.projectboaty.shared.entity.EntityResource;

public class ResourceStorage implements Serializable {
	
	private static final long serialVersionUID = 1L;
	public ConcurrentLinkedQueue<EntityResource> resources = new ConcurrentLinkedQueue<EntityResource>();
	public int maxNumberOfStacks = 1;

}
