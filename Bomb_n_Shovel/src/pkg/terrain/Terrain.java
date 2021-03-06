package pkg.terrain;

import pkg.turns.*;
import javafx.scene.canvas.*;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import pkg.*;
import pkg.engine.*;

public class Terrain extends GameObject
{

	public static int[][] terrain;
	public static int[][] terrainTile;
	public static Sprite[][] terrainSpr;

	TurnManager turnManager;

	public static int terrain_w, terrain_h, cellSize;

	int basePt1_x, basePt1_y, basePt2_x, basePt2_y;
	long seed;

	Color c_water = Color.rgb(89, 125, 206);
	Rectangle bkg = new Rectangle();

	Canvas[][] chunk;
	int chunkSize;

	double cam_mx, cam_my, cam_mxgui, cam_mygui;
	public static boolean camMove;
	int camDragAl = -1, camDragTime = 30;

	public static boolean endturn;

	public boolean uiBlock;
	double uiHide; //0 - hidden, 1 - shown. In-between - cool WHOOSHes.
	double uiHideTar;

	//TIMER STUFF
	public boolean timerEn;
	int timerTime, timerAl;
	//TIMER STUFF

	//recipy_GUI_stuff
	private boolean RECIPE_GUI = false;
	//recipy_GUI_stuff

	public Terrain(long seed_arg, TurnManager turnManager_arg, boolean timerEn_arg)
	{
		super();
		//objIndex.add(Terrain.class);

		turnManager = turnManager_arg;

		camMove = false;
		endturn = false;
		uiBlock = false;

		terrain_w = 96;
		terrain_h = 96;
		cellSize = 32;
		chunkSize = 16;

		chunk = new Canvas[(int) Math.ceil(terrain_w / chunkSize)][(int) Math
			.ceil(terrain_h / chunkSize)];
		for (int i = 0; i < chunk.length; i += 1)
		{
			for (int k = 0; k < chunk[0].length; k += 1)
			{
				chunk[i][k] = new Canvas(chunkSize * cellSize, chunkSize * cellSize);
				chunk[i][k].setLayoutX(i * chunkSize * cellSize);
				chunk[i][k].setLayoutY(k * chunkSize * cellSize);
			}
		}

		//Creating terrain.
		seed = (long) Math.floor(Math.abs(seed_arg) / 100000);

		System.out.println(seed);
		terrainCreate(seed);
		terrainRender();
		//Creating terrain.

		//Turn manager.
		Player p1 = turnManager.playerGet(0);
		Player p2 = turnManager.playerGet(1);

		for (int i = 0; i < 4; i += 1)
		{
			p1.peasantAdd(new Peasant((basePt1_x + Mathe.rotate_x[i]) * 32,
				(basePt1_y + Mathe.rotate_y[i]) * 32));
		}

		for (int i = 0; i < 4; i += 1)
		{
			p2.peasantAdd(new Peasant((basePt2_x + Mathe.rotate_x[i]) * 32,
				(basePt2_y + Mathe.rotate_y[i]) * 32));
		}
		//Turn manager.

		//TIMER STUFF
		timerEn = timerEn_arg;
		timerTime = 5 * 60;
		timerAl = timerTime;
		//TIMER STUFF
	}

	@Override
	public void STEP()
	{
		if (!uiBlock && Obj.objCount(MatchResult.class) > 0)
		{
			uiBlock = true;
		}

		//CAMERA
		if (!uiBlock)
		{
			if (Input.mbCheckPress)
			{
				cam_mx = Input.mouse_x;
				cam_my = Input.mouse_y;
				cam_mxgui = Input.mouse_xgui;
				cam_mygui = Input.mouse_ygui;
				camDragAl = camDragTime;
			}

			if (Input.mbCheck)
			{
				if (camDragAl > -1)
				{
					camDragAl -= 1;
				}

				if (!camMove && (Mathe.pointDistance(Input.mouse_xgui, Input.mouse_ygui, cam_mxgui, cam_mygui)
					> 32 || camDragAl == 0))
				{
					camMove = true;
					cam_mx = Input.mouse_x;
					cam_my = Input.mouse_y;
					cam_mxgui = Input.mouse_xgui;
					cam_mygui = Input.mouse_ygui;
				}

				if (camMove)
				{
					Camera.viewer = null;
					Camera.setPosition((cam_mx - (Input.mouse_x - Camera.get_x())),
						cam_my - (Input.mouse_y - Camera.get_y()));
				}
			}
			else
			{
				if (!Input.mbCheckRelease) //Using this little trick we can activate this next step mouse was released.
				{
					camMove = false;
				}
			}
			//CAMERA
		}
	}

