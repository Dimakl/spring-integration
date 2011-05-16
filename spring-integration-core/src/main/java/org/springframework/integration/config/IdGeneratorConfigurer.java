/*
 * Copyright 2002-2011 the original author or authors.
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

package org.springframework.integration.config;

import java.lang.reflect.Field;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.integration.MessageHeaders;
import org.springframework.integration.MessageHeaders.IdGenerator;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * @author Oleg Zhurakousky
 * @since 2.0.4
 */
public final class IdGeneratorConfigurer implements ApplicationListener<ApplicationContextEvent>{

	private final Log logger = LogFactory.getLog(getClass());
	
	private volatile String generatorContextId;


	public void onApplicationEvent(ApplicationContextEvent event) {
		if (event instanceof ContextRefreshedEvent){
			if (!StringUtils.hasText(generatorContextId)){
				ApplicationContext contex = event.getApplicationContext();
				if (this.setIdGenerator(contex)){
					this.generatorContextId = contex.getId();
				}	
			}
		}
		else if (event instanceof ContextClosedEvent){
			ApplicationContext contex = event.getApplicationContext();
			if (contex.getId().equals(generatorContextId)){
				this.unsetIdGenerator();
				this.generatorContextId = null;
			}
		}
		
	}

	private boolean setIdGenerator(ApplicationContext context) {
		try {
			IdGenerator idGeneratorBean = context.getBean(IdGenerator.class);
			if (logger.isDebugEnabled()) {
				logger.debug("using custom MessageHeaders.IdGenerator [" + idGeneratorBean.getClass() + "]");
			}
			Field idGeneratorField = ReflectionUtils.findField(MessageHeaders.class, "idGenerator");
			ReflectionUtils.makeAccessible(idGeneratorField);

			if (logger.isInfoEnabled()) {
				logger.info("Message IDs will be generated using custom IdGenerator [" + idGeneratorBean.getClass() + "]");
			}
			ReflectionUtils.setField(idGeneratorField, null, idGeneratorBean);
		}
		catch (BeanDefinitionStoreException e) {
			// let this one propagate
			throw e;
		}
		catch (NoSuchBeanDefinitionException e) {
			// We will use the default.
			if (logger.isDebugEnabled()) {
				logger.debug("Unable to locate MessageHeaders.IdGenerator. Will use default: UUID.randomUUID()");
			}
			return false;
		}
		catch (IllegalStateException e) {
			if (logger.isWarnEnabled()) {
				logger.warn("Unexpected exception occurred while accessing idGenerator of MessageHeaders." +
						" Will use default: UUID.randomUUID()", e);
			}
			return false;
		}
		return true;
	}

	private void unsetIdGenerator() {
		try {
			Field idGeneratorField = ReflectionUtils.findField(MessageHeaders.class, "idGenerator");
			ReflectionUtils.makeAccessible(idGeneratorField);
			idGeneratorField.set(null, null);
		}
		catch (Exception e) {
			if (logger.isWarnEnabled()) {
				logger.warn("Unexpected exception occurred while accessing idGenerator of MessageHeaders.", e);
			}
		}
	}

}
