/* ***************************************************
	^> File Name: ItemAdapter.java
	^> Author: AoEiuV020
	^> Mail: 490674483@qq.com
	^> Created Time: 2016/04/21 - 23:44:33
*************************************************** */
package com.aoeiuv020.filemanager;
import com.aoeiuv020.tool.Logger;
import com.aoeiuv020.tool.FileOperator;
import android.widget.*;
import android.graphics.drawable.Drawable;
import android.content.pm.*;
import android.content.*;
import android.view.*;
import android.os.*;
import java.util.*;
import java.io.*;
import java.text.SimpleDateFormat;
class ItemAdapter extends BaseAdapter
{
	private final List<Item> mList=new LinkedList<Item>();
	private Context mContext=null;
	private LayoutInflater mInflater=null;
	private Comparator mComparator=FileComparator.NameASC;
	private static final FilenameFilter filter=new MyFilenameFilter();
	public ItemAdapter(Context context)
	{
		mContext=context;
		mInflater=LayoutInflater.from(context);
	}
	public void setComparator(Comparator comparator)
	{
		mComparator=comparator;
	}
	public void clearSelection()
	{
		for(Item item:mList)
		{
			item.selected=false;
		}
		notifyDataSetChanged();
	}
	public List<File> getSelectedFiles()
	{
		List<File> files=new LinkedList<File>();
		for(Item item:mList)
		{
			if(item.selected)
				files.add(item.file);
		}
		return files;
	}
	public void set(File file)
	{
		mList.clear();
		addAll(file.listFiles(filter));
		Collections.sort(mList,mComparator);
		notifyDataSetChanged();
	}
	public void add(File file)
	{
		add(file,false);
	}
	public void add(File file,boolean flush)
	{
		Item item=new Item();
		item.file=file;
		mList.add(item);
		if(flush)
			notifyDataSetChanged();
	}
	public void addAll(File[] list)
	{
		if(list==null||list.length==0)
			return;
		for(File file:list)
		{
			add(file);
		}
		notifyDataSetChanged();
	}
	public void addAll(Collection<File> list)
	{
		if(list==null||list.size()==0)
			return;
		for(File file:list)
		{
			add(file);
		}
		notifyDataSetChanged();
	}
	@Override
	public int getCount()
	{
		return mList.size();
	}
	@Override
	public Object getItem(int position)
	{
		return mList.get(position);
	}
	@Override
	public long getItemId(int position)
	{
		return position;
	}
	@Override
	public View getView(int position,View convertView,ViewGroup parent)
	{
		View view=null;
		ViewHolder holder=null;
		if(convertView==null)
		{
			holder=new ViewHolder();
			view=ViewHolder.inflate(mInflater);
			holder.find(view);
			view.setTag(holder);
		}
		else
		{
			view=convertView;
			holder=(ViewHolder)view.getTag();
		}
		Item item=(Item)mList.get(position);
		holder.set(item);
		return view;
	}
	public static class FileComparator
	{
		public static final Comparator<Item> NameASC=new NameComparator(){
				@Override
				public int compare(String nameA,String nameB)
				{
					return nameA.compareTo(nameB);
				}
			};
		public static final Comparator<Item> NameDESC=new NameComparator(){
				@Override
				public int compare(String nameA,String nameB)
				{
					return -nameA.compareTo(nameB);
				}
			};
		public static final Comparator<Item> SizeASC=new SizeComparator(){
				@Override
				public int compare(Long lengthA,Long lengthB)
				{
					return lengthA.compareTo(lengthB);
				}
			};
		public static final Comparator<Item> SizeDESC=new SizeComparator(){
				@Override
				public int compare(Long lengthA,Long lengthB)
				{
					return -lengthA.compareTo(lengthB);
				}
			};
		public static final Comparator<Item> TimeASC=new TimeComparator(){
				@Override
				public int compare(Long timeA,Long timeB)
				{
					return timeA.compareTo(timeB);
				}
			};
		public static final Comparator<Item> TimeDESC=new TimeComparator(){
				@Override
				public int compare(Long timeA,Long timeB)
				{
					return -timeA.compareTo(timeB);
				}
			};
		private static abstract class TimeComparator implements Comparator<Item>
		{
			@Override
			public int compare(Item a,Item b)
			{
				if(a.file==b.file)
					return 0;
				if(a.file.isDirectory()&&!b.file.isDirectory())
					return Integer.MIN_VALUE;
				if(!a.file.isDirectory()&&b.file.isDirectory())
					return Integer.MAX_VALUE;
				long timeA=0,timeB=0;
				timeA=getTime(a.file);
				timeB=getTime(b.file);
				return compare(timeA,timeB);
			}
			private long getTime(File file)
			{
				if(file==null)
					return 0;
				return file.lastModified();
			}
			public abstract int compare(Long timeA,Long timeB);
		}
		private static abstract class SizeComparator implements Comparator<Item>
		{
			@Override
			public int compare(Item a,Item b)
			{
				if(a.file==b.file)
					return 0;
				if(a.file.isDirectory()&&!b.file.isDirectory())
					return Integer.MIN_VALUE;
				if(!a.file.isDirectory()&&b.file.isDirectory())
					return Integer.MAX_VALUE;
				long lengthA=0,lengthB=0;
				lengthA=getLength(a.file);
				lengthB=getLength(b.file);
				return compare(lengthA,lengthB);
			}
			private long getLength(File file)
			{
				return FileOperator.getLengthLong(file);
			}
			public abstract int compare(Long lengthA,Long lengthB);
		}
		private static abstract class NameComparator implements Comparator<Item>
		{
			@Override
			public int compare(Item a,Item b)
			{
				if(a.file==b.file)
					return 0;
				if(a.file.isDirectory()&&!b.file.isDirectory())
					return Integer.MIN_VALUE;
				if(!a.file.isDirectory()&&b.file.isDirectory())
					return Integer.MAX_VALUE;
				String nameA=null,nameB=null;
				if(a.file!=null)
					nameA=a.file.getName();
				if(b.file!=null)
					nameB=b.file.getName();
				return compare(nameA,nameB);
			}
			public abstract int compare(String nameA,String nameB);
		}
	}
	public static class Item implements Comparable<Item>
	{
		public File file=null;
		public boolean selected=false;
		@Override
		public int compareTo(Item item)
		{
			return FileComparator.NameASC.compare(this,item);
		}
		@Override
		public String toString()
		{
			return String.format("<%d,%s,%s>",hashCode(),file==null?file:file.getName(),selected);
		}
	}
	public static class ViewHolder
	{
		private static final int ICON_FOLDER=R.drawable.format_folder;
		private static final int ICON_FILE_UNKNOWN=R.drawable.format_unknown;
		private static final SimpleDateFormat sdformat=new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
		public ImageView image=null;
		public TextView name=null;
		public TextView size=null;
		public TextView permission=null;
		public TextView time=null;
		public CheckBox selected=null;
		public static View inflate(LayoutInflater inflater)
		{
			return inflater.inflate(R.layout.layout_item,null);
		}
		public static Drawable getApkIcon(Context context, String apkPath) {
			//网上复制的方法，
			PackageManager pm = context.getPackageManager();
			PackageInfo info = pm.getPackageArchiveInfo(apkPath,
					PackageManager.GET_ACTIVITIES);
			if (info != null) {
				ApplicationInfo appInfo = info.applicationInfo;
				appInfo.sourceDir = apkPath;
				appInfo.publicSourceDir = apkPath;
				try {
					return appInfo.loadIcon(pm);
				} catch (OutOfMemoryError e) {
					Logger.e(e);
				}
			}
			return null;
		}
		public void setIcon(ImageView imageView,File file)
		{
			if(file.isDirectory())
				image.setImageResource(ICON_FOLDER);
			else
			{
				String extension=FileOperator.getExtension(file.getName());
				if("apk".equals(extension))
				{
					Drawable icon=getApkIcon(imageView.getContext(),file.getAbsolutePath());
					if(icon==null)
						image.setImageResource(ICON_FILE_UNKNOWN);
					else
						image.setImageDrawable(icon);
				}
				else
					image.setImageResource(ICON_FILE_UNKNOWN);
			}
		}
		public void set(Item item)
		{
			if(item==null||item.file==null)
				return;
			File file=item.file;
			setIcon(image,file);
			name.setText(""+file.getName());
			if(file.isDirectory())
				size.setText(""+FileOperator.getLengthLong(file));
			else
				size.setText(""+FileOperator.getLengthString(file));
			permission.setText(""+(file.isDirectory()?"d":"-")+(file.canRead()?"r":"-")+(file.canWrite()?"w":"-")+(file.canExecute()?"x":"-"));
			time.setText(""+sdformat.format(new Date(file.lastModified())));
			Logger.v("ViewHolder set %s",item);
			selected.setOnCheckedChangeListener(new SelectListener(item));
			selected.setChecked(item.selected);
		}
		public void find(View view)
		{
			if(view==null)
				return;
			image=(ImageView)view.findViewById(R.id.item_image);
			name=(TextView)view.findViewById(R.id.item_name);
			size=(TextView)view.findViewById(R.id.item_size);
			permission=(TextView)view.findViewById(R.id.item_permission);
			time=(TextView)view.findViewById(R.id.item_time);
			selected=(CheckBox)view.findViewById(R.id.item_selected);
		}
		private class SelectListener implements CompoundButton.OnCheckedChangeListener
		{
			private Item mItem=null;
			public SelectListener(Item item)
			{
				mItem=item;
			}
			@Override
			public void onCheckedChanged(CompoundButton buttonView,boolean isChecked)
			{
				Logger.v("onCheckedChanged ,%s,%s",mItem,isChecked);
				mItem.selected=isChecked;
			}
		}
	}
}
class MyFilenameFilter implements FilenameFilter
{
	@Override
	public boolean accept(File dir,String filename)
	{
		boolean result=true;
		if(filename.equals("."))
			result=false;
		else if(filename.equals(".."))
			result=false;
		return result;
	}
}
