import java.awt.*;
import java.awt.image.*;
import java.applet.*;
import java.util.*;
import java.awt.event.*;
import java.net.*;
import javax.swing.*;

public class Game extends Applet
{
	////////////////////////////////////the bounds of the game window////////////////////////////////////////
	public final int frameWidth = 600;
	public final int frameHeight = 650;
	////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	///////////////////////////////for double buffering/////////////////////////////////////////////////////
	public Image offscreen;
	public Graphics bufferGraphics;
	////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	////////////////////////the stuff you need to run the game/////////////////////////////////////////////
	public User u = new User(300, 400, this);	
	public Simulation myLevel;
	public int levelnum = 0;//starting level
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	
	///////////////////////Sound Information///////////////////////////////////////////////////////////////
	
	public AudioClip bgm;
	public AudioClip exp;
	public AudioClip pfire;
	public AudioClip cfire;
	
	//to play an explosion
	//exp.play();
	
	//to play a playerbullet
	//pfire.play();
	
	//to play a bullet
	//cfire.play();
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public Random r = new Random();
	
	/////////////////////////////////all input information//////////////////////////////////////////////
	public Inputs i = new Inputs();
	private boolean [] playerInputs = new boolean [10];//array positions are determined by the numpad on the right side of the keyboard

	public class Inputs extends Thread implements KeyListener
	{		
		public void run()
		{
			addKeyListener(this);
		}
		
		public void keyReleased(KeyEvent e)
		{
			if(e.getKeyCode() == KeyEvent.VK_DOWN)
			{
				playerInputs[2] = false;
			}
			if(e.getKeyCode() == KeyEvent.VK_UP)
			{
				playerInputs[8] = false;
			}
			if(e.getKeyCode() == KeyEvent.VK_LEFT)
			{
				playerInputs[4] = false;
			}
			if(e.getKeyCode() == KeyEvent.VK_RIGHT)
			{
				playerInputs[6] = false;
			}
			if(e.getKeyCode() == KeyEvent.VK_SPACE)
			{
				playerInputs[5] = true;//fire
			}
			if(e.getKeyCode() == e.VK_DELETE)
			{
				u.weaponLevel = 0;
				u.health = 100;
				u.score = 0;
				levelnum = 0;
				myLevel = instanciateLevel(levelnum);
			}
		}
	
		public void keyPressed(KeyEvent e)
		{
			if(e.getKeyCode() == KeyEvent.VK_DOWN)
			{
				playerInputs[2] = true ;//down			
			}
			if(e.getKeyCode() == KeyEvent.VK_UP)
			{
				playerInputs[8] = true;//up				
			}
			if(e.getKeyCode() == KeyEvent.VK_LEFT)
			{
				playerInputs[4] = true;//left		
			}
			if(e.getKeyCode() == KeyEvent.VK_RIGHT)
			{
				playerInputs[6] = true;//right				
			}
			if(e.getKeyCode()==KeyEvent.VK_BACK_SPACE)
			{
				u.weaponCheat = true;
			}
			if(e.getKeyCode()==KeyEvent.VK_HOME)
			{
				u.lifeCheat = true;
			}
			if(e.getKeyCode() == KeyEvent.VK_PAGE_UP)
			{
				u.rapidFire = true;
			}
			
		}
		
		public void keyTyped(KeyEvent e)
		{			
		}
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public class BackgroundObject
	{
		public int posX;
		public int posY;
		public int width = 60;
		public int height = 60;
		public ImageObserver images;
		public BackgroundObject(int x, int y, ImageObserver io)
		{
			posX = x;
			posY = y;
			images = io;		
		}
		
		public void act()//if the object moves or shrinks or grows etc.
		{
		}
		
		public void handleCollision(Object other)
		{
			if(other instanceof Plane);
			if(other instanceof User);
			if(other instanceof Bullet);
		}
		
		public void drawMe(Graphics g)
		{
			Image myImage = Toolkit.getDefaultToolkit().getImage("blue.gif");
			g.drawImage(myImage,posX,posY,width,height,images);
		}
	}
	
	public class ForegroundObject
	{
		public int posX;
		public int posY;
		public int width = 60;
		public int height = 60;
		public ImageObserver images;
		public ForegroundObject(int x, int y, ImageObserver io)
		{
			posX = x;
			posY = y;
			images = io;		
		}
		
		public void act()//if the object moves or shrinks or grows etc.
		{
		}
		
		public void handleCollision(Object other)
		{
		}
		
		public void drawMe(Graphics g)
		{
			Image myImage = Toolkit.getDefaultToolkit().getImage("blue.gif");
			g.drawImage(myImage,posX,posY,width,height,images);
		}
	}
	
	public class PowerUp extends ForegroundObject
	{
		public int VelX;
		public int VelY;
		
		public PowerUp(int x, int y, int VelX, int VelY, ImageObserver io)
		{
			super(x, y, io);
			this.VelX = VelX;
			this.VelY = VelY;
		}
		
