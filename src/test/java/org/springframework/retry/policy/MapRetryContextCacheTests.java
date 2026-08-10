/*
 * Copyright 2006-2007 the original author or authors.
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

package org.springframework.retry.policy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;

import org.springframework.retry.context.RetryContextSupport;

public class MapRetryContextCacheTests {

	MapRetryContextCache cache = new MapRetryContextCache();

	@Test
	public void testPut() {
		RetryContextSupport context = new RetryContextSupport(null);
		cache.put("foo", context);
		assertEquals(context, cache.get("foo"));
	}

	@Test
	public void testPutExistingKeyAtCapacity() {
		RetryContextSupport first = new RetryContextSupport(null);
		RetryContextSupport replacement = new RetryContextSupport(null);
		cache.setCapacity(1);
		cache.put("foo", first);
		cache.put("foo", replacement);
		assertSame(replacement, cache.get("foo"));
	}

	@Test
	public void testLeastRecentlyUsedEntryIsEvicted() {
		MapRetryContextCache cache = new MapRetryContextCache(2);
		cache.put("first", new RetryContextSupport(null));
		cache.put("second", new RetryContextSupport(null));

		cache.get("first");
		cache.put("third", new RetryContextSupport(null));

		assertTrue(cache.containsKey("first"));
		assertFalse(cache.containsKey("second"));
		assertTrue(cache.containsKey("third"));
	}

	@Test
	public void testStrictCapacityRejectsOnlyNewKeys() {
		MapRetryContextCache cache = strictCache(1);
		RetryContextSupport first = new RetryContextSupport(null);
		RetryContextSupport replacement = new RetryContextSupport(null);
		cache.put("first", first);
		cache.put("first", replacement);

		try {
			cache.put("second", new RetryContextSupport(null));
			fail("Expected RetryCacheCapacityExceededException");
		}
		catch (RetryCacheCapacityExceededException ex) {
			assertSame(replacement, cache.get("first"));
			assertFalse(cache.containsKey("second"));
		}
	}

	@Test
	public void testEvictionWarningDoesNotContainTheKey() {
		final List<String> messages = Collections.synchronizedList(new ArrayList<String>());
		Logger logger = (Logger) LogManager.getLogger("org.springframework.retry.policy.AbstractMapRetryContextCache");
		Level originalLevel = logger.getLevel();
		AbstractAppender appender = new AbstractAppender("cache-test", null, PatternLayout.createDefaultLayout(), false,
				null) {
			@Override
			public void append(LogEvent event) {
				messages.add(event.getMessage().getFormattedMessage());
			}
		};
		appender.start();
		logger.addAppender(appender);
		logger.setLevel(Level.WARN);
		try {
			MapRetryContextCache cache = new MapRetryContextCache(1);
			cache.put("first", new RetryContextSupport(null));
			cache.put("secret-business-key", new RetryContextSupport(null));
			assertEquals(1, messages.size());
			assertFalse(messages.get(0).contains("secret-business-key"));
		}
		finally {
			logger.removeAppender(appender);
			logger.setLevel(originalLevel);
			appender.stop();
		}
	}

	@Test
	public void testStrictCapacityIsAtomicForConcurrentNewKeys() throws Exception {
		final int capacity = 16;
		final int threadCount = 64;
		final MapRetryContextCache cache = strictCache(capacity);
		final CountDownLatch start = new CountDownLatch(1);
		final CountDownLatch done = new CountDownLatch(threadCount);
		final AtomicInteger successful = new AtomicInteger();
		final AtomicInteger capacityFailures = new AtomicInteger();
		final List<Throwable> unexpected = Collections.synchronizedList(new ArrayList<Throwable>());

		for (int i = 0; i < threadCount; i++) {
			final int key = i;
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						start.await();
						cache.put(Integer.valueOf(key), new RetryContextSupport(null));
						successful.incrementAndGet();
					}
					catch (RetryCacheCapacityExceededException ex) {
						capacityFailures.incrementAndGet();
					}
					catch (Throwable ex) {
						unexpected.add(ex);
					}
					finally {
						done.countDown();
					}
				}
			}, "retry-cache-writer-" + i);
			thread.start();
		}

		start.countDown();
		done.await();
		assertTrue(unexpected.toString(), unexpected.isEmpty());
		assertEquals(capacity, successful.get());
		assertEquals(threadCount - capacity, capacityFailures.get());
		int stored = 0;
		for (int i = 0; i < threadCount; i++) {
			if (cache.containsKey(Integer.valueOf(i))) {
				stored++;
			}
		}
		assertEquals(capacity, stored);
	}

	@Test
	public void testExistingKeyCanBeUpdatedConcurrentlyAtCapacity() throws Exception {
		final int threadCount = 32;
		final MapRetryContextCache cache = strictCache(1);
		final CountDownLatch start = new CountDownLatch(1);
		final CountDownLatch done = new CountDownLatch(threadCount);
		final List<Throwable> failures = Collections.synchronizedList(new ArrayList<Throwable>());
		cache.put("shared", new RetryContextSupport(null));

		for (int i = 0; i < threadCount; i++) {
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						start.await();
						cache.put("shared", new RetryContextSupport(null));
					}
					catch (Throwable ex) {
						failures.add(ex);
					}
					finally {
						done.countDown();
					}
				}
			}, "retry-cache-updater-" + i);
			thread.start();
		}

		start.countDown();
		done.await();
		assertTrue(failures.toString(), failures.isEmpty());
		assertTrue(cache.containsKey("shared"));
	}

	@Test
	public void testRemove() {
		assertFalse(cache.containsKey("foo"));
		RetryContextSupport context = new RetryContextSupport(null);
		cache.put("foo", context);
		assertTrue(cache.containsKey("foo"));
		cache.remove("foo");
		assertFalse(cache.containsKey("foo"));
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
