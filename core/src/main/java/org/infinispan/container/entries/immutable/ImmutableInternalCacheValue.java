package org.infinispan.container.entries.immutable;

import org.infinispan.commons.util.Immutables;
import org.infinispan.container.entries.InternalCacheEntry;
import org.infinispan.container.entries.InternalCacheValue;
import org.infinispan.metadata.Metadata;

public class ImmutableInternalCacheValue<V> implements InternalCacheValue<V>, Immutables.Immutable {
   private final InternalCacheEntry<?, V> entry;

   public ImmutableInternalCacheValue(InternalCacheEntry<?, V> entry) {
      if (!(entry instanceof Immutables.Immutable)) throw new IllegalStateException("Immutable entry expected");
      this.entry = entry;
   }

   @Override
   public boolean canExpire() {
      return entry.canExpire();
   }

   @Override
   public long getCreated() {
      return entry.getCreated();
   }

   @Override
   public long getLastUsed() {
      return entry.getLastUsed();
   }

   @Override
   public long getLifespan() {
      return entry.getLifespan();
   }

   @Override
   public long getMaxIdle() {
      return entry.getMaxIdle();
   }

   @Override
   public V getValue() {
      return entry.getValue();
   }

   @Override
   public boolean isExpired(long now) {
      return entry.isExpired(now);
   }

   @Override
   public boolean isExpired() {
      return entry.isExpired();
   }

   @Override
   public <K> InternalCacheEntry<K, V> toInternalCacheEntry(K key) {
      return (InternalCacheEntry<K, V>)entry;
   }

   @Override
   public long getExpiryTime() {
      return entry.toInternalCacheValue().getExpiryTime();
   }

   @Override
   public Metadata getMetadata() {
      return entry.getMetadata();
   }
}
