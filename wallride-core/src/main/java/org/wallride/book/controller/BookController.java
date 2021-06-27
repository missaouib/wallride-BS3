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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wallride.domain.Book;
import org.wallride.exception.DuplicateCodeException;
import org.wallride.exception.EmptyCodeException;
import org.wallride.book.service.BookService;
import org.wallride.support.Actions;
import org.wallride.support.AuthorizedUser;
import org.wallride.support.CustomResourceBundleControl;
import org.wallride.support.ReportUtils;
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
@RequestMapping("/{language}/book")
@SessionAttributes(names = {"books", "pageable", "pagination", "searchUrl"})
public class BookController {

	@Inject
	private BookService bookService;

	@Inject
	private MessageSourceAccessor messageSourceAccessor;
	
	@PersistenceContext
	private EntityManager entityManager;

	private static Logger logger = LoggerFactory.getLogger(BookController.class);

	public static final int ITEMS_PER_PAGE = 3;
	
	@ModelAttribute("form")
	public BookForm form(ModelMap modelMap) {
		// Cover the case where form validation fails and the user is redirected back.
		// Source is https://web.archive.org/web/20160606223639/https://gerrydevstory.com/2013/07/11/preserving-validation-error-messages-on-spring-mvc-form-post-redirect-get/
		BookForm form = (BookForm) modelMap.get("form");
		if (form == null) {
			form = new BookForm();
		}
		return form;
	}

	@ModelAttribute("logger")
	public Logger logger() {
		return logger;
	}

