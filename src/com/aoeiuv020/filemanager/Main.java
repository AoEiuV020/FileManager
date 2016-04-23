package com.aoeiuv020.filemanager;
import com.aoeiuv020.tool.Logger;
import com.aoeiuv020.widget.SimpleToast;

import android.app.Activity;
import android.os.*;
import android.widget.*;
import android.view.*;
import android.net.*;
import android.content.*;
import android.util.Log;
import java.util.*;
import java.io.*;
import android.webkit.MimeTypeMap;
import android.net.Uri;
import android.app.AlertDialog;
import android.content.DialogInterface;


public class Main extends Activity implements AdapterView.OnItemClickListener
{
	private final String TAG=""+this;
	private ListView lvFileList=null;
	private File currentFile=null;
	private ItemAdapter mAdapter=null;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		lvFileList=(ListView)findViewById(R.id.filelist);
		LayoutInflater inflater=LayoutInflater.from(this);
		View parent=inflater.inflate(R.layout.layout_parent,null);
		parent.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View view)
			{
				Main.this.backParent();
			}
		});
		lvFileList.addHeaderView(parent);
		currentFile=Environment.getExternalStorageDirectory();
		mAdapter=new ItemAdapter(this);
		mAdapter.set(currentFile);
		lvFileList.setAdapter(mAdapter);
		lvFileList.setOnItemClickListener(this);
    }
	public void backParent()
	{
		File parent=currentFile.getParentFile();
		if(parent!=null)
			currentFile=parent;
		mAdapter.set(currentFile);
	}
	@Override
	public void onItemClick(AdapterView<?> parent,View view,int position,long id)
	{
		ItemAdapter.Item item=(ItemAdapter.Item)parent.getAdapter().getItem(position);
		File file=item.file;
		Logger.v("onItemClick %d,%s",position,file.getPath());
		if(file==null)
		{
			//不可到达,
			Logger.e(new Exception("Item"+item+"里没file"));
		}
		if(file.isDirectory())
		{
			currentFile=file;
			ItemAdapter adapter=null;
			//有Header，这里永真,
			if(parent.getAdapter() instanceof HeaderViewListAdapter)
			{
				adapter=(ItemAdapter)((HeaderViewListAdapter)parent.getAdapter()).getWrappedAdapter();
			}
			else
				adapter=(ItemAdapter)parent.getAdapter();
			adapter.set(file);
		}
		else
		{
			Intent intent=new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			String extension=file.getName();
			int lastDot=extension.lastIndexOf('.');
			if(lastDot!=-1)
			{
				extension=extension.substring(lastDot+1);
			}
			String type=MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
			intent.setDataAndType(Uri.fromFile(file),type);
			try
			{
				startActivity(intent);
			}
			catch(ActivityNotFoundException e)
			{
				SimpleToast.makeText(this,String.format("没有应用可以打开文件%s,后辍%s,类型%s",file.getName(),extension,type));
			}
		}
	}
	private long lastTime=0;
	@Override
	public void onBackPressed()
	{
		long currentTime=System.currentTimeMillis();
		if(currentTime-lastTime<1*1000)
			finish();
		else
		{
			lastTime=currentTime;
			backParent();
		}
	}
}