		public void act()
		{
			posX+= VelX;
			posY+= VelY;			
		}
		
		public void handleCollision(Object other)
		{
		}
	}
	
	public class WeaponUpgrade extends PowerUp
	{
		public WeaponUpgrade(int x, int y, int VelX, int VelY, ImageObserver io)
		{
			super(x,y,VelX, VelY, io);
			this.width = 30;
			this.height = 30;
		}
		
		public void handleCollision(Object other)
		{
			if(other instanceof User)
			{
				User u = (User)other;
				u.weaponLevel++;
				myLevel.myForeground.remove(this);
			}
		}
		
		public void drawMe(Graphics g)
		{
			Image myImage = Toolkit.getDefaultToolkit().getImage("weaponup.gif");
			g.drawImage(myImage,posX,posY,width,height,images);
		}
	}
	
	public class HealthRestore extends PowerUp
	{
		public HealthRestore(int x, int y, int VelX, int VelY, ImageObserver io)
		{
			super(x,y,VelX, VelY, io);
			this.width = 30;
			this.height = 30;
		}
		
		public void handleCollision(Object other)
		{
			if(other instanceof User)
			{
				User u = (User)other;
				u.health = 100;
				myLevel.myForeground.remove(this);
			}
		}
		
		public void drawMe(Graphics g)
		{
			Image myImage = Toolkit.getDefaultToolkit().getImage("heart.gif");
			g.drawImage(myImage,posX,posY,width,height,images);
		}
	}
	
	public class Star extends BackgroundObject
	{
		public Star(int posX,int posY, ImageObserver io)
		{
			super(posX, posY, io);
		}
		
		public void drawMe(Graphics g)
		{
			g.setColor(new Color(200,200,200));
			g.drawOval(posX, posY, 1, 1);
		}
		
		public void act()
		{
			posY+=2;
			if(posY>frameHeight)
				posY = 0;
		}
	}
	
	public class Sentence extends ForegroundObject
	{
		public String myString;
		
		public Sentence(int x, int y, String s, ImageObserver io)
		{
			super(x, y, io);
			myString = s;	
		}
		
		public void act()
		{
		}
		
		public void drawMe(Graphics g)
		{
			g.setFont(new Font("Ariel", Font.BOLD, 40));
			g.setColor(Color.white);
			g.drawString(myString, posX, posY);
		}
	}
	
	public class ScrollingSentence extends Sentence
	{
		public int scrollSpeed;
		public int size = 30;
		
		public ScrollingSentence(int x, int y, String s, int speed,  ImageObserver io)
		{
			super(x, y, s, io);
			scrollSpeed = speed;
		}
		
		public void act()
		{
			if(posY>600)
			{
				posY = -99;
				return;
			}
			posY+=scrollSpeed;
				
		}
		
		public void drawMe(Graphics g)
		{
			g.setFont(new Font("Ariel", Font.BOLD, size));
			g.setColor(Color.white);
			g.drawString(myString, posX, posY);
		}
	}
	
	public class Explosion extends BackgroundObject
	{
		public boolean finished;//since we are using an animated gif, we need to know when to stop it
		
		public Explosion(int x, int y, ImageObserver io)
		{
			super(x, y, io);
			finished = false;
		}
		
		public void act()
		{
			if(!finished)
			{
				width -= 3;
				height -= 3;
				if(width<0||height<0)
				{
					width = 0;
					height = 0;
					finished = true;
				}
			}
		}
		
		public void drawMe(Graphics g)
		{
			Image myImage = Toolkit.getDefaultToolkit().getImage("Explode.gif");
			g.drawImage(myImage,posX,posY,width,height,images);
		}
	}
	
	public class User
	{
		private int health = 100;
		private int score = 0;
		private int sheilds = 100;
		public ArrayList<Bullet> myBullets = new ArrayList();
		public int posX;
		public int posY;
		public int width = 50;
		public int height = 50;
		public ImageObserver images;
		public int weaponLevel = 0;
		public boolean weaponCheat = false;
		public boolean lifeCheat = false;
		public boolean rapidFire = false;
		public User(int posX, int posY, ImageObserver io)
		{
			this.posX = posX;
			this.posY = posY;
			images = io;
		}
		
