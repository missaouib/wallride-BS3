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

package org.wallride.support;

import org.springframework.context.i18n.LocaleContextHolder;
import org.wallride.domain.Tag;
import org.wallride.service.TagService;

import java.util.List;

public class TagUtils {

	private TagService tagService;

	public TagUtils(TagService tagService) {
		this.tagService = tagService;
	}

	public List<Tag> getAllTags() {
		return tagService.getTags(LocaleContextHolder.getLocale().toString());
	}
}
