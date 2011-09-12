package com.mannsclann;

import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

/**
 * Item class is used to store all available items in the game.  We have ArrayLists containting each type.
 * The purpose is to get a random number that will be used to access the index of the item.  ie if we get
 * a random number of 1-4, we can decide which amount of gas to used, index 1,2,3,or 4 accordingly.
 * 
 * This class is only a list of the available items.  The InGameItems class
 * will actually incorporate these items into the game.
 * @author dustin
 *
 */
public class Item {
	Random generator = new Random();
	protected Bitmap image;
	protected Context mContext;
	protected int xCoord, yCoord, mScreenWidth;

	public Item(Context context, int screenWidth) {
		image = null;
		mContext = context;
		this.mScreenWidth = screenWidth;
		this.xCoord = getRandomXCoord();
		this.yCoord = -25;
		}

	public int getxCoord() {
		return xCoord;
	}

	public void setxCoord(int xCoord) {
		this.xCoord = xCoord;
	}

	public int getyCoord() {
		return yCoord;
	}

	public void setyCoord(int yCoord) {
		this.yCoord = yCoord;
	}

	public void addToYCoord(int number) {
		this.yCoord += number;
	}

	public int getRandomXCoord() {
		return generator.nextInt(mScreenWidth);
	}

	public Bitmap getImage() {
		return this.image;
	}

	public int getWidth() {
		return this.image.getWidth();
	}

	public int getHeight() {
		return this.image.getHeight();
	}

	public Rect getRect() {
		Rect rect = new Rect(xCoord, yCoord, xCoord + getWidth(), yCoord + getHeight());
		return rect;
	}
}

class ShotGunItem extends Item {

	public ShotGunItem(Context context, int screenWidth) {
		super(context, screenWidth);
		image = BitmapFactory.decodeResource(context.getResources(), R.drawable.shotgun);
		if (image.getWidth() + xCoord > mScreenWidth)
			xCoord = mScreenWidth - image.getWidth();
	}
}

class AutomaticRifle extends Item {
	
	public AutomaticRifle(Context context, int screenWidth) {
		super(context, screenWidth);
		image = BitmapFactory.decodeResource(context.getResources(), R.drawable.scaled_ak);
		if (image.getWidth() + xCoord > mScreenWidth)
			xCoord = mScreenWidth - image.getWidth();
	}
}
