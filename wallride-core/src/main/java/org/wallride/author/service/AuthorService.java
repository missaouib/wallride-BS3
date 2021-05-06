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

package org.wallride.author.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.wallride.author.controller.AuthorForm;
import org.wallride.author.repository.AuthorRepository;
import org.wallride.domain.Author;
import org.wallride.domain.User;
import org.wallride.exception.DuplicateCodeException;
import org.wallride.exception.EmptyCodeException;
import org.wallride.exception.ServiceException;
import org.wallride.support.AuthorizedUser;
import org.wallride.web.support.Pagination;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class AuthorService {

	@Resource
	private AuthorRepository authorRepository;

	public Author createAuthor(AuthorForm form, AuthorizedUser createdBy) {
		if (form.getCode() == null) {
			throw new EmptyCodeException();
		}
		Author duplicate = authorRepository.findOneByCodeAndLanguage(form.getCode(), form.getLanguage());
		if (duplicate != null) {
			throw new DuplicateCodeException(form.getCode());
		}

		LocalDateTime now = LocalDateTime.now();
		Author author = new Author();
		author.setCode(form.getCode());
		author.setLanguage(form.getLanguage());
		author.setName(form.getName());
		author.setDescription(form.getDescription());

		author.setCreatedAt(now);
		author.setCreatedBy(createdBy.toString());
		author.setUpdatedAt(now);
		author.setUpdatedBy(createdBy.toString());

		return authorRepository.saveAndFlush(author);
	}

	public Author updateAuthor(AuthorForm form, AuthorizedUser updatedBy) {
		Author author = authorRepository.findOneForUpdateById(form.getId());
		if (author == null) {
			throw new ServiceException();
		}
		if (!updatedBy.getRoles().contains(User.Role.ADMIN)) {
			throw new ServiceException();
		}
		
		if (form.getCode() == null) {
			throw new EmptyCodeException();
		}
		Author duplicate = authorRepository.findOneByCodeAndLanguage(form.getCode(), form.getLanguage());
		if (duplicate != null && !duplicate.equals(author)) {
			throw new DuplicateCodeException(form.getCode());
		}

		LocalDateTime now = LocalDateTime.now();
		author.setCode(form.getCode());
		author.setLanguage(form.getLanguage());
		author.setName(form.getName());
		author.setDescription(form.getDescription());

		author.setUpdatedAt(now);
		author.setUpdatedBy(updatedBy.toString());

		return authorRepository.saveAndFlush(author);
	}

	public Author deleteAuthor(AuthorForm form, AuthorizedUser deletedBy) {
		Author author = authorRepository.findOneForUpdateById(form.getId());
		if (author == null) {
			throw new ServiceException();
		}
		if (!deletedBy.getRoles().contains(User.Role.ADMIN)) {
			throw new ServiceException();
		}
		authorRepository.delete(author);
		return author;
	}

	public Author getAuthorById(long id) {
		return authorRepository.findOneById(id);
	}

	public List<Long> getAuthorIds(AuthorForm form) {
		return authorRepository.searchForId(form);
	}

	public Page<Author> getAuthors(AuthorForm form) {
		return authorRepository.search(form);
	}

	public Page<Author> getAuthors(AuthorForm form, Pageable pageable) {
		return authorRepository.search(form, pageable);
	}

	public boolean isFormBlank(AuthorForm form) {
		boolean isCodeEdited = form.getCode() != null && !form.getCode().isEmpty();
		boolean isNameEdited = form.getName() != null && !form.getName().isEmpty();
		boolean isDescriptionEdited = form.getDescription() != null && !form.getDescription().isEmpty();
		boolean isKeywordEdited = form.getKeyword() != null && !form.getKeyword().isEmpty();
		return !(isCodeEdited || isNameEdited || isDescriptionEdited || isKeywordEdited);
	}

	public void searchAndSetPagination(
			AuthorForm form,
			String language,
			Pageable pageable,
			String searchUrl,
			Model model) {
		Page<Author> authors = getAuthors(form.withLanguage(language), pageable);
		model.addAttribute("authors", authors);
		model.addAttribute("pagination", new Pagination<>(authors, searchUrl));
	}

	public String buildSearchUrl(HttpServletRequest servletRequest) {
		return ServletUriComponentsBuilder.fromRequest(servletRequest)
				.replaceQueryParam("page")
				.build()
				.toUriString();
	}

	public String buildSearchUrl(String searchUrl) {
		return ServletUriComponentsBuilder.fromHttpUrl(searchUrl)
				.replaceQueryParam("page")
				.build()
				.toUriString();
	}
}