	@Override
	public void DRAW()
	{
		Draw.setDepth(10001);
		Draw.setColor(c_water);
		Draw.drawRectangle(bkg, Camera.get_x() - 100, Camera.get_y() - 100,
			Camera.get_x() + Game.scr_w / Camera.getScale_x() + 100,
			Camera.get_y() + Game.scr_h / Camera.getScale_y() + 100, false);

		int draw_xstart = (int) Math.max(0, Math.floor(Camera.get_x() / cellSize));
		int draw_ystart = (int) Math.max(0, Math.floor(Camera.get_y() / cellSize));
		int draw_xend = (int) Math.min(terrain_w,
			Math.ceil((Camera.get_x() + Game.scr_w / Camera.getScale_x()) / cellSize) + 2);
		int draw_yend = (int) Math.min(terrain_h,
			Math.ceil((Camera.get_y() + Game.scr_h / Camera.getScale_y()) / cellSize) + 2);

		for (int k = draw_ystart; k < draw_yend; k += 1)
		{
			for (int i = draw_xstart; i < draw_xend; i += 1)
			{
				//Additional sprite.
				if (terrainSpr[i][k] != null)
				{
					Draw.setDepth((int) -(k + 0.5) * cellSize + TileProp.getDepth(terrain[i][k]));
					Draw.drawSprite(terrainSpr[i][k], (i + 0.5) * cellSize, (k + 0.5) * cellSize);

					if (terrain[i][k] != 2 && terrain[i][k] != 0)
					{
						Draw.setDepth(1000);
						Draw.drawSprite(new Sprite(Spr.big_shadow), (i + 0.5) * cellSize, (k + 0.5) * cellSize);
					}
				}
				//Additional sprite.
			}
		}

		Draw.setDepth(10000);

		draw_xstart = (int) Math.max(0, Math.floor(Camera.get_x() / (cellSize * chunkSize)));
		draw_ystart = (int) Math.max(0, Math.floor(Camera.get_y() / (cellSize * chunkSize)));
		draw_xend = (int) Math.min(chunk.length, Math.ceil(
			(Camera.get_x() + Game.scr_w / Camera.getScale_x()) / (cellSize * chunkSize)));
		draw_yend = (int) Math.min(chunk[0].length, Math.ceil(
			(Camera.get_y() + Game.scr_h / Camera.getScale_y()) / (cellSize * chunkSize)));

		for (int i = draw_xstart; i < draw_xend; i += 1)
		{
			for (int k = draw_ystart; k < draw_yend; k += 1)
			{
				Draw.draw(chunk[i][k]);
			}
		}

	}

//	void drawItem(Inventory.Item item,int coordX,int coordY){
//                switch (item){
//                    case wood:{
//                        Draw.drawSprite(new Sprite(Spr.inv_items), 0, coordX, coordY);
//                        break;
//                    }
//                    case stone:{
//                        Draw.drawSprite(new Sprite(Spr.inv_items), 1, coordX, coordY);
//                        break;
//                    }
//                    case BLOOD:{
//                        Draw.drawSprite(new Sprite(Spr.inv_items), 2, coordX, coordY);
//                        break;
//                    }
//                }
//	}
	@Override
	public void DRAW_GUI()
	{
		Peasant currentPeasant = turnManager.getCurrentPeasant();
		
		if (turnManager.isCurrentPlayerLocal() && !uiBlock)
		{
			uiHideTar = 0;
		}
		else
		{
			uiHideTar = 1;
		}

		if (Math.abs(uiHideTar - uiHide) > 0.01)
		{
			uiHide += (uiHideTar - uiHide) / 4.0;
		}
		else
		{
			uiHide = uiHideTar;
		}

		endturn = false;
		if (Input.mbCheckRelease && !uiBlock && uiHide == uiHideTar)
		{

			//ZOOM BUTTON
			double xx = Camera.scr_w - (64 + 8) * 2 - 3 + uiHide * (64 + 8), yy
				= Camera.scr_h - 64 - 8;
			if (Mathe
				.pointInRectangle(Input.mouse_xgui, Input.mouse_ygui, xx, yy, xx + 64, yy + 64))
			{
				Input.mouseClear();
				if (Camera.getScale_x() == 1)
				{
					Camera.setScaleTar(0.5, 0.5);
				}
				else
				{
					Camera.setScaleTar(1, 1);
				}
			}
			//ZOOM BUTTON

			//END TURN BUTTON
			xx = Camera.scr_w - (64 + 8) + uiHide * (64 + 8);
			yy = Camera.scr_h - 64 - 8;
			if (Mathe
				.pointInRectangle(Input.mouse_xgui, Input.mouse_ygui, xx, yy, xx + 64, yy + 64))
			{
				Input.mouseClear();
				endturn = true;
				if (timerEn && !turnManager.getCurrentPeasant().moving)
				{
					timerAl = timerTime;
				}
			}
			//END TURN BUTTON
			
			//CRAFT BUTTONS
			
			int _y = 0;
			for(int i = 0; i < Inventory.recipes.length; i += 1)
			{
				if (currentPeasant.inventory.checkRecipe(Inventory.recipes[i]) 
					&& !currentPeasant.inventory.contains(Inventory.recipes[i][0]))
				{
					int xo = 8;
					int yo = 8 + (64 + 8) * _y;
					if (Mathe.pointInRectangle(Input.mouse_xgui, Input.mouse_ygui, xo, yo, xo + 64, yo + 64))
					{
						currentPeasant.inventory.craft(Inventory.recipes[i]);
						Input.mouseClear();
						break;
					}
					_y += 1;
				}
			}
			
			//CRAFT BUTTONS
		}
		
		Draw.setDepth(10);

		drawItems(currentPeasant);
		
		int _y = 0;
		for(int i = 0; i < Inventory.recipes.length; i += 1)
		{
			if (currentPeasant.inventory.checkRecipe(Inventory.recipes[i])
				&& !currentPeasant.inventory.contains(Inventory.recipes[i][0]))
			{
				Draw.drawSprite(new Sprite(Spr.gui_buttons), 0, 8, 8 + (64 + 8) * _y);
				Draw.drawSprite(new Sprite(Spr.inv_items), Inventory.recipes[i][0].imgId, 8, 8 + (64 + 8) * _y);
				_y += 1;
			}
		}
		
		
		
		if (timerEn)
		{
			if (timerAl > 0 && turnManager.isCurrentPlayerLocal())
			{
				timerAl -= 1;
			}
			if (timerAl == 0)
			{
				//Perform timer action.
				Peasant p = turnManager.getCurrentPeasant();

				if (p != null)
				{
					if (!p.moving)
					{
						endturn = true;
						timerAl = timerTime;
					}
				}

			}

			double timer_x = 8 - uiHide * (64 + 16), timer_y = Camera.scr_h - 64 - 8;
			Draw.setColor(Color.rgb(222, 238, 214));
			Draw.drawCircle(new Circle(), timer_x + 32, timer_y + 32, 26, false);

			Arc arc = new Arc();
			arc.setFill(Color.rgb(208, 70, 72));
			arc.setCenterX(timer_x + 32);
			arc.setCenterY(timer_y + 32);
			arc.setRadiusX(26);
			arc.setRadiusY(26);
			arc.setStartAngle(90);
			arc.setLength(timerAl / (double) timerTime * 360.0);
			arc.setType(ArcType.ROUND);

			Draw.draw(arc);

			Draw.drawSprite(new Sprite(Spr.timer), 0, timer_x, timer_y);
		}

	}

