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

package org.wallride.book.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.wallride.book.controller.BookForm;
import org.wallride.book.repository.BookRepository;
import org.wallride.domain.Author;
import org.wallride.domain.Book;
import org.wallride.domain.Publisher;
import org.wallride.domain.User;
import org.wallride.exception.DuplicateCodeException;
import org.wallride.exception.EmptyCodeException;
import org.wallride.exception.ServiceException;
import org.wallride.support.AuthorizedUser;
import org.wallride.web.support.Pagination;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class BookService {

	@Resource
	private BookRepository bookRepository;

	@PersistenceContext
	private EntityManager entityManager;

	public Book createBook(BookForm form, AuthorizedUser createdBy) {
		if (form.getCode() == null) {
			throw new EmptyCodeException();
		}
		Book duplicate = bookRepository.findOneByCodeAndLanguage(form.getCode(), form.getLanguage());
		if (duplicate != null) {
			throw new DuplicateCodeException(form.getCode());
		}

		LocalDateTime now = LocalDateTime.now();
		Book book = new Book();
		book.setCode(form.getCode());
		book.setLanguage(form.getLanguage());
		book.setTitle(form.getTitle());

		book.getAuthors().clear();
		for (long authorId : form.getAuthorIds()) {
			book.getAuthors().add(entityManager.getReference(Author.class, authorId));
		}

		Publisher publisher = null;
		if (form.getPublisherId() != null) {
			publisher = entityManager.getReference(Publisher.class, Long.parseLong(form.getPublisherId()));
		}
		book.setPublisher(publisher);
		book.setDescription(form.getDescription());
		book.setIsbn(form.getIsbn());

		book.setCreatedAt(now);
		book.setCreatedBy(createdBy.toString());
		book.setUpdatedAt(now);
		book.setUpdatedBy(createdBy.toString());

		return bookRepository.saveAndFlush(book);
	}

	public Book updateBook(BookForm form, AuthorizedUser updatedBy) {
		Book book = bookRepository.findOneForUpdateById(form.getId());
		if (book == null) {
			throw new ServiceException();
		}
		if (!updatedBy.getRoles().contains(User.Role.ADMIN)) {
			throw new ServiceException();
		}
		
		if (form.getCode() == null) {
			throw new EmptyCodeException();
		}
		Book duplicate = bookRepository.findOneByCodeAndLanguage(form.getCode(), form.getLanguage());
		if (duplicate != null && !duplicate.equals(book)) {
			throw new DuplicateCodeException(form.getCode());
		}

		LocalDateTime now = LocalDateTime.now();
		book.setCode(form.getCode());
		book.setLanguage(form.getLanguage());
		book.setTitle(form.getTitle());

		book.getAuthors().clear();
		for (long authorId : form.getAuthorIds()) {
			book.getAuthors().add(entityManager.getReference(Author.class, authorId));
		}

		Publisher publisher = null;
		if (form.getPublisherId() != null) {
			publisher = entityManager.getReference(Publisher.class, Long.parseLong(form.getPublisherId()));
		}
		book.setPublisher(publisher);
		book.setDescription(form.getDescription());
		book.setIsbn(form.getIsbn());

		book.setUpdatedAt(now);
		book.setUpdatedBy(updatedBy.toString());

		return bookRepository.saveAndFlush(book);
	}

	public Book deleteBook(BookForm form, AuthorizedUser deletedBy) {
		Book book = bookRepository.findOneForUpdateById(form.getId());
		if (book == null) {
			throw new ServiceException();
		}
		if (!deletedBy.getRoles().contains(User.Role.ADMIN)) {
			throw new ServiceException();
		}
		bookRepository.delete(book);
		return book;
	}

	public Book getBookById(long id) {
		return bookRepository.findOneById(id);
	}

	public List<Long> getBookIds(BookForm form) {
		return bookRepository.searchForId(form);
	}

	public Page<Book> getBooks(BookForm form) {
		return bookRepository.search(form);
	}

	public Page<Book> getBooks(BookForm form, Pageable pageable) {
		return bookRepository.search(form, pageable);
	}

	public boolean isFormBlank(BookForm form) {
		boolean isCodeEdited = form.getCode() != null && !form.getCode().isEmpty();
		boolean isTitleEdited = form.getTitle() != null && !form.getTitle().isEmpty();
		boolean isAuthorIdsEdited = form.getAuthorIds() != null && !form.getAuthorIds().isEmpty();
		boolean isPublisherEdited = form.getPublisherId() != null && !form.getPublisherId().isEmpty();
		boolean isDescriptionEdited = form.getDescription() != null && !form.getDescription().isEmpty();
		boolean isIsbnEdited = form.getIsbn() != null && !form.getIsbn().isEmpty();
		boolean isKeywordEdited = form.getKeyword() != null && !form.getKeyword().isEmpty();
		return !(isCodeEdited || isTitleEdited || isAuthorIdsEdited || isPublisherEdited
				|| isIsbnEdited || isDescriptionEdited || isKeywordEdited);
	}

	public void searchAndSetPagination(
			BookForm form,
			String language,
			Pageable pageable,
			String searchUrl,
			Model model) {
		Page<Book> books = getBooks(form.withLanguage(language), pageable);
		model.addAttribute("books", books);
		model.addAttribute("pagination", new Pagination<>(books, searchUrl));
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
