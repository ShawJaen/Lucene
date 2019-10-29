package com.test.util;



import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ���ݿ��������������
 * ���ܣ���ȡ���ݿ����Ӷ��󣬹ر����ӣ�ִ�и�����䣬ִִ��ѯ���
 * ����Oracle11g��������һ��λ���ڣ�D:\Oracle\app\oracle\product\11.2.0\server\jdbc\lib\ojdbc6_g.jar
 * ���ʹ�����ӳأ������������ã�
 * 1���ҵ�tomcat��context.xml�ļ�����<Context>�¼����ϣ�
  		<!-- XML�ļ��в�������֡�&�����ţ����Ա���ʹ��HTMLת���ַ� -->
  		<Resource name="mysqljdbc" auth="Container" type="javax.sql.DataSource" maxActive="100" maxIdle="30" maxWait="10000" username="karaoke" password="karaoke123" driverClassName="com.mysql.jdbc.Driver" url="jdbc:mysql://172.16.4.253:3306/children_kalaok?useUnicode=true&amp;characterEncoding=utf-8"/>
 * 2���ڹ��̵�web.xml������ϣ�
 		<resource-ref>
			<res-ref-name>mysqljdbc</res-ref-name>
			<res-type>javax.sql.DataSource</res-type>
			<res-auth>Container</res-auth>
		</resource-ref>
 * @start 2012��ĳ��ĳ��
 * @last 2014��7��3��
 * @version 1.0
 * @author LXA
 */
public class JdbcUtil
{
	private static final Logger log = LoggerFactory.getLogger(JdbcUtil.class);//��־����
	private Connection con = null;//���������Ӷ���
	private PreparedStatement ps = null;//����Ԥ����������
	private ResultSet rs = null;//�������������
	
	/**
	 * �޲εĹ��췽����ʹ��Ĭ�����õ�һЩ������Ĭ������²�ʹ�����ӳ�
	 */
	public JdbcUtil()
	{
		String url="jdbc:mysql://127.0.0.1:3306/ssm?useUnicode=true&characterEncoding=utf-8&serverTimezone=UTC";
		String user="root";
		String password="123";
		getConnection("mysql", url, user, password);
	}
	
	/**
	 * ʹ��JNDI��ȡ���ӣ���������tomcat�Լ���Ŀ��web.xml�������úø��ֲ���
	 */
	public JdbcUtil(String jndi)
	{
		getConnection(jndi);
	}
	
	/**
	 * �вεĹ��췽��
	 * @param url
	 * @param user �û���
	 * @param password 
	 */
	public JdbcUtil(String url,String user,String password)
	{
		getConnection("mysql", url, user, password);
	}
	
	/**
	 * �вεĹ��췽��
	 * @param url
	 * @param user �û���
	 * @param password 
	 */
	public JdbcUtil(String database,String url,String user,String password)
	{
		getConnection(database, url, user, password);
	}
	
	/**
	 * ��ͨ�Ļ�ȡ���ݿ�����
	 * @param database ���ݿ����࣬������mysql��orcal
	 * @param url ���ݿ����ӵ�URL
	 * 		Oracleʾ����jdbc:oracle:thin:@192.168.3.228:1521:XE��
	 * 		MySQLʾ����jdbc:mysql://172.16.4.253:3306/new-health-province?useUnicode=true&characterEncoding=utf-8
	 * @param user ���ݿ����ӵ��û���
	 * @param password ���ݿ����ӵ�����
	 * @return ���������Ӷ���
	 */
	private Connection getConnection(String database,String url,String user,String password)
	{
		try
		{
			String className = "";
			if("mysql".equals(database))
				className="com.mysql.jdbc.Driver";
			else if("orcal".equals(database))
				className="oracle.jdbc.driver.OracleDriver";
			Class.forName(className);
			log.debug("��ʼ�����������ݿ⣡");
			con = DriverManager.getConnection(url, user, password);
			log.debug("���ӳɹ���");
		}
		catch (Exception e)
		{
			log.error("�������ݿ�ʧ�ܣ�", e);
		}
		return con;
	}
	
	/**
	 * ��ȡ���ӣ�ʹ�����ݿ����ӳ�
	 * @param jndi ������tomcat��context.xml����Ķ���
	 * @return ���������Ӷ���
	 */
	public Connection getConnection(String jndi)
	{
		try
		{
			log.debug("��ʼ�����������ݿ⣡");
			Context context=new InitialContext();
			DataSource dataSource=(DataSource)context.lookup("java:comp/env/"+jndi);
			con=dataSource.getConnection();
			log.debug("���ӳɹ���");
		}
		catch (Exception e)
		{
			log.error("�������ݿ�ʧ�ܣ�", e);
		}
		return con;
	}
	
	/**
	 * �ر�����ռ�е���Դ
	 */
	public void closeAll()
	{
		try
		{
			if(rs!=null)
				rs.close();
			if(ps!=null)
				ps.close();
			if (con != null)
				con.close();
			log.debug("���ݿ������ѹرգ�");
		}
		catch (Exception e)
		{
			log.error("���Թر����ݿ�����ʱ����", e);
		}
	}
	
