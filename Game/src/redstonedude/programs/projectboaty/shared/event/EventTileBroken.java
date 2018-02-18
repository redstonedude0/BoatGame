package redstonedude.programs.projectboaty.shared.event;

import redstonedude.programs.projectboaty.shared.raft.Raft;
import redstonedude.programs.projectboaty.shared.raft.Tile;

public class EventTileBroken extends Event {

	public Tile tile;
	public Raft parent;
	
	public EventTileBroken(Tile t, Raft p) {
		super("EventTileBroken");
		tile = t;
		parent = p;
	}
	
}
