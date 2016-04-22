/* ***************************************************
	^> File Name: ItemAdapter.java
	^> Author: AoEiuV020
	^> Mail: 490674483@qq.com
	^> Created Time: 2016/04/21 - 23:44:33
*************************************************** */
package com.aoeiuv020.filemanager;
import com.aoeiuv020.tool.Logger;
import android.widget.*;
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
	private Comparator mComparator=MyComparator.NameASC;
	private static final FilenameFilter filter=new MyFilenameFilter();
	public ItemAdapter(Context context)
	{
		mContext=context;
		mInflater=LayoutInflater.from(context);
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
	public static class MyComparator
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
		public static final Comparator<Item> TimeASC=null;
		public static final Comparator<Item> TimeDESC=null;
		public static final Comparator<Item> SizeASC=null;
		public static final Comparator<Item> SizeDESC=null;
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
			return MyComparator.NameASC.compare(this,item);
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
		private static final int ICON_FILE=R.drawable.format_unknown;
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
		public void set(Item item)
		{
			if(item==null||item.file==null)
				return;
			File file=item.file;
			int resId=ICON_FILE;
			if(file.isDirectory())
				resId=ICON_FOLDER;
			image.setImageResource(resId);
			name.setText(""+file.getName());
			size.setText(""+file.length());
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
