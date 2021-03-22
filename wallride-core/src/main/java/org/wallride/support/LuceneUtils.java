package org.wallride.support;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.Scorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.wallride.domain.DomainObject;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.util.Assert;

public class LuceneUtils {

	public static <T extends DomainObject<?>> List<T> highlight(EntityManager entityManager,
			String keyword, List<T> items, String[] fieldNames) {
		if (keyword == null) {
			return items;
		}
		FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
		Analyzer analyzer = fullTextEntityManager.getSearchFactory().getAnalyzer("synonyms");

		List<T> highlightedItems = new ArrayList<>();
		for (T item : items) {
			T highlightedItem = highlight(analyzer, keyword, item, fieldNames);
			highlightedItems.add(highlightedItem);
		}

		return highlightedItems;
	}

	private static <T extends DomainObject<?>> T highlight(Analyzer analyzer,
			String keyword, T item, String[] fieldNames) {
		for (String fieldName : fieldNames) {
			Object content = get(item, fieldName);
			String highlightedContent = highlight(analyzer, keyword, (String) content);
			set(item, fieldName, highlightedContent, String.class);
		}
		return item;
	}

	private static String highlight(Analyzer analyzer,
			String keyword, String item) {
		if (keyword == null) {
			return item;
		}
		String fieldName = "fieldName";

		QueryParser queryParser = new QueryParser(fieldName, analyzer);
		Query query = null;
		try {
			query = queryParser.parse(keyword);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		Formatter formatter = new SimpleHTMLFormatter("<span class='highlight'>", "</span>");
		Scorer scorer = new QueryScorer(query);
		Highlighter highlighter = new Highlighter(formatter, scorer);
		highlighter.setTextFragmenter(new SimpleSpanFragmenter((QueryScorer) scorer, Integer.MAX_VALUE));

		Assert.notNull(item, "The item must be non-null.");

		String highlightedItem = "";
		try {
			highlightedItem = highlighter.getBestFragment(analyzer, fieldName, item);
		}
		catch (IOException | InvalidTokenOffsetsException e) {
			e.printStackTrace();
		}

		if (highlightedItem != null) {
			return highlightedItem;
		}
		return item;
	}

	private static void set(Object obj, String name, Object value, Class<?> parameterType) {
		Class<?> c = obj.getClass();
		try {
			Method med = c.getMethod("set" + initStr(name), parameterType);
			med.invoke(obj, value);	
		} catch (Exception e) {
			e.printStackTrace();
		} 	
	}

	private static Object get(Object obj, String name) {
		Class<?> c = obj.getClass();
		try {
			Method med = c.getMethod("get" + initStr(name));
			return med.invoke(obj);	
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} 	
	}

	private static String initStr(String a) {
		return a.substring(0,1).toUpperCase() + a.substring(1);
	}
}