package pkg;

import pkg.turns.Cmd;
import pkg.terrain.Terrain;
import pkg.turns.Player;
import pkg.engine.*;
import pkg.pathfinder.*;
import pkg.terrain.TileProp;
import pkg.turns.LocalPlayer;

import static pkg.engine.Draw.drawSprite;

public class Peasant extends Entity
{

	public Player myPlayer = null;
	public Cmd command = null;
	public Inventory inventory = null;
	public boolean initiative = false;
	public int tid = -1;
	public PathPoint pathList;
	public int hp = 100;

	//Movement.
	int target_x, target_y;
	public boolean moving = false;
	double x_prev, y_prev;
	double z, z_prev;

	double xscale = 1;

	public int cx_prev, cy_prev;

	double moveSpd = 3,
		moveStaminaMax = 16,
		moveStamina = 0;
	//Movement.
	
	Sprite spr;
	
	
	
	//Tool levels.
	//0 - bare hands
	//1 - wooden tool
	//2 - stone tool
	int[] toolLvl = new int[3];
	
	int[] swordDamage = {34, 50, 100};
	
	int sword = 0,
		axe = 1,
		pickaxe = 2;
	
	Inventory.Item[][] toolTiers = 
	{
		{Inventory.Item.woodenSword, Inventory.Item.stoneSword},
		{Inventory.Item.woodenAxe, Inventory.Item.stoneAxe},
		{Inventory.Item.woodenPickaxe, Inventory.Item.stonePickaxe},
	};
	
	int[][] noAxeMask = // A little bit shitcoding, but eh, whatever. This will simplify things.
	{
		{1},
	};
	
	int[][] woodenAxeMask = 
	{
		{0, 1, 0},
		{1, 1, 1},
		{0, 1, 0},
	};
	
	int[][] stoneAxeMask = 
	{
		{0, 1, 1, 1, 0},
		{1, 1, 1, 1, 1},
		{1, 1, 1, 1, 1},
		{1, 1, 1, 1, 1},
		{0, 1, 1, 1, 0},
	};
	
	
	public Peasant(double x_arg, double y_arg)
	{
		super(x_arg, y_arg, false);
		//objIndex.add(Obj.oid.peasant);
		inventory = new Inventory();

		spr = new Sprite(Spr.peasant);
	}

	@Override
	public void STEP()
	{
		detectToolLevels();
		
		try
		{
			if (moving)
			{
				double sign_x = Math.signum(x - pathList.x * Terrain.cellSize);
				double sign_y = Math.signum(y - pathList.y * Terrain.cellSize);

				if (sign_y == 0)
				{
					xscale = -sign_x;
				}

				x -= sign_x * moveSpd;
				y -= sign_y * moveSpd;

				z = Mathe.lsin(8, 180 * (Mathe.lerp(x_prev, x, pathList.x * Terrain.cellSize) + Mathe.lerp(y_prev, y, pathList.y * Terrain.cellSize)));

				if (Mathe.pointDistance(x, y, pathList.x * Terrain.cellSize, pathList.y * Terrain.cellSize) < moveSpd)
				{
					x = pathList.x * Terrain.cellSize;
					y = pathList.y * Terrain.cellSize;
					x_prev = x;
					y_prev = y;
					z = 0;
					pathList = pathList.next;

					moveStamina -= 1;

					if (pathList == null)
					{
						if (moveStamina > 0)
						{
							interact();
						}
						moving = false;
					}

					if (moveStamina <= 0)
					{
						moving = false;
					}
				}
			}

		}
		catch (Exception e)
		{
			moving = false;
		}

		//MOVING INPUTS/////////////////////////////////////////////
		if (command != null)
		{
			if (command.cmp("move"))
			{
				if (moveStamina > 0)
				{
					Camera.viewer = this;

					x_prev = x;
					y_prev = y;
					moving = true;
				}
			}

			if (command.cmp("setpath"))
			{
				int cx = (int) command.get(0),
					cy = (int) command.get(1);

				for (ObjIter it = new ObjIter(Entity.class); it.end(); it.inc())
				{
					if (it.get() != this && !((Entity) it.get()).passable)
					{
						((Entity) it.get()).tileStore();
					}
				}

				Pathfinder pathfinder = new Pathfinder(Terrain.terrain);
				pathList = pathfinder.pathFind((int) x / Terrain.cellSize, (int) y / Terrain.cellSize, cx, cy);

				for (ObjIter it = new ObjIter(Entity.class); it.end(); it.inc())
				{
					if (it.get() != this && !((Entity) it.get()).passable)
					{
						((Entity) it.get()).tileRestore();
					}
				}

				cx_prev = cx;
				cy_prev = cy;
			}

			if (command.cmp("interact"))
			{
				if (moveStamina > 0)
				{
					cx_prev = (int) command.get(0);
					cy_prev = (int) command.get(1);
					interact();
				}
			}

			if (command.cmp("endturn"))
			{
				myPlayer.endTurn();
			}

			command = null;
		}

		//MOVING INPUTS/////////////////////////////////////////////
	}
	
