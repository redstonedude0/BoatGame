package redstonedude.programs.projectboaty.shared.raft;

import java.io.Serializable;
import java.util.ArrayList;

import redstonedude.programs.projectboaty.server.physics.VectorDouble;
import redstonedude.programs.projectboaty.shared.task.Task;

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
		tiles = t;
	}
	
	public synchronized void addTile(Tile t) {
		tiles.add(t);
	}
	
	public synchronized ArrayList<Task> getTasks() {
		return (ArrayList<Task>) tasks.clone();
	}
	
	public synchronized void addTask(Task t) {
		tasks.add(t);
	}
	
	public synchronized void removeTask(Task t) {
		tasks.remove(t);
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
