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

package org.wallride.web.controller.guest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.wallride.domain.Article;
import org.wallride.domain.BlogLanguage;
import org.wallride.service.ArticleService;
import org.wallride.web.controller.guest.article.ArticleSearchForm;
import org.wallride.web.support.Pagination;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/")
public class IndexController {

	@Inject
	private ArticleService articleService;

	@RequestMapping
	public String index(
			@PageableDefault(10) Pageable pageable,
			BlogLanguage blogLanguage,
			Model model,
			HttpServletRequest servletRequest) {
		ArticleSearchForm form = new ArticleSearchForm() {};
		form.setLanguage(blogLanguage.getLanguage());

		Page<Article> articles = articleService.getArticles(form.toArticleSearchRequest(), pageable);
		model.addAttribute("articles", articles);
		model.addAttribute("pageable", pageable);
		model.addAttribute("pagination", new Pagination<>(articles, servletRequest));
		return "index";
//
//
//		Blog blog = blogService.getBlogById(Blog.DEFAULT_ID);
//		String defaultLanguage = blog.getDefaultLanguage();
//		redirectAttributes.addAttribute("language", defaultLanguage);
//		return "redirect:/{language}/";
	}
}
