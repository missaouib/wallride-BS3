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

package org.wallride.book.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.beans.BeanUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.wallride.domain.Author;
import org.wallride.domain.Book;

public class BookForm implements Serializable {

	interface CreateValidations {}
	interface UpdateValidations {}

	@NotNull(groups = {UpdateValidations.class})
	private Long id;

	@NotNull(groups = {CreateValidations.class, UpdateValidations.class})
	private String code;

	private String language;

	@NotNull(groups = {CreateValidations.class, UpdateValidations.class})
	private String title;

	private List<Long> authorIds = new ArrayList<>();

	private String publisherId;

	private String description;

	@Size(max = 17, groups = {CreateValidations.class, UpdateValidations.class})
	private String isbn;

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

	public BookForm withLanguage(String language) {
		this.language = language;
		return this;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<Long> getAuthorIds() {
		return authorIds;
	}

	public void setAuthorIds(List<Long> authorIds) {
		this.authorIds = authorIds;
	}

	public String getPublisherId() {
		return publisherId;
	}

	public void setPublisherId(String publisherId) {
		this.publisherId = publisherId;
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

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public BookForm withKeyword(String keyword) {
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

	public static BookForm createFormFromDomainObject(Book book) {
		BookForm form = new BookForm();
		BeanUtils.copyProperties(book, form);

		form.setPublisherId(book.getPublisher() != null ? Long.toString(book.getPublisher().getId()) : null);

		for (Author author : book.getAuthors()) {
			form.getAuthorIds().add(author.getId());
		}

		return form;
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
	
	public BookForm createBookSearchRequest() {
		BookForm searchRequest = new BookForm();
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
		return "BookForm [authorIds=" + authorIds + ", code=" + code + ", confirmed=" + confirmed + ", description="
				+ description + ", id=" + id + ", isbn=" + isbn + ", keyword=" + keyword + ", language=" + language
				+ ", publisherId=" + publisherId + ", title=" + title + "]";
	}
}