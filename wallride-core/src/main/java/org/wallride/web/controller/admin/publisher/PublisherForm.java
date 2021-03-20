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

package org.wallride.web.controller.admin.publisher;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import org.springframework.beans.BeanUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.wallride.domain.Publisher;

public class PublisherForm implements Serializable {

	private static final long serialVersionUID = 7023828135033954866L;

    interface CreateValidations {}
	interface UpdateValidations {}
	@NotNull(groups = {UpdateValidations.class}) private Long id;
	@NotNull(groups = {CreateValidations.class, UpdateValidations.class}) private String code;
	@NotNull(groups = {CreateValidations.class, UpdateValidations.class}) private String name;
	private String country;
	private String webpage;
	private String notes;
	private String language;
	private String keyword;

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

	public PublisherForm withLanguage(String language) {
		this.language = language;
		return this;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public PublisherForm withKeyword(String keyword) {
		this.keyword = keyword;
		return this;
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

	public static PublisherForm createFormfromDomainObject(Publisher publisher) {
		PublisherForm request = new PublisherForm();
		BeanUtils.copyProperties(publisher, request);

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
	
	public PublisherForm createPublisherSearchRequest() {
		PublisherForm searchRequest = new PublisherForm();
		searchRequest.setKeyword(getKeyword());
		searchRequest.setLanguage(LocaleContextHolder.getLocale().toLanguageTag());
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
		return "PublisherRequest [code=" + code + ", country=" + country + ", id=" + id
				+ ", keyword=" + keyword + ", language=" + language + ", name=" + name + ", notes="
				+ notes + ", webpage=" + webpage + "]";
	}
}