package com.mannsclann;

import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

/**Current test, rock...
 * Made from the Gascan Method
 * 
 * 
 * @author joe
 *
 */
class Rock extends Enemies {
	private final int[] damageAmounts = { 10,25,50,100 };
	private int damageAmount;
	private AssetManager assets;
	
	public Rock(Context context, int screenWidth) {
		super(context, screenWidth);
		// Used to randomly select the amount of fuel in the can.
		// 10 - 33%, 25 - 33%, 50 - 24%, 100 - 10%
		int randomType = generator.nextInt(100);
		if (randomType >= 0 && randomType < 33)	damageAmount = damageAmounts[0];
		else if (randomType >= 33 && randomType < 66)	damageAmount = damageAmounts[1];
		else if (randomType >= 66 && randomType < 90)	damageAmount = damageAmounts[2];
		else /*(randomType >= 90)*/	damageAmount = damageAmounts[3];
		
		//image = type;
		image = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
				context.getResources(), R.drawable.rock),50,50,false);
		if (image.getWidth() + xCoord > mScreenWidth)
			xCoord = mScreenWidth - image.getWidth();
	}

	
	// Getter for damageAmount
	public int getdamageAmount() {
		return damageAmount;
	}
	
	// Setter for damageAmount
	public void setdamageAmount(int index) {
		damageAmount = damageAmounts[index];
	}
}
