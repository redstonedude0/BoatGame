package redstonedude.programs.projectboaty.shared.event;

import redstonedude.programs.projectboaty.shared.entity.EntityCharacter;

public class EventCharacterDespawn extends Event {

	public EntityCharacter entityCharacter;
	
	public EventCharacterDespawn(EntityCharacter entityCharacter) {
		super("EventCharacterDespawn");
		this.entityCharacter = entityCharacter;
	}
	
}
