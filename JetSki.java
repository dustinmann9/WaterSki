package com.mannsclann;

import com.mannsclann.WaterSki.SkiViewLayout.SkiView;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;
import android.view.Display;

public class JetSki {
	private Bitmap playerImage;
	private float ammoAmount; // 100 * gaugeDec = full tank
	private float damageAmount; // 100 * gaugeDec = noDamage
	private float guageDec;
	private float bitmapHeight,bitmapWidth;
	private float posX, posY, money;
	private SkiView mSkiView;
	private Gun mGun;
	private Handler mHandler;
	
	// Left and right are the left and right coordinates of the ammo gauge.
	public JetSki(Context context, float leftAmmoCoord, float rightAmmoCoord, SkiView ski, Handler handler) {
		playerImage = BitmapFactory.decodeResource(context.getResources(),R.drawable.player);
		guageDec = (rightAmmoCoord - leftAmmoCoord) / 100;
		ammoAmount = guageDec * 100;
		damageAmount = 0;
		bitmapHeight = playerImage.getHeight();
		bitmapWidth = playerImage.getWidth();
		money = 0;
		posX = (ski.getImageWidth() - bitmapWidth) * 0.5f;
		posY = (ski.getImageHeight() - bitmapHeight) * 0.5f;
		mSkiView = ski;
		mHandler = handler;
		mGun = new Gun(context, this, mHandler);
	}

	public void setAmmoAmount(int ammoAmount) {
		this.ammoAmount = ammoAmount;
	}

	public float getDamageAmount() {
		return damageAmount;
	}

	public void setDamageAmount(int damageAmount) {
		this.damageAmount = damageAmount;
	}

	public Bitmap getBitmap() {
		return this.playerImage;
	}

	public float getAmmo() {
		return ammoAmount;
	}
	
	// We do nothing if we have a basic gone.
	public void useAmmo() {
		if(mGun.getShotgun()) ammoAmount -= 3 * guageDec;// + 10;  //remove the +10, used just to make testing faster.
		if (mGun.getAutomatic()) ammoAmount -= guageDec;
	}

	public float getAmmoPercent() {
		return ammoAmount / guageDec;
	}

	public float getAmmoUsed() {
		return guageDec * 100 - getAmmo();
	}
	
	// Used to add ammo.  If the amount added puts us over 100 percent, we
	// make the ammo amount 100%, otherwise we add that amount.
	public void addAmmo(int percent) {
		if (percent > 100 - getAmmoPercent()) ammoAmount = guageDec * 100;
		else ammoAmount += guageDec * percent;
	}

	public void addDamage(int newDamage) {
		this.damageAmount += newDamage * guageDec;
	}

	public void addMoney(int amount) {
		money += amount;
	}

	// Used to test to see if the player runs overtop an item
	// Depending on the item, we perform its function. i.e. a gas can
	// adds fuel, money adds points, etc.  Return true if the player is on 
	// top of an item, false otherwise.
	public boolean resolveItemCollision(Item item) {
		// object surrounding rectangles see if they collide.
		Rect rectPlayer = getRect();
		Rect rectItem = item.getRect();

		// If the player overlaps the item we continue.
		if (overlaps(rectPlayer, rectItem)) {
			// next we determine the type of the item, and carry out
			// each specific task.
			return true;
		}
		return false;
	}

	public boolean overlaps(Rect rectPlayer, Rect rectItem) {
		// Get coords of each rectangle
		int itemLeft = rectItem.left, itemBottom = rectItem.bottom, itemTop = rectItem.top, itemRight = rectItem.right;
		int playerLeft = rectPlayer.left, playerBottom = rectPlayer.bottom, playerTop = rectPlayer.top, playerRight = rectPlayer.right;

		// test the various overlapping positions
		if (playerLeft <= itemLeft && playerRight > itemLeft) { 
			if (playerTop <= itemTop && playerBottom > itemTop) return true;
			else if (playerTop >= itemTop && playerTop < itemBottom)return true;
		} 
		else if (playerLeft >= itemLeft && playerLeft < itemRight) {
			if (playerTop <= itemTop && playerBottom > itemTop) return true;
			else if (playerTop >= itemTop && playerTop < itemBottom)return true;		
		}
		return false;

	}
	// Used to determine the X/Y coord of the player.
	public void setCoord(int mPosX, int mPosY) {
		posX = mPosX;
		posY = mPosY;
	}

	public Rect getRect() {
		// create a rectangle around the player.
		Rect rect = new Rect((int)posX, (int)posY, (int)(posX + bitmapWidth), (int)(posY + bitmapHeight));
		return rect;
	}

	public float getXcoord() {
		return this.posX;
	}

	public float getYcoord() {
		return this.posY;
	}

	public void updatePosition(float newX, float newY) {
		resolveCollisionWithinBounds(newX, newY);

	}

	public void resolveCollisionWithinBounds(float newX, float newY) {
		// if the position is beyond right border, we make the new pos, the
		// right border - player width.  Should give impression of stopping on
		// right edge.
		if (newX + bitmapWidth > mSkiView.getWidth())newX = mSkiView.getWidth() - bitmapWidth;
		// if it is past left border, align with left
		if (newX < 0)newX = 0;
			
		// if it is below bottom border, align with bottom border.
		if (newY + bitmapHeight > mSkiView.getHeight())newY = mSkiView.getHeight() - bitmapHeight;
		//if it is past top border, align with top
		if (newY < 0)newY = 0;
		setCoord((int)newX,(int)newY);
	}
	
	public void shoot() {
		this.mGun.shoot();
	}
	
	public Gun getGun() {
		return this.mGun;
	}
	
	public void basicGun() {
		this.mGun.setShotgun(false);
		this.mGun.setAutomatic(false);
		ammoAmount = guageDec * 100;
	}
}
