package redstonedude.programs.projectboaty.shared.net;

import java.io.Serializable;
import java.util.ArrayList;

import redstonedude.programs.projectboaty.shared.task.Task;

public class PacketRequestSetTaskList extends Packet implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public ArrayList<Task> tasks;
	
	public PacketRequestSetTaskList() {
		super();
		packetID = "PacketRequestSetTaskList";
	}

}
