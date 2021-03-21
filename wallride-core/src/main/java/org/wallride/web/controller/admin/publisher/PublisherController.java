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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wallride.domain.Publisher;
import org.wallride.exception.DuplicateCodeException;
import org.wallride.exception.EmptyCodeException;
import org.wallride.support.AuthorizedUser;
import org.wallride.service.PublisherService;
import org.wallride.web.support.Pagination;
import org.wallride.web.support.RestValidationErrorModel;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ValidationException;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

@Controller
@RequestMapping("/{language}/publisher")
public class PublisherController {

	@Inject
	private PublisherService publisherService;
	@Inject
	private MessageSourceAccessor messageSourceAccessor;
	@PersistenceContext
	private EntityManager entityManager;

	private static Logger logger = LoggerFactory.getLogger(PublisherController.class);

	public static final int ITEMS_PER_PAGE = 10;
	
	public enum Actions {
		ADD,
		EDIT,
		SAVE,
		UPDATE,
		DELETE,
		SHOW,
		CANCEL;
	}

	@ModelAttribute("form")
	public PublisherForm form(Model model) {
		// Cover the case where form validation fails and the user is redirected back
		// Source is https://web.archive.org/web/20160606223639/https://gerrydevstory.com/2013/07/11/preserving-validation-error-messages-on-spring-mvc-form-post-redirect-get/
		PublisherForm form = (PublisherForm) model.asMap().get("form");
		if (form == null) {
			form = new PublisherForm();
		}
		return form;
	}