	/**
	 * ִ�����ݿ�ĸ��²�������������ɾ���ģ�ִ�к����ֶ��ر����ݿ�����
	 * @param sql ��Ҫִ�е�Ԥ�������
	 * @param params Ԥ�������Ĳ����б�
	 * @return ��Ӱ�������
	 */
	public int update(String sql,String... params)
	{
		log.debug("ִ��SQL��"+sql);
		int count = 0;//��Ӱ�������
		try
		{
			ps=con.prepareStatement(sql);
			for(int i=0;i<params.length;i++)
				ps.setString(i+1, params[i]);
			count=ps.executeUpdate();
		}
		catch (SQLException e)
		{
			log.debug("ִ��updateʱ����", e);
		}
		log.debug("��Ӱ�������:{}", count);
		return count;
	}
	
	/**
	 * ִ�����ݿ�ĸ��²�����ִ�к���Զ��ر����ݿ�����
	 * @param sql ��Ҫִ�е�Ԥ�������
	 * @param params Ԥ�������Ĳ����б�
	 * @return ��Ӱ�������
	 */
	public int updateWithClose(String sql,String... params)
	{
		int count = update(sql, params);
		closeAll();//�ر�����
		return count;
	}
	
	/**
	 * ִ�����ݿ�Ĳ�ѯ����
	 * @param sql ��Ҫִ�е�Ԥ�������
	 * @param params Ԥ�������Ĳ����б�
	 * @return ���ز�ѯ�Ľ����������ΪResultSet
	 */
	public ResultSet query(String sql,String... params)
	{
		log.debug("ִ�в�ѯSQL��"+sql);
		try
		{
			ps=con.prepareStatement(sql);
			for(int i=0;i<params.length;i++)
				ps.setString(i+1, params[i]);
			rs=ps.executeQuery();
		}
		catch (SQLException e)
		{
			log.debug("ִ��queryʱ����", e);
		}
		//���ڲ�ѯ�Ƿ��ؽ�������ڵ��ô˷�����ʱ��Ҫ��ResultSet.Next()�ķ�����
		//�������ﻹ���ܹر����ݿ�����
		return rs;
	}
	
	/**
	 * ִ�����ݿ�Ĳ�ѯ����������ҳ
	 * @param sql ��Ҫִ�е�Ԥ�������
	 * @param params Ԥ�������Ĳ����б�
	 * @return ���ز�ѯ�Ľ����������ΪResultSet
	 */
	public ResultSet queryByPage(int page,int pageSize, String sql,String... params)
	{
		sql = sql + " limit "+(page-1)*pageSize+","+pageSize;
		return query(sql, params);
	}
	
	/**
	 * ��ѯ�������һ�еĵ�һ�е�int�����ݣ�һ�㶼�ǻ�ȡcount(*)��ֵ
	 * @param sql ��Ҫִ�е�Ԥ�������
	 * @param params Ԥ�������Ĳ����б�
	 * @return ���ص�int������
	 */
	public int queryInt(String sql,String... params)
	{
		ResultSet resultSet = query(sql, params);
		int result = 0;
		try
		{
			if(resultSet.next())
				result = resultSet.getInt(1);//ע���һ��������1��һ�㶼�ǻ�ȡcount(*)��ֵ
		}
		catch (Exception e)
		{
			log.error("ִ��queryInt����", e);
		}
		return result;
	}
	/**
	 * ��ѯ�������һ�еĵ�һ�е�String������
	 * @param sql ��Ҫִ�е�Ԥ�������
	 * @param params Ԥ�������Ĳ����б�
	 * @return ���ص�int������
	 */
	public String queryString(String sql,String... params)
	{
		ResultSet rs = query(sql, params);
		String result = "";
		try
		{
			if(rs.next())
				result = rs.getString(1);//ע���һ��������1
		}
		catch (Exception e)
		{
			log.error("ִ��queryString����", e);
		}
		return result;
	}
	
	/**
	 * ��ѯ��������һ�еĵ�һ�е�int�����ݣ�һ�㶼�ǻ�ȡcount(*)��ֵ
	 * @param sql ��Ҫִ�е�Ԥ�������
	 * @param params Ԥ�������Ĳ����б�
	 * @return ���ص�int������
	 */
	public int queryIntWithClose(String sql,String... params)
	{
		int result = queryInt(sql, params);
		closeAll();
		return result;
	}
	
	public static void main(String[] args) throws Exception
	{
		//���ڲ�ѯ��ʾ������
		JdbcUtil jdbc = new JdbcUtil();
		String code = "103104000003";
		ResultSet rs = jdbc.query("select * from video where `code`=? ", code);
		while(rs.next())
		{
			System.out.println(rs.getInt("video_id"));
			System.out.println(rs.getString("player"));
			System.out.println(rs.getDate("upload_date"));
			System.out.println(rs.getString(1));//ע��������1��ʼ
			
		}
		jdbc.closeAll();
	}
}