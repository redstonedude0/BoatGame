package redstonedude.programs.projectboaty.shared.net;

import java.io.Serializable;

public class PacketSetControl extends Packet implements Serializable {

	private static final long serialVersionUID = 1L;

	public String uuid;

	public double requiredForwardTranslation = 0;
	public double requiredClockwiseRotation = 0;
	public double requiredRightwardTranslation = 0;

	public PacketSetControl() {
		super();
		packetID = "PacketSetControl";
	}

}
