package org.infinispan.util;

import static org.infinispan.commons.util.Util.toStr;

import org.infinispan.commons.util.Immutables;
import org.infinispan.container.DataContainer;
import org.infinispan.container.entries.CacheEntry;
import org.infinispan.container.entries.InternalCacheEntry;
import org.infinispan.container.entries.InternalCacheValue;
import org.infinispan.metadata.Metadata;

/**
 * Factory for generating immutable type wrappers for core types.
 *
 * @author Jason T. Greene
 * @author Galder Zamarreño
 * @author Tristan Tarrant
 * @since 4.0
 */
public class CoreImmutables extends Immutables {

   /**
    * Wraps a {@link InternalCacheEntry}} with an immutable {@link InternalCacheEntry}}. There is no copying involved.
    *
    * @param entry the internal cache entry to wrap.
    * @return an immutable {@link InternalCacheEntry}} wrapper that delegates to the original entry.
    */
   public static <K, V> InternalCacheEntry<K, V> immutableInternalCacheEntry(InternalCacheEntry<K, V> entry) {
      return (InternalCacheEntry<K, V>) entry.immutableCopy();
   }

   /**
    * TODO...
    *
    * Creates an immutable version of cache entry whose attributes are set on
    * construction. This is a true shallow copy because referencing the
    * {@link CacheEntry} could lead to changes in the attributes over time,
    * due to its mutability.
    *
    * @param entry the cache entry to provide immutability view for
    * @return an immutable {@link CacheEntry}} wrapper contains the values of
    * the wrapped cache entry at construction time.
    */
   public static <K, V> CacheEntry<K, V> cacheEntryCopy(CacheEntry<K, V> entry) {
      return entry.immutableCopy();
   }

}
