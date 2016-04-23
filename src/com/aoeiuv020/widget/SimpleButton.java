/* ***************************************************
	^> File Name: SimpleButton.java
	^> Author: AoEiuV020
	^> Mail: 490674483@qq.com
	^> Created Time: 2016/03/01 - 13:35:56
*************************************************** */
package com.aoeiuv020.widget;
import android.widget.Button;
import android.view.View;
import android.content.Context;
import android.view.ViewGroup;
public class SimpleButton extends Button implements View.OnClickListener,View.OnLongClickListener
{
	public SimpleButton(Context context)
	{
		super(context);
		setOnClickListener((View.OnClickListener)this);
		setOnLongClickListener((View.OnLongClickListener)this);
		setText("SimpleButton");
	}
	@Override
	public void onClick(View view)
	{
		SimpleToast.makeText(view.getContext(),""+view+" click");
	}
	@Override
	public boolean onLongClick(View view)
	{
		SimpleToast.makeText(view.getContext(),""+view+" long click");
		return false;
	}
}
