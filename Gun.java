package com.mannsclann;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


class Gun {
	private final float[] strength = { 10, 20, 100 };
	private boolean automatic, shotgun;
	private JetSki mPlayer;
	private int type;
	private Context mContext;
	private ArrayList<Bullet> mBullets;
	private Handler mHandler;

	public Gun(Context context, JetSki player, Handler handler) {
		mPlayer = player;
		mContext = context;
		mBullets = new ArrayList<Bullet>();
		automatic = false;
		shotgun = false;
		mHandler = handler;
	}

	public ArrayList<Bullet> getArray() {
		return mBullets;
	}

	public void shoot() {
		if (shotgun) {
			for (int i = 1; i < 4; i++) {
				mBullets.add(new Bullet(mContext, mPlayer, i));
			}
		} else mBullets.add(new Bullet(mContext, mPlayer, 2));
		// TODO find better spot.
		for (int i = 0; i < mBullets.size(); i++) {
			if (mBullets.get(i).getPosY() < 0)mBullets.remove(i); // TODO Make 0 the top border instead of 
		}
	}

	public void resolveCollisionWithBullet(Bullet bullet, ArrayList<Item> items, ArrayList<Enemies> enemies, int index) {
		Message msg = new Message();
		Bundle b = new Bundle();
		int pointsToAdd = 0;
		for (int i = 0; i < items.size(); i++) {
			if (overlaps(items.get(i), bullet)) {
				items.remove(i);
				mBullets.remove(index);
			}
		}
		Enemies enemy;
		for (int j = 0; j < enemies.size(); j++) {
			enemy = enemies.get(j);
			if (overlaps(enemy, bullet)) {
				enemy.loseHealth();
				if (enemy.getHealth() < 1) {
					pointsToAdd += enemy.getPoints();
					enemies.remove(j);
				}
				mBullets.remove(index);
			}

		}

		b.putString("status", "");
		b.putInt("points", pointsToAdd); // TODO Fix point magic number.
		msg.setData(b);
		mHandler.sendMessage(msg);
	}

	public boolean overlaps(Item item, Bullet bullet) {
		/*// Get coords of each rectangle
		int itemLeft = item.getxCoord(), itemBottom = item.getyCoord() + item.getHeight(),
		itemTop = item.getyCoord(), itemRight = item.getxCoord() + item.getWidth();
		int bulletLeft = (int)bullet.getPosX(), bulletBottom = (int)bullet.getPosY() + bullet.getImage().getHeight(), 
		bulletTop = (int)bullet.getPosY(), bulletRight = (int)bullet.getPosX() + bullet.getImage().getWidth();
		// If it is a gas can we don't want to destroy it.
		if (item instanceof ShotGunItem || item instanceof ShotGun)return false;
		// test the various overlapping positions
		if (bulletLeft <= itemLeft && bulletRight > itemLeft) { 
			if (bulletTop <= itemTop && bulletBottom > itemTop) return true;
			else if (bulletTop >= itemTop && bulletTop < itemBottom)return true;
		} 
		else if (bulletLeft >= itemLeft && bulletLeft < itemRight) {
			if (bulletTop <= itemTop && bulletBottom > itemTop) return true;
			else if (bulletTop >= itemTop && bulletTop < itemBottom)return true;		
		}
		*/
		return false;
	}

	// Same as the items, but instead compairing enemies
	public boolean overlaps(Enemies enemy, Bullet bullet) {
		// Get coords of each rectangle
		int enemyLeft = enemy.getxCoord(), enemyBottom = enemy.getyCoord() + enemy.getHeight(),
		enemyTop = enemy.getyCoord(), enemyRight = enemy.getxCoord() + enemy.getWidth();
		int bulletLeft = (int)bullet.getPosX(), bulletBottom = (int)bullet.getPosY() + bullet.getImage().getHeight(), 
		bulletTop = (int)bullet.getPosY(), bulletRight = (int)bullet.getPosX() + bullet.getImage().getWidth();

		// test the various overlapping positions
		if (bulletLeft <= enemyLeft && bulletRight > enemyLeft) { 
			if (bulletTop <= enemyTop && bulletBottom > enemyTop) return true;
			else if (bulletTop >= enemyTop && bulletTop < enemyBottom)return true;
		} 
		else if (bulletLeft >= enemyLeft && bulletLeft < enemyRight) {
			if (bulletTop <= enemyTop && bulletBottom > enemyTop) return true;
			else if (bulletTop >= enemyTop && bulletTop < enemyBottom)return true;		
		}
		return false;
	}

	public void setShotgun(boolean b) {
		shotgun = b;
	}

	public void setAutomatic(boolean b) {
		this.automatic = b;
	}

	public boolean getShotgun() {
		return shotgun;
	}

	public boolean getAutomatic() {
		return automatic;
	}
}


class Bullet {
	private float posX, posY;
	private Bitmap mImage;
	private final float theta = 20;
	private Context mContext;
	private JetSki mPlayer;
	// used for making a shotgun, type 1 is left, 2 is middle, 3 is right.
	private int type;

	public Bullet(Context context, JetSki player, int setType) {
		mPlayer = player;
		mContext = context;
		mImage = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.bullet);
		posX = player.getXcoord() + (player.getBitmap().getWidth() / 2);
		posY = player.getYcoord();
		this.type = setType;
	}

	public float getPosX() {
		return posX;
	}

	public void setPosX(float posX) {
		this.posX = posX;
	}

	public float getPosY() {
		return posY;
	}

	public void setPosY(float posY) {
		this.posY = posY;
	}

	public Bitmap getImage() {
		return mImage;
	}

	public void fireBullet(int speed, boolean shotgun) {
		if (!shotgun) {
			moveY(speed);
		} else {
			moveY(speed);
			switch (this.type){
			case 1 : moveX(-1); break;
			case 2 : ; break;
			case 3 : moveX(1); break;
			}
		}
	}

	public void moveY(int speed) {
		setPosY(getPosY() - speed);
	}

	public void moveX(int speed) {
		setPosX(getPosX() - speed);
	}
}