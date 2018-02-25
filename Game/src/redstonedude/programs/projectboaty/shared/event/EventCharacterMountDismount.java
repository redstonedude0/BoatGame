package redstonedude.programs.projectboaty.shared.event;

import redstonedude.programs.projectboaty.shared.entity.EntityCharacter;

public class EventCharacterMountDismount extends Event {

	public EntityCharacter entityCharacter;
	
	public EventCharacterMountDismount(EntityCharacter entityCharacter) {
		super("EventCharacterMountDismount");
		this.entityCharacter = entityCharacter;
	}
	
}
