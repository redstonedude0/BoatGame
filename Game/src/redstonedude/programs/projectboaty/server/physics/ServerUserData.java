package redstonedude.programs.projectboaty.server.physics;

import java.io.Serializable;
import java.net.InetAddress;

import redstonedude.programs.projectboaty.shared.net.UserData;

public class ServerUserData extends UserData implements Serializable {

	private static final long serialVersionUID = 1L;
	//public VectorDouble cameraPosition = new VectorDouble(0,0);
	public InetAddress IP;
	
}
