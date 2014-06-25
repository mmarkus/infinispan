package org.infinispan.container.entries.immutable;

import org.infinispan.commons.util.Immutables;
import org.infinispan.container.DataContainer;
import org.infinispan.container.entries.CacheEntry;
import org.infinispan.container.entries.InternalCacheEntry;
import org.infinispan.container.entries.InternalCacheValue;
import org.infinispan.metadata.Metadata;
import org.infinispan.util.CoreImmutables;

import static org.infinispan.commons.util.Util.toStr;

/**
 * Immutable version of InternalCacheEntry for traversing data containers.
 */
public class ImmutableInternalCacheEntry<K, V> extends  ImmutableCacheEntry<K,V> implements InternalCacheEntry<K, V>, Immutables.Immutable {
   private final InternalCacheEntry<K, V> entry;
   private final int hash;

   public ImmutableInternalCacheEntry(InternalCacheEntry<K, V> entry) {
      this.entry = entry;
      this.hash = entry.hashCode();
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
   public long getExpiryTime() {
      return entry.getExpiryTime();
   }

   @Override
   public long getLastUsed() {
      return entry.getLastUsed();
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
   public void touch() {
      throw new UnsupportedOperationException();
   }

   @Override
   public void touch(long currentTimeMillis) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void reincarnate() {
      throw new UnsupportedOperationException();
   }

   @Override
   public void reincarnate(long now) {
      throw new UnsupportedOperationException();
   }

   @Override
   public InternalCacheValue<V> toInternalCacheValue() {
      return new ImmutableInternalCacheValue(this);
   }


   @Override
   @SuppressWarnings("unchecked")
   public boolean equals(Object o) {
      if (!(o instanceof InternalCacheEntry))
         return false;

      InternalCacheEntry entry = (InternalCacheEntry) o;
      return entry.equals(this.entry);
   }

   @Override
   public int hashCode() {
      return hash;
   }


   @Override
   public InternalCacheEntry clone() {
      return new ImmutableInternalCacheEntry(entry.clone());
   }

   @Override
   public CacheEntry<K, V> immutableCopy() {
      return this;
   }

   @Override
   CacheEntry entry() {
      return entry;
   }
}
