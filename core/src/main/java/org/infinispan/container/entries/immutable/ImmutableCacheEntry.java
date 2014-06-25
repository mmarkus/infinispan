package org.infinispan.container.entries.immutable;

import org.infinispan.commons.util.Immutables;
import org.infinispan.container.DataContainer;
import org.infinispan.container.entries.CacheEntry;
import org.infinispan.metadata.Metadata;

import static org.infinispan.commons.util.Util.toStr;

public abstract class ImmutableCacheEntry<K,V> implements CacheEntry<K,V>, Immutables.Immutable {
   
   @Override
   public String toString() {
      return toStr(getKey()) + "=" + toStr(getValue());
   }

   abstract CacheEntry<K,V> entry();

   @Override
   public boolean undelete(boolean doUndelete) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void setMetadata(Metadata metadata) {
      throw new UnsupportedOperationException();
   }

   @Override
   public long getLifespan() {
      return entry().getLifespan();
   }

   @Override
   public long getMaxIdle() {
      return entry().getMaxIdle();
   }

   @Override
   public boolean skipLookup() {
      return false;
   }

   @Override
   public boolean isChanged() {
      return entry().isChanged();
   }

   @Override
   public boolean isCreated() {
      return entry().isCreated();
   }

   @Override
   public boolean isNull() {
      return entry().isNull();
   }

   @Override
   public boolean isRemoved() {
      return entry().isRemoved();
   }

   @Override
   public boolean isEvicted() {
      return entry().isEvicted();
   }

   @Override
   public boolean isValid() {
      return entry().isValid();
   }

   @Override
   public boolean isLoaded() {
      return entry().isLoaded();
   }

   @Override
   public void rollback() {
      throw new UnsupportedOperationException();
   }

   @Override
   public void setCreated(boolean created) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void setRemoved(boolean removed) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void setChanged(boolean changed) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void setEvicted(boolean evicted) {
      entry().setEvicted(evicted);
   }

   @Override
   public void setValid(boolean valid) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void setLoaded(boolean loaded) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void setSkipLookup(boolean skipLookup) {
      throw new UnsupportedOperationException();
   }

   @Override
   public Metadata getMetadata() {
      return entry().getMetadata();
   }

   @Override
   public K getKey() {
      return entry().getKey();
   }

   @Override
   public V getValue() {
      return entry().getValue();
   }

   @Override
   public V setValue(V value) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void commit(DataContainer container, Metadata metadata) {
      throw new UnsupportedOperationException();
   }

   @Override
   public CacheEntry clone() {
      try {
         return (CacheEntry) super.clone();
      } catch (CloneNotSupportedException e) {
         throw new AssertionError(e);
      }
   }

   @Override
   public CacheEntry<K, V> immutableCopy() {
      return this;
   }
}
