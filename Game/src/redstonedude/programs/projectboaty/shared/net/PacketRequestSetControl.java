package redstonedude.programs.projectboaty.shared.net;

import java.io.Serializable;

public class PacketRequestSetControl extends Packet implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public double requiredForwardTranslation = 0;
	public double requiredClockwiseRotation = 0;
	public double requiredRightwardTranslation = 0;
	
	public PacketRequestSetControl() {
		super();
		packetID = "PacketRequestSetControl";
	}

}