	@ExceptionHandler(BindException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public @ResponseBody RestValidationErrorModel bindException(BindException e) {
		logger.debug("BindException", e);
		return RestValidationErrorModel.fromBindingResult(e.getBindingResult(), messageSourceAccessor);
	}

	@RequestMapping(method = RequestMethod.GET)
	public String root() {
		return "redirect:/_admin/{language}/book/search";
	}

	@RequestMapping(value = "/search", method = {RequestMethod.GET, RequestMethod.POST})
	public String search(
			@PathVariable String language,
			@ModelAttribute("form") BookForm form,
			BindingResult result,
			Model model,
			@PageableDefault(ITEMS_PER_PAGE) Pageable pageable,
			HttpServletRequest servletRequest)
			throws UnsupportedEncodingException {
		String searchUrl = bookService.buildSearchUrl(servletRequest);
		model.addAttribute("searchUrl", searchUrl);

		bookService.searchAndSetPagination(form, language, pageable, searchUrl, model);
		model.addAttribute("pageable", pageable);

		if (form.getId() != null) {
			Book selectedBook = bookService.getBookById(form.getId());
			model.addAttribute("selectedBook", selectedBook);
		}

		Set<String> buttons = new HashSet<>();
		buttons.add(Actions.ADD.toString());
		buttons.add(Actions.EDIT.toString());
		buttons.add(Actions.SHOW.toString());
		model.addAttribute("buttons", buttons);

		logger.info("search form.getKeyword() = {}", form.getKeyword());

		return "book/index";
	}

	@RequestMapping(value = "/show", method = {RequestMethod.GET, RequestMethod.POST})
	public String show(
			@PathVariable String language,
			@RequestParam long id,
			@ModelAttribute("form") BookForm form,
			BindingResult result,
			@ModelAttribute("books") Page<Book> books,
			@ModelAttribute("searchUrl") String searchUrl,
			Model model) {
		Set<String> buttons = new HashSet<>();
		buttons.add(Actions.ADD.toString());
		buttons.add(Actions.EDIT.toString());
		buttons.add(Actions.SHOW.toString());
		model.addAttribute("buttons", buttons);

		logger.info("show form.getKeyword() = {}", form.getKeyword());

		if (result.hasErrors()) {
			return "book/index";
		}
		
		Book book = bookService.getBookById(id);
		model.addAttribute("selectedBook", book);
		model.addAttribute("pagination", new Pagination<>(books, bookService.buildSearchUrl(searchUrl)));

		BookForm editForm = BookForm.createFormFromDomainObject(book);
		model.addAttribute("form", editForm.withKeyword(form.getKeyword()));
		return "book/index";
	}

	@RequestMapping(value = "/create", method = RequestMethod.POST)
	public String create(
			@PathVariable String language,
			@ModelAttribute("form") BookForm form,
			Model model)
			throws BindException {
		Set<String> buttons = new HashSet<>();
		buttons.add(Actions.SAVE.toString());
		buttons.add(Actions.CANCEL.toString());
		model.addAttribute("buttons", buttons);

		BookForm newForm = form;
		if (bookService.isFormBlank(form)) newForm = new BookForm();
		model.addAttribute("form", newForm);

		return "book/index";
	}

	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public String save(
			@PathVariable String language,
			@Validated(BookForm.CreateValidations.class) @ModelAttribute("form") BookForm form,
			BindingResult result,
			@ModelAttribute("pageable") Pageable pageable,
			@ModelAttribute("searchUrl") String searchUrl,
			Model model,
			AuthorizedUser authorizedUser,
			RedirectAttributes redirectAttributes) {
		Set<String> buttons = new HashSet<>();
		buttons.add(Actions.SAVE.toString());
		buttons.add(Actions.CANCEL.toString());
		model.addAttribute("buttons", buttons);

		if (result.hasErrors()) {
			return "book/index";
		}

		Book book = null;
		try {
			book = bookService.createBook(form, authorizedUser);
		}
		catch (EmptyCodeException e) {
			result.rejectValue("code", "NotNull");
		}
		catch (DuplicateCodeException e) {
			result.rejectValue("code", "NotDuplicate");
		}
		if (result.hasErrors()) {
			return "book/index";
		}

		redirectAttributes.addFlashAttribute("savedBook", book);
		redirectAttributes.addAttribute("language", language);
		redirectAttributes.addAttribute("id", book.getId());

		bookService.searchAndSetPagination(form, language, pageable,
				bookService.buildSearchUrl(searchUrl), model);
		return "redirect:/_admin/{language}/book/show";
	}

	@RequestMapping(value = "/edit", method = RequestMethod.POST)
	public String edit(
			@PathVariable String language,
			@ModelAttribute("form") BookForm form,
			Model model,
			RedirectAttributes redirectAttributes) {
		if (form.getId() == null) {
			redirectAttributes.addFlashAttribute("noBookSelected", true);
			return "redirect:/_admin/{language}/book/search";
		}
		Book book = bookService.getBookById(form.withLanguage(language).getId());
		model.addAttribute("selectedBook", book);

		Set<String> buttons = new HashSet<>();
		buttons.add(Actions.UPDATE.toString());
		buttons.add(Actions.DELETE.toString());
		buttons.add(Actions.CANCEL.toString());
		model.addAttribute("buttons", buttons);

		BookForm newForm = form;
		if (bookService.isFormBlank(form)) {
			newForm = BookForm.createFormFromDomainObject(book)
					.withKeyword(form.getKeyword());
		}
		model.addAttribute("form", newForm);

		return "book/index";
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	public String update(
			@PathVariable String language,
			@Validated(BookForm.UpdateValidations.class) @ModelAttribute("form") BookForm form,
			BindingResult result,
			@ModelAttribute("pagination") Pagination<Book> pagination,
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

		if (result.hasErrors()) {
			logger.debug("Errors: {}", result);
			Book book = bookService.getBookById(form.getId());
			model.addAttribute("selectedBook", book);
			return "book/index";
		}

		Book book = null;
		try {
			book = bookService.updateBook(form, authorizedUser);
		}
		catch (EmptyCodeException e) {
			result.rejectValue("code", "NotNull");
		}
		catch (DuplicateCodeException e) {
			result.rejectValue("code", "NotDuplicate");
		}
		if (result.hasErrors()) {
			logger.debug("Errors: {}", result);
			return "book/index";
		}
		model.addAttribute("selectedBook", book);

		redirectAttributes.addFlashAttribute("savedBook", book);
		redirectAttributes.addAttribute("language", language);
		redirectAttributes.addAttribute("id", book.getId());
		redirectAttributes.addAttribute("page", pagination.getCurrentPageNumber());

		bookService.searchAndSetPagination(form, language, pageable,
				bookService.buildSearchUrl(searchUrl), model);
		return "redirect:/_admin/{language}/book/show";
	}

	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	public String delete(
			@PathVariable String language,
			@ModelAttribute("form") BookForm form,
			BindingResult result,
			@RequestParam int page,
			@ModelAttribute("books") Page<Book> books,
			@ModelAttribute("pageable") Pageable pageable,
			@ModelAttribute("searchUrl") String searchUrl,
			Model model,
			AuthorizedUser authorizedUser,
			RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			logger.debug("Errors: {}", result);
			return "redirect:/_admin/{language}/book/search";
		}

		Book book = null;
		try {
			book = bookService.deleteBook(form, authorizedUser);
		}
		catch (ValidationException e) {
			if (result.hasErrors()) {
				logger.debug("Errors: {}", result);
				return "redirect:/_admin/{language}/book/search";
			}
			throw e;
		}

		List<String> errorMessages = null;
		if (result.hasErrors()) {
			errorMessages = new ArrayList<>();
			for (ObjectError error : result.getAllErrors()) {
				errorMessages.add(messageSourceAccessor.getMessage(error));
			}
		}

		redirectAttributes.addFlashAttribute("deletedBook", book);
		redirectAttributes.addFlashAttribute("errorMessages", errorMessages);
		redirectAttributes.addAttribute("page", page);

		if (books.getContent().size() > 1) {
			bookService.searchAndSetPagination(form, language, pageable,
					bookService.buildSearchUrl(searchUrl), model);
			books = (Page<Book>) model.asMap().get("books");
			Book firstBook = books.getContent().get(0);
			redirectAttributes.addAttribute("id", firstBook.getId());
			return "redirect:/_admin/{language}/book/show";
		}

		if (page > 0) {
			redirectAttributes.addAttribute("page", page - 1);
		}
		return "redirect:/_admin/{language}/book/search";
	}

	@RequestMapping(value = "/report", method = RequestMethod.POST)
	public ResponseEntity<byte[]> getReportPdf(
			@PathVariable String language,
			@ModelAttribute("form") BookForm form) {
		// Taken from http://blog.triadworks.com.br/jasperreports-gerando-relatorios-pdf-na-web
		final InputStream inStream = getClass().getResourceAsStream("/jasperreport/books.jrxml");
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
			List<Book> results = bookService.getBooks(form).getContent();
			JRDataSource beanDataSource = new JRBeanCollectionDataSource(results);
			jasperPrint = JasperFillManager.fillReport(jasper, params, beanDataSource);
		} catch (JRException e) {
			logger.warn("Could not fill the report from the data source. {}", e);
		}

		// Copied from https://stackoverflow.com/a/16656382
		String filename = "booksReport.pdf";
		HttpHeaders headers = ReportUtils.getHttpHeaders(filename);
		ResponseEntity<byte[]> response = ReportUtils.exportReportToPdf(jasperPrint, headers);
		return response;
	}
}