		public void move()
		{
			if(u.weaponCheat)
				weaponLevel = 100;
			if(u.lifeCheat)
				health = 100;
			/*if(health<100&&sheilds>0)
			{
				sheilds -= 100-health;
				health = 100;
				if (sheilds <0)
				{
					health -= 0-sheilds;
					sheilds = 0;
				}	
			}
			sheilds++;*/
				
			if(playerInputs[4])
			{
				posX -= 10;
				if(posX<0)
				{
					posX = 0;
				}

			}
			if(playerInputs[6])
			{
				posX +=10;
				if(u.posX+width>frameWidth)
				{
					posX = frameWidth-width;
				}

			}
			if(playerInputs[8])
			{
				posY -=10;
				if(posY<0)
				{
					posY = 0;
				}

			}
			if(playerInputs[2])
			{
				posY+=10;
				if(posY>frameHeight-height-9)
				{
					posY = frameHeight-height-9;
				}

			}
			if(playerInputs[5])
			{
				/*for(int i = 0;i<weaponLevel+1;i++)
				{
					myBullets.add(new PlayerBullet(posX + ((i+1)*width)/(weaponLevel+2), posY, images));
				}*/
				switch(u.weaponLevel)
				{
					case 0:
						myBullets.add(new PlayerBullet(posX +(u.width/2)-3, posY, images));
						break;
					case 1:
						myBullets.add(new PlayerBullet(posX +(width/3)-3, posY, images));
						myBullets.add(new PlayerBullet(posX +(2*width/3)-3, posY, images));
						break;
					case 2:
						myBullets.add(new SlantBullet(posX +(width/4)-3, posY,-1,9, images));
						myBullets.add(new SlantBullet(posX +(2*width/4)-3, posY,0, 10, images));
						myBullets.add(new SlantBullet(posX +(3*width/4)-3, posY,1,9, images));
						break;
					case 3:
						myBullets.add(new SlantBullet(posX +(width/5)-3, posY,-1,9, images));
						myBullets.add(new SlantBullet(posX +(2*width/5)-3, posY,-1, 9, images));
						myBullets.add(new SlantBullet(posX +(3*width/5)-3, posY,1,9, images));
						myBullets.add(new SlantBullet(posX +(4*width/5)-3, posY,1,9, images));
						break;
					case 4:
						myBullets.add(new SlantBullet(posX +(width/5)-3, posY,-2,8, images));
						myBullets.add(new SlantBullet(posX +(2*width/5)-3, posY,-1, 9, images));
						myBullets.add(new SlantBullet(posX +(2*width/5)-3, posY,0, 10, images));
						myBullets.add(new SlantBullet(posX +(3*width/5)-3, posY,1,9, images));
						myBullets.add(new SlantBullet(posX +(4*width/5)-3, posY,2,8, images));
						break;
					case 5:
						myBullets.add(new SlantBullet(posX +(width/5)-3, posY,-2,8, images));
						myBullets.add(new SlantBullet(posX +(2*width/5)-3, posY,-1, 9, images));
						myBullets.add(new SlantBullet(posX +(2*width/5)-3, posY,0, 10, images));
						myBullets.add(new SlantBullet(posX +(3*width/5)-3, posY,0,10, images));
						myBullets.add(new SlantBullet(posX +(4*width/5)-3, posY,1,9, images));
						myBullets.add(new SlantBullet(posX +(4*width/5)-3, posY,2,8, images));
						break;
					case 6:
						myBullets.add(new SlantBullet(posX +(width/5)-3, posY,-3,7, images));
						myBullets.add(new SlantBullet(posX +(2*width/5)-3, posY,-2, 8, images));
						myBullets.add(new SlantBullet(posX +(2*width/5)-3, posY,-1, 9, images));
						myBullets.add(new SlantBullet(posX +(3*width/5)-3, posY,0,10, images));
						myBullets.add(new SlantBullet(posX +(4*width/5)-3, posY,1,9, images));
						myBullets.add(new SlantBullet(posX +(4*width/5)-3, posY,2,8, images));
						myBullets.add(new SlantBullet(posX +(4*width/5)-3, posY,3,7, images));
						break;
					default:
						
						break;
				}
				if(u.weaponLevel==100)
				{
					if(myLevel.myPlanes.size() != 0)
					{
						for(int x = 0;x<myLevel.myPlanes.size();x++)
							myBullets.add(new MissileBullet((posX+posX+width)/2, posY - 30,(Plane)myLevel.myPlanes.get(x), images));
					}
					else
					{
						myBullets.add(new MissileBullet(posX +(u.width/2)-3, posY, images));
					}
				}
				pfire.play();
				playerInputs[5] = false;
				if(rapidFire)
					playerInputs[5] = true;
			}
			for(int x = 0; x<myBullets.size();x++)
			{
				myBullets.get(x).act();
				if(myBullets.get(x).posY<0)
				{
					myBullets.remove(x);
				}
			}
		}
		
		public void handleCollision(Object other)
		{
		}
		
		public void drawMe(Graphics g)
		{
			Image myImage = Toolkit.getDefaultToolkit().getImage("blue.gif");    
			//Draw image at its natural size
			g.drawImage(myImage,posX,posY,width,height,images);
			//draw health bar
			g.setColor(Color.red);
			g.fillRect(posX, posY+height+7, width, 2);
			g.setColor(Color.green);
			g.fillRect(posX, posY+height+7,(int)(((double)health/100.0)*width), 2);
		}
	}
	
	class Bullet
	{
		public int posX;
		public int posY;
		ImageObserver images;
		public int damage = 25;
		public Bullet(int x, int y, ImageObserver io)
		{
			posX= x;
			posY= y;
			images = io;
		}
		
