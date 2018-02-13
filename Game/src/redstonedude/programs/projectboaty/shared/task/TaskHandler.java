package redstonedude.programs.projectboaty.shared.task;

import java.util.ArrayList;
import java.util.Random;

import redstonedude.programs.projectboaty.server.physics.VectorDouble;
import redstonedude.programs.projectboaty.shared.entity.EntityCharacter;

public class TaskHandler {
	
	public static Random rand = new Random();
	
	public static Task getTask(ArrayList<Task> tasks, EntityCharacter ec) {
		int size = tasks.size();
		if (size != 0) {
			int index = rand.nextInt(size);
			Task t = tasks.get(index);
			t.assignedEntityID = ec.uuid;
			return t;
		} else {
			TaskWander tw = new TaskWander(ec.uuid);
			tw.targetLoc = new VectorDouble(0,1);
			tw.targetLoc_absolute = false;
			return tw;
		}
	}
	
}
