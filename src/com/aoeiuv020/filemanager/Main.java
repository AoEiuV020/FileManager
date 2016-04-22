package com.aoeiuv020.filemanager;
import com.aoeiuv020.tool.Logger;
import com.aoeiuv020.tool.FileOperator;
import com.aoeiuv020.widget.SimpleToast;
import com.aoeiuv020.widget.SimpleDialog;

import android.app.Activity;
import android.app.Dialog;
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


public class Main extends Activity implements AdapterView.OnItemClickListener,View.OnClickListener
{
	private final String TAG=""+this;
	private ListView lvFileList=null;
	private Deque<File> mFilesStack=new LinkedList<File>();
	private Map<File,Integer> mPosition=new HashMap<File,Integer>();
	private Deque<List<File>> mClipper=new LinkedList<List<File>>();
	private ItemAdapter mAdapter=null;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		findViewById(R.id.create).setOnClickListener(this);
		findViewById(R.id.copy).setOnClickListener(this);
		findViewById(R.id.moveto).setOnClickListener(this);
		findViewById(R.id.paste).setOnClickListener(this);
		findViewById(R.id.delete).setOnClickListener(this);
		findViewById(R.id.quit).setOnClickListener(this);
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
		mFilesStack.push(Environment.getExternalStorageDirectory());
		mAdapter=new ItemAdapter(this);
		lvFileList.setAdapter(mAdapter);
		lvFileList.setOnItemClickListener(this);
		flush();
    }
	@Override
	public void onClick(View actionButton)
	{
		switch(actionButton.getId())
		{
			case R.id.create:
				Main.this.doCreate();
				break;
			case R.id.delete:
				Main.this.doDelete();
				break;
			case R.id.quit:
				finish();
				break;
			case R.id.copy:
				List<File> selectedFiles=mAdapter.getSelectedFiles();
				int selectedCount=selectedFiles.size();
				if(selectedCount>0)
				{
					mClipper.push(selectedFiles);
					mAdapter.clearSelection();
					SimpleToast.makeText(this,"复制"+selectedCount+"项");
				}
				else
					SimpleToast.makeText(this,"没选择不能复制");
				break;
			case R.id.paste:
				try
				{
					List<File> clipperFiles=mClipper.pop();
					StringBuffer sbuf=new StringBuffer();
					for(File file:clipperFiles)
					{
						File newFile=new File(mFilesStack.peek(),file.getName());
						File result=FileOperator.copy(file,newFile,null);
						if(result!=null)
							sbuf.append(result.getAbsolutePath()+"\n");
					}
					if(sbuf.length()>0)
						SimpleToast.makeText(this,sbuf.toString()+"这些文件复制失败");
				}
				catch(NoSuchElementException e)
				{
					SimpleToast.makeText(this,"还没复制不能粘贴");
				}
				flush();
				break;
			case R.id.moveto:
				try
				{
					List<File> files=mClipper.pop();
					StringBuffer sbuf=new StringBuffer();
					for(File file:files)
					{
						File newFile=new File(mFilesStack.peek(),file.getName());
						boolean result=file.renameTo(newFile);
						if(!result)
							sbuf.append(file.getAbsolutePath()+"\n");
					}
					if(sbuf.length()>0)
						SimpleToast.makeText(this,sbuf.toString()+"这些文件移动失败");
				}
				catch(NoSuchElementException e)
				{
					SimpleToast.makeText(this,"还没复制不能粘贴");
				}
				flush();
				break;
		}
	}
	/**
	 * 新建文件和文件夹功能
	 * 整个过程都在这里，
	 */
	public void doCreate()
	{
		LayoutInflater inflater=LayoutInflater.from(this);
		View createView=inflater.inflate(R.layout.layout_create,null);
		final Dialog dialog=SimpleDialog.show("新建",createView);
		final EditText etName=(EditText)createView.findViewById(R.id.create_name);
		Button bFile=(Button)createView.findViewById(R.id.create_file);
		Button bFolder=(Button)createView.findViewById(R.id.create_folder);
		View.OnClickListener listener=new View.OnClickListener(){
			@Override
			public void onClick(View createButton)
			{
				String name=""+etName.getText();
				File file=null;
				switch(createButton.getId())
				{
					case R.id.create_file:
						Logger.v("new file %s",name);
						file=new File(Main.this.mFilesStack.peek(),name);
						try
						{
							if(file.createNewFile())
								Main.this.mAdapter.add(file,true);
							else
								SimpleToast.makeText(createButton.getContext(),"创新文件"+name+"失败");
						}
						catch(IOException e)
						{
							SimpleToast.makeText(createButton.getContext(),"创新文件"+name+"异常 "+e);
						}
						break;
					case R.id.create_folder:
						Logger.v("new folder %s",name);
						file=new File(Main.this.mFilesStack.peek(),name);
						if(file.mkdirs())
							Main.this.enter(file);
						else
							SimpleToast.makeText(createButton.getContext(),"创新文件"+name+"失败");
						break;
				}
				dialog.cancel();
			}
		};
		bFile.setOnClickListener(listener);
		bFolder.setOnClickListener(listener);
	}
	/**
	 * 删除功能
	 * 整个删除过程都在这里，
	 */
	public void doDelete()
	{
		if(!SimpleDialog.show(this,"确认全部删除?","我只说一次，\n包括文件夹的里的所有文件"))
			return;
		List<File> files=mAdapter.getSelectedFiles();
		List<File> deleteErrorFiles=new LinkedList<File>();
		for(File file:files)
		{
			Logger.v("delete file %s",file.getAbsolutePath());
			File result=FileOperator.delete(file,null);
			if(result!=null)
				deleteErrorFiles.add(result);
		}
		if(!deleteErrorFiles.isEmpty())
		{
			StringBuffer sbuf=new StringBuffer();
			sbuf.append("以下文件删除失败:\n");
			for(File file:deleteErrorFiles)
			{
				sbuf.append(file.getAbsolutePath()+"\n");
			}
			SimpleToast.makeText(this,sbuf.toString());
		}
		flush();
	}
	public void flush()
	{
		mAdapter.set(mFilesStack.peek());
		Integer position=mPosition.get(mFilesStack.peek());
		Logger.v("Map Position size=%d position=%s",mPosition.size(),""+position);
		if(position!=null)
		{
			lvFileList.setSelection(position);
		}
	}
	public void enter(File file)
	{
		mPosition.put(mFilesStack.peek(),lvFileList.getFirstVisiblePosition());
		if(file!=null)
			mFilesStack.push(file);
		flush();
	}
	/**
	  * 只剩一个就不pop，
	  * 而是返回false，
	  */
	public boolean back()
	{
		if(mFilesStack.size()==1)
			return false;
		mFilesStack.pop();
		flush();
		return true;
	}
	public void backParent()
	{
		File parent=mFilesStack.peek().getParentFile();
		enter(parent);
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
			enter(file);
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
		//如果能返回上一个目录，
		if(back())
			return;
		long currentTime=System.currentTimeMillis();
		if(currentTime-lastTime<2*1000)
			finish();
		else
		{
			lastTime=currentTime;
			SimpleToast.makeText(this,"再点一次退出");
		}
	}
}