		public void act()
		{
			posY += 15;
		}
		
		public void handleCollision(Object other)
		{
			
		}
		
		public void drawMe(Graphics g)
		{
			Image myImage = Toolkit.getDefaultToolkit().getImage("enemyshot.gif");        
			//Draw image at its natural size first.
			g.drawImage(myImage,posX,posY,5,5,images);
		}
	}
	
	class SlantBullet extends PlayerBullet
	{
		public int speedX;
		public int speedY;
		public SlantBullet(int x, int y, int dx, int dy, ImageObserver io)
		{
			super(x, y, io);
			speedX = dx;
			speedY = dy;
		}
		
		public void act()
		{
			posX += speedX;
			posY -= speedY;
		}
	}
	
	class MissileBullet extends PlayerBullet
	{
		public int distX;
		public int distY;
		int targetX;
		int targetY;
		Plane target;
		private int cons;
		public int hypotenuse;
		
		public MissileBullet(int x, int y, ImageObserver io){
			super(x, y, io);
			cons = 0;  
		}
		
		public MissileBullet(int x, int y, Plane plne, ImageObserver io)
		{
			super(x, y, io);
			cons = 1;
			int targetX = plne.posX;
			int targetY = plne.posY;
			target = plne;
			distX = targetX - posX;
			distY = posY - targetY;
			hypotenuse = (int)Math.sqrt((distX*distX)+(distY*distY));
		}
		
		public void act()
		{
			if(cons == 1){
				if(myLevel.myPlanes.size() != 0){
					if(target != (Plane)myLevel.myPlanes.get(myLevel.myPlanes.size() - 1)){
						target = (Plane)myLevel.myPlanes.get(myLevel.myPlanes.size() - 1);
					}
					targetX = target.posX;
					targetY = target.posY;
					distX = targetX - posX;
					distY = posY - targetY;
					if(targetY > posY - 14){
						posY -= 30;
					}
					int hypotenuse2 = (int)Math.sqrt((distX*distX)+(distY*distY));
					if(hypotenuse2 != 0){
						hypotenuse = hypotenuse2;
					}
					posX += (int)distX*25/hypotenuse;
					posY -= (int)distY*25/hypotenuse;
				}
				else{
					posY -= 30;
				}
			}
			else{
				posY -= 30;
			}
		}
		
		public void drawMe(Graphics g){
			Image myImage = Toolkit.getDefaultToolkit().getImage("missile.gif");        
			g.drawImage(myImage,posX,posY,10,20,images);
		}
	}
	
	class TargetBullet extends Bullet
	{
		public int distX;
		public int distY;
		public int hypotenuse;
		public TargetBullet(int x, int y, int x2, int y2, ImageObserver io)
		{
			super(x, y, io);
			int targetX = x2;
			int targetY = y2;
			distX = posX-targetX;
			distY = posY-targetY;
			hypotenuse = (int)Math.sqrt((distX*distX)+(distY*distY));
		}
		
		public void act()
		{
			posX -= (int)distX*20/hypotenuse;
			posY -= (int)distY*20/hypotenuse;			
		}
	}
	
	class RandomBullet extends Bullet
	{
		public int bulletSpeed;
		public RandomBullet(int posX, int posY, ImageObserver io)
		{
			super(posX, posY, io);
			Random rand = new Random();
			bulletSpeed = rand.nextInt(6)+10;
		}
		
		public void act()
		{
			posY+=bulletSpeed;
		}
	}
	
	class PlayerBullet extends Bullet{
		
		public PlayerBullet(int posX, int posY, ImageObserver io)
		{
			super(posX, posY, io);
			damage = 10;
		}
		
		public void act()
		{
			posY -= 10;
		}
		
		public void drawMe(Graphics g)
		{
			Image myImage = Toolkit.getDefaultToolkit().getImage("shot.gif");        
			g.drawImage(myImage,posX,posY,5,5,images);
		}
	}
	
	class Plane{
		public ArrayList<Bullet> myBullets = new ArrayList();
		protected Random rand = new Random();
		public int health = 50;
		public int posX;
		public int posY;
		public int width = 60;
		public int height = 60;
		public ImageObserver images;
		public Plane(int x, int y, ImageObserver io)
		{
			posX = x;
			posY = y;
			images = io;
		}
		
		public void act()
		{
		}
		
		public void fire()
		{
			myBullets.add(new Bullet(posX+15, posY + 30, images));
			cfire.play();
		}
		
		public void handleCollision(Object other)
		{
			
		}
		
		public void drawMe(Graphics g)
		{
			Image myImage = Toolkit.getDefaultToolkit().getImage("red.gif");        
			g.drawImage(myImage,posX,posY,width,height,images);
			
			g.setColor(Color.red);
			g.fillRect(posX, posY-7, width, 2);
			g.setColor(Color.green);
			g.fillRect(posX, posY-7,(int)(((double)health/50.0)*width), 2);
		}
	}
	
