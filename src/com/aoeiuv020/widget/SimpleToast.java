/* ***************************************************
	^> File Name: SimpleToast.java
	^> Author: AoEiuV020
	^> Mail: 490674483@qq.com
	^> Created Time: 2016/03/01 - 13:49:53
*************************************************** */
package com.aoeiuv020.widget;
import android.widget.Toast;
import android.content.Context;
public class SimpleToast
{
	private SimpleToast()
	{
	}
	public static void makeText(Context context,String string)
	{
		try
		{
			Toast toast=Toast.makeText(context,string,Toast.LENGTH_SHORT);
			toast.show();
		}
		catch(Exception e)
		{
		}
	}
}

