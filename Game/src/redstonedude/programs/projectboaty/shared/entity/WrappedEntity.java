package redstonedude.programs.projectboaty.shared.entity;

import java.io.Serializable;

public class WrappedEntity implements Serializable {
	
	private static final long serialVersionUID = 1L;
	public Entity entity;
	
	public WrappedEntity(Entity e) {
		entity = e;
	}
}
