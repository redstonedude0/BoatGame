package redstonedude.programs.projectboaty.shared.task;

import java.io.Serializable;

import redstonedude.programs.projectboaty.client.physics.ClientPhysicsHandler;

public class TaskCollect extends TaskLocationTarget implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public String collectionUUID = "";
	
	public TaskCollect() {
		taskTypeID = "TaskCollect";
	}

	@Override
	public void targetReached() {
		//great, for now just delete the barrel and leave it at that
		ClientPhysicsHandler.removeEntity(collectionUUID);
		completed = true;
	}

	@Override
	public void init() {
		//no initialisation needed
	}

}
