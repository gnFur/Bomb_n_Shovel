package pkg.engine;

import pkg.Lobby;
import pkg.terrain.Terrain;
import pkg.terrain.TileProp;

public class GameWorld
{

	public static Terrain terrain;

	public static void CREATE()
	{
		TileProp.init();

		Mathe.randomize();
		Input.CREATE();
		Draw.CREATE();

		new Lobby();
	}

	public static void UPDATE()
	{
		Camera.UPDATE();
		Input.UPDATE();
		Obj.UPDATE();
		Draw.UPDATE();
	}

}
