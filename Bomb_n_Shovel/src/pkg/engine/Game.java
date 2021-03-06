package pkg.engine;

import java.util.concurrent.TimeUnit;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.scene.canvas.Canvas;

import javafx.animation.AnimationTimer;
import javafx.event.EventHandler;
import javafx.stage.WindowEvent;

public class Game extends Application
{

	static int gameSpeed = 60;
	private final double timeMul = 1000000000;
	public static double currentTime = 0;
	public static boolean create = true;
	public static Pane root, //Root surface.
		gui, //For gui layer.
		rotatesurf, //For rotating appsurf.
		appsurf;    //For all the graphics.

	public static int scr_w = 1000;
	public static int scr_h = 800;

	public static boolean inFocus;

	@Override
	public void start(Stage primaryStage)
	{
		final long timeStart = System.nanoTime();

		root = new Pane();
		gui = new Pane();
		rotatesurf = new Pane();
		appsurf = new Pane();

		gui.setMinSize(scr_w, scr_h);
		gui.setMaxSize(scr_w, scr_h);
		rotatesurf.setMinSize(scr_w, scr_h);
		rotatesurf.setMaxSize(scr_w, scr_h);
		appsurf.setMinSize(scr_w, scr_h);
		appsurf.setMaxSize(scr_w, scr_h);

		Canvas canvas = new Canvas(scr_w, scr_h);
		Scene scene = new Scene(root, scr_w, scr_h);

		primaryStage.setScene(scene);
		primaryStage.setTitle("Bomb n' Shovel.");

		root.getChildren().add(canvas);
		rotatesurf.getChildren().add(appsurf);
		root.getChildren().add(rotatesurf);
		root.getChildren().add(gui);

		//primaryStage.setFullScreen(true);
		primaryStage.setMinWidth(scr_w + 4);
		primaryStage.setMinHeight(scr_h + 25 + 6);
		primaryStage.setMaxWidth(scr_w + 4);
		primaryStage.setMaxHeight(scr_h + 25 + 6);
		primaryStage.setResizable(false);

		primaryStage.show();

		new AnimationTimer()
		{
			@Override
			public void handle(long timeCur)
			{
				if (Game.create)
				{
					GameWorld.CREATE();
					Game.create = false;
				}

				long timePrev = System.nanoTime();
				currentTime = (System.nanoTime() - timeStart) / timeMul;

				inFocus = root.getScene().getWindow().focusedProperty().get();

				GameWorld.UPDATE();

				try
				{
					//WAITING.
					long dt = (long) (timeMul / gameSpeed - (System.nanoTime() - timePrev));
					TimeUnit.NANOSECONDS.sleep(dt);
					//WAITING.
				}
				catch (InterruptedException ex)
				{
				}
			}
		}.start();

		//Closing additional thread;
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>()
		{
			@Override
			public void handle(WindowEvent event)
			{
				System.out.println("Bye^^");
			}
		});
		//Closing additional thread.

	}

	public static void main(String[] args)
	{
		launch(args);
	}

}
