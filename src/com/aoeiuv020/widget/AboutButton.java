/* ***************************************************
	^> File Name: AboutButton.java
	^> Author: AoEiuV020
	^> Mail: 490674483@qq.com
	^> Created Time: 2016/03/01 - 14:52:55
*************************************************** */
package com.aoeiuv020.widget;
import android.view.View;
import android.content.Context;
import java.io.File;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.StringWriter;
import java.util.Scanner;
public class AboutButton extends SimpleButton
{
	public AboutButton(Context context)
	{
		super(context);
		setText("关于");
	}
	@Override
	public void onClick(View view)
	{
		SimpleDialog.show(view.getContext(),getTitle(),getAboutMessage(view.getContext()),null);
	}
	private String getEncoding()
	{
		return "UTF8";
	}
	private String getTitle()
	{
		return "关于";
	}
	private String getAboutMessage(Context context)
	{
		String about="";
		InputStreamReader fin=null;
		try
		{
			fin=new InputStreamReader(context.getResources().getAssets().open("about.txt"),getEncoding());
			char[] buf=new char[100];
			int len;
			StringWriter stringWriter=new StringWriter();
			while((len=fin.read(buf))>0)
			{
				stringWriter.write(buf,0,len);
			}
			about=stringWriter.toString();
		}
		catch(Exception e)
		{
			about="怎么说呢，";
		}
		finally
		{
			try
			{
				if(fin!=null)
				{
					fin.close();
				}
			}
			catch(Exception e)
			{ }
		}
		return about;
	}
}
