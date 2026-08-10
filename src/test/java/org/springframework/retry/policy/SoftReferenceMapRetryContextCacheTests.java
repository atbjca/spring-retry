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

import java.lang.ref.SoftReference;
import java.util.Map;

import org.junit.Test;

import org.springframework.retry.context.RetryContextSupport;
import org.springframework.test.util.ReflectionTestUtils;

public class SoftReferenceMapRetryContextCacheTests {

	SoftReferenceMapRetryContextCache cache = new SoftReferenceMapRetryContextCache();

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
		SoftReferenceMapRetryContextCache cache = new SoftReferenceMapRetryContextCache(2);
		RetryContextSupport first = new RetryContextSupport(null);
		RetryContextSupport second = new RetryContextSupport(null);
		RetryContextSupport third = new RetryContextSupport(null);
		cache.put("first", first);
		cache.put("second", second);

		cache.get("first");
		cache.put("third", third);

		assertTrue(cache.containsKey("first"));
		assertFalse(cache.containsKey("second"));
		assertTrue(cache.containsKey("third"));
	}

	@Test
	public void testStrictCapacityRejectsOnlyNewKeys() {
		SoftReferenceMapRetryContextCache cache = strictCache(1);
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
	@SuppressWarnings("unchecked")
	public void testCollectedReferenceIsRemovedFromTheCache() {
		RetryContextSupport context = new RetryContextSupport(null);
		cache.put("foo", context);
		Map<Object, SoftReference<org.springframework.retry.RetryContext>> map = (Map<Object, SoftReference<org.springframework.retry.RetryContext>>) ReflectionTestUtils
				.getField(cache, "map");
		map.get("foo").clear();

		assertFalse(cache.containsKey("foo"));
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

	private SoftReferenceMapRetryContextCache strictCache(int capacity) {
		try {
			return SoftReferenceMapRetryContextCache.class.getConstructor(Integer.TYPE, Boolean.TYPE)
					.newInstance(capacity, false);
		}
		catch (Exception ex) {
			throw new AssertionError("SoftReferenceMapRetryContextCache must expose the strict-capacity constructor",
					ex);
		}
	}

}
