package org.infinispan.grouping;

import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.distribution.group.Group;
import org.infinispan.test.MultipleCacheManagersTest;
import org.testng.annotations.Test;

import java.io.Serializable;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Test (groups = "functional", testName = "grouping.BasicGetGroupKeysTest")
public class BasicGetGroupKeysTest extends MultipleCacheManagersTest {

   @Override
   protected void createCacheManagers() throws Throwable {
      ConfigurationBuilder dcc = getDefaultClusteredCacheConfig(CacheMode.DIST_SYNC, false);
      dcc.clustering().hash().groups().enabled(true);
      createCluster(dcc, 4);
      waitForClusterToForm();
   }

   public void testGetGroupKeys() throws Throwable {
      String group = "myGroup";

      for (int i = 0; i < 100; i++) {
         cache(i % 4).put(new GroupKey<Integer>(group, i), i);
      }

      assertEquals(0, cache(0).get(new GroupKey<Integer>(group, 0)));

      for (int i = 0; i < 100; i++) {
         assertEquals(i, cache(i % 4).get(new GroupKey<Integer>(group, i)));
      }

      Set<Integer> keys = cache(0).getGroupKeys(group);
      for (int i = 0; i < 100; i++)
         assertTrue(keys.contains(i), "Missing " + i);
   }

   static class GroupKey<K> implements Serializable {

      final String group;
      final K key;

      GroupKey(String group, K key) {
         this.group = group;
         this.key = key;
      }

      @Group
      public String getGroup() {
         return group;
      }

      public K getKey() {
         return key;
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;

         GroupKey groupKey = (GroupKey) o;

         if (group != null ? !group.equals(groupKey.group) : groupKey.group != null) return false;
         if (key != null ? !key.equals(groupKey.key) : groupKey.key != null) return false;

         return true;
      }

      @Override
      public int hashCode() {
         int result = group != null ? group.hashCode() : 0;
         result = 31 * result + (key != null ? key.hashCode() : 0);
         return result;
      }
   }
}
