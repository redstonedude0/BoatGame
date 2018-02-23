package redstonedude.programs.projectboaty.shared.entity;

import java.io.Serializable;
import java.util.Random;

import redstonedude.programs.projectboaty.shared.physics.VectorDouble;

public class EntityResource extends Entity implements Serializable {
	
	private static final long serialVersionUID = 2L;

	private VectorDouble vel = new VectorDouble();
	
	public static enum ResourceType {
		Wood(75,100,true,"Wood"), Scrap(75,500,false,"Scrap"), Cloth(75,25,true,"Cloth"), Fish(75,25,true,"Fish"), Coral(75,10,true,"Coral"), Bricks(75,25,false,"Bricks"), Gold(500,500,false,"Gold");
		
		public final int maxStackSize;
		public final int maxHP;
		public final boolean floats;
		public final String textureName;
		ResourceType(int maxStackSize, int maxHP, boolean floats, String textureName) {
			this.maxStackSize = maxStackSize;
			this.maxHP = maxHP;
			this.floats = floats;
			this.textureName = textureName;
		}
	}
	public ResourceType resourceType;
	public int quantity;
	public int hp;

	public EntityResource(ResourceType type) {
		super();
		entityTypeID = "EntityResource";
		resourceType = type;
		hp = type.maxHP;
		quantity = type.maxStackSize;
	}

	public void setVel(VectorDouble v) {
		this.vel = v;
	}

	public VectorDouble getVel() {
		return new VectorDouble(vel);
	}
	
	public static EntityResource randomBarrelResource() {
		ResourceType type = ResourceType.Wood;
		Random rand = new Random();
		int value = rand.nextInt(100);
		if (value >= 0 && value < 40) {
			type = ResourceType.Wood;
		} else if (value >= 40 && value < 50) {
			type = ResourceType.Scrap;
		} else if (value >= 50 && value < 75) {
			type = ResourceType.Cloth;
		} else if (value >= 75 && value < 90) {
			type = ResourceType.Fish;
		} else if (value >= 90 && value < 97) {
			type = ResourceType.Coral;
		} else if (value >= 97 && value < 99) {
			type = ResourceType.Bricks;
		} else if (value >= 99 && value < 100) {
			type = ResourceType.Gold;
		}
		EntityResource er = new EntityResource(type);
		return er;
	}
}