	class BossPlane extends Plane
	{
		private boolean toLeft = true;
		private int leftBound;
		private int rightBound;
		public BossPlane(int posX, int posY, ImageObserver io)
		{
			super(posX, posY, io);
			leftBound = 0;
			rightBound = frameWidth;
			health = 200;
		}
		
		public BossPlane(int posX, int posY, int rBound, int lBound, ImageObserver io)
		{
			super(posX, posY, io);
			leftBound = lBound;
			rightBound = rBound;
		}
		
		public void act()
		{
			if(toLeft)
			{
				posX-=20;
				if(posX<leftBound)
				{
					posX = 0;
					toLeft = false;
				}
			}else{
				posX+=20;
				if(posX+width>rightBound)
				{
					posX = rightBound-width;
					toLeft = true;
				}
			}
			
			if(rand.nextDouble()<.25)
			{
				fire();
			}
			
			for(int x = 0;x<myBullets.size();x++)
			{
				Bullet b = myBullets.get(x);
				if(b!=null)
				{
					b.act();
					if(b.posY>frameHeight)
					{
						myBullets.remove(b);
					}
				}
			}
		}
		
		public void fire()
		{
			myBullets.add(new TargetBullet((posX+posX+width)/2, posY + 30,u.posX, u.posY, images));
		}
		
		public void drawMe(Graphics g)
		{
			Image myImage = Toolkit.getDefaultToolkit().getImage("green.gif");        
			//Draw image at its natural size first.
		
			g.drawImage(myImage,posX,posY,width,height,images);
			
			g.setColor(Color.red);
			g.fillRect(posX, posY-7, width, 2);
			g.setColor(Color.green);
			g.fillRect(posX, posY-7,(int)(((double)health/200.0)*width), 2);
		}
	}
	
	class RandomPlane extends Plane
	{
		public RandomPlane(int posX, int posY, ImageObserver io)
		{
			super(posX, posY, io);
		}
		public void fire()
		{
			myBullets.add(new RandomBullet(posX+15, posY + 30,images));
		}
	}
	
	class SquarePlane extends Plane
	{
		private int leftBound;
		private int rightBound;
		private int topBound;
		private int bottomBound;
		private boolean clockwise;
		
		public SquarePlane(int posX, int posY, int lBound, int rBound, int tBound, int bBound, ImageObserver io)
		{
			super(posX, posY, io);
			leftBound = lBound;
			rightBound = rBound;
			topBound = tBound;
			bottomBound = bBound;
			clockwise = true;
		}
		
		public void act()
		{
			if(clockwise)
			{
				if(posY <= topBound)
				{
					posY= topBound;
					posX+=20;
					return;
				}
				if(posX+width>=rightBound)
				{
					posX = rightBound-width;
					posY+=20;
					return;
				}
				if(posY+height>=bottomBound)
				{
					posY = bottomBound-width;
					posX-=20;
					return;
				}
				if(posX<=leftBound)
				{
					posX = leftBound;
					posY-=20;
					return;
				}
			}else{//under construction
				if(posY <= topBound)
				{
					posY= topBound;
					posX-=20;
					if(!(posX<=leftBound))
						return;
				}
				if(posX<=leftBound)
				{
					posX = leftBound;
					posY+=20;
					if(!(posY+height>=bottomBound))
						return;
				}
				if(posY+height>=bottomBound)
				{
					posY = bottomBound-height;
					posX+=20;
					if(!(posX+width>=rightBound))
						return;
				}
				if(posX+width>=rightBound)
				{
					posX = rightBound-width;
					posY+=20;
					return;
				}
			}
			
			if(rand.nextDouble()<.15)
			{
				fire();
			}
			
			for(int x = 0;x<myBullets.size();x++)
			{
				Bullet b = myBullets.get(x);
				if(b!=null)
				{
					b.act();
					if(b.posY>frameHeight)
					{
						myBullets.remove(b);
					}
				}
			}
		}
		
		
	}
	
	class FormationPlane extends Plane
	{
		private boolean toLeft = true;
		private int leftBound;
		private int rightBound;
		public FormationPlane(int posX, int posY, ImageObserver io)
		{
			super(posX, posY, io);
			leftBound = 0;
			rightBound = frameWidth;
		}
		
		public FormationPlane(int posX, int posY, int rBound, int lBound, ImageObserver io)
		{
			super(posX, posY, io);
			leftBound = lBound;
			rightBound = rBound;
		}
		
		public void act()
		{
			if(toLeft)
			{
				posX-=20;
				if(posX<leftBound)
				{
					posX = 0;
					toLeft = false;
				}
			}else{
				posX+=20;
				if(posX+width>rightBound)
				{
					posX = rightBound-width;
					toLeft = true;
				}
			}
			
			if(rand.nextDouble()<.15)
			{
				fire();
			}
			
			for(int x = 0;x<myBullets.size();x++)
			{
				Bullet b = myBullets.get(x);
				if(b!=null)
				{
					b.act();
					if(b.posY>frameHeight)
					{
						myBullets.remove(b);
					}
				}
			}
		}
		
