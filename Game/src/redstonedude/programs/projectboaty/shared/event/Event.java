package redstonedude.programs.projectboaty.shared.event;

public class Event {
	
	public String eventTypeID;
	
	public Event(String type) {
		eventTypeID = type;
	}
	
	public void fire() {
		EventRegistry.fireEvent(this);
	}
	
}
