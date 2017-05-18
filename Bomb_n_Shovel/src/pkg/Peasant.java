package pkg;

import pkg.net.Cmd;
import pkg.terrain.Terrain;
import pkg.turns.Player;
import pkg.engine.*;
import pkg.pathfinder.*;
import pkg.terrain.TileProp;
import pkg.turns.LocalPlayer;

public class Peasant extends Entity
{
  public Player myPlayer=null;
  public Cmd command=null;
  
  public boolean initiative=false;
  public int tid=-1;
  
  public PathPoint pathList;
   
  //Movement.
  int target_x,target_y;
  public boolean moving=false;
  double x_prev,y_prev;
  double z;
  
  public int cx_prev,cy_prev;
  
  double moveSpd=        3,
         moveStaminaMax=16,
         moveStamina=    0;
      
  //Movement.
  
  Sprite spr;

  public Peasant(double x_arg,double y_arg)
  {
    super(x_arg,y_arg,false);
    objIndex.add(Obj.oid.peasant);
    
    spr=new Sprite(Spr.peasant);
  }
  
  @Override 
  public void STEP()
  {
    if (moving)
    { 
      x-=Math.signum(x-pathList.x*Terrain.cellSize)*moveSpd;
      y-=Math.signum(y-pathList.y*Terrain.cellSize)*moveSpd;
      z=Mathe.lsin(8,180*(Mathe.lerp(x_prev,x,pathList.x*Terrain.cellSize)+Mathe.lerp(y_prev,y,pathList.y*Terrain.cellSize)));
      
      if (Mathe.pointDistance(x,y,pathList.x*Terrain.cellSize,pathList.y*Terrain.cellSize)<moveSpd)
      {
        x=pathList.x*Terrain.cellSize;
        y=pathList.y*Terrain.cellSize;
        x_prev=x;
        y_prev=y;
        z=0;
        pathList=pathList.next;
        
        moveStamina-=1;
        
        if (pathList==null)
        {
          if (moveStamina>0)
          {interact();}
          moving=false;
        }
        
        if (moveStamina<=0)
        {moving=false;}
      }   
    }
    
    
    //MOVING INPUTS/////////////////////////////////////////////
   
    if (command!=null)
    {
      if (command.cmp("move"))
      {
        if (moveStamina>0)
        {
          Camera.viewer=this;
          
          x_prev=x;
          y_prev=y;
          moving=true;
        }
      }
        
      if (command.cmp("setpath"))
      {
        int cx=(int)command.get(0),
            cy=(int)command.get(1);
        
        for(ObjIter it=new ObjIter(Obj.oid.entity); it.end(); it.inc())
        {
          if (it.get()!=this && !((Entity)it.get()).passable)
          {((Entity)it.get()).tileStore();}
        }
        
        Pathfinder pathfinder=new Pathfinder(Terrain.terrain);
        pathList=pathfinder.pathFind((int)x/Terrain.cellSize,(int)y/Terrain.cellSize,cx,cy);
        
        for(ObjIter it=new ObjIter(Obj.oid.entity); it.end(); it.inc())
        {
          if (it.get()!=this && !((Entity)it.get()).passable)
          {((Entity)it.get()).tileRestore();}
        }
        
        cx_prev=cx;
        cy_prev=cy;
      }
      
      if (command.cmp("interact"))
      {
        if (moveStamina>0)
        {
          cx_prev=(int)command.get(0);
          cy_prev=(int)command.get(1);
          interact();
        }
      }
        
      if (command.cmp("endturn"))
      {myPlayer.endTurn();}
      
      command=null;
    }
     
    //MOVING INPUTS/////////////////////////////////////////////
  }
  
  @Override 
  public void DRAW()
  {
    Draw.setDepth((int)(-y));
    Draw.drawSprite(spr,tid,x+16,y+16+z);

    if (initiative && pathList!=null && myPlayer instanceof LocalPlayer)
    {
      PathPoint p=pathList;
      
      int add,i=0;
      while(p!=null)
      {
        Draw.setDepth((int)-p.y*Terrain.cellSize+1);
        
        if (i<moveStamina)
        {add=0;}
        else
        {add=2;}
        i+=1;
        Draw.drawSprite(new Sprite(Spr.path),((p.next==null)?1:0)+add,p.x*Terrain.cellSize,p.y*Terrain.cellSize);
        p=p.next;
      }
    }
  }
  
  @Override
  public void DESTROY()
  {
    if (myPlayer!=null)
    {myPlayer.peasantRemove(this);}
  }
  
  /**
   * Restores stamina.
   */
  public void staminaRefill()
  {moveStamina=moveStaminaMax;}
  
  void interact()
  {
    System.out.println("sip?");
    Entity intEntity=null;
    
    //Looking for entity in a cell.
    for(ObjIter it=new ObjIter(Obj.oid.entity); it.end(); it.inc())
    {
      Entity other=(Entity)it.get();
      if (other!=this)
      {
        if (other.x/Terrain.cellSize==cx_prev && other.y/Terrain.cellSize==cy_prev)
        {
          intEntity=other;
          break;
        }
      }
    }
    //Looking for entity in a cell.
  
    if (intEntity!=null)
    {
      //Entity actions.
      if (intEntity instanceof Peasant)
      {
        if (((Peasant)intEntity).tid!=tid)
        {
          //Kill fagget.
          Obj.objDestroy(intEntity);
        }
      }
      //Entity actions.
    }
    else
    {
      //Tile actions.
      
      int tile=Terrain.terrain[cx_prev][cy_prev];
      
      if (tile==1)
      {
        Terrain.terrain[cx_prev][cy_prev]=0;
        Terrain.terrainSpr[cx_prev][cy_prev]=null;
        moveStamina=0;
      }
      
      //Tile actions.
    }
    
  }
  
  
  public boolean pathCheck()
  {
    boolean isCorrect=true;
    PathPoint listBuf=pathList;
    
    for(ObjIter it=new ObjIter(Obj.oid.entity); it.end(); it.inc())
    {
      if (it.get()!=this && !((Entity)it.get()).passable)
      {((Entity)it.get()).tileStore();}
    }
    
    while(listBuf!=null)
    {
      if (!TileProp.isPassable(Terrain.terrain[listBuf.x][listBuf.y]))
      {
        isCorrect=false;
        break;
      }
      listBuf=listBuf.next;
    }
    
    for(ObjIter it=new ObjIter(Obj.oid.entity); it.end(); it.inc())
    {
      if (it.get()!=this && !((Entity)it.get()).passable)
      {((Entity)it.get()).tileRestore();}
    }
    
    return isCorrect;
    
  }
  
}


