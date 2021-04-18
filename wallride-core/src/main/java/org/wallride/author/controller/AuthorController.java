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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.wallride.author.service.AuthorService;
import org.wallride.domain.Author;
import org.wallride.exception.DuplicateCodeException;
import org.wallride.exception.EmptyCodeException;
import org.wallride.author.service.AuthorService.Actions;
import org.wallride.support.AuthorizedUser;
import org.wallride.support.CustomResourceBundleControl;
import org.wallride.support.ReportUtils;
import org.wallride.web.support.DomainObjectSavedModel;
import org.wallride.web.support.Pagination;
import org.wallride.web.support.RestValidationErrorModel;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
@RequestMapping("/{language}/author")
@SessionAttributes(names = {"authors", "pageable", "pagination", "searchUrl"})
public class AuthorController {

	@Inject
	private AuthorService authorService;

	@Inject
	private MessageSourceAccessor messageSourceAccessor;
	
	@PersistenceContext
	private EntityManager entityManager;

	private static Logger logger = LoggerFactory.getLogger(AuthorController.class);

	public static final int ITEMS_PER_PAGE = 3;
	
	@ModelAttribute("form")
	public AuthorForm form(ModelMap modelMap) {
		// Cover the case where form validation fails and the user is redirected back
		// Source is https://web.archive.org/web/20160606223639/https://gerrydevstory.com/2013/07/11/preserving-validation-error-messages-on-spring-mvc-form-post-redirect-get/
		AuthorForm form = (AuthorForm) modelMap.get("form");
		if (form == null) {
			form = new AuthorForm();
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
		return "redirect:/_admin/{language}/author/search";
	}

	@RequestMapping(value = "/search", method = {RequestMethod.GET, RequestMethod.POST})
	public String search(
			@PathVariable String language,
			@ModelAttribute("form") AuthorForm form,
			BindingResult result,
			Model model,
			@PageableDefault(ITEMS_PER_PAGE) Pageable pageable,
			HttpServletRequest servletRequest)
			throws UnsupportedEncodingException {
		String searchUrl = authorService.buildSearchUrl(servletRequest);
		model.addAttribute("searchUrl", searchUrl);

		authorService.searchAndSetPagination(form, language, pageable, searchUrl, model);
		model.addAttribute("pageable", pageable);

		if (form.getId() != null) {
			Author selectedAuthor = authorService.getAuthorById(form.getId());
			model.addAttribute("selectedAuthor", selectedAuthor);
		}

		Set<String> buttons = new HashSet<>();
		buttons.add(Actions.ADD.toString());
		buttons.add(Actions.EDIT.toString());
		buttons.add(Actions.SHOW.toString());
		model.addAttribute("buttons", buttons);

		return "author/index";
	}

	@ResponseBody
	@RequestMapping(value = "/list", method = RequestMethod.POST)
	public List<Author> list(
			@PathVariable String language,
			@RequestParam String keyword) {
		AuthorForm form = new AuthorForm()
				.withKeyword(keyword)
				.withLanguage(language);
		return authorService.getAuthors(form).getContent();
	}

	@ResponseBody
	@RequestMapping(value = "/get", method = RequestMethod.POST)
	public Author get(@RequestParam Long id) {
		return authorService.getAuthorById(id);
	}

	@RequestMapping(value="/authors", method=RequestMethod.POST)
	public @ResponseBody DomainObjectSavedModel<Long> authors(
			@Validated(AuthorForm.CreateValidations.class) AuthorForm form,
			BindingResult result,
			AuthorizedUser authorizedUser,
			HttpServletRequest request,
			HttpServletResponse response) throws BindException {
		if (result.hasErrors()) {
			throw new BindException(result);
		}

		Author author = null;
		try {
			author = authorService.createAuthor(form, authorizedUser);
		} catch (EmptyCodeException e) {
			result.rejectValue("code", "NotNull");
		}
		catch (DuplicateCodeException e) {
			result.rejectValue("code", "NotDuplicate");
		}
		if (result.hasErrors()) {
			throw new BindException(result);
		}

		FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
		flashMap.put("savedAuthor", author);
		RequestContextUtils.getFlashMapManager(request).saveOutputFlashMap(flashMap, request, response);
		return new DomainObjectSavedModel<>(author);
	}

	@RequestMapping(value = "/show", method = {RequestMethod.GET, RequestMethod.POST})
	public String show(
			@PathVariable String language,
			@RequestParam long id,
			@ModelAttribute("form") AuthorForm form,
			@ModelAttribute("authors") Page<Author> authors,
			@ModelAttribute("searchUrl") String searchUrl,
			BindingResult result,
			Model model) {
		Set<String> buttons = new HashSet<>();
		buttons.add(Actions.ADD.toString());
		buttons.add(Actions.EDIT.toString());
		buttons.add(Actions.SHOW.toString());
		model.addAttribute("buttons", buttons);
		
		Author author = authorService.getAuthorById(id);
		model.addAttribute("selectedAuthor", author);
		model.addAttribute("pagination", new Pagination<>(authors, authorService.buildSearchUrl(searchUrl)));

		AuthorForm editForm = AuthorForm.createFormFromDomainObject(author);
		model.addAttribute("form", editForm.withKeyword(form.getKeyword()));
		return "author/index";
	}

	@RequestMapping(value = "/create", method = RequestMethod.POST)
	public String create(
			@PathVariable String language,
			@ModelAttribute("form") AuthorForm form,
			BindingResult result,
			Model model)
			throws BindException {
		Set<String> buttons = new HashSet<>();
		buttons.add(Actions.SAVE.toString());
		buttons.add(Actions.CANCEL.toString());
		model.addAttribute("buttons", buttons);

		AuthorForm newForm = form;
		if (authorService.isFormBlank(form)) newForm = new AuthorForm();
		model.addAttribute("form", newForm);

		return "author/index";
	}

	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public String save(
			@PathVariable String language,
			@Validated(AuthorForm.CreateValidations.class) @ModelAttribute("form") AuthorForm form,
			BindingResult errors,
			@ModelAttribute("pageable") Pageable pageable,
			@ModelAttribute("searchUrl") String searchUrl,
			Model model,
			AuthorizedUser authorizedUser,
			RedirectAttributes redirectAttributes) {
		Set<String> buttons = new HashSet<>();
		buttons.add(Actions.SAVE.toString());
		buttons.add(Actions.CANCEL.toString());
		model.addAttribute("buttons", buttons);

		if (errors.hasErrors()) {
			return "author/index";
		}

		Author author = null;
		try {
			author = authorService.createAuthor(form, authorizedUser);
		}
		catch (EmptyCodeException e) {
			errors.rejectValue("code", "NotNull");
		}
		catch (DuplicateCodeException e) {
			errors.rejectValue("code", "NotDuplicate");
		}
		if (errors.hasErrors()) {
			return "author/index";
		}

		redirectAttributes.addFlashAttribute("savedAuthor", author);
		redirectAttributes.addAttribute("language", language);
		redirectAttributes.addAttribute("id", author.getId());

		authorService.searchAndSetPagination(form, language, pageable,
				authorService.buildSearchUrl(searchUrl), model);
		return "redirect:/_admin/{language}/author/show";
	}

	@RequestMapping(value = "/edit", method = RequestMethod.POST)
	public String edit(
			@PathVariable String language,
			@ModelAttribute("form") AuthorForm form,
			Model model,
			RedirectAttributes redirectAttributes) {
		if (form.getId() == null) {
			redirectAttributes.addFlashAttribute("noAuthorSelected", true);
			return "redirect:/_admin/{language}/author/search";
		}
		Author author = authorService.getAuthorById(form.withLanguage(language).getId());
		model.addAttribute("selectedAuthor", author);

		Set<String> buttons = new HashSet<>();
		buttons.add(Actions.UPDATE.toString());
		buttons.add(Actions.DELETE.toString());
		buttons.add(Actions.CANCEL.toString());
		model.addAttribute("buttons", buttons);

		AuthorForm newForm = form;
		if (authorService.isFormBlank(form)) {
			newForm = AuthorForm.createFormFromDomainObject(author)
					.withKeyword(form.getKeyword());
		}
		model.addAttribute("form", newForm);

		return "author/index";
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	public String update(
			@PathVariable String language,
			@Validated(AuthorForm.UpdateValidations.class) @ModelAttribute("form") AuthorForm form,
			BindingResult errors,
			@ModelAttribute("pagination") Pagination<Author> pagination,
			@ModelAttribute("pageable") Pageable pageable,
			@ModelAttribute("searchUrl") String searchUrl,
			Model model,
			AuthorizedUser authorizedUser,
			RedirectAttributes redirectAttributes) {
		Set<String> buttons = new HashSet<>();
		buttons.add(Actions.UPDATE.toString());
		buttons.add(Actions.DELETE.toString());
		buttons.add(Actions.CANCEL.toString());
		model.addAttribute("buttons", buttons);

		if (errors.hasErrors()) {
			logger.debug("Errors: {}", errors);
			Author author = authorService.getAuthorById(form.getId());
			model.addAttribute("selectedAuthor", author);
			return "author/index";
		}

		Author author = null;
		try {
			author = authorService.updateAuthor(form, authorizedUser);
		}
		catch (EmptyCodeException e) {
			errors.rejectValue("code", "NotNull");
		}
		catch (DuplicateCodeException e) {
			errors.rejectValue("code", "NotDuplicate");
		}
		if (errors.hasErrors()) {
			logger.debug("Errors: {}", errors);
			return "author/index";
		}
		model.addAttribute("selectedAuthor", author);

		redirectAttributes.addFlashAttribute("savedAuthor", author);
		redirectAttributes.addAttribute("language", language);
		redirectAttributes.addAttribute("id", author.getId());
		redirectAttributes.addAttribute("page", pagination.getCurrentPageNumber());

		authorService.searchAndSetPagination(form, language, pageable,
				authorService.buildSearchUrl(searchUrl), model);
		return "redirect:/_admin/{language}/author/show";
	}

	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	public String delete(
			@PathVariable String language,
			@ModelAttribute("form") AuthorForm form,
			BindingResult errors,
			@RequestParam int page,
			@ModelAttribute("authors") Page<Author> authors,
			@ModelAttribute("pageable") Pageable pageable,
			@ModelAttribute("searchUrl") String searchUrl,
			Model model,
			AuthorizedUser authorizedUser,
			RedirectAttributes redirectAttributes) {
		if (errors.hasErrors()) {
			logger.debug("Errors: {}", errors);
			return "redirect:/_admin/{language}/author/search";
		}

		Author author = null;
		try {
			author = authorService.deleteAuthor(form, authorizedUser);
		}
		catch (ValidationException e) {
			if (errors.hasErrors()) {
				logger.debug("Errors: {}", errors);
				return "redirect:/_admin/{language}/author/search";
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

		redirectAttributes.addFlashAttribute("deletedAuthor", author);
		redirectAttributes.addFlashAttribute("errorMessages", errorMessages);
		redirectAttributes.addAttribute("page", page);

		if (authors.getContent().size() > 1) {
			authorService.searchAndSetPagination(form, language, pageable,
					authorService.buildSearchUrl(searchUrl), model);
			authors = (Page<Author>) model.asMap().get("authors");
			Author firstAuthor = authors.getContent().get(0);
			redirectAttributes.addAttribute("id", firstAuthor.getId());
			return "redirect:/_admin/{language}/author/show";
		}

		if (page > 0) {
			redirectAttributes.addAttribute("page", page - 1);
		}
		return "redirect:/_admin/{language}/author/search";
	}

	@RequestMapping(value = "/report", method = RequestMethod.POST)
	public ResponseEntity<byte[]> getReportPdf(
			@PathVariable String language,
			@ModelAttribute("form") AuthorForm form) {
		// Taken from http://blog.triadworks.com.br/jasperreports-gerando-relatorios-pdf-na-web
		final InputStream inStream = getClass().getResourceAsStream("/jasperreport/authors.jrxml");
		JasperReport jasper = ReportUtils.compileReport(inStream);

		// Taken from https://stackoverflow.com/a/27532493
		Map<String, Object> params = new HashMap<>();

		// Used the second solution from https://stackoverflow.com/a/30562269
		// Since JDK 9 or below reads property files in ISO-8859-1 by default
		// according to http://www.javapractices.com/topic/TopicAction.do?Id=208,
		// set the encoding to UTF-8.
		Locale.setDefault(ReportUtils.getLocale(language));
		ResourceBundle bundle = ResourceBundle.getBundle("messages/messages", new CustomResourceBundleControl("UTF-8"));
		params.put("REPORT_RESOURCE_BUNDLE", bundle);

		JasperPrint jasperPrint = null;
		try {
			List<Author> results = authorService.getAuthors(form).getContent();
			JRDataSource beanDataSource = new JRBeanCollectionDataSource(results);
			jasperPrint = JasperFillManager.fillReport(jasper, params, beanDataSource);
		} catch (JRException e) {
			logger.warn("Could not fill the report from the data source. {}", e);
		}

		// Copied from https://stackoverflow.com/a/16656382
		String filename = "authorsReport.pdf";
		HttpHeaders headers = ReportUtils.getHttpHeaders(filename);
		ResponseEntity<byte[]> response = ReportUtils.exportReportToPdf(jasperPrint, headers);
		return response;
	}
}