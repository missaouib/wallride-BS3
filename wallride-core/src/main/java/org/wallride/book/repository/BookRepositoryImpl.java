/*
 * Copyright 2014 Tagbangers, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wallride.book.repository;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.BooleanJunction;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;
import org.wallride.book.controller.BookForm;
import org.wallride.domain.Book;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.List;
import java.util.stream.Collectors;

public class BookRepositoryImpl implements BookRepositoryCustom {

	@PersistenceContext
	private EntityManager entityManager;

	private static Logger logger = LoggerFactory.getLogger(BookRepositoryImpl.class);

	@Override
	public Page<Book> search(BookForm form) {
		return search(form, Pageable.unpaged());
	}

	@Override
	public Page<Book> search(BookForm form, Pageable pageable) {
		Session session = (Session) entityManager.getDelegate();
		Criteria criteria = session.createCriteria(Book.class)
				.setFetchMode("authors", FetchMode.JOIN);

		logger.info("search form.getKeyword() = {}", form.getKeyword());
		FullTextQuery persistenceQuery = buildFullTextQuery(form, pageable, criteria);
		int resultSize = persistenceQuery.getResultSize();
		List<Book> results = persistenceQuery.getResultList();
		return new PageImpl<>(results, pageable, resultSize);
	}

	@Override
	public List<Long> searchForId(BookForm form) {
		FullTextQuery persistenceQuery = buildFullTextQuery(form, Pageable.unpaged(), null);
		persistenceQuery.setProjection("id");
		List<Object[]> results = persistenceQuery.getResultList();
		List<Long> nos = results.stream().map(result -> (long) result[0]).collect(Collectors.toList());
		return nos;
	}

	public FullTextQuery buildFullTextQuery(BookForm form, Pageable pageable, Criteria criteria) {
		FullTextEntityManager fullTextEntityManager =  Search.getFullTextEntityManager(entityManager);
		QueryBuilder qb = fullTextEntityManager.getSearchFactory()
				.buildQueryBuilder()
				.forEntity(Book.class)
				.get();

		@SuppressWarnings("rawtypes")
		BooleanJunction<BooleanJunction> junction = qb.bool();
		junction.must(qb.all().createQuery());

		if (StringUtils.hasText(form.getKeyword())) {
			Analyzer analyzer = fullTextEntityManager.getSearchFactory().getAnalyzer("synonyms");
			String[] fields = new String[] {
					"code", "title", "description", "authors.name"
			};
			MultiFieldQueryParser parser = new MultiFieldQueryParser(fields, analyzer);
			parser.setDefaultOperator(QueryParser.Operator.AND);
			Query query = null;
			try {
				query = parser.parse(form.getKeyword());
			}
			catch (ParseException e1) {
				try {
					query = parser.parse(QueryParser.escape(form.getKeyword()));
				}
				catch (ParseException e2) {
					throw new RuntimeException(e2);
				}
			}
			junction.must(query);
		}

		if (StringUtils.hasText(form.getLanguage())) {
			junction.must(qb.keyword().onField("language").matching(form.getLanguage()).createQuery());
		}

		Query searchQuery = junction.createQuery();

		Sort sort = new Sort(new SortField("sortCode", SortField.Type.STRING));

		FullTextQuery persistenceQuery = fullTextEntityManager
				.createFullTextQuery(searchQuery, Book.class)
				.setCriteriaQuery(criteria)
				.setSort(sort);
		if (pageable.isPaged()) {
			persistenceQuery.setFirstResult((int) pageable.getOffset());
			persistenceQuery.setMaxResults(pageable.getPageSize());
		}
		return persistenceQuery;
	}
}
