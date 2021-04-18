package org.wallride.domain;

import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SortNatural;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.SortableField;

@Entity
@NamedEntityGraphs({
    @NamedEntityGraph(name = BookInformation.SHALLOW_GRAPH_NAME,
			attributeNodes = {
					@NamedAttributeNode("authors")}
	),
    @NamedEntityGraph(name = BookInformation.DEEP_GRAPH_NAME,
			attributeNodes = {
					@NamedAttributeNode("authors")})
})
@Table(name = "book_information", uniqueConstraints = @UniqueConstraint(columnNames = {"code", "language"}))
@Inheritance(strategy = InheritanceType.JOINED)
@DynamicInsert
@DynamicUpdate
@Indexed
public class BookInformation extends DomainObject<Long> {

    private static final long serialVersionUID = -7146572154669005679L;

    public static final String SHALLOW_GRAPH_NAME = "BOOK_INFORMATION_SHALLOW_GRAPH";
	public static final String DEEP_GRAPH_NAME = "BOOK_INFORMATION_DEEP_GRAPH";

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
	@IndexedEmbedded(includeEmbeddedObjectId = true)
	private Publisher publisher;

	@Field
	private String description;

	@Column(length = 17)
	private String isbn;

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

	public Publisher getPublisher() {
		return publisher;
	}

	public void setPublisher(Publisher publisher) {
		this.publisher = publisher;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getIsbn() {
		return isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}

    @Override
    public String print() {
        return getCode() + " - " + getTitle();
    }
}
