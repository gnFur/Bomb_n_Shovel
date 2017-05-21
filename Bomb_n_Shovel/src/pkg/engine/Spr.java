package pkg.engine;

/**
 * List of sprites.
 */
public class Spr
{
  public final static String resPath="resources/";
  
  //frames_h,frames_v,center_x,center_y
  public final static Sprite peasant=      new Sprite("peasant.png",      2,1, 12, 32),
                             kitten=       new Sprite("testkitten.png",   1,1, 40, 40),
                             path=         new Sprite("path.png",         2,2,  0,  0),
                             tree=         new Sprite("tree.png",         1,1, 24, 59),
                             terrain=      new Sprite("terrain.png",      4,6,  0,  0),
                             menu_buttons= new Sprite("menu_buttons.png", 1,4,101, 45),
                             timer_buttons=new Sprite("timer_buttons.png",3,1,  0,  0),
                             button_back=  new Sprite("button_back.png",  1,1,  0,  0),
                             gui_buttons=  new Sprite("gui_buttons.png",  2,1,  0,  0),
                             paper=        new Sprite("paper.png",        1,1,128,128),
                             timer=        new Sprite("timer.png",        1,1,  2, 14); 
}

