package org.wallride.domain;

import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SortNatural;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.SortableField;

@Entity
@NamedEntityGraphs({
    @NamedEntityGraph(name = Book.SHALLOW_GRAPH_NAME),
    @NamedEntityGraph(name = Book.DEEP_GRAPH_NAME)
})
@Table(name = "book", uniqueConstraints = @UniqueConstraint(columnNames = {"code", "language"}))
@DynamicInsert
@DynamicUpdate
@Indexed
public class Book extends DomainObject<Long> {

    private static final long serialVersionUID = -7146572154669005679L;

    public static final String SHALLOW_GRAPH_NAME = "BOOK_SHALLOW_GRAPH";
	public static final String DEEP_GRAPH_NAME = "BOOK_DEEP_GRAPH";

    @Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Field(name = "sortId", analyze = Analyze.NO, index = Index.NO)
	@SortableField(forField = "sortId")
	private long id;

	@Column(length = 200, nullable = false)
	@Field(analyze = Analyze.NO)
	private String code;

	@Column(length = 6, nullable = false)
	@Field(analyze = Analyze.NO)
	private String language;

    @Column(length = 200)
	@Field
	private String title;

	@ManyToMany
	@JoinTable(
			name = "book_author",
			joinColumns = {@JoinColumn(name = "book_id")},
			inverseJoinColumns = @JoinColumn(name = "author_id", referencedColumnName = "id"))
	@SortNatural
	@IndexedEmbedded(includeEmbeddedObjectId = true)
	private SortedSet<Author> authors = new TreeSet<>();

    @ManyToOne
    @JoinColumn(name = "publisher_id")
	private Publisher publisher;

	@Lob
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

	public SortedSet<Author> getAuthors() {
		return authors;
	}

	public void setAuthors(SortedSet<Author> authors) {
		this.authors = authors;
	}

    @Override
    public String print() {
        return getTitle();
    }
}
