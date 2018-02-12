package redstonedude.programs.projectboaty.shared.net;

import redstonedude.programs.projectboaty.shared.raft.Raft;

public class UserData {
	
	public String uuid;
	public Raft raft;
	public double requiredForwardTranslation = 0;
	public double requiredClockwiseRotation = 0;
	public double requiredRightwardTranslation = 0;
	
}
