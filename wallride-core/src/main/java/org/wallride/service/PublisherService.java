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

package org.wallride.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wallride.domain.Publisher;
import org.wallride.domain.User;
import org.wallride.exception.DuplicateCodeException;
import org.wallride.exception.EmptyCodeException;
import org.wallride.exception.ServiceException;
import org.wallride.repository.PublisherRepository;
import org.wallride.support.AuthorizedUser;
import org.wallride.web.controller.admin.publisher.PublisherController;
import org.wallride.web.controller.admin.publisher.PublisherForm;

import javax.annotation.Resource;

import java.time.LocalDateTime;

@Service
@Transactional(rollbackFor = Exception.class)
public class PublisherService {

	@Resource
	private PublisherRepository publisherRepository;

	public enum Actions {
		ADD,
		EDIT,
		SAVE,
		UPDATE,
		DELETE,
		SHOW,
		CANCEL;
	}

	public Publisher createPublisher(PublisherForm form, AuthorizedUser createdBy) {
		if (form.getCode() == null) {
			throw new EmptyCodeException();
		}
		Publisher duplicate = publisherRepository.findOneByCodeAndLanguage(form.getCode(), form.getLanguage());
		if (duplicate != null) {
			throw new DuplicateCodeException(form.getCode());
		}

		LocalDateTime now = LocalDateTime.now();
		Publisher publisher = new Publisher();
		publisher.setCode(form.getCode());
		publisher.setName(form.getName());
		publisher.setCountry(form.getCountry());
		publisher.setWebpage(form.getWebpage());
		publisher.setNotes(form.getNotes());
		publisher.setLanguage(form.getLanguage());

		publisher.setCreatedAt(now);
		publisher.setCreatedBy(createdBy.toString());
		publisher.setUpdatedAt(now);
		publisher.setUpdatedBy(createdBy.toString());

		return publisherRepository.saveAndFlush(publisher);
	}

	public Publisher updatePublisher(PublisherForm form, AuthorizedUser updatedBy) {
		Publisher publisher = publisherRepository.findOneForUpdateById(form.getId());
		if (publisher == null) {
			throw new ServiceException();
		}
		if (!updatedBy.getRoles().contains(User.Role.ADMIN)) {
			throw new ServiceException();
		}
		
		if (form.getCode() == null) {
			throw new EmptyCodeException();
		}
		Publisher duplicate = publisherRepository.findOneByCodeAndLanguage(form.getCode(), form.getLanguage());
		if (duplicate != null && !duplicate.equals(publisher)) {
			throw new DuplicateCodeException(form.getCode());
		}

		LocalDateTime now = LocalDateTime.now();
		publisher.setCode(form.getCode());
		publisher.setName(form.getName());
		publisher.setCountry(form.getCountry());
		publisher.setWebpage(form.getWebpage());
		publisher.setNotes(form.getNotes());
		publisher.setLanguage(form.getLanguage());

		publisher.setUpdatedAt(now);
		publisher.setUpdatedBy(updatedBy.toString());

		return publisherRepository.saveAndFlush(publisher);
	}

	public Publisher deletePublisher(PublisherForm form, AuthorizedUser deletedBy) {
		Publisher publisher = publisherRepository.findOneForUpdateById(form.getId());
		if (publisher == null) {
			throw new ServiceException();
		}
		if (!deletedBy.getRoles().contains(User.Role.ADMIN)) {
			throw new ServiceException();
		}
		publisherRepository.delete(publisher);
		return publisher;
	}

	public Publisher getPublisherById(long id) {
		return publisherRepository.findOneById(id);
	}

	public Page<Publisher> getPublishers(PublisherForm form) {
		Pageable pageable = PageRequest.of(0, PublisherController.ITEMS_PER_PAGE);
		return getPublishers(form, pageable);
	}

	public Page<Publisher> getPublishers(PublisherForm form, Pageable pageable) {
		return publisherRepository.search(form, pageable);
	}

	public boolean isFormNotBlank(PublisherForm form) {
		boolean isCodeEdited = form.getCode() != null && !form.getCode().isEmpty();
		boolean isNameEdited = form.getName() != null && !form.getName().isEmpty();
		boolean isCountryEdited = form.getCountry() != null && !form.getCountry().isEmpty();
		boolean isWebpageEdited = form.getWebpage() != null && !form.getWebpage().isEmpty();
		boolean isNotesEdited = form.getNotes() != null && !form.getNotes().isEmpty();
		boolean isKeywordEdited = form.getKeyword() != null && !form.getKeyword().isEmpty();
		if (isCodeEdited ||	isNameEdited || isCountryEdited || isWebpageEdited || isNotesEdited || isKeywordEdited) {
			return true;
		}
		return false;
	}
}
