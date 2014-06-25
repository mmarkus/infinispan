package org.infinispan.container.entries.immutable;

import org.infinispan.commons.util.Immutables;
import org.infinispan.container.DataContainer;
import org.infinispan.container.entries.CacheEntry;
import org.infinispan.container.entries.MVCCEntry;
import org.infinispan.container.entries.StateChangingEntry;

public class ImmutableMVCCEntry<K, V> extends ImmutableCacheEntry<K,V> implements MVCCEntry<K, V>, Immutables.Immutable {
   private final MVCCEntry<K, V> entry;
   private final int hash;

   public ImmutableMVCCEntry(MVCCEntry<K, V> entry) {
      this.entry = entry;
      this.hash = entry.hashCode();
   }

   @Override
   CacheEntry<K, V> entry() {
      return entry;
   }

   @Override
   public int hashCode() {
      return hash;
   }

   @Override
   public boolean equals(Object obj) {
      return obj instanceof MVCCEntry && obj.equals(this);
   }

   @Override
   public void copyForUpdate(DataContainer<? super K, ? super V> container) {
      throw new UnsupportedOperationException();
   }

   @Override
   public byte getStateFlags() {
      return entry.getStateFlags();
   }

   @Override
   public void copyStateFlagsFrom(StateChangingEntry other) {
      throw new UnsupportedOperationException();
   }
}
