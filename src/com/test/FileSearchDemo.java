package com.test;

import java.io.File;
import java.nio.file.FileSystems;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.wltea.analyzer.lucene.IKAnalyzer;

import com.test.util.FileUtil;

/**
 * ����Lucene5.5.4���ļ�����demo
 * @author liuxianan
 * @date 2017-05-02
 */
public class FileSearchDemo
{
	public static final String INDEX_PATH = "E:\\lucene"; // ���Lucene�����ļ���λ��
	public static final String SCAN_PATH = "E:\\text"; // ��Ҫ��ɨ���λ�ã����Ե�ʱ��ǵö����������һЩ�ļ�
	
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
			// ��ȡ��ɨ��Ŀ¼�µ������ļ���������Ŀ¼
			List<File> files = FileUtil.listAllFiles(SCAN_PATH);
			for(int i=0; i<files.size(); i++)
			{
				Document document = new Document();
				File file = files.get(i);
				document.add(new Field("content", FileUtil.readFile(file.getAbsolutePath()), TextField.TYPE_STORED));
				document.add(new Field("fileName", file.getName(), TextField.TYPE_STORED));
				document.add(new Field("filePath", file.getAbsolutePath(), TextField.TYPE_STORED));
				document.add(new Field("updateTime", file.lastModified()+"", TextField.TYPE_STORED));
				indexWriter.addDocument(document);
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
			
			String[] fields = {"fileName", "content"}; // Ҫ�������ֶΣ�һ������ʱ������ֻ����һ���ֶ�
			// �ֶ�֮������ǹ�ϵ��MUST��ʾand��MUST_NOT��ʾnot��SHOULD��ʾor���м���fields�ͱ����м���clauses
			BooleanClause.Occur[] clauses = {BooleanClause.Occur.SHOULD, BooleanClause.Occur.SHOULD};
			// MultiFieldQueryParser��ʾ���������� ͬʱ���Խ������ո���ַ����������������"�Ϻ� �й�" 
			Query multiFieldQuery = MultiFieldQueryParser.parse(keyWord, fields, clauses, analyzer);
			
			// 5������searcher�������ҷ���TopDocs
			TopDocs topDocs = indexSearcher.search(multiFieldQuery, 100); // ����ǰ100�����
			System.out.println("���ҵ�ƥ�䴦��" + topDocs.totalHits); // totalHits��scoreDocs.length������û������
			// 6������TopDocs��ȡScoreDoc����
			ScoreDoc[] scoreDocs = topDocs.scoreDocs;
			System.out.println("���ҵ�ƥ���ĵ�����" + scoreDocs.length);
			
			QueryScorer scorer = new QueryScorer(multiFieldQuery, "content");
			// �Զ����������
			SimpleHTMLFormatter htmlFormatter = new SimpleHTMLFormatter("<span style=\"backgroud:red\">", "</span>");
			Highlighter highlighter = new Highlighter(htmlFormatter, scorer);
			highlighter.setTextFragmenter(new SimpleSpanFragmenter(scorer));
			for (ScoreDoc scoreDoc : scoreDocs)
			{
				// 7������searcher��ScoreDoc�����ȡ�����Document����
				Document document = indexSearcher.doc(scoreDoc.doc);
				//TokenStream tokenStream = new SimpleAnalyzer().tokenStream("content", new StringReader(content));
				//TokenSources.getTokenStream("content", tvFields, content, analyzer, 100);
				//TokenStream tokenStream = TokenSources.getAnyTokenStream(indexSearcher.getIndexReader(), scoreDoc.doc, "content", document, analyzer);
				//System.out.println(highlighter.getBestFragment(tokenStream, content));
				System.out.println("-----------------------------------------");
				System.out.println(document.get("fileName") + ":" + document.get("filePath"));
				System.out.println(highlighter.getBestFragment(analyzer, "content", document.get("content")));
				System.out.println("");
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
		FileSearchDemo demo = new FileSearchDemo();
		demo.creatIndex();
		demo.search("�Ϻ�");
	}
}