		public void drawMe(Graphics g)
		{
			Image myImage = Toolkit.getDefaultToolkit().getImage("red.gif");        
			//Draw image at its natural size first.
		
			g.drawImage(myImage,posX,posY,width,height,images);
			
			g.setColor(Color.red);
			g.fillRect(posX, posY-7, width, 2);
			g.setColor(Color.green);
			g.fillRect(posX, posY-7,(int)(((double)health/50.0)*width), 2);
		}
	}
		
 	class Simulation
	{
		public ArrayList <BackgroundObject> myBackground = new ArrayList<BackgroundObject>();
		public ArrayList <ForegroundObject> myForeground = new ArrayList<ForegroundObject>();
		public ArrayList <Plane> myPlanes = new ArrayList<Plane>();
		public User myUser;
		ImageObserver images;
		public boolean keepGoing;
		public Simulation(ArrayList thePlanes, User theUser, ImageObserver io)
		{
			myPlanes.addAll(thePlanes);
			myUser = theUser;
			images = io;
			keepGoing = true;
		}
		
		public Simulation(ArrayList thePlanes, User theUser, ArrayList theBackground, ImageObserver io)
		{
			myPlanes.addAll(thePlanes);
			myUser = theUser;
			images = io;
			keepGoing = true;
			myBackground.addAll(theBackground);
		}
		
		public Simulation(ArrayList thePlanes, User theUser, ArrayList theBackground, ArrayList theForeground, ImageObserver io)
		{
			myPlanes.addAll(thePlanes);
			myUser = theUser;
			images = io;
			keepGoing = true;
			myBackground.addAll(theBackground);
			myForeground.addAll(theForeground);
		}
			
		public void step()
		{
			if(keepGoing)
			{
				try{
					Thread pause = new Thread();
					pause.sleep(25);
				}catch(Exception e)
				{}
				moveBackground();
				moveForeground();
				movePlanes();
				myUser.move();
				cleanUp();
				repaint();
			}else{
				try{
					Thread pause = new Thread();
					pause.sleep(330);
				}catch(Exception e)
				{}
				levelnum++;
				startLevel();
			}
		}
		
		public void moveBackground()
		{
			for(int x = 0; x<myBackground.size();x++)
			{
				myBackground.get(x).act();				
			}
		}
		
		public void moveForeground()
		{
			for(int x = 0; x<myForeground.size();x++)
			{
				myForeground.get(x).act();				
			}
		}
		
		public void movePlanes()
		{
			for(int x = 0; x<myPlanes.size();x++)
			{
				myPlanes.get(x).act();				
			}
		}
		
		public void paintAll(Graphics g)
		{
			g.setColor(Color.black);
			g.fillRect(0, 0, frameWidth, frameHeight);
			//draw score bar
			g.setColor(new Color(40, 40, 40));
			g.fillRect(0,frameHeight, frameWidth, frameHeight+50);
			g.setColor(Color.white);
			g.setFont(new Font("Ariel", Font.BOLD, 10));
			g.drawString(("Level: "+(levelnum+1)), 20, frameHeight+20);
			g.drawString("Score: "+myUser.score, frameWidth-100, frameHeight+20);
			for(BackgroundObject b: myBackground)
			{
				b.drawMe(g);
			}
			for(int x = 0; x<myPlanes.size(); x++)
			{
				if(myPlanes.get(x) != null)
				{
					myPlanes.get(x).drawMe(g);
					for(int y= 0; y<myPlanes.get(x).myBullets.size();y++)
					{
						myPlanes.get(x).myBullets.get(y).drawMe(g);
					}	
				}
			}
			if(myUser!=null)
			{
				myUser.drawMe(g);
				for(int y= 0; y<myUser.myBullets.size();y++)
				{
					myUser.myBullets.get(y).drawMe(g);
				}
			}
			for(ForegroundObject b: myForeground)
			{
				b.drawMe(g);
			}			
		}
		
