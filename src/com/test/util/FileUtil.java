package com.test.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;


/**
 * �ļ�����������
 * 
 * @start 2013-11-08
 * @last 2014-10-22
 * @version 1.0
 * @author LXA
 */
public class FileUtil
{
	private static Logger2 log = LoggerFactory2.getLogger(FileUtil.class);

	/**
	 * ��ȡ�ļ��ĺ�׺������ǰ��ĵ㣬�硰.jpg�������Զ�תСд
	 * @param path
	 * @return
	 */
	public static String getExt(String path)
	{
		int idx = path.lastIndexOf(".");
		if (idx < 0)
			return "";
		return path.substring(idx).toLowerCase();
	}

	/**
	 * �ݹ�ɾ���ļ����ļ���
	 * @param file
	 * @return �����Ƿ�ɾ���ɹ�������ļ��������ڣ�Ҳ����true
	 */
	public static boolean delete(File file)
	{
		boolean flag = true;// ����ļ������Ͳ�������ôĬ�Ͼ���true
		if (file.isFile())
			flag &= file.delete();
		else if (file.isDirectory())
		{
			for (File f : file.listFiles())
				flag &= delete(f);
			flag &= file.delete();
		}
		return flag;
	}

	/**
	 * ���Ƶ����ļ�
	 * @param src �����Ƶ��ļ���
	 * @param dest Ŀ���ļ���
	 * @param override ���Ŀ���ļ����ڣ��Ƿ񸲸�
	 * @return ������Ƴɹ�����true�����򷵻�false
	 */
	public static boolean copy(String src, String dest, boolean override)
	{
		File srcFile = new File(src);
		if (!srcFile.exists()) // �ж�Դ�ļ��Ƿ����
		{
			log.error("Դ�ļ������ڣ�{}", src);
			return false;
		}
		else if (!srcFile.isFile())
		{
			log.error("Դ�ļ�����һ���ļ���{}", src);
			return false;
		}
		File destFile = new File(dest); // �ж�Ŀ���ļ��Ƿ����
		if (destFile.exists())
		{
			if (override) // ���Ŀ���ļ����ڲ�������
				destFile.delete();// ɾ���Ѿ����ڵ�Ŀ���ļ�������Ŀ���ļ���Ŀ¼���ǵ����ļ�
			else
			{
				log.error("�Ѵ���ͬ���ļ��Ҳ������ǣ�{}", dest);
				return false;
			}
		}
		else
		{
			if (!destFile.getParentFile().exists()) // ���Ŀ���ļ�����Ŀ¼�����ڣ��򴴽�Ŀ¼
			{
				if (!destFile.getParentFile().mkdirs())
				{
					log.error("����ļ��в��������Զ�����ʧ�ܣ�{}", dest);
					return false;// �����ļ�ʧ�ܣ�����Ŀ���ļ�����Ŀ¼ʧ��
				}
			}
		}
		// �����ļ�
		int byteread = 0; // ��ȡ���ֽ���
		InputStream in = null;
		OutputStream out = null;
		try
		{
			in = new FileInputStream(srcFile);
			out = new FileOutputStream(destFile);
			byte[] buffer = new byte[1024];
			while ((byteread = in.read(buffer)) != -1)
				out.write(buffer, 0, byteread);
			return true;
		}
		catch (Exception e)
		{
			log.error("�����ļ�ʧ�ܣ�", e);
			return false;
		}
		finally
		{
			try
			{
				if (out != null)
					out.close();
				if (in != null)
					in.close();
			}
			catch (Exception e)
			{
				log.error("���Թر���ʱʧ�ܣ�", e);
				e.printStackTrace();
			}
		}
	}

	/**
	 * ���Ƶ����ļ��������򸲸�
	 * @param src
	 * @param dest
	 * @return
	 */
	public static boolean copy(String src, String dest)
	{
		return copy(src, dest, true);
	}

	/**
	 * �����ļ�
	 * @param src
	 * @param dest
	 * @return
	 */
	public static boolean cut(String src, String dest)
	{
		return new File(src).renameTo(new File(dest));
	}

	/**
	 * �������ļ�
	 * @param src
	 * @param newName
	 * @return
	 */
	public static boolean rename(File src, String newName)
	{
		return src.renameTo(new File(src.getParent() + "\\" + newName));
	}

	/**
	 * �������ļ�
	 * @param src
	 * @param newName
	 * @return
	 */
	public static boolean rename(String src, String newName)
	{
		File file = new File(src);
		return rename(file, newName);
	}