	@ExceptionHandler(BindException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public @ResponseBody RestValidationErrorModel bindException(BindException e) {
		logger.debug("BindException", e);
		return RestValidationErrorModel.fromBindingResult(e.getBindingResult(), messageSourceAccessor);
	}

	@RequestMapping(method = RequestMethod.GET)
	public String root() {
		return "redirect:/_admin/{language}/publisher/search";
	}

	@RequestMapping(value = "/search", method = {RequestMethod.GET, RequestMethod.POST})
	public String search(
			@PathVariable String language,
			@ModelAttribute("form") PublisherForm form,
			BindingResult result,
			@PageableDefault(ITEMS_PER_PAGE) Pageable pageable,
			Model model,
			HttpServletRequest servletRequest)
			throws UnsupportedEncodingException {
		form.setLanguage(language);
		Page<Publisher> publishers = publisherService.getPublishers(form, pageable);
		model.addAttribute("publishers", publishers);
		model.addAttribute("pageable", pageable);
		model.addAttribute("pagination", new Pagination<>(publishers, servletRequest));

		if (!publishers.isEmpty()) {
			Publisher selectedPublisher = publishers.getContent().get(0);
			model.addAttribute("selectedPublisher", selectedPublisher);
			PublisherForm editForm = PublisherForm.createFormfromDomainObject(selectedPublisher);
			editForm.setKeyword(form.getKeyword());
			model.addAttribute("form", editForm);
		}

		Set<String> buttons = new HashSet<>();
		buttons.add(Actions.ADD.toString());
		buttons.add(Actions.EDIT.toString());
		buttons.add(Actions.SHOW.toString());
		model.addAttribute("buttons", buttons);

		return "publisher/index";
	}

	@RequestMapping(value = "/show", method = {RequestMethod.GET, RequestMethod.POST})
	public String show(
			@PathVariable String language,
			@RequestParam long id,
			@ModelAttribute("form") PublisherForm form,
			BindingResult result,
			@PageableDefault(ITEMS_PER_PAGE) Pageable pageable,
			Model model,
			RedirectAttributes redirectAttributes,
			HttpServletRequest servletRequest) {
		form.setLanguage(language);
		Publisher publisher = publisherService.getPublisherById(id);
		model.addAttribute("selectedPublisher", publisher);

		Set<String> buttons = new HashSet<>();
		buttons.add(Actions.ADD.toString());
		buttons.add(Actions.EDIT.toString());
		buttons.add(Actions.SHOW.toString());
		model.addAttribute("buttons", buttons);

		if (!language.equals(publisher.getLanguage())) {
			redirectAttributes.addAttribute("language", language);
			return "redirect:/_admin/{language}/publisher/search";
		}

		PublisherForm editForm = PublisherForm.createFormfromDomainObject(publisher);
		editForm.setKeyword(form.getKeyword());
		model.addAttribute("form", editForm);

		Page<Publisher> publishers = publisherService.getPublishers(editForm, pageable);
		model.addAttribute("publishers", publishers);
		model.addAttribute("pageable", pageable);
		model.addAttribute("pagination", new Pagination<>(publishers, servletRequest));

		return "publisher/index";
	}

	@RequestMapping(value = "/create", method = RequestMethod.POST)
	public String create(
			@PathVariable String language,
			@ModelAttribute("form") PublisherForm form,
			BindingResult result,
			@PageableDefault(ITEMS_PER_PAGE) Pageable pageable,
			Model model,
			RedirectAttributes redirectAttributes,
			HttpServletRequest servletRequest)
			throws BindException {
		form.setLanguage(language);
		Page<Publisher> publishers = publisherService.getPublishers(form, pageable);
		model.addAttribute("publishers", publishers);
		model.addAttribute("pageable", pageable);
		model.addAttribute("pagination", new Pagination<>(publishers, servletRequest));

		String keyword = form.getKeyword();
		form = new PublisherForm();
		form.setKeyword(keyword);
		model.addAttribute("form", form);

		Set<String> buttons = new HashSet<>();
		buttons.add(Actions.SAVE.toString());
		buttons.add(Actions.CANCEL.toString());
		model.addAttribute("buttons", buttons);

		return "publisher/index";
	}

	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public String save(
			@PathVariable String language,
			@Validated(PublisherForm.CreateValidations.class) @ModelAttribute("form") PublisherForm form,
			BindingResult errors,
			@PageableDefault(ITEMS_PER_PAGE) Pageable pageable,
			AuthorizedUser authorizedUser,
			RedirectAttributes redirectAttributes,
			Model model,
			HttpServletRequest servletRequest) {
		form.setLanguage(language);
		Page<Publisher> publishers = publisherService.getPublishers(form, pageable);
		model.addAttribute("publishers", publishers);
		model.addAttribute("pageable", pageable);
		model.addAttribute("pagination", new Pagination<>(publishers, servletRequest));

		Set<String> buttons = new HashSet<>();
		buttons.add(Actions.SAVE.toString());
		buttons.add(Actions.CANCEL.toString());
		model.addAttribute("buttons", buttons);

		if (errors.hasErrors()) {
			return "publisher/index";
		}

		Publisher publisher = null;
		try {
			publisher = publisherService.createPublisher(form, authorizedUser);
		}
		catch (EmptyCodeException e) {
			errors.rejectValue("code", "NotNull");
		}
		catch (DuplicateCodeException e) {
			errors.rejectValue("code", "NotDuplicate");
		}
		if (errors.hasErrors()) {
			return "publisher/index";
		}

		redirectAttributes.addFlashAttribute("savedPublisher", publisher);
		redirectAttributes.addAttribute("language", language);
		redirectAttributes.addAttribute("id", publisher.getId());
		return "redirect:/_admin/{language}/publisher/show";
	}

	@RequestMapping(value = "/edit", method = RequestMethod.POST)
	public String edit(
			@PathVariable String language,
			@ModelAttribute("form") PublisherForm form,
			Model model,
			@PageableDefault(ITEMS_PER_PAGE) Pageable pageable,
			RedirectAttributes redirectAttributes,
			HttpServletRequest servletRequest) {
		form.setLanguage(language);
		Publisher publisher = publisherService.getPublisherById(form.getId());
		model.addAttribute("selectedPublisher", publisher);

		Set<String> buttons = new HashSet<>();
		buttons.add(Actions.UPDATE.toString());
		buttons.add(Actions.DELETE.toString());
		buttons.add(Actions.CANCEL.toString());
		model.addAttribute("buttons", buttons);

		if (!language.equals(publisher.getLanguage())) {
			redirectAttributes.addAttribute("language", language);
			return "redirect:/_admin/{language}/publisher/search";
		}

		PublisherForm editForm = PublisherForm.createFormfromDomainObject(publisher);
		editForm.setKeyword(form.getKeyword());
		model.addAttribute("form", editForm);

		Page<Publisher> publishers = publisherService.getPublishers(editForm, pageable);
		model.addAttribute("publishers", publishers);
		model.addAttribute("pageable", pageable);
		model.addAttribute("pagination", new Pagination<>(publishers, servletRequest));

		return "publisher/index";
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	public String update(
			@PathVariable String language,
			@Validated(PublisherForm.UpdateValidations.class) @ModelAttribute("form") PublisherForm form,
			BindingResult errors,
			Model model,
			@PageableDefault(ITEMS_PER_PAGE) Pageable pageable,
			AuthorizedUser authorizedUser,
			RedirectAttributes redirectAttributes,
			HttpServletRequest servletRequest) {
		form.setLanguage(language);
		Page<Publisher> publishers = publisherService.getPublishers(form, pageable);
		model.addAttribute("publishers", publishers);
		model.addAttribute("pageable", pageable);
		Pagination<Publisher> pagination = new Pagination<>(publishers, servletRequest);
		model.addAttribute("pagination", pagination);

		Set<String> buttons = new HashSet<>();
		buttons.add(Actions.UPDATE.toString());
		buttons.add(Actions.DELETE.toString());
		buttons.add(Actions.CANCEL.toString());
		model.addAttribute("buttons", buttons);

		if (errors.hasErrors()) {
			logger.debug("Errors: {}", errors);
			Publisher publisher = publisherService.getPublisherById(form.getId());
			model.addAttribute("selectedPublisher", publisher);
			return "publisher/index";
		}

		Publisher publisher = null;
		try {
			publisher = publisherService.updatePublisher(form, authorizedUser);
		}
		catch (EmptyCodeException e) {
			errors.rejectValue("code", "NotNull");
		}
		catch (DuplicateCodeException e) {
			errors.rejectValue("code", "NotDuplicate");
		}
		if (errors.hasErrors()) {
			logger.debug("Errors: {}", errors);
			return "publisher/index";
		}
		model.addAttribute("selectedPublisher", publisher);

		redirectAttributes.addFlashAttribute("savedPublisher", publisher);
		redirectAttributes.addAttribute("language", language);
		redirectAttributes.addAttribute("id", publisher.getId());
		redirectAttributes.addAttribute("page", pagination.getCurrentPageNumber());
		return "redirect:/_admin/{language}/publisher/show";
	}

	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	public String delete(
			@ModelAttribute("form") PublisherForm form,
			BindingResult errors,
			@RequestParam int page,
			Model model,
			AuthorizedUser authorizedUser,
			RedirectAttributes redirectAttributes) {
		if (errors.hasErrors()) {
			logger.debug("Errors: {}", errors);
			return "redirect:/_admin/{language}/publisher/search";
		}

		Publisher publisher = null;
		try {
			publisher = publisherService.deletePublisher(form, authorizedUser);
		}
		catch (ValidationException e) {
			if (errors.hasErrors()) {
				logger.debug("Errors: {}", errors);
				return "redirect:/_admin/{language}/publisher/search";
			}
			throw e;
		}

		List<String> errorMessages = null;
		if (errors.hasErrors()) {
			errorMessages = new ArrayList<>();
			for (ObjectError error : errors.getAllErrors()) {
				errorMessages.add(messageSourceAccessor.getMessage(error));
			}
		}

		redirectAttributes.addFlashAttribute("deletedPublisher", publisher);
		redirectAttributes.addFlashAttribute("errorMessages", errorMessages);
		redirectAttributes.addAttribute("page", page);
		return "redirect:/_admin/{language}/publisher/search";
	}

	@RequestMapping(value = "/report", method = RequestMethod.GET)
	public ResponseEntity<byte[]> getReportPdf(
			@PathVariable String language,
			HttpServletRequest request) {
		// Taken from http://blog.triadworks.com.br/jasperreports-gerando-relatorios-pdf-na-web
		final InputStream inStream = getClass().getResourceAsStream("/jasperreport/publishers.jrxml");
		JasperReport jasper = null;
		try {
			jasper = JasperCompileManager.compileReport(inStream);
		} catch (JRException e) {
			e.printStackTrace();
			logger.warn("Could not compile the report. {}", e);
		}

		// Taken from https://stackoverflow.com/a/27532493
		Map<String, Object> params = new HashMap<>();
		params.put("REPORT_RESOURCE_BUNDLE", ResourceBundle.getBundle("jasperreport/publishers", new Locale(language)));

		JasperPrint jasperPrint = null;
		try {
			// TODO move to service/repository
			TypedQuery<Publisher> query = entityManager.createQuery("FROM Publisher ORDER BY language, code", Publisher.class);
			List<Publisher> results = query.getResultList();
			JRDataSource beanDataSource = new JRBeanCollectionDataSource(results);
			jasperPrint = JasperFillManager.fillReport(jasper, params, beanDataSource);
		} catch (JRException e) {
			e.printStackTrace();
			logger.warn("Could not fill the report from the data source. {}", e);
		}
	
		// Copied from https://stackoverflow.com/a/16656382
		String filename = "publishersReport.pdf";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PDF);
		headers.setContentDispositionFormData(filename, filename);
		headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
	
		ResponseEntity<byte[]> response = null;
		try {
			byte[] report = JasperExportManager.exportReportToPdf(jasperPrint);
			response = new ResponseEntity<>(report, headers, HttpStatus.OK);
		} catch (JRException e) {
			e.printStackTrace();
			logger.warn("Could not export the report to the pdf stream. {}", e);
		}

		return response;
	}
}