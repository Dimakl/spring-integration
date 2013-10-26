/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.integration.file.filters;


import java.io.File;

import org.springframework.integration.metadata.MetadataStore;

/**
 * @author Gary Russell
 * @since 3.0
 *
 */
public class FileSystemPersistentAcceptOnceFileListFilter extends AbstractPersistentAcceptOnceFileListFilter<File> {

	public FileSystemPersistentAcceptOnceFileListFilter(MetadataStore store, String prefix) {
		super(store, prefix);
	}

	@Override
	protected long modified(File file) {
		return file.lastModified();
	}

	@Override
	protected String fileName(File file) {
		return file.getAbsolutePath();
	}

}