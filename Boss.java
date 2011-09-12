package com.mannsclann;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Boss extends Enemies {
	// I want to have the bosses move really slow, the easiest way to do this
	// is to only allow the addtoYcoord method to run every few tries
	// So i'll use the counter to time it up.
	private int counter;
	
	public Boss(Context context, int screenWidth) {
		super(context, screenWidth);
		this.counter = 0;
	}
	
	public void addToYCoord(int number) {
		if (counter == 3) { 
			this.yCoord += 1; // 1 pixel may not be enough.
			counter = 0;
		} else counter ++;
	}
}

class Baby extends Boss {
	private Resources res;

	public Baby(Context context, int screenWidth) {
		super(context, screenWidth);
		this.res = context.getResources();
		this.health = 200;
		this.image = BitmapFactory.decodeResource(
				res, R.drawable.baby);
		// TODO Change the points to be dynamic basic on a variance of point values instead of always being 50
		this.points = 1000;
		// Middle of screen eventually
		this.xCoord = (int)((mScreenWidth - image.getWidth()) * 0.5f);
		
	}

	public int getPoints() {
		return this.points;
	}
}