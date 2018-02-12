package redstonedude.programs.projectboaty.shared.net;

import java.io.Serializable;

import redstonedude.programs.projectboaty.shared.raft.Raft;

public class UserData implements Serializable {
	
	private static final long serialVersionUID = 1L;
	public String uuid;
	public Raft raft;
	public double requiredForwardTranslation = 0;
	public double requiredClockwiseRotation = 0;
	public double requiredRightwardTranslation = 0;
	
}
