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

package org.springframework.retry.policy;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.retry.RetryContext;

/**
 * 基于内存 {@link Map} 的 {@link RetryContextCache} 公共实现，支持可配置容量和 LRU
 * 淘汰策略。普通有状态重试可以淘汰最久未访问的上下文；全局断路器状态则应使用 严格容量模式，避免静默丢失全局状态。
 *
 * @param <V> 缓存中实际保存的值类型
 * @author Stephane Nicoll
 * @author BJCA
 * @since 1.3.5
 */
public abstract class AbstractMapRetryContextCache<V> implements RetryContextCache {

	/**
	 * 默认最大容量。该值保持在较低水平，避免不稳定的 item key 意外填满缓存。
	 */
	public static final int DEFAULT_CAPACITY = 4096;

	private static final Log logger = LogFactory.getLog(AbstractMapRetryContextCache.class);

	private final Map<Object, V> map;

	private final boolean failIfFull;

	private int capacity;

	/**
	 * 使用指定容量和满载策略创建缓存。
	 * @param capacity 最大容量
	 * @param removeEldestEntries 满载时是否淘汰最久未访问的条目
	 */
	protected AbstractMapRetryContextCache(int capacity, boolean removeEldestEntries) {
		this.capacity = capacity;
		Map<Object, V> target;
		if (removeEldestEntries) {
			target = new LinkedHashMap<Object, V>(capacity, 0.75f, true) {
				private static final long serialVersionUID = 1L;

				@Override
				protected boolean removeEldestEntry(Map.Entry<Object, V> eldest) {
					boolean evict = size() > AbstractMapRetryContextCache.this.capacity;
					if (evict && logger.isWarnEnabled()) {
						logger.warn("Retry cache capacity limit breached. "
								+ "Do you need to re-consider the implementation of the key generator, "
								+ "or the equals and hashCode of the items that failed?");
					}
					return evict;
				}
			};
		}
		else {
			target = new HashMap<Object, V>();
		}
		this.map = Collections.synchronizedMap(target);
		this.failIfFull = !removeEldestEntries;
	}

	/**
	 * 向子类提供同步 map，以便完成软引用清理等复合操作。
	 * @return 当前缓存 map
	 */
	protected final Map<Object, V> getMap() {
		return this.map;
	}

	/**
	 * 更新缓存容量。容量修改与写入共享同一同步边界。
	 * @param capacity 新容量
	 */
	protected void setCapacity(int capacity) {
		synchronized (this.map) {
			this.capacity = capacity;
		}
	}

	@Override
	public boolean containsKey(Object key) {
		return this.map.containsKey(key);
	}

	@Override
	public RetryContext get(Object key) {
		V value = this.map.get(key);
		return value != null ? fromValue(value) : null;
	}

	@Override
	public void put(Object key, RetryContext context) {
		synchronized (this.map) {
			if (this.failIfFull && !this.map.containsKey(key) && this.map.size() >= this.capacity) {
				throw new RetryCacheCapacityExceededException("Cache capacity limit breached. "
						+ "Do you need to re-consider the implementation of the key generator, "
						+ "or the equals and hashCode of the items that failed?");
			}
			this.map.put(key, toValue(context));
		}
	}

	@Override
	public void remove(Object key) {
		this.map.remove(key);
	}

	/**
	 * 将重试上下文转换成具体缓存值。
	 * @param context 待保存的上下文
	 * @return 缓存值
	 */
	protected abstract V toValue(RetryContext context);

	/**
	 * 将具体缓存值还原为重试上下文。
	 * @param value 缓存值
	 * @return 重试上下文
	 */
	protected abstract RetryContext fromValue(V value);

}
