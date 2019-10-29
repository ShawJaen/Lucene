package com.test;

import java.nio.file.FileSystems;
import java.sql.ResultSet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.wltea.analyzer.lucene.IKAnalyzer;

import com.test.util.JdbcUtil;

/**
 * ����Lucene5.5.4�����ݿ�����demo
 * @author liuxianan
 */
public class DbSearchDemo
{
	public static final String INDEX_PATH = "E:\\lucene-db";
	public static final String JDBC_URL = "jdbc:mysql://127.0.0.1:3306/ssm?useUnicode=true&characterEncoding=utf-8&serverTimezone=UTC";
	public static final String USER = "root";
	public static final String PWD = "123";
	
	/**
	 * ��������
	 */
	public void creatIndex()
	{
		IndexWriter indexWriter = null;
		try
		{
			Directory directory = FSDirectory.open(FileSystems.getDefault().getPath(INDEX_PATH));
			//Analyzer analyzer = new StandardAnalyzer();
			Analyzer analyzer = new IKAnalyzer(true);
			IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
			indexWriter = new IndexWriter(directory, indexWriterConfig);
			indexWriter.deleteAll();// �����ǰ��index
			
			JdbcUtil jdbc = new JdbcUtil(JDBC_URL, USER, PWD);
			ResultSet rs = jdbc.query("select * from blog");
			while(rs.next())
			{
				Document document = new Document();
				document.add(new Field("id", rs.getString("id"), TextField.TYPE_STORED));
				document.add(new Field("title", rs.getString("title"), TextField.TYPE_STORED));
				document.add(new Field("content", rs.getString("content"), TextField.TYPE_STORED));
				document.add(new Field("tag", rs.getString("tags"), TextField.TYPE_STORED));
				document.add(new Field("url", rs.getString("url"), TextField.TYPE_STORED));
				indexWriter.addDocument(document);
			}
			jdbc.closeAll();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if(indexWriter != null) indexWriter.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * ����
	 */
	public void search(String keyWord)
	{
		DirectoryReader directoryReader = null;
		try
		{
			// 1������Directory
			Directory directory = FSDirectory.open(FileSystems.getDefault().getPath(INDEX_PATH));
			// 2������IndexReader
			directoryReader = DirectoryReader.open(directory);
			// 3������IndexReader����IndexSearch
			IndexSearcher indexSearcher = new IndexSearcher(directoryReader);
			// 4������������Query
			// Analyzer analyzer = new StandardAnalyzer();
			Analyzer analyzer = new IKAnalyzer(true); // ʹ��IK�ִ�
			
			// �򵥵Ĳ�ѯ������Query��ʾ������Ϊcontent����keyWord���ĵ�
			//Query query = new QueryParser("content", analyzer).parse(keyWord);
			
			String[] fields = {"title", "content", "tag"};
			// MUST ��ʾand��MUST_NOT ��ʾnot ��SHOULD��ʾor
			BooleanClause.Occur[] clauses = {BooleanClause.Occur.SHOULD, BooleanClause.Occur.SHOULD, BooleanClause.Occur.SHOULD};
			// MultiFieldQueryParser��ʾ���������� ͬʱ���Խ������ո���ַ����������������"�Ϻ� �й�" 
			Query multiFieldQuery = MultiFieldQueryParser.parse(keyWord, fields, clauses, analyzer);
			
			// 5������searcher�������ҷ���TopDocs
			TopDocs topDocs = indexSearcher.search(multiFieldQuery, 100); // ����ǰ100�����
			System.out.println("���ҵ�ƥ�䴦��" + topDocs.totalHits);
			// 6������TopDocs��ȡScoreDoc����
			ScoreDoc[] scoreDocs = topDocs.scoreDocs;
			System.out.println("���ҵ�ƥ���ĵ�����" + scoreDocs.length);
			
			QueryScorer scorer = new QueryScorer(multiFieldQuery, "content");
			SimpleHTMLFormatter htmlFormatter = new SimpleHTMLFormatter("<span style=\"backgroud:red\">", "</span>");
			Highlighter highlighter = new Highlighter(htmlFormatter, scorer);
			highlighter.setTextFragmenter(new SimpleSpanFragmenter(scorer));
			for (ScoreDoc scoreDoc : scoreDocs)
			{
				// 7������searcher��ScoreDoc�����ȡ�����Document����
				Document document = indexSearcher.doc(scoreDoc.doc);
				String content = document.get("content");
				//TokenStream tokenStream = new SimpleAnalyzer().tokenStream("content", new StringReader(content));
				//TokenSources.getTokenStream("content", tvFields, content, analyzer, 100);
				//TokenStream tokenStream = TokenSources.getAnyTokenStream(indexSearcher.getIndexReader(), scoreDoc.doc, "content", document, analyzer);
				//System.out.println(highlighter.getBestFragment(tokenStream, content));
				System.out.println("-----------------------------------------");
				System.out.println("���±��⣺"+document.get("title"));
				System.out.println("���µ�ַ��" + document.get("url"));
				System.out.println("�������ݣ�");
				System.out.println(highlighter.getBestFragment(analyzer, "content", content));
				System.out.println("");
				// 8������Document�����ȡ��Ҫ��ֵ
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if(directoryReader != null) directoryReader.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public static void main(String args[])
	{
		DbSearchDemo demo = new DbSearchDemo();
		demo.creatIndex();
		demo.search("����֮ʱ");
	}
}