	/**
	 * ���ļ��ж�ȡ����
	 * @param filePath
	 * @return
	 */
	public static String readFile(String filePath, String encoding)
	{
		log.debug("��ʼ��ȡ�ļ�:{}", filePath);
		try
		{
			FileInputStream fis = new FileInputStream(filePath);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis, encoding));
			String s = "";
			StringBuffer sb = new StringBuffer();
			while ((s = br.readLine()) != null)
				sb.append(s+"\n");
			br.close();
//			System.out.println("����:"+sb.toString());
			return sb.toString();
		}
		catch (Exception e)
		{
			log.error("��ȡ�ļ�ʧ�ܣ�", e);
			return null;
		}
	}

	/**
	 * ��Ĭ�ϵ�utf-8�����ȡ�ļ�
	 * @param filePath
	 * @return
	 */
	public static String readFile(String filePath)
	{
		return readFile(filePath, "UTF-8");
	}

	/**
	 * д���ļ�
	 * @param filePath
	 * @param text
	 * @param encoding
	 * @return
	 */
	public static boolean writeFile(String filePath, String text, String encoding)
	{
		try
		{
			FileOutputStream fos = new FileOutputStream(filePath);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos, encoding));
			bw.append(text);
			bw.close();
			return true;
		}
		catch (Exception e)
		{
			log.error("д���ļ�ʧ�ܣ�", e);
			return false;
		}
	}

	/**
	 * д���ļ���Ĭ��utf-8����
	 * @param filePath
	 * @param text
	 * @return
	 */
	public static boolean writeFile(String filePath, String text)
	{
		return writeFile(filePath, text, "utf-8");
	}
	
	/**
	 * ����ִ����
	 * @author LXA
	 */
	public interface TraverseExecuter
	{
		/**
		 * ����ĳ���ļ���ʱ��Ҫ�ڲ���Ҫִ�еķ���
		 * @param filePath �ļ�·��
		 * @param fileName �ļ���
		 */
		public void execute(String filePath, String fileName);
	}
	
	/**
	 * ����ĳ���ļ���ִ��һ�β���
	 * @param filePath �ļ���·��
	 * @param fileNameFilter ɸѡ����Ϊnullʱ��ɸѡ
	 * @param executer ��Ҫ���õķ���
	 */
	public static void traverse(String filePath, FilenameFilter fileNameFilter, TraverseExecuter executer)
	{
		try
		{
			File root = new File(filePath);
			for(File file : root.listFiles(fileNameFilter))
			{
				String path = file.getAbsolutePath();
				if(file.isFile())
					executer.execute(path, file.getName());
				else if(file.isDirectory())
					traverse(path, fileNameFilter, executer);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * �г�ĳ��·���µ������ļ����������ļ��У������������ļ�����ô��������
	 * @param filePath ��Ҫ�������ļ�·��
	 * @param fileNameFilter �ļ���������
	 * @return �ļ�����
	 */
	public static List<File> listAllFiles(String filePath, FilenameFilter fileNameFilter)
	{
		List<File> files= new ArrayList<File>();
		try
		{
			File root = new File(filePath);
			if(!root.exists()) return files;
			if(root.isFile()) files.add(root);
			else
			{
				for(File file : root.listFiles(fileNameFilter))
				{
					if(file.isFile()) files.add(file);
					else if(file.isDirectory())
					{
						files.addAll(listAllFiles(file.getAbsolutePath(), fileNameFilter));
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return files;
	}
	
	/**
	 * �г�ĳ��·���µ������ļ����������ļ��У������������ļ�����ô��������
	 * @param filePath ��Ҫ�������ļ�·��
	 * @return �ļ�����
	 */
	public static List<File> listAllFiles(String filePath)
	{
		return listAllFiles(filePath, null);
	}
	
	/**
	 * ����ĳ���ļ���ִ��һ�β���
	 * @param filePath �ļ���·��
	 * @param executer ��Ҫ���õķ���
	 */
	public static void traverse(String filePath, TraverseExecuter executer)
	{
		traverse(filePath, null, executer);
	}
	
	/**
	 * ����ĳ��·����ɾ�����еĿ��ļ���
	 * @param filePath
	 */
	public static void deleteEmptyFolder(File file)
	{
		File[] files = file.listFiles();
		if(files == null || files.length == 0)
		{
			delete(file);
			log.info("�����ļ���Ϊ�գ���ɾ����"+file.getAbsolutePath());
		}
		for(File f : files)
		{
			if(f.isDirectory())
				deleteEmptyFolder(f);
		}
	}
	
	/**
	 * ����ĳ��·����ɾ�����еĿ��ļ���
	 * @param filePath
	 */
	public static void deleteEmptyFolder(String filePath)
	{
		deleteEmptyFolder(new File(filePath));
	}

	/**
	 * ��һ��������д�뵽�����
	 * @param is ������
	 * @param os �����
	 * @param closeInput �Ƿ�ر�������
	 * @param closeOutput �Ƿ�ر������
	 * @throws IOException
	 */
	public static void writeIO(InputStream is, OutputStream os, Boolean closeInput, Boolean closeOutput) throws IOException
	{
		byte[] buf = new byte[1024];
		int len = -1;
		while ((len = is.read(buf)) != -1)
			os.write(buf, 0, len);
		if(closeInput)
		{
			if(is != null )
				is.close();
		}
		if(closeOutput)
		{
			if( os != null )
			{
				os.flush();
				os.close();
			}
		}
	}
	
	/**
	 * ��һ��������д�뵽�����
	 * @param is ������
	 * @param os �����
	 * @throws IOException
	 */
	public static void writeIO(InputStream is, OutputStream os) throws IOException
	{
		writeIO(is, os, false, false);
	}

}


/**
 * Ϊ�˱������jar�������Լ�ĳЩ����Ҫ�ռǼ�¼�ĳ��ϣ��ڲ�дһ����򵥵�log������<br>
 * ������Ƶ������ط���ʱ������ż���log4j��jar�������Էſ�����ע��<br>
 * Ȼ���ͷ����Logger��LoggerFactory������һ��2<br>
 * ע��ͬһ����Logger2��LoggerFactory2�����ظ�<br>
 * @author LXA
 */
class Logger2
{
	public void debug(String info, Object... args)
	{
		for(int i=0; i<args.length; i++) 
			info = info.replaceFirst("\\{\\}", args[i].toString());
		System.out.println(info);
	}
	
	public void error(String info, Throwable throwable)
	{
		System.err.println(info);
		throwable.printStackTrace();
	}
	public void error(String info, String aaa)
	{
		System.err.println(info);
	}
	public void error(String info)
	{
		System.err.println(info);
	}
	public void info(String info)
	{
		System.out.println(info);
	}
}
class LoggerFactory2
{
	public static <T> Logger2 getLogger(Class<T> cls)
	{
		return new Logger2();
	}
}