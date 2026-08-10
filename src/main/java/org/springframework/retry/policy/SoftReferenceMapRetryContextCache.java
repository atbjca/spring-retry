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

import java.lang.ref.SoftReference;
import java.util.Map;

import org.springframework.retry.RetryContext;

/**
 * Map-based implementation of {@link RetryContextCache}. The map backing the cache of
 * contexts is synchronized and its entries are soft-referenced, so may be garbage
 * collected under pressure.
 *
 * @see MapRetryContextCache for non-soft referenced version
 * @author Dave Syer
 */
public class SoftReferenceMapRetryContextCache extends AbstractMapRetryContextCache<SoftReference<RetryContext>> {

	/**
	 * Create a {@link SoftReferenceMapRetryContextCache} with default capacity.
	 */
	public SoftReferenceMapRetryContextCache() {
		this(DEFAULT_CAPACITY);
	}

	/**
	 * 使用指定容量创建缓存；满载时淘汰最久未访问的条目。
	 * @param capacity 缓存容量
	 */
	public SoftReferenceMapRetryContextCache(int capacity) {
		this(capacity, true);
	}

	/**
	 * 使用指定容量和满载策略创建缓存。
	 * @param capacity 缓存容量
	 * @param removeEldestEntries 满载时是否淘汰最久未访问的条目
	 * @since 1.3.5
	 */
	public SoftReferenceMapRetryContextCache(int capacity, boolean removeEldestEntries) {
		super(capacity, removeEldestEntries);
	}

	/**
	 * Public setter for the capacity. Prevents the cache from growing unboundedly if
	 * items that fail are misidentified and two references to an identical item actually
	 * do not have the same key. This can happen when users implement equals and hashCode
	 * based on mutable fields, for instance.
	 * @param capacity the capacity to set
	 */
	public void setCapacity(int capacity) {
		super.setCapacity(capacity);
	}

	@Override
	public boolean containsKey(Object key) {
		Map<Object, SoftReference<RetryContext>> map = getMap();
		synchronized (map) {
			SoftReference<RetryContext> reference = map.get(key);
			if (reference == null) {
				return false;
			}
			if (reference.get() == null) {
				// 软引用已被回收，及时移除只剩 key 的空条目。
				map.remove(key);
				return false;
			}
			return true;
		}
	}

	@Override
	protected SoftReference<RetryContext> toValue(RetryContext context) {
		return new SoftReference<RetryContext>(context);
	}

	@Override
	protected RetryContext fromValue(SoftReference<RetryContext> value) {
		return value.get();
	}

}