		public void handleCollisions()
		{
			for(int i = 0;i<myForeground.size();i++)
			{
				ForegroundObject a = myForeground.get(i);
				if((a.posX>myUser.posX)&&(a.posX<myUser.posX+myUser.width))
				{
					if((a.posY>myUser.posY)&&(a.posY<myUser.posY+myUser.height))
					{
						a.handleCollision(myUser);
					}
				}
				if((a.posX+a.width>myUser.posX)&&(a.posX+a.width<myUser.posX+myUser.width))
				{
					if((a.posY>myUser.posY)&&(a.posY<myUser.posY+myUser.height))
					{
						a.handleCollision(myUser);
					}
				}
				if((a.posX>myUser.posX)&&(a.posX<myUser.posX+myUser.width))
				{
					if((a.posY+a.height>myUser.posY)&&(a.posY+a.height<myUser.posY+myUser.height))
					{
						a.handleCollision(myUser);
					}
				}
				if((a.posX+a.width>myUser.posX)&&(a.posX+a.width<myUser.posX+myUser.width))
				{
					if((a.posY+a.height>myUser.posY)&&(a.posY+a.height<myUser.posY+myUser.height))
					{
						a.handleCollision(myUser);
					}
				}
			}
			for(int x = 0;x<myPlanes.size();x++)
			{
				Plane p = myPlanes.get(x);
				for(int i = 0; i<p.myBullets.size();i++)
				{
					Bullet b = p.myBullets.get(i);
					if(b.posX>myUser.posX&&b.posX<myUser.posX+myUser.width)
							if(b.posY>myUser.posY&&b.posY<myUser.posY+myUser.height)
							{
								p.myBullets.remove(b);
								myUser.health-= b.damage; 
							}
				}
				for(int y = 0; y<myPlanes.size();y++)//check plane/plane collision
				{
					//if two planes are inside one another
					if(p!=null&&myPlanes.get(y)!=null&&(!p.equals(myPlanes.get(y))))
					{
						Plane other = myPlanes.get(y);
						if(p.posX>other.posX&&p.posX<other.posX+other.width)
							if(p.posY>other.posY&&p.posY<other.posY+other.height)
							{
								p.health-=25;
								myPlanes.get(y).health-=25;
							}
					}
				}
				for(int z = 0;z<myUser.myBullets.size();z++)
				{
					if(myUser.myBullets.get(z).posY<0)
					{
						myUser.myBullets.remove(z);
						continue;
					}
					//if a player bullet collides with a plane
					if(p!=null)
						if(myUser.myBullets.get(z).posX>p.posX&&myUser.myBullets.get(z).posX<p.posX+p.width)
							if(myUser.myBullets.get(z).posY>p.posY&&myUser.myBullets.get(z).posY<p.posY+p.height)
							{
								p.health -=myUser.myBullets.get(z).damage;
								myUser.myBullets.remove(z);
							}		
				}
				//check player and plane collisions
				if(myUser.posX>p.posX&&myUser.posX<p.posX+p.width)
					if(myUser.posY>p.posY&&myUser.posY<p.posY+p.height)	
					{
						myUser.health -= 25;
						p.health -= 25;
					}		
			}
			
		}
		
		public void cleanUp()
		{
			handleCollisions();
			for(int a = 0;a<myForeground.size();a++)
			{
				ForegroundObject f = myForeground.get(a);
				if((f.posX<0)||(f.posX+f.width>frameWidth)||(f.posY<0)||(f.posY+f.height>frameHeight))
				{
					myForeground.remove(f);
				}
			}
				
			for(int x = 0; x<myPlanes.size(); x++)
			{
				Plane p = myPlanes.get(x);
				if(p!=null)
				{
					if(p!=null&&p.posY>=800)//if lower than applet bottom
					{
						p.posY = 0;//reset position
					}
					for(int i = 0; i<p.myBullets.size();i++)//check player/bullet collisions
					{
						Bullet b = p.myBullets.get(i);
						if(b.posY>frameHeight)
						{
							p.myBullets.remove(b);
							continue;
						}
						if(b.posY<0)
						{
							p.myBullets.remove(b);
							continue;
						}
						if(b.posX>frameWidth)
						{
							p.myBullets.remove(b);
						}
						if(b.posX<0)
						{
							p.myBullets.remove(b);
						}
						
					}
					
					//if plane is dead, remove it
					if(p!=null&&p.health<=0)
					{
						myUser.score++;
						myBackground.add(new Explosion(p.posX, p.posY, images));
						exp.play();
						myPlanes.remove(p);
						if(r.nextDouble()<.4)
						{
							myForeground.add(new WeaponUpgrade(p.posX, p.posY, 0, 10, images));
						}
						if(r.nextDouble()<.4)
						{
							myForeground.add(new HealthRestore(p.posX, p.posY, 0, 10, images));
						}
						if(myPlanes.size()==0)
						{
							keepGoing = false;
						}					
					}
					//if user is dead, end simulation
					if(myUser!= null&&myUser.health<=0)
					{
						keepGoing = false;
					}
				}
			}
		}
	}
	
	public void init()
	{
		try{
			bgm = JApplet.newAudioClip(new URL("file:Space_Theme.mp3"));
			exp = JApplet.newAudioClip(new URL("file:DSRXPLOD.wav"));
			pfire = JApplet.newAudioClip(new URL("file:22laser_blast.wav"));
			cfire = JApplet.newAudioClip(new URL("file:22laser_blast.wav"));
			bgm.loop();
		}
		catch(Exception e){
			System.out.println(e.toString());
		}
		offscreen = createImage(frameWidth,frameHeight+50);
		bufferGraphics = offscreen.getGraphics();
		try{
			Thread pause = new Thread();
			pause.sleep(300);
			}catch(Exception e)
			{}
		startLevel();
	}
	
