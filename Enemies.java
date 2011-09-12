package com.mannsclann;

import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;

public class Enemies {
	Random generator = new Random();
	protected Bitmap image;
	protected Context mContext;
	protected int xCoord, yCoord, points, health, mScreenWidth;
	
	public Enemies(Context context, int screenWidth) {
		image = null;
		mContext = context;
		this.mScreenWidth = screenWidth;
		this.xCoord = getRandomXCoord();
		this.yCoord = -25;
		this.points = 0;
		this.health = 1;
		
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
	
	public int getRandomXCoord() {
		return generator.nextInt(mScreenWidth);
	}
	
	public int getPoints() {
		return this.points;
	}
	
	public int getHealth() {
		return this.health;
	}
	
	public void setHealth(int h) {
		this.health = h;
	}
	
	public void loseHealth() {
		this.health--;
	}
	
}
