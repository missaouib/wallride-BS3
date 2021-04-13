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

package org.wallride.author.controller;

import java.io.Serializable;
import java.util.SortedSet;

import javax.validation.constraints.NotNull;

import org.springframework.beans.BeanUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.wallride.domain.Author;
import org.wallride.domain.Book;

public class AuthorForm implements Serializable {

	private static final long serialVersionUID = 2027706685900948291L;

	interface CreateValidations {}
	interface UpdateValidations {}
	@NotNull(groups = {UpdateValidations.class}) private Long id;
	@NotNull(groups = {CreateValidations.class, UpdateValidations.class}) private String code;
	private String language;
	@NotNull(groups = {CreateValidations.class, UpdateValidations.class}) private String name;
	private String description;
	private SortedSet<Book> books;
	private String keyword;
	private boolean confirmed;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
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

	public AuthorForm withLanguage(String language) {
		this.language = language;
		return this;
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

	public SortedSet<Book> getBooks() {
		return books;
	}

	public void setBooks(SortedSet<Book> books) {
		this.books = books;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public AuthorForm withKeyword(String keyword) {
		this.keyword = keyword;
		return this;
	}

	public boolean isConfirmed() {
		return confirmed;
	}

	public void setConfirmed(boolean confirmed) {
		this.confirmed = confirmed;
	}

	public boolean isSearchRequestEmpty() {
		if (StringUtils.hasText(getKeyword())) {
			return false;
		}
		if (StringUtils.hasText(getLanguage())) {
			return false;
		}
		return true;
	}

	public static AuthorForm createFormfromDomainObject(Author author) {
		AuthorForm request = new AuthorForm();
		BeanUtils.copyProperties(author, request);

		return request;
	}

	public boolean isKeywordEmpty() {
		if (StringUtils.hasText(getKeyword())) {
			return false;
		}
		return true;
	}
	
	public boolean isAdvancedSearch() {
		return false;
	}
	
	public AuthorForm createAuthorSearchRequest() {
		AuthorForm searchRequest = new AuthorForm();
		searchRequest.setKeyword(getKeyword());
		searchRequest.setLanguage(LocaleContextHolder.getLocale().toString());
		return searchRequest;
	}

	public MultiValueMap<String, String> toQueryParams() {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		if (StringUtils.hasText(keyword)) {
			params.add("keyword", keyword);
		}
		return params;
	}

	@Override
	public String toString() {
		return "AuthorForm [books=" + books + ", code=" + code + ", confirmed=" + confirmed + ", description="
				+ description + ", id=" + id + ", keyword=" + keyword + ", language=" + language + ", name=" + name
				+ "]";
	}

}