	public void startLevel()
	{
		i.run();
		if(u.health<=0)
		{
			levelnum = 99;					
		}
		myLevel = instanciateLevel(levelnum);
		if(myLevel!=null)
			myLevel.step();
	}
	
	public Simulation instanciateLevel(int num)//fill next level with stuff
	{
		ArrayList planes = new ArrayList();
		ArrayList back = new ArrayList();
		ArrayList front = new ArrayList();
		if(num == 99)//game over (bad)
		{
			front.add(new Sentence(100, 200,"Game Over",  this));
			front.add(new Sentence(100, 300, "You Lose", this));
			front.add(new WeaponUpgrade(100, 100, 0, 1, this));
			return new Simulation(new ArrayList(), u, new ArrayList(), front, this);			
		}
		if(num == 0)
		{
			planes.add(new FormationPlane(100, 10, this));
			for(int x = 0;x<30;x++)
				back.add(new Star(r.nextInt(frameWidth), r.nextInt(frameHeight), this));
			return new Simulation(planes, u, back, new ArrayList(), this);
		}
		if(num==1)
		{
			planes.add(new FormationPlane(100, 10, this));
			planes.add(new FormationPlane(50, 110, this));
			for(int x = 0;x<30;x++)
				back.add(new Star(r.nextInt(frameWidth), r.nextInt(frameHeight), this));
			return new Simulation(planes, u, back, new ArrayList(), this);
		}
		if(num==2)
		{
			planes.add(new FormationPlane(100, 10, this));
			planes.add(new FormationPlane(50, 110, this));
			planes.add(new FormationPlane(150, 210, this));
			for(int x = 0;x<30;x++)
				back.add(new Star(r.nextInt(frameWidth), r.nextInt(frameHeight), this));
			return new Simulation(planes, u, back, new ArrayList(), this);
		}
		if(num==3)
		{
			planes.add(new FormationPlane(0, 10, this));
			planes.add(new FormationPlane(50, 110, this));
			planes.add(new FormationPlane(100, 210, this));
			planes.add(new FormationPlane(150, 310, this));
			for(int x = 0;x<30;x++)
				back.add(new Star(r.nextInt(frameWidth), r.nextInt(frameHeight), this));
			return new Simulation(planes, u, back, new ArrayList(), this);
		}
		if(num==4)
		{
			planes.add(new BossPlane(100, 10, this));
			for(int x = 0;x<30;x++)
				back.add(new Star(r.nextInt(frameWidth), r.nextInt(frameHeight), this));
			return new Simulation(planes, u, back, new ArrayList(), this);
		}
		if(num==5)
		{
			planes.add(new BossPlane(0, 10, this));
			planes.add(new BossPlane(50, 110, this));
			for(int x = 0;x<30;x++)
				back.add(new Star(r.nextInt(frameWidth), r.nextInt(frameHeight), this));
			return new Simulation(planes, u, back, new ArrayList(), this);
		}
		if(num==6)
		{
			planes.add(new BossPlane(0, 10, this));
			planes.add(new BossPlane(50, 110, this));
			planes.add(new BossPlane(100,210,this));
			for(int x = 0;x<30;x++)
				back.add(new Star(r.nextInt(frameWidth), r.nextInt(frameHeight), this));
			return new Simulation(planes, u, back, new ArrayList(), this);
		}
		if(num==7)
		{
			planes.add(new BossPlane(0, 10, this));
			planes.add(new BossPlane(50, 110, this));
			planes.add(new BossPlane(100,210,this));
			planes.add(new BossPlane(150,310,this));
			for(int x = 0;x<30;x++)
				back.add(new Star(r.nextInt(frameWidth), r.nextInt(frameHeight), this));
			return new Simulation(planes, u, back, new ArrayList(), this);
		}
		if(num == 8)//game over (good)
		{
			front.add(new Sentence(100, 100,"Game Over",  this));
			front.add(new Sentence(100, 200, "You Win", this));
			front.add(new ScrollingSentence(100, 250, "Creator:", 1, this));
			front.add(new ScrollingSentence(150, 300, "John Kossa", 1, this));
			front.add(new ScrollingSentence(100, 350, "Assistant Programmer:", 1, this));
			front.add(new ScrollingSentence(150, 400, "Albert Chan", 1, this));
			front.add(new ScrollingSentence(100, 450, "Next time:", 1, this));
			front.add(new ScrollingSentence(100, 500, "press Backspace", 1, this));
			front.add(new ScrollingSentence(100, 550, "for a cheat!", 1, this));
			for(int x = 0;x<30;x++)
				back.add(new Star(r.nextInt(frameWidth), r.nextInt(frameHeight), this));
			return new Simulation(new ArrayList(), u, back, front, this);			
		}
		return null;
	}
	
	public void update(Graphics g)
	{
		paint(g);
	}

	public void paint(Graphics g)
	{
		if(myLevel!=null)
		{
			myLevel.paintAll(bufferGraphics);
			g.drawImage(offscreen,0,0,this);
			myLevel.step();
		}
	}
}