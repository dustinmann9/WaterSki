package com.mannsclann;

import java.util.ArrayList;
import java.util.Random;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class WaterSki extends Activity {

	private SkiViewLayout mSkiViewLayout;
	private SensorManager mSensorManager;
	private PowerManager mPowerManager;
	private WindowManager mWindowManager;
	private Display mDisplay;
	private WakeLock mWakeLock;
	private Handler mHandler;
	public int difficultyLevel;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Get an instance of the SensorManager
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		// Get an instance of the PowerManager
		mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);

		// Get an instance of the WindowManager
		mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		mDisplay = mWindowManager.getDefaultDisplay();


		// Create a bright wake lock
		mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, getClass()
				.getName());

		// instantiate our simulation view and set it as the activity's content

		setDifficulty(3); //TODO Update difficulty by menu

		mSkiViewLayout = new SkiViewLayout(this);
		setContentView(mSkiViewLayout);


	}

	@Override
	protected void onResume() {
		super.onResume();
		/*
		 * when the activity is resumed, we acquire a wake-lock so that the
		 * screen stays on, since the user will likely not be fiddling with the
		 * screen or buttons.
		 */
		mWakeLock.acquire();

		// Start the simulation
	}

	@Override
	protected void onPause() {
		super.onPause();
		/*
		 * When the activity is paused, we make sure to stop the simulation,
		 * release our sensor resources and wake locks
		 */

		// Stop the simulation


		// and release our wake-lock
		mWakeLock.release();
	}

	public void setDifficulty(int difficulty) {
		difficultyLevel = difficulty;
	}

	public class SkiViewLayout extends RelativeLayout {
		protected SkiView mSkiView;
		protected RelativeLayout relative;
		protected TextView mAmmoText, mPoints, mGameStatus;
		protected LayoutInflater mLayoutInflater;
		protected Rect mRectAmmo, mRectDamage, mRectAmmoGone, mRectDamageGone;
		protected int mAmmoRemainLocation, mDamageRemainLocation, mScreenWidth, mScreenHeight;
		protected int mOrigAmmoRemainLocation, mOrigDamageRemainLocation;
		protected JetSki mPlayer;
		protected int mGameLevel;


		public SkiViewLayout(Context context) {
			super(context);
			// Initialize all views/layouts
			init(context);
			// add the skiView object (the background, player, etc.) to screen
			// add the items found in table.xml(textviews) to the screen.
			addView(mSkiView);
			addView(relative);
			// Set the level to 1.
			mGameLevel = 1;
		}

		public void init(Context context) {
			// Parameters to tell the different views how much space to take up
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,
					RelativeLayout.LayoutParams.FILL_PARENT);
			lp.height = RelativeLayout.LayoutParams.FILL_PARENT;
			// The width, in pixels of the screen
			mScreenWidth = mDisplay.getWidth();
			// The height, in pixels of the screen
			mScreenHeight = mDisplay.getHeight();
			// Create a layout inflater to inflate the layouts from table.xml
			mLayoutInflater = getLayoutInflater();
			relative = (RelativeLayout)mLayoutInflater.inflate(R.layout.table,null);
			relative.setLayoutParams(lp);
			// Create an object for the TextView containing the amount of money
			// acquired.
			mPoints = (TextView)relative.getChildAt(1);
			// Used to update Game Over messages, etc.
			mGameStatus = (TextView)relative.getChildAt(3);
			// X coordinate of the right edge of the damage bar.
			mDamageRemainLocation = (int)Math.round(mScreenWidth / 5.0);
			// Create a green rectangle to represent a full damage bar.
			mRectDamage = new Rect(2,20,mDamageRemainLocation,30); // TODO Update location information to be a fraction of overall screen size
			// instead of magic numbers.			
			// x coord of left edge of the Ammo bar
			mAmmoRemainLocation =  mScreenWidth - (int)Math.round(mScreenWidth / 5.0);
			// create a rectangle to represent a full Ammo bar.
			mRectAmmo = new Rect(mAmmoRemainLocation,20,mScreenWidth - 2, 30);
			// this rectangle is used to show in red the amount of Ammo used.  As
			// you use Ammo it increments to the right as the green rect decrements to the right.
			// This gives the visual effect of the bar getting smaller.
			mRectAmmoGone = new Rect(mOrigAmmoRemainLocation,20,mAmmoRemainLocation,30);
			mRectDamageGone = new Rect(mDamageRemainLocation,20,mDamageRemainLocation,30);
			// Create a new ski view object
			mSkiView = new SkiView(context);
			// x coordinates of where the Ammo gauge is.  Orig is used to store the
			// left coordinate of the gauge.
			mOrigAmmoRemainLocation = mAmmoRemainLocation;
			// Everything for damage is the same as for Ammo, but backwards
			mOrigDamageRemainLocation = mDamageRemainLocation;
			mPlayer = new JetSki(context, mOrigAmmoRemainLocation, mScreenWidth - 2, mSkiView, mHandler);
		}

		public class SkiView extends SurfaceView implements SurfaceHolder.Callback, SensorEventListener {
			private SkiThread thread;
			private Sensor mAccelerometer;
			private float mXDpi, mYDpi, mXOrigin, mYOrigin, mSensorX, mSensorY;
			private Bitmap mPlayerBitmap, mBackgroundImage;
			private ArrayList<Item> gameItems;
			private ArrayList<Enemies> gameEnemies;
			private Random mGenerator;
			private Context mContext;
			private boolean mAddBoss, mStartedBossFight, mBossDestroyed, autoFire;
			private long startTime, endTime;


			public SkiView(Context context) {
				super(context);
				mContext = context;
				// Initialize our random number generator.
				mGenerator = new Random();
				// Initialize the items array.
				gameItems = new ArrayList<Item>();
				// Initialize the enemies array.
				gameEnemies = new ArrayList<Enemies>();
				// Create our background image from the drawables folder.
				mBackgroundImage = BitmapFactory.decodeResource(getResources(), R.drawable.ocean);
				mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

				SurfaceHolder holder = getHolder();
				holder.addCallback(this);
				// used to keep track of a time difference between when touch the screen and releasing your finger
				startTime = 0;
				endTime = 0;


				thread = new SkiThread(holder,context, new Handler() {
					@Override
					public void handleMessage(Message msg) {
						Bundle b = msg.getData();
						int newPoints = Integer.parseInt((String) mPoints.getText()) + b.getInt("points");
						String strStatus = b.getString("status");
						if (!strStatus.equals("")) {
							mGameStatus.setText(strStatus);
							mGameStatus.setVisibility(View.VISIBLE);
						} else mGameStatus.setVisibility(View.INVISIBLE);
						mPoints.setText("" + newPoints);
					}
				});
				DisplayMetrics metrics = new DisplayMetrics();
				getWindowManager().getDefaultDisplay().getMetrics(metrics);
				mPlayerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.player);
				mAddBoss = false;
				mStartedBossFight = false;
				mBossDestroyed = false;
				endTime = 0;
				startTime = 0;
			}


			public void surfaceChanged(SurfaceHolder holder, int format, int width,
					int height) {
			}

			public void surfaceCreated(SurfaceHolder holder) {
				mBackgroundImage = Bitmap.createScaledBitmap(mBackgroundImage, mScreenWidth, mScreenHeight, false);
				thread.setRunning(true);
				thread.start();
			}

			public void surfaceDestroyed(SurfaceHolder holder) {
				thread.setRunning(false);
				// TODO Remove logging
				Log.d("Stop thread", "message from Stop Thread");
			}

			public SkiThread getThread() {
				return thread;
			}

			public void onSensorChanged(SensorEvent event) {
				thread.update(event);
			}

			@Override
			protected void onSizeChanged(int w, int h, int oldw, int oldh) {
				// compute the origin of the screen relative to the origin of
				// the bitmap
				mXOrigin = (w - mPlayerBitmap.getWidth()) * 0.5f;
				mYOrigin = (h - mPlayerBitmap.getHeight()) * 0.5f;
			}

			public void onAccuracyChanged(Sensor sensor, int accuracy) {
				// TODO Auto-generated method stub

			}

			// OnTouch Listener used to shoot the gun and use ammo
			// Also want to detect a long touch to advance levels.
			@Override
			public boolean onTouchEvent(MotionEvent event) {

				int action = event.getAction();
				// calculate the time between touching down and releasing.

				// Do various actions based on the event.
				switch (action) {
				case (MotionEvent.ACTION_DOWN): {
					startTime = System.currentTimeMillis();
					mPlayer.shoot();
					mPlayer.useAmmo();
					if (mPlayer.getGun().getAutomatic()) thread.autoFire();
					break;
				}
				case (MotionEvent.ACTION_UP) : {
					if (mPlayer.getGun().getAutomatic()) thread.stopAutoFire();
				}
				}
				long difference = endTime - startTime;
				if (difference > 1500 && difference < 3000 && mBossDestroyed) nextLevel();
				return true;
			}

			// Used to increase the Level by 1;
			public void nextLevel(){
				mGameLevel++;
				resetGame();
			}

			public void resetGame() {
				mAddBoss = false;
				mBossDestroyed = false;
				mStartedBossFight = false;
				thread.resetCounter();
				thread.sendMessage("status", "");
			}

			// Used to randomly generate an integer.  The int will be equivalent to an index
			// in the items array so that we can randomly select a type of item to appear.
			public int randomItemIndex() {
				int random = mGenerator.nextInt(100);
				if (random > 0 && random < 25) return 1;
				else if (random >= 25 && random < 50) return 2;
				else if (random >= 80 && random < 90) return 3;
				else if (random >= 90) return 4;
				else return 0;
			}

			// Used to randomly generate a number between 0 and 99.  I can use it to randomly select something
			// and since it is between 0 and 99 I can set weights, so if it is between 1 and 33, it will occur
			// 33% of the time, etc.
			public int randomEnemyIndex() {
				int random = mGenerator.nextInt(100);
				// After determining other enemeies set up weights.
				if(random<70)
					return 1;				
				else if(random>69 && random<90)
					return 2;
				else
					return 3;
			}

			// Used to create the new item and add it to the gameItems array.
			public void addNewItem() {
				int index = randomItemIndex();
				// if index = 1, create a new gas can and add to array.
				switch (index) {
				case 3: gameItems.add(new AutomaticRifle(mContext, mScreenWidth)); break;
				case 4: gameItems.add(new ShotGunItem(mContext, mScreenWidth)); break;
				default : break;
				}
			}
			// Add enemies based on level
			public void addEnemy() {
				if (mAddBoss && gameEnemies.size() == 0 && !mStartedBossFight) {
					mStartedBossFight = true;
					switch (mGameLevel) {
					case 1 : addLvl1Boss(); break;
					case 2 : addLvl2Boss(); break;
					}
					return;
				} else if (!mAddBoss){
					switch (mGameLevel) {
					case 1 : addLvl1Enemy(); break;
					case 2 : addLvl2Enemy(); break;
					// Reset level because we only have 2 levels so far
					case 3 : mGameLevel = 1; break;
					}
				}
			}

			// Add a new lvl1 enemy on screen.
			public void addLvl1Enemy() {
				gameEnemies.add(new Duck(mContext, mScreenWidth));
			}

			public void addLvl2Enemy() {
				/*int index = randomEnemyIndex();
				switch (index) {
				case 1 : */
				gameEnemies.add(new Rock(mContext, mScreenWidth));
			}
			// Add a Level 1 Boss
			public void addLvl1Boss() {
				gameEnemies.add(new Obama(mContext, mScreenWidth));
			}
			// add a level 2 boss
			public void addLvl2Boss() {
				gameEnemies.add(new Baby(mContext, mScreenWidth));
			}
			// Updates the position of all the items.
			public void updatePositions() {
				// Update items
				for (int i = 0; i < gameItems.size(); i++) {
					gameItems.get(i).addToYCoord(difficultyLevel);
				}
				// Update enemies
				for (int j = 0; j < gameEnemies.size(); j++) {
					gameEnemies.get(j).addToYCoord(difficultyLevel);
				}
			}

			public int getImageWidth() {
				return this.mBackgroundImage.getWidth();
			}

			public int getImageHeight() {
				return this.mBackgroundImage.getHeight();
			}

			class SkiThread extends Thread {
				private SurfaceHolder mSurfaceHolder;
				private Random mRandom;
				private int bgX;
				private int bgY;
				private int newBGY;
				private int moveBGY;
				private boolean mRun;
				private int rollingCounter;


				public SkiThread(SurfaceHolder holder, Context context, Handler handle) {
					mHandler = handle;
					mSurfaceHolder = holder;
					mContext = context;

					bgX = 0;
					bgY = 0;
					mRandom = new Random();
				}

				public void sendMessage(String name, String message) {
					Bundle b = new Bundle();
					Message msg = new Message();
					b.putString(name, message);
					msg.setData(b);
					mHandler.sendMessage(msg);
				}

				@Override
				public void run() {
					mSensorManager.registerListener(mSkiView,mAccelerometer,SensorManager.SENSOR_DELAY_UI);

					while (mRun) {
						// Message and Bundle in order to send info to main thread.  Required to update UI.
						Canvas c = null;
						try {
							if (shouldAddItem(rollingCounter)) mSkiView.addNewItem();
							if (shouldAddEnemy(rollingCounter)) mSkiView.addEnemy();

							// This means you defeated the boss.
							if (mStartedBossFight && gameEnemies.size() == 0) {
								sendMessage("status", "Level Complete\nTap and Hold to\nFor Next Level");
								mBossDestroyed = true;
								rollingCounter = 0;
							}
							if (autoFire && rollingCounter % 5 == 0) {
								mPlayer.shoot();
								mPlayer.useAmmo();
							}
							// capture the time and send that to endTime for use in how long a touch is held
							endTime = System.currentTimeMillis();
							// Move the game items
							mSkiView.updatePositions();

							// check to see if the player contacts the items.
							for (int zz = 0; zz < gameItems.size(); zz++) {
								// create Item object
								Item itm = gameItems.get(zz);

								if(mPlayer.resolveItemCollision(itm)) {
									gameItems.remove(zz);
									if (itm instanceof ShotGunItem) {
										mPlayer.getGun().setShotgun(true);
										mPlayer.addAmmo(100);
									}
									if (itm instanceof AutomaticRifle) {
										mPlayer.getGun().setAutomatic(true);
										mPlayer.addAmmo(100);
									}
								}
							}
							// If out of ammo we make gun normal.
							if (mPlayer.getAmmo() <= 0)
							{ mPlayer.basicGun(); autoFire = false; }
							rollingCounter++;

							c = mSurfaceHolder.lockCanvas(null);
							synchronized (mSurfaceHolder) {
								doDraw(c, rollingCounter);
							}

						}

						catch (Exception e) {
							Log.d("Exception", e.getLocalizedMessage());
							surfaceDestroyed(mSurfaceHolder);
						}

						finally {
							if (c != null){
								mSurfaceHolder.unlockCanvasAndPost(c);
							}
						}
					}
				}

				public void autoFire() {
					autoFire = true;
				}

				public void stopAutoFire() {
					autoFire = false;
				}

				// Reset counter
				public void resetCounter() {
					this.rollingCounter = 0;
				}

				// Test to see if we should add an item. Right now stubbed out to not have to wait.
				public boolean shouldAddItem(int rollingCounter) {
					/*int z = mRandom.nextInt(100);
					if (counter % 100 == 0 && z > 33 && z < 66)return true;
					return false;
					 */
					if (rollingCounter % 20 == 0)return true;
					return false;
				}

				// Used to stop adding enemies so the boss can appear
				public boolean shouldAddEnemy(int rollingCounter) {
					if (rollingCounter < 1000) { // Make it a lot longer for actual boss,
						// For now we have it at 1000 to not have to wait for testing. 
						if (rollingCounter % 20 == 0) return true;
						else return false;
					}
					else mAddBoss = true;
					return true;
				}

				public void setRunning(boolean b) {
					mRun = b;
				}

				public boolean getRunStatus() {
					return mRun;
				}

				public void doDraw(Canvas canvas, int rollingCounter) {

					Paint paint = new Paint();
					paint.setStyle(Paint.Style.FILL);
					paint.setColor(Color.rgb(100, 200, 100)); //TODO Fix Dark green

					moveBGY += 1; //TODO Fix Magic Number
					newBGY = mBackgroundImage.getHeight() - (moveBGY);
					if (newBGY <= 0) {
						moveBGY = 0;
						canvas.drawBitmap(mBackgroundImage,0,moveBGY,null);
					} else {
						canvas.drawBitmap(mBackgroundImage,0,-1 * mBackgroundImage.getHeight() + moveBGY,null);
						canvas.drawBitmap(mBackgroundImage,0,moveBGY,null);
					}


					mAmmoRemainLocation = mOrigAmmoRemainLocation + (int)mPlayer.getAmmoUsed();
					mRectAmmo.set(mAmmoRemainLocation, 20, mScreenWidth - 2, 30); // TODO Fix magic numbers again.
					canvas.drawRect(mRectAmmo, paint);
					// Adjust the damage meter.
					mDamageRemainLocation = (int)(mOrigDamageRemainLocation - mPlayer.getDamageAmount());
					mRectDamage.set(2,20,mDamageRemainLocation,30);
					canvas.drawRect(mRectDamage, paint);

					paint.setColor(Color.RED);

					mRectAmmoGone.set(mOrigAmmoRemainLocation, 20,mAmmoRemainLocation,30);
					canvas.drawRect(mRectAmmoGone, paint);

					//adjust damage gone gauge
					mRectDamageGone.set(mDamageRemainLocation, 20, mOrigDamageRemainLocation, 30);
					canvas.drawRect(mRectDamageGone, paint);

					// Move bullets
					for (int aa = 0; aa < mPlayer.getGun().getArray().size(); aa++) {
						Bullet bullet = mPlayer.getGun().getArray().get(aa);
						bullet.fireBullet(10, mPlayer.getGun().getShotgun()); // TODO Fix magic number for speed;
						mPlayer.getGun().resolveCollisionWithBullet(bullet, gameItems, gameEnemies, aa);
						canvas.drawBitmap(bullet.getImage(),bullet.getPosX(), bullet.getPosY(),null);
					}


					// Draw player sprite.
					float xc = mPlayer.getXcoord();
					float yc = mPlayer.getYcoord();
					mPlayer.updatePosition(-mSensorX + xc, mSensorY + yc);

					canvas.drawBitmap(mPlayer.getBitmap(), xc, yc, null);

					for (int z = 0; z < gameItems.size(); z++) {
						Item item = gameItems.get(z);
						if (item.getyCoord() > mScreenHeight)gameItems.remove(z);
						else canvas.drawBitmap(item.getImage(),item.getxCoord(),item.getyCoord(),null);
					}
					// draw all of the Enemies
					for (int z = 0; z < gameEnemies.size(); z++) {
						Enemies enemy = gameEnemies.get(z);
						if (enemy.getyCoord() > mScreenHeight)gameEnemies.remove(z);
						else canvas.drawBitmap(enemy.getImage(),enemy.getxCoord(),enemy.getyCoord(),null);
					}
					canvas.save();
					canvas.restore();

					// and make sure to redraw asap
					postInvalidate();
				}

				private void update(SensorEvent event) {
					if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
						return;
					/*
					 * record the accelerometer data, the event's timestamp as well as
					 * the current time. The latter is needed so we can calculate the
					 * "present" time during rendering. In this application, we need to
					 * take into account how the screen is rotated with respect to the
					 * sensors (which always return data in a coordinate space aligned
					 * to with the screen in its native orientation).
					 */

					switch (mDisplay.getRotation()) {
					case Surface.ROTATION_0:
						mSensorX = event.values[0];
						mSensorY = event.values[1];
						break;
					case Surface.ROTATION_90:
						mSensorX = -event.values[1];
						mSensorY = event.values[0];
						break;
					case Surface.ROTATION_180:
						mSensorX = -event.values[0];
						mSensorY = -event.values[1];
						break;
					case Surface.ROTATION_270:
						mSensorX = event.values[1];
						mSensorY = -event.values[0];
						break;
					}
				}
			}
		}
	}
}