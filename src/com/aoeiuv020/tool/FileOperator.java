/* ***************************************************
	^> File Name: FileOperator.java
	^> Author: AoEiuV020
	^> Mail: 490674483@qq.com
	^> Created Time: 2016/04/23 - 02:10:23
*************************************************** */
package com.aoeiuv020.tool;
import java.io.*;
/**
 * 大多方法都是返回File，
 * 返回null表示成功，
 * 其他表示失败，
 * 返回的File就是失败的那个File,
 */
public class FileOperator
{
	public static File rename(File file,File newFile)
	{
		return rename(file,newFile,null);
	}
	public static File rename(File file,File newFile,MoveListener listener)
	{
		File result=null;
		if(newFile.exists())
		{
		}
		else if(!file.renameTo(newFile))
		{
			Logger.v("renameTo false %s",file);
			result=copy(file,newFile,listener);
			if(result==null)
			{
				result=delete(file,listener);
				if(result!=null)
				{
					Logger.v("delete false %s",file);
				}
			}
			else
				Logger.v("copy false %s",file);
		}
		return result;
	}
	/**
	 * 重命名，其实就是移动，
	 * 移动到file上一级下的newFileName,
	 */
	public static File rename(File file,String name)
	{
		if(name==null||name.isEmpty())
			return file;
		File parent=getParentFile(file);
		File newFile=newFile(parent,name);
		return rename(file,newFile);
	}
	/**
	 * @param name 新文件名，可以包含斜杆slash(/)，也就意味着可以直接移动到别的目录去，如果'/'开头，就无视参数file，从根目录开始，
	 */
	public static File newFile(File dir,String name)
	{
		File result=null;
		if(File.separatorChar==name.charAt(0))
			result=new File(name);
		else
		{
			result=new File(dir,name);
		}
		return result;
	}
	public static File getParentFile(File file)
	{
		File parent=file.getParentFile();
		if(parent==null)
		{
			//恶心的方法，居然会返回null，又没有别的可替代，
			parent=new File(file.getAbsolutePath()).getParentFile();
			//这个parent不会再null，除非file是根目录，
		}
		return parent;
	}
	public static String getExtension(String fileName)
	{
		String extension="";
		int lastDot=fileName.lastIndexOf('.');
		if(lastDot!=-1)
		{
			extension=fileName.substring(lastDot+1);
		}
		return extension;
	}
	public static long getLengthLong(File file)
	{
		if(file==null)
			return 0;
		long length=0;
		if(file.isDirectory())
		{
			File[] subFiles=file.listFiles();
			if(subFiles==null)
				length=0;
			else
				length=subFiles.length;
		}
		else
		{
			length=file.length();
		}
		return length;
	}
	public static String getLengthString(File file)
	{
		if(file==null)
			return "0B";
		StringBuffer sbuf=new StringBuffer();
		double length=file.length();
		int c=0;
		while(length>1024)
		{
			length/=1024;
			++c;
		}
		sbuf.append(String.format("%.02f",length));
		switch(c)
		{
			case 0:
				sbuf.append("B");
				break;
			case 1:
				sbuf.append("KB");
				break;
			case 2:
				sbuf.append("MB");
				break;
			default:
				sbuf.append("GB");
				break;
		}
		return sbuf.toString();
	}
	public static File delete(File file)
	{
		return delete(file,null);
	}
	public static File delete(File file,DeleteListener listener)
	{
		if(file.isDirectory())
		{
			File[] subFiles=file.listFiles();
			if(subFiles!=null)
				for(File f:subFiles)
				{
					File result=delete(f,listener);
					if(result!=null)
						return result;
				}
		}
		boolean isDeleted=file.delete();
		if(listener!=null)
			listener.onDelete(file,isDeleted);
		if(!isDeleted)
			return file;
		return null;
	}
	public static File copy(File file,File newFile)
	{
		return copy(file,newFile,null);
	}
	/**
	 * 返回失败的文件，
	 * 目标如果不存在，结果newFile内容等于file,
	 * 目标如果存在，
	 *		目标如果是文件夹，结果同copy(file,newFile(newFile,file.getName()))
	 *		目标如果不是文件夹，直接读file写进newFile,
	 * 默认不覆盖，
	 * 一堆回调函数，挺影响速度的，可以为null,
	 * @param listener onCover()覆盖时询问是否覆盖，onCopy(File,File)复制每一个文件(包括文件夹)时通知一次，onCopy(InputStream input,OutputStream output,byte[] buf,int bufSize,int len) 复制每一个文件过程中，每复制一个bufSize(介于 1K ~ 2M 分8次)通知一次，
	 */
	public static File copy(File file,File newFile,CopyListener listener)
	{
		if(!file.exists()||!file.canRead())
			return file;
		if(file.isDirectory()&&newFile.isFile())
			return file;
		if(!newFile.exists())
		{
			File parent=getParentFile(newFile);
			if(!parent.exists()&&!parent.mkdirs())
				return file;
		}
		else if(newFile.isDirectory())
		{
			newFile=newFile(newFile,file.getName());
		}
		if(newFile.exists())
		{
			//默认不覆盖，
			if(listener==null||!listener.onCover(file,newFile))
				return null;
		}
		else
		{
			try
			{
				if(file.isDirectory())
				{
					if(!newFile.mkdir())
						throw new IOException("创建文件夹失败");
				}
				else
				{
					newFile.createNewFile();
				}
			}
			catch(IOException e)
			{
				//java这里就莫名奇妙了，创建文件夹不抛异常，创建文件偏要抛异常，不喜欢java的异常处理，
				//无法创建原因只会是没有上级目录写权限，
				return file;
			}
		}
		/*
		 * 以上处理newFile,
		 * 处理结果是,
		 * file.exists()
		 *	&&file.canRead()
		 *	&&newFile.exists()
		 *	&&newFile.getParentFile().exists()
		 *	&&file.isDirectory()==newFile.isDirectory();
		 * 以下是复制，
		 */
		if(listener!=null)
			listener.onCopy(file,newFile);
		if(file.isDirectory())
		{
			//file.isDirectory()&&newFile.isDirectory()
			File[] subFiles=file.listFiles();
			if(subFiles!=null)
				for(File f:subFiles)
				{
					File result=copy(f,newFile,listener);
					if(result!=null)
						return result;
				}
		}
		else
		{
			//file.isFile()&&newFile.isFile()
			try
			{
				long fileLength=file.length();
				//bufSize 介于 1K ~ 2M 分8次，
				int bufSize=0;
				if(fileLength>16*1024*1024)
					bufSize=2*1024*1014;
				else if(fileLength>8*1024)
					bufSize=(int)fileLength/8;
				else
					bufSize=1*1024;
				InputStream input=new FileInputStream(file);
				OutputStream output=new FileOutputStream(newFile);
				Long len=copy(input,output,bufSize,listener);
				if(len==null)
					return file;
				input.close();
				output.close();
			}
			catch(FileNotFoundException e)
			{
				//不可到达，
				throw new RuntimeException("也许是代码问题",e);
			}
			catch(IOException e)
			{
				//不可到达，不知道什么情况会关闭失败，
				throw new RuntimeException("文件关闭失败",e);
			}
		}
		return null;
	}
	/**
	 * @return 返回null表示失败，其他表示文件长度，
	 */
	private static Long copy(InputStream input,OutputStream output,int bufSize,CopyStreamProgressListener listener)
	{
		Long result=(long)0;
		byte[] buf=new byte[bufSize];
		try
		{
			int len=0;
			if(listener!=null)
				listener.onCopy(input,output,buf,bufSize,len);
			while((len=input.read(buf))>0)
			{
				output.write(buf,0,len);
				result+=len;
				if(listener!=null)
					listener.onCopy(input,output,buf,bufSize,len);
			}
		}
		catch(IOException e)
		{
			//空间满了或者没权限返回失败，
			result=null;
		}
		return result;
	}
	public static interface CoverListener
	{
		public boolean onCover(File file,File newFile);
	}
	public static interface CopyStreamProgressListener
	{
		public void onCopy(InputStream input,OutputStream output,byte[] buf,int bufSize,int len);
	}
	public static interface CopyListener extends CoverListener,CopyStreamProgressListener
	{
		public void onCopy(File file,File newFile);
		public void onFinished();
	}
	public static interface DeleteListener
	{
		public void onDelete(File file,boolean isDeleted);
		public void onFinished();
	}
	/**
	 * 不需要继承CoverListener,但是看着顺眼，
	 */
	public static interface MoveListener extends CoverListener,CopyListener,DeleteListener
	{
	}
}
