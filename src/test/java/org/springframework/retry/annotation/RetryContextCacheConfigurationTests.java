/*
 * Copyright 2014-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.retry.annotation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import org.springframework.beans.DirectFieldAccessor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.context.RetryContextSupport;
import org.springframework.retry.policy.MapRetryContextCache;
import org.springframework.retry.policy.RetryCacheCapacityExceededException;
import org.springframework.retry.policy.RetryContextCache;

/**
 * 注解式普通重试与断路器缓存 Bean 路由回归测试。
 *
 * @author BJCA
 */
public class RetryContextCacheConfigurationTests {

	@Test
	public void namedCachesAreAppliedIndependently() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(NamedCaches.class);
		try {
			RetryContextCache stateful = context.getBean("retryContextCache", RetryContextCache.class);
			RetryContextCache circuitBreaker = context.getBean("circuitBreakerRetryContextCache",
					RetryContextCache.class);
			assertSame(stateful, adviceCache(context, "retryContextCache"));
			assertSame(circuitBreaker, adviceCache(context, "circuitBreakerRetryContextCache"));
			assertNotSame(stateful, circuitBreaker);
		}
		finally {
			context.close();
		}
	}

	@Test
	public void uniqueUnnamedCacheOnlyOverridesStatefulCache() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(UniqueUnnamedCache.class);
		try {
			RetryContextCache custom = context.getBean("customCache", RetryContextCache.class);
			RetryContextCache stateful = adviceCache(context, "retryContextCache");
			RetryContextCache circuitBreaker = adviceCache(context, "circuitBreakerRetryContextCache");
			assertSame(custom, stateful);
			assertNotSame(custom, circuitBreaker);
			assertStrict(circuitBreaker);
		}
		finally {
			context.close();
		}
	}

	@Test
	public void multipleUnnamedCachesDoNotCauseArbitrarySelection() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
				MultipleUnnamedCaches.class);
		try {
			RetryContextCache first = context.getBean("firstCache", RetryContextCache.class);
			RetryContextCache second = context.getBean("secondCache", RetryContextCache.class);
			RetryContextCache stateful = adviceCache(context, "retryContextCache");
			RetryContextCache circuitBreaker = adviceCache(context, "circuitBreakerRetryContextCache");
			assertNotSame(first, stateful);
			assertNotSame(second, stateful);
			assertNotSame(first, circuitBreaker);
			assertNotSame(second, circuitBreaker);
			assertNotSame(stateful, circuitBreaker);
		}
		finally {
			context.close();
		}
	}

	@Test
	public void defaultCachesUseLruAndStrictPolicies() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(NoCustomCache.class);
		try {
			MapRetryContextCache stateful = (MapRetryContextCache) adviceCache(context, "retryContextCache");
			MapRetryContextCache circuitBreaker = (MapRetryContextCache) adviceCache(context,
					"circuitBreakerRetryContextCache");
			stateful.setCapacity(1);
			stateful.put("first", new RetryContextSupport(null));
			stateful.put("second", new RetryContextSupport(null));
			assertFalse(stateful.containsKey("first"));
			assertTrue(stateful.containsKey("second"));
			assertStrict(circuitBreaker);
		}
		finally {
			context.close();
		}
	}

	private RetryContextCache adviceCache(AnnotationConfigApplicationContext context, String field) {
		RetryConfiguration configuration = context.getBean(RetryConfiguration.class);
		return (RetryContextCache) new DirectFieldAccessor(configuration).getPropertyValue("advice." + field);
	}

	private void assertStrict(RetryContextCache cache) {
		MapRetryContextCache mapCache = (MapRetryContextCache) cache;
		mapCache.setCapacity(1);
		mapCache.put("first", new RetryContextSupport(null));
		try {
			mapCache.put("second", new RetryContextSupport(null));
			fail("Expected RetryCacheCapacityExceededException");
		}
		catch (RetryCacheCapacityExceededException ex) {
			assertTrue(mapCache.containsKey("first"));
			assertFalse(mapCache.containsKey("second"));
		}
	}

	@Configuration
	@EnableRetry
	static class NamedCaches {

		@Bean
		RetryContextCache retryContextCache() {
			return new MapRetryContextCache();
		}

		@Bean
		RetryContextCache circuitBreakerRetryContextCache() {
			return new MapRetryContextCache();
		}

	}

	@Configuration
	@EnableRetry
	static class UniqueUnnamedCache {

		@Bean
		RetryContextCache customCache() {
			return new MapRetryContextCache();
		}

	}

	@Configuration
	@EnableRetry
	static class MultipleUnnamedCaches {

		@Bean
		RetryContextCache firstCache() {
			return new MapRetryContextCache();
		}

		@Bean
		RetryContextCache secondCache() {
			return new MapRetryContextCache();
		}

	}

	@Configuration
	@EnableRetry
	static class NoCustomCache {

	}

}
