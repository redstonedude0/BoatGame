package redstonedude.programs.projectboaty.shared.net.serverbound;

import java.io.Serializable;

import redstonedude.programs.projectboaty.shared.net.Packet;

public class PacketRequestSetControl extends Packet implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public double requiredForwardTranslation = 0;
	public double requiredClockwiseRotation = 0;
	public double requiredRightwardTranslation = 0;
	
	public PacketRequestSetControl() {
		super("PacketRequestSetControl");
	}

}
