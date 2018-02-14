package redstonedude.programs.projectboaty.shared.net.clientbound;

import java.io.Serializable;

import redstonedude.programs.projectboaty.shared.net.Packet;
import redstonedude.programs.projectboaty.shared.raft.Tile;

public class PacketTileState extends Packet implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public final Tile tile;
	public String uuid;
	
	public PacketTileState(Tile t) {
		super("PacketTileState");
		tile = t;
	}

}
