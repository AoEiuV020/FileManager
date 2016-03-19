package com.aoeiuv020.filemanager;

import android.app.Activity;
import android.os.Bundle;
import android.widget.*;
import android.view.*;
import android.content.*;
import android.util.Log;
import java.util.*;
import java.io.*;
import android.webkit.MimeTypeMap;
import android.net.Uri;
import android.app.AlertDialog;
import android.content.DialogInterface;


public class Main extends Activity implements AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener
{
	private final String TAG=""+this;
	private final int ICON_FOLDER=android.R.drawable.sym_contact_card;
	private final int ICON_FILE=android.R.drawable.star_on;
	private List<File> fileList=null;
	private final String ROOT_PATH="/";
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		show(ROOT_PATH);
    }
	void setFileList(String string)
	{
		File file=new File(string);
		setFileList(file);
	}
	void setFileList(File file)
	{
		fileList=new ArrayList<File>();
		if(file==null)
		{
			return;
		}
		if(file.isDirectory())
		{
			File parent=file.getParentFile();
			if(parent!=null)
			{
				fileList.add(parent);
			}
			else
			{
				fileList.add(file);
			}
			fileList.addAll(Arrays.asList(file.listFiles()));
		}
		else
		{
			//正常不会到这里，
			Log.w(TAG,"fileList only one file");
			fileList.add(file);
		}
	}
	void show(String string)
	{
		File file=new File(string);
		show(file);
	}
	void show(File directory)
	{
		List<Map<String,Object>> list=new LinkedList<Map<String,Object>>();
		Map<String,Object> map=null;
		setFileList(directory);
		int i=0;
		for(File file:fileList)
		{
			map=new HashMap<String,Object>();
			if(file.isDirectory())
			{
				map.put("image",ICON_FOLDER);
			}
			else
			{
				map.put("image",ICON_FILE);
			}
			++i;
			if(i==1)
			{
				map.put("text","..");
			}
			else
			{
				map.put("text",file.getName());
			}
			list.add(map);
		}
		String[] from={"image","text"};
		int[] to={R.id.image,R.id.text};
		ListAdapter adapter=new SimpleAdapter(this,list,R.layout.main,from,to);
		ListView listView=new ListView(this);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
		listView.setOnItemLongClickListener(this);
		setContentView(listView);
	}
	void resetContentView(File file)
	{
		show(file);
	}
	@Override
	public boolean onItemLongClick(AdapterView<?> parent,View view,int position,long id)
	{
		final File file=fileList.get(position);
		final EditText editText=new EditText(this);
		AlertDialog.Builder builder=new AlertDialog.Builder(this);
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setView(editText);
		builder.setTitle("输入拓展名");
		builder.setPositiveButton("OK",new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog,int id)
			{
				String extension=""+editText.getText();
				String type=MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
				Intent intent=new Intent();
				intent.setAction(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(file),""+type);
				try
				{
					startActivity(intent);
				}
				catch(ActivityNotFoundException e)
				{
					Log.w(Main.this.TAG,""+extension+","+type);
				}
			}
		});
		builder.show();
		return true;
	}
	@Override
	public void onItemClick(AdapterView<?> parent,View view,int position,long id)
	{
		File file=fileList.get(position);
		if(file.isDirectory())
		{
			resetContentView(file);
		}
		else
		{
			String extension=getExtension(file);
			String type=MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
			Intent intent=new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(file),type);
			try
			{
				startActivity(intent);
			}
			catch(ActivityNotFoundException e)
			{
				Log.w(Main.this.TAG,""+extension+","+type);
			}
		}
	}
	String getExtension(File file)
	{
		if(file!=null)
		{
			return getExtension(file.getName());
		}
		return "";
	}
	String getExtension(String fileName)
	{
		String extension=fileName;
		int lastDot=fileName.lastIndexOf('.');
		if(lastDot!=-1)
		{
			extension=fileName.substring(lastDot+1);
		}
		return extension;
	}
}
