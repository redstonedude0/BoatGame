package redstonedude.programs.projectboaty.shared.physics;

import java.io.Serializable;

public class Location implements Serializable {

	private static final long serialVersionUID = 1L;

	VectorDouble pos;
	boolean isAbsolute;
	String raft_uuid; // if not absolute
}
