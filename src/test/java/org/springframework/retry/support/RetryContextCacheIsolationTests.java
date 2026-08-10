/*
 * Copyright 2006-2026 the original author or authors.
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
package org.springframework.retry.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.policy.CircuitBreakerRetryPolicy;
import org.springframework.retry.policy.MapRetryContextCache;
import org.springframework.retry.policy.SimpleRetryPolicy;

/**
 * CVE-2026-41710 的缓存耗尽与双缓存隔离回归测试。
 *
 * @author BJCA
 */
public class RetryContextCacheIsolationTests {

	@Test
	public void uniqueFailedKeysDoNotPermanentlyBlockStatefulRetry() throws Throwable {
		RetryTemplate template = new RetryTemplate();
		MapRetryContextCache cache = new MapRetryContextCache(2);
		template.setRetryContextCache(cache);
		template.setRetryPolicy(new SimpleRetryPolicy(2));

		executeAndFail(template, "first", null);
		executeAndFail(template, "second", null);
		executeAndFail(template, "third", null);

		assertFalse(cache.containsKey("first"));
		assertTrue(cache.containsKey("second"));
		assertTrue(cache.containsKey("third"));

		executeAndFail(template, "healthy", null);
		String result = template.execute(new RetryCallback<String, RuntimeException>() {
			@Override
			public String doWithRetry(RetryContext context) {
				return "ok";
			}
		}, new DefaultRetryState("healthy"));

		assertEquals("ok", result);
		assertFalse(cache.containsKey("healthy"));
	}

	@Test
	public void sameRawKeyIsIsolatedBetweenStatefulAndCircuitBreakerCaches() throws Throwable {
		MapRetryContextCache statefulCache = new MapRetryContextCache(2);
		MapRetryContextCache circuitBreakerCache = strictCache(2);
		RetryTemplate statefulTemplate = template(statefulCache, circuitBreakerCache, new SimpleRetryPolicy(2));
		RetryTemplate circuitBreakerTemplate = template(statefulCache, circuitBreakerCache,
				new CircuitBreakerRetryPolicy(new SimpleRetryPolicy(2)));
		AtomicReference<RetryContext> statefulContext = new AtomicReference<RetryContext>();
		AtomicReference<RetryContext> circuitBreakerContext = new AtomicReference<RetryContext>();

		executeAndFail(statefulTemplate, "same", statefulContext);
		executeAndFail(circuitBreakerTemplate, "same", circuitBreakerContext);

		assertSame(statefulContext.get(), statefulCache.get("same"));
		assertSame(circuitBreakerContext.get(), circuitBreakerCache.get("same"));
		assertNotSame(statefulContext.get(), circuitBreakerContext.get());

		String result = statefulTemplate.execute(new RetryCallback<String, RuntimeException>() {
			@Override
			public String doWithRetry(RetryContext context) {
				return "ok";
			}
		}, new DefaultRetryState("same"));

		assertEquals("ok", result);
		assertFalse(statefulCache.containsKey("same"));
		assertTrue(circuitBreakerCache.containsKey("same"));

		AtomicReference<RetryContext> reopenedCircuitBreakerContext = new AtomicReference<RetryContext>();
		executeAndFail(circuitBreakerTemplate, "same", reopenedCircuitBreakerContext);
		assertSame(circuitBreakerContext.get(), reopenedCircuitBreakerContext.get());
		assertSame(circuitBreakerContext.get(), circuitBreakerCache.get("same"));
	}

	private RetryTemplate template(MapRetryContextCache statefulCache, MapRetryContextCache circuitBreakerCache,
			org.springframework.retry.RetryPolicy policy) {
		RetryTemplate template = new RetryTemplate();
		template.setRetryContextCache(statefulCache);
		setCircuitBreakerRetryContextCache(template, circuitBreakerCache);
		template.setRetryPolicy(policy);
		return template;
	}

	private void executeAndFail(RetryTemplate template, Object key, final AtomicReference<RetryContext> contextHolder)
			throws Throwable {
		try {
			template.execute(new RetryCallback<Object, RuntimeException>() {
				@Override
				public Object doWithRetry(RetryContext context) {
					if (contextHolder != null) {
						contextHolder.set(context);
					}
					throw new RuntimeException("planned");
				}
			}, new DefaultRetryState(key));
			fail("Expected RuntimeException");
		}
		catch (RuntimeException ex) {
			assertEquals("planned", ex.getMessage());
		}
	}

	private void setCircuitBreakerRetryContextCache(RetryTemplate template, MapRetryContextCache cache) {
		try {
			Method method = RetryTemplate.class.getMethod("setCircuitBreakerRetryContextCache",
					org.springframework.retry.policy.RetryContextCache.class);
			method.invoke(template, cache);
		}
		catch (Exception ex) {
			throw new AssertionError("RetryTemplate must expose a circuit-breaker cache setter", ex);
		}
	}

	private MapRetryContextCache strictCache(int capacity) {
		try {
			return MapRetryContextCache.class.getConstructor(Integer.TYPE, Boolean.TYPE).newInstance(capacity, false);
		}
		catch (Exception ex) {
			throw new AssertionError("MapRetryContextCache must expose the strict-capacity constructor", ex);
		}
	}

}
