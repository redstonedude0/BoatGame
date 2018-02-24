package redstonedude.programs.projectboaty.shared.raft;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import redstonedude.programs.projectboaty.client.net.ClientPacketHandler;
import redstonedude.programs.projectboaty.client.physics.ClientPhysicsHandler;
import redstonedude.programs.projectboaty.shared.entity.Entity;
import redstonedude.programs.projectboaty.shared.entity.EntityCharacter;
import redstonedude.programs.projectboaty.shared.entity.EntityResource;
import redstonedude.programs.projectboaty.shared.entity.EntityResource.ResourceType;
import redstonedude.programs.projectboaty.shared.entity.WrappedEntity;
import redstonedude.programs.projectboaty.shared.event.EventTileBroken;
import redstonedude.programs.projectboaty.shared.physics.VectorDouble;
import redstonedude.programs.projectboaty.shared.task.Task;
import redstonedude.programs.projectboaty.shared.task.TaskHandler;
import redstonedude.programs.projectboaty.shared.task.TaskWander;

public class Raft implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private VectorDouble pos = new VectorDouble(); //absolute
	public double theta = 0;
	private VectorDouble velocity = new VectorDouble(); //absolute
	public double dtheta = 0;
	
	public double sin = 0;
	public double cos = 1;
	private VectorDouble COMPos = new VectorDouble(); //relative 
	
	private ArrayList<Tile> tiles = new ArrayList<Tile>();
	private ArrayList<Task> tasks = new ArrayList<Task>();
	private transient Tile constructionTile; //used to show current mouse position, doesn't need to be serialized or even stored server-side
	
	public Collection<EntityResource> getTotalResources() {
		ArrayList<EntityResource> resources = new ArrayList<EntityResource>();
		for (ResourceType rt: ResourceType.values()) {
			EntityResource er= new EntityResource(rt,0);
			resources.add(er);
		}
		//tiles
		for (Tile t: getTiles()) {
			if (t != null && t.storage != null) {
				ResourceStorage rs = t.storage;
				storage: for (EntityResource er: rs.resources) {
					for (EntityResource resource: resources) {
						if (er.resourceType == resource.resourceType) {
							resource.quantity += er.quantity;
							continue storage;//get next item in storage
						}
					}
				}
			}
		}
		entities: for (WrappedEntity e: ClientPhysicsHandler.getWrappedEntities()) {
			if (e != null && e.entity != null) {
				if (e.entity instanceof EntityCharacter) {
					EntityCharacter ec = (EntityCharacter) e.entity;
					if (ec.ownerUUID.equals(ClientPacketHandler.currentUserUUID)) {
						if (ec.carrying != null) {
							for (EntityResource resource: resources) {
								if (ec.carrying.resourceType == resource.resourceType) {
									resource.quantity += ec.carrying.quantity;
									continue entities;//get next entity
								}
							}
						}
					}
				}
			}
		}
		return resources;
	}
	
	public synchronized Tile getConstructionTile() {
		return constructionTile;
	}
	
	public synchronized void setConstructionTile(Tile t) {
		constructionTile = t;
	}
	
	public synchronized ArrayList<Tile> getTiles() {
		return (ArrayList<Tile>) tiles.clone();
	}
	
	public synchronized void setTiles(ArrayList<Tile> t) {
		tiles = t; //server-side only?
	}
	
	public synchronized void addTile(Tile t) {
		tiles.add(t);
		t.checkState();
	}
	
	public synchronized void removeAllTiles(List<Tile> ts) {
		tiles.removeAll(ts);
		for (Tile t: ts) {
			new EventTileBroken(t,this).fire();
		}
	}
	
	public Tile getTileAt(int x, int y) {
		for (Tile t: getTiles()) {
			if ((int) t.getPos().x == x) {
				if ((int) t.getPos().y == y) {
					return t;
				}
			}
		}
		return null;
	}
	
	public void setTileAt(Tile tile) {
		Tile target = null;
		for (Tile t: tiles) {
			//System.out.println("compare: " + t.getPos().x + ":" + tile.getPos().x);
			if (t.getPos().equals(tile.getPos())) {
				target = t;
				break;
			}
		}
		if (target != null) {
			tiles.remove(target);
			tiles.add(tile);
			tile.checkState();
		}
	}
	
	public synchronized void setTasks(ArrayList<Task> t) {
		tasks = t; //don't need to update TaskHandler since this was received from the server
	}
	
	public synchronized ArrayList<Task> getTasks() {
		return (ArrayList<Task>) tasks.clone();
	}
	
	/**
	 * Returns all tasks, including both the list of unstarted tasks from getTasks() and the tasks currently in progress
	 * CLIENTSIDE
	 * @return
	 */
	public synchronized ArrayList<Task> getAllTasks() {
		ArrayList<Task> ts = getTasks();
		for (WrappedEntity we: ClientPhysicsHandler.getWrappedEntities()) {
			Entity e = we.entity;
			if (e instanceof EntityCharacter) {
				EntityCharacter ec = (EntityCharacter) e;
				if (ec.ownerUUID.equals(ClientPacketHandler.currentUserUUID) && ec.currentTask != null) {
					ts.add(ec.currentTask);
				}
			}
		}
		return ts;
	}
	
	/**
	 * Returns all tasks, including both the list of unstarted tasks from getTasks() and the tasks currently in progress
	 * CLIENTSIDE
	 * @return
	 */
	public synchronized ArrayList<Task> getAllTasksNotWander() {
		ArrayList<Task> ts = getAllTasks();
		ts.removeIf(new Predicate<Task>() {
			@Override
			public boolean test(Task t) {
				if (t instanceof TaskWander) {
					return true;
				}
				return false;
			}
		});
		return ts;
	}
	
	public synchronized void addTask(Task t) {
		tasks.add(t);
		TaskHandler.sendList(this);
	}
	
	public synchronized void removeTask(Task t) {
		tasks.remove(t);
		TaskHandler.sendList(this);
	}
	
	public VectorDouble getUnitX() {
		VectorDouble v = new VectorDouble();
		v.x = cos;
		v.y = sin;
		return v;
	}
	
	public VectorDouble getUnitY() {
		VectorDouble v = new VectorDouble();
		v.x = sin;
		v.y = -cos;
		return v;
	}
	
	public void setPos(VectorDouble pos) {
		this.pos = pos;
	}
	
	public VectorDouble getPos() {
		return new VectorDouble(pos);
	}
	
	public void setVelocity(VectorDouble velocity) {
		this.velocity = velocity;
	}
	
	public VectorDouble getVelocity() {
		return new VectorDouble(velocity);
	}
	
	public void setCOMPos(VectorDouble COMPos) {
		this.COMPos = COMPos;
	}
	
	public VectorDouble getCOMPos() {
		return new VectorDouble(COMPos);
	}
	
}