	private void drawItems(Peasant p_active)
	{
		int size = p_active.inventory.inv.size();
		
		int _x = 0, 
				_y = 0;
		
		double origX = Camera.scr_w - (64 + 8) * 2;
		double origY = Camera.scr_h - (64 + 8) * 3;
		
		for(int i = 0; i < 6; i += 1)
		{
			double coordX = origX + (64 + 8) * _x + uiHide * (64 + 8);
			double coordY = origY + (64 + 8) *_y;
			
			Draw.drawSprite(new Sprite(Spr.gui_buttons), i, coordX, coordY);
			
			if (i < size)
			{
				Inventory.Item item = p_active.inventory.inv.get(i);
				Draw.drawSprite(new Sprite(Spr.inv_items), item.imgId, coordX, coordY);
			}
			
			_x += 1;
			if (_x >= 2)
			{
				_x = 0;
				_y += 1;
			}
		}
	}

	private void drawRecipies()
	{

		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 2; j++)
			{
				double coordX = Camera.scr_w - (64 + 8) * (2 - j) - 3 + uiHide * (64 + 8);
				double coordY = Camera.scr_h - 64 * (7 - i) - 8;
				Draw.drawSprite(new Sprite(Spr.inv_items), (i % 3 + i + j) + 4, coordX, coordY);
			}
		}
	}

	@Override
	public void DESTROY()
	{
		Obj.objDestroy(turnManager);
	}

	/**
	 * Renders terrain.
	 */
	final void terrainRender()
	{
		for (int i = 0; i < chunk.length; i += 1)
		{
			for (int k = 0; k < chunk[0].length; k += 1)
			{
				terrainChunkRender(i, k);
			}
		}
	}

	/**
	 * Renders one chunk.
	 */
	void terrainChunkRender(int x, int y)
	{
		GraphicsContext surf = chunk[x][y].getGraphicsContext2D();
		Image terrImg = Spr.terrain.img.getImage();

		int tid, yy, xx;

		for (int i = 0; i < chunkSize; i += 1)
		{
			for (int k = 0; k < chunkSize; k += 1)
			{
				tid = terrainTile[x * chunkSize + i][y * chunkSize + k];
				yy = tid / 4;
				xx = tid - yy * 4;

				surf.drawImage(terrImg, xx * cellSize, yy * cellSize, cellSize, cellSize,
					i * cellSize, k * cellSize, cellSize, cellSize);
				//x,y,w,h,x1,y1,x2,y2
			}
		}
	}

	private void terrainCreate(long seed)
	{
		//Generator.
		TerrainGenerator gen = new TerrainGenerator(terrain_w, terrain_h, seed);

		Mathe.randomPush();
		Mathe.randomSetSeed(seed);

		//ISLANDS
		int island_lmin = 10, island_lmax = 18, island_diradd = 30, islandSize_min = 5,
			islandSize_max = 8;

		double l, d;

		//First team.
		l = Mathe.random(island_lmin, island_lmax);
		d = Mathe.random(360);
		basePt1_x = terrain_w / 2 + (int) Mathe.lcos(l, d);
		basePt1_y = terrain_h / 2 + (int) Mathe.lsin(l, d);
		gen.islandAdd(basePt1_x, basePt1_y, Mathe.irandom(islandSize_min, islandSize_max));
		//First team.

		//Second team.
		d += 180 + Mathe.irandom(-island_diradd, island_diradd);
		basePt2_x = terrain_w / 2 + (int) Mathe.lcos(l, d);
		basePt2_y = terrain_h / 2 + (int) Mathe.lsin(l, d);
		gen.islandAdd(basePt2_x, basePt2_y, Mathe.irandom(islandSize_min, islandSize_max));
		//Second team.

		//Middle island.
		gen.islandAdd(terrain_w / 2, terrain_h / 2, Mathe.irandom(islandSize_min, islandSize_max));
		//Middle island.

		//Center point.
		int ptC_x = (basePt1_x + basePt2_x + terrain_w / 2) / 3, ptC_y
			= (basePt1_y + basePt2_y + terrain_h / 2) / 3;
		//Center point.

		double baseDir = Mathe.pointDirection(basePt1_x, basePt1_y, basePt2_x, basePt2_y) + 90;

		//Additional islands.
		int islandAm = Mathe.irandom(3, 5);
		for (int i = 0; i < islandAm; i += 1)
		{
			l = Mathe.random(island_lmin / 2, island_lmax / 2);
			d = baseDir + 180 * Mathe.irandom(1) + Mathe.irandom(-45, 45);
			gen.islandAdd(ptC_x + (int) Mathe.lcos(l, d), ptC_y + (int) Mathe.lsin(l, d),
				Mathe.irandom(5, 8));
		}
		//Additional islands.

		islandAm = Mathe.irandom(3, 5);
		for (int i = 0; i < islandAm; i += 1)
		{
			l = Mathe.random(island_lmax * 0.5, island_lmax * 0.75);
			d = baseDir + Mathe.irandom(-30, 30) + 90 * Mathe.irandom(1);
			gen.islandAdd(ptC_x + (int) Mathe.lcos(l, d), ptC_y + (int) Mathe.lsin(l, d),
				Mathe.irandom(3, 4));
		}

		Mathe.randomPop();

		terrain = gen.terrainGenerate(terrain_w, terrain_h);
		terrainSpr = new Sprite[terrain_w][terrain_h];
		terrainTile = gen.terrainAutotile(terrain, terrainSpr);
		//Generator.

	}

	/**
	 * Safely gets value out of terrain.
	 *
	 * @return Terrain value or 2 if out of bounds.
	 */
	public static int tget(int x, int y)
	{
		try
		{
			return terrain[x][y];
		}
		catch (Exception e)
		{
			return 2;
		}
	}

	/**
	 * Safely gets value out of array.
	 *
	 * @return Array value or 2 if out of bounds.
	 */
	public static int get(int[][] terr, int x, int y)
	{
		try
		{
			return terr[x][y];
		}
		catch (Exception e)
		{
			return 2;
		}
	}

}
