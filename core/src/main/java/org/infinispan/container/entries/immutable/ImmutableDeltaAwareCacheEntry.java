package org.infinispan.container.entries.immutable;

import org.infinispan.commons.util.Immutables;
import org.infinispan.container.entries.CacheEntry;
import org.infinispan.container.entries.DeltaAwareCacheEntry;
import org.infinispan.container.entries.InternalCacheEntry;

public class ImmutableDeltaAwareCacheEntry<K,V> extends ImmutableCacheEntry<K,V> implements CacheEntry<K,V>, Immutables.Immutable {

   private final DeltaAwareCacheEntry entry;
   private final int hash;

   public ImmutableDeltaAwareCacheEntry(DeltaAwareCacheEntry entry) {
      this.entry = entry;
      this.hash = entry.hashCode();
   }

   @Override
   public boolean equals(Object o) {
      return (o instanceof CacheEntry) && o.equals(this);
   }

   @Override
   public int hashCode() {
      return hash;
   }

   @Override
   CacheEntry<K, V> entry() {
      return entry;
   }

}