	int[] invPosX = {4, 25, 8, 23};
	int[] invPosY = {4, 2, -7, -6};
	int[] invScale = {-1, 1, 1, 1};
	int[] invDepth = {0, 0, 1, 1};
	
	
	@Override
	public void DRAW()
	{

		Draw.setDepth((int) (-y));
		drawSprite(new Sprite(Spr.shadow), 0, x + Terrain.cellSize / 2, y + Terrain.cellSize / 2);
		drawSprite(spr, tid, x + Terrain.cellSize / 2, y + Terrain.cellSize / 2 + z, xscale, 1, 0, 1);
		
		for(int i = 0; i < inventory.inv.size(); i += 1)
		{
			Draw.setDepth((int) (-y + invDepth[i]));
		
			drawSprite(new Sprite(Spr.peasant_items), inventory.inv.get(i).imgId, x + invPosX[i], y + invPosY[i] + z_prev, invScale[i], 1, 0);
		}
		z_prev = z;
		
		if (initiative && pathList != null && myPlayer instanceof LocalPlayer)
		{
			PathPoint p = pathList;

			int add, i = 0;
			while (p != null)
			{
				Draw.setDepth(-999999);//(int) -p.y * Terrain.cellSize + 1);

				if (i < moveStamina)
				{
					add = 0;
				}
				else
				{
					add = 2;
				}
				i += 1;
				drawSprite(new Sprite(Spr.path), ((p.next == null) ? 1 : 0) + add, p.x * Terrain.cellSize, p.y * Terrain.cellSize);
				p = p.next;
			}
		}
	}

	@Override
	public void DESTROY()
	{
		if (myPlayer != null)
		{
			myPlayer.peasantRemove(this);
		}
	}

	/**
	 * Restores stamina.
	 */
	public void staminaRefill()
	{
		moveStamina = moveStaminaMax;
	}

	/**
	 * Interacts with tile.
	 */
	void interact()
	{
		//System.out.println("sip?");
		Entity intEntity = null;

		//Looking for entity in a cell.
		for (ObjIter it = new ObjIter(Entity.class); it.end(); it.inc())
		{
			Entity other = (Entity) it.get();
			if (other != this)
			{
				if (other.x / Terrain.cellSize == cx_prev && other.y / Terrain.cellSize == cy_prev)
				{
					intEntity = other;
					break;
				}
			}
		}
		//Looking for entity in a cell.

		if (intEntity != null)
		{
			//Entity actions.
			if (intEntity instanceof Peasant)
			{
				Peasant intPeasant = (Peasant) intEntity;
				if (intPeasant.tid != tid)
				{
					intPeasant.hp -= swordDamage[toolLvl[sword]];
					if (intPeasant.hp <= 0)
					{
						Obj.objDestroy(intPeasant);
					}
					moveStamina = 0;
				}
			}
			//Entity actions.
		}
		else
		{
			//Tile actions.

			int tile = Terrain.terrain[cx_prev][cy_prev];

			if (tile == 1)
			{
				chopTree(cx_prev, cy_prev);
				inventory.addItem(Inventory.Item.wood);
				
				moveStamina = moveStamina - 1;
			}

			if (tile == 4 && toolLvl[pickaxe] >= 1)
			{
				Terrain.terrain[cx_prev][cy_prev] = 0;
				Terrain.terrainSpr[cx_prev][cy_prev] = null;

				inventory.addItem(Inventory.Item.stone);
				moveStamina = moveStamina - 1;
			}

			if (tile == 5 && toolLvl[pickaxe] >= 2)
			{
				Terrain.terrain[cx_prev][cy_prev] = 0;
				Terrain.terrainSpr[cx_prev][cy_prev] = null;

				inventory.addItem(Inventory.Item.gunpowder);
				moveStamina = moveStamina - 1;
			}

			//Tile actions.
		}

	}

	public boolean pathCheck()
	{
		boolean isCorrect = true;
		PathPoint listBuf = pathList;

		for (ObjIter it = new ObjIter(Entity.class); it.end(); it.inc())
		{
			if (it.get() != this && !((Entity) it.get()).passable)
			{
				((Entity) it.get()).tileStore();
			}
		}

		while (listBuf != null)
		{
			if (!TileProp.isPassable(Terrain.terrain[listBuf.x][listBuf.y]))
			{
				isCorrect = false;
				break;
			}
			listBuf = listBuf.next;
		}

		for (ObjIter it = new ObjIter(Entity.class); it.end(); it.inc())
		{
			if (it.get() != this && !((Entity) it.get()).passable)
			{
				((Entity) it.get()).tileRestore();
			}
		}

		return isCorrect;

	}

	/**
	 * Looks into inventory and determines tool levels.
	 */
	private void detectToolLevels()
	{
		for(int tool = 0; tool < toolLvl.length; tool += 1)
		{
			toolLvl[tool] = 0;
			for(int i = 0; i < toolTiers[tool].length; i += 1)
			{
				if (inventory.contains(toolTiers[tool][i]))
				{
					toolLvl[tool] = i + 1;
				}
			}
		}
	}
	
	private void chopTree(int center_x, int center_y)
	{
		
		int[][] mask = noAxeMask;
		
		if (toolLvl[axe] == 1)
		{
			mask = woodenAxeMask;
		}
		if (toolLvl[axe] == 2)
		{
			mask = stoneAxeMask;
		}
		
		int ox = center_x - (mask[0].length / 2);
		int oy = center_y - (mask.length / 2);
		
		for(int mx = ox; mx < ox + mask[0].length; mx += 1)
		{
			for(int my = oy; my < oy + mask.length; my += 1)
			{
				if (mask[mx - ox][my - oy] == 1 && Terrain.tget(mx, my) == 1)	
				{
					Terrain.terrain[mx][my] = 0;
					Terrain.terrainSpr[mx][my] = null;
					new TreeFallingEffect(mx * Terrain.cellSize, my * Terrain.cellSize);
				}
			}	
		}
	}
	
}
