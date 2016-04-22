/* ***************************************************
	^> File Name: FileOperator.java
	^> Author: AoEiuV020
	^> Mail: 490674483@qq.com
	^> Created Time: 2016/04/23 - 02:10:23
*************************************************** */
package com.aoeiuv020.tool;
import java.io.*;
public class FileOperator
{
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
	public static File copy(File file,File newFile,CopyListener listener)
	{
		return copy(file,newFile,listener,listener,listener);
	}
	/**
	 * 返回失败的文件，
	 * 目标如果不存在，结果newFile内容等于file,
	 * 目标如果存在，
	 *		目标如果是文件夹，结果同copy(file,new File(newFile,file.getName()))
	 *		目标如果不是文件夹，直接读file写进newFile,
	 * 默认不覆盖，
	 * 一堆回调函数，挺影响速度的，可以为null,
	 * @param coverListener 覆盖时询问是否覆盖，
	 * @param fileListener 复制每一个文件(包括文件夹)时通知一次，
	 * @param streamListener 复制每一个文件过程中，每复制一个bufSize(介于 1K ~ 2M 分8次)通知一次，
	 */
	public static File copy(File file,File newFile,CoverListener coverListener,CopyFileProgressListener fileListener,CopyStreamProgressListener streamListener)
	{
		if(!file.exists()||!file.canRead())
			return file;
		if(file.isDirectory()&&newFile.isFile())
			return file;
		if(!newFile.exists())
		{
			File parent=newFile.getParentFile();
			if(parent==null)
			{
				//恶心的方法，居然会返回null，又没有别的可替代，
				parent=new File(newFile.getAbsolutePath()).getParentFile();
				//这个parent不会再null，因为不存在的newFile不可能是根目录，
			}
			if(!parent.exists()&&!parent.mkdirs())
				return file;
		}
		else if(newFile.isDirectory())
		{
			newFile=new File(newFile,file.getName());
		}
		if(newFile.exists())
		{
			//默认不覆盖，
			if(coverListener==null||!coverListener.onCover(file,newFile))
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
		if(fileListener!=null)
			fileListener.onCopy(file,newFile);
		if(file.isDirectory())
		{
			//file.isDirectory()&&newFile.isDirectory()
			File[] subFiles=file.listFiles();
			if(subFiles!=null)
				for(File f:subFiles)
				{
					File result=copy(f,newFile,coverListener,fileListener,streamListener);
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
				long len=copy(input,output,bufSize,streamListener);
				if(len==0)
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
	 * 返回0表示失败，
	 */
	private static long copy(InputStream input,OutputStream output,int bufSize,CopyStreamProgressListener listener)
	{
		long result=0;
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
			result=0;
		}
		return result;
	}
	public static interface CoverListener
	{
		public boolean onCover(File file,File newFile);
	}
	public static interface CopyFileProgressListener
	{
		public void onCopy(File file,File newFile);
		public void onFinished();
	}
	public static interface CopyStreamProgressListener
	{
		public void onCopy(InputStream input,OutputStream output,byte[] buf,int bufSize,int len);
	}
	public static interface CopyListener extends CoverListener,CopyFileProgressListener,CopyStreamProgressListener
	{
	}
	public static interface DeleteListener
	{
		public void onDelete(File file,boolean isDeleted);
		public void onFinished();
	}
}
