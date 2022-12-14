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

package org.wallride.author.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.wallride.domain.Author;

import javax.persistence.LockModeType;

@Repository
@Transactional
public interface AuthorRepository extends JpaRepository<Author, Long>, AuthorRepositoryCustom {

	@EntityGraph(value = Author.DEEP_GRAPH_NAME, type = EntityGraph.EntityGraphType.FETCH)
	Author findOneById(Long id);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Author findOneForUpdateById(Long id);

	@EntityGraph(value = Author.DEEP_GRAPH_NAME, type = EntityGraph.EntityGraphType.FETCH)
	Author findOneByCodeAndLanguage(String code, String language);
}
