package org.wallride.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.SortableField;

@Entity
@NamedEntityGraphs({
		@NamedEntityGraph(name = Author.SHALLOW_GRAPH_NAME),
		@NamedEntityGraph(name = Author.DEEP_GRAPH_NAME)
})
@Table(name = "author", uniqueConstraints = @UniqueConstraint(columnNames = {"code", "language"}))
@DynamicInsert
@DynamicUpdate
@Indexed
public class Author extends DomainObject<Long> implements Comparable<Author> {

	public static final String SHALLOW_GRAPH_NAME = "AUTHOR_SHALLOW_GRAPH";
	public static final String DEEP_GRAPH_NAME = "AUTHOR_DEEP_GRAPH";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Field(name = "sortId", analyze = Analyze.NO, index = Index.NO)
	@SortableField(forField = "sortId")
	private long id;
	
	@Column(length = 200, nullable = false)
	@Fields({
		@Field,
		@Field(name = "sortCode", analyze = Analyze.NO, index = Index.NO)
	})
	@SortableField(forField = "sortCode")
	private String code;

	@Column(length = 6, nullable = false)
	@Field(analyze = Analyze.NO)
	private String language;

	@Column(length = 255)
	@Field
	private String name;

	@Field
	private String description;

	@Override
	public Long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	// public SortedSet<Book> getBooks() {
	// 	return books;
	// }

	// public void setBooks(SortedSet<Book> books) {
	// 	this.books = books;
	// }

	@Override
	public String print() {
		return getCode() + " - " + getName();
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int compareTo(Author o) {
		return (int) (this.getId() - o.getId());
	}
}
