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

import org.hibernate.annotations.*;
import org.hibernate.search.annotations.*;
import org.hibernate.search.annotations.Index;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@NamedEntityGraphs({
		@NamedEntityGraph(name = Publisher.SHALLOW_GRAPH_NAME),
		@NamedEntityGraph(name = Publisher.DEEP_GRAPH_NAME)
})
@Table(name = "publisher", uniqueConstraints = @UniqueConstraint(columnNames = {"code", "language"}))
@Inheritance(strategy = InheritanceType.JOINED)
@DynamicInsert
@DynamicUpdate
@Indexed
public class Publisher extends DomainObject<Long> {

	private static final long serialVersionUID = 4527787065269897622L;

	public static final String SHALLOW_GRAPH_NAME = "PUBLISHER_SHALLOW_GRAPH";
	public static final String DEEP_GRAPH_NAME = "PUBLISHER_DEEP_GRAPH";

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

	@Column(nullable = false)
	@Field
	private String name;

	private String country;

	private String webpage;

	@Lob
	private String notes;

	@Column(length = 6, nullable = false)
	@Field(analyze = Analyze.NO)
	private String language;

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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getWebpage() {
		return webpage;
	}

	public void setWebpage(String webpage) {
		this.webpage = webpage;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	@Override
	public String print() {
		return getName();
	}

	@Override
	public String toString() {
		return print();
	}
}
