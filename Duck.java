package com.mannsclann;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;

public class Duck extends Enemies {
	private Resources res;
	
	public Duck(Context context, int screenWidth) {
		super(context, screenWidth);
		this.res = context.getResources();
		this.image = BitmapFactory.decodeResource(res, R.drawable.duck);
		// TODO Change the points to be dynamic basic on a variance of point values instead of always being 50
		this.points = 50;
		if (image.getWidth() + xCoord > mScreenWidth)
			xCoord = mScreenWidth - image.getWidth();
	}
	
	public int getPoints() {
		return this.points;
	}

}
