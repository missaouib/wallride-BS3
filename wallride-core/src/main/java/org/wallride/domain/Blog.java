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

package org.wallride.domain;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.IndexedEmbedded;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Domain object representing a blog.
 *
 * @author Takeshi Ogawa
 */
@Entity
@NamedEntityGraphs({
		@NamedEntityGraph(name = Blog.DEEP_GRAPH_NAME,
				attributeNodes = {
						@NamedAttributeNode("languages")})
})
@Table(name = "blog")
@DynamicInsert
@DynamicUpdate
public class Blog extends DomainObject<Long> {

	public static final long DEFAULT_ID = 1;

	public static final String DEEP_GRAPH_NAME = "BLOG_DEEP_GRAPH";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(length = 200, nullable = false, unique = true)
	@Field(analyze = Analyze.NO)
	private String code;

	@Column(length = 6, nullable = false)
	@Field(analyze = Analyze.NO)
	private String defaultLanguage;

	@Embedded
	@IndexedEmbedded(includeEmbeddedObjectId = true)
	private GoogleAnalytics googleAnalytics;

	@OneToMany(mappedBy = "blog", cascade = CascadeType.ALL)
	@IndexedEmbedded(includeEmbeddedObjectId = true)
	private Set<BlogLanguage> languages = new HashSet<>();

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

	public String getDefaultLanguage() {
		return defaultLanguage;
	}

	public void setDefaultLanguage(String defaultLanguage) {
		this.defaultLanguage = defaultLanguage;
	}

	public GoogleAnalytics getGoogleAnalytics() {
		return googleAnalytics;
	}

	public void setGoogleAnalytics(GoogleAnalytics googleAnalytics) {
		this.googleAnalytics = googleAnalytics;
	}

	public Set<BlogLanguage> getLanguages() {
		return languages;
	}

	public void setLanguages(Set<BlogLanguage> languages) {
		this.languages = languages;
	}

	public BlogLanguage getLanguage(String language) {
		for (BlogLanguage blogLanguage : getLanguages()) {
			if (blogLanguage.getLanguage().equals(language)) {
				return blogLanguage;
			}
		}
		return null;
	}

	public String getTitle() {
		return getTitle(getDefaultLanguage());
	}

	public String getTitle(String language) {
		return getLanguage(language).getTitle();
	}

	public boolean isMultiLanguage() {
		return (getLanguages().size() > 1);
	}

	@Override
	public String print() {
		return getTitle();
	}
}
