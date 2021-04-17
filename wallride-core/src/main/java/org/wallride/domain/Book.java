package org.wallride.domain;

import javax.persistence.Entity;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Indexed;

@Entity
@NamedEntityGraphs({
    @NamedEntityGraph(name = Book.SHALLOW_GRAPH_NAME),
    @NamedEntityGraph(name = Book.DEEP_GRAPH_NAME)
})
@Table(name = "book")
@DynamicInsert
@DynamicUpdate
@Analyzer(definition = "synonyms")
@Indexed
public class Book extends BookInformation implements Comparable<Book> {

	public static final String SHALLOW_GRAPH_NAME = "BOOK_SHALLOW_GRAPH";
	public static final String DEEP_GRAPH_NAME = "BOOK_DEEP_GRAPH";

	@Override
	public int compareTo(Book bookId) {
		return (int) (bookId.getId() - getId());
	}
}
