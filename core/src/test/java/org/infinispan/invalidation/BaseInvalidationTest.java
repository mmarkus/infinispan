/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009 Red Hat Inc. and/or its affiliates and other
 * contributors as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a full listing of
 * individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.infinispan.invalidation;

import static org.easymock.EasyMock.*;
import org.infinispan.AdvancedCache;
import org.infinispan.api.mvcc.LockAssert;
import org.infinispan.commands.remote.CacheRpcCommand;
import org.infinispan.commands.write.ClearCommand;
import org.infinispan.commands.write.InvalidateCommand;
import org.infinispan.config.Configuration;
import static org.infinispan.context.Flag.CACHE_MODE_LOCAL;
import org.infinispan.remoting.rpc.ResponseFilter;
import org.infinispan.remoting.rpc.ResponseMode;
import org.infinispan.remoting.rpc.RpcManager;
import org.infinispan.remoting.rpc.RpcManagerImpl;
import org.infinispan.remoting.transport.Address;
import org.infinispan.remoting.transport.Transport;
import org.infinispan.test.MultipleCacheManagersTest;
import org.infinispan.test.TestingUtil;
import org.infinispan.util.concurrent.locks.LockManager;
import static org.testng.AssertJUnit.*;
import org.testng.annotations.Test;

import javax.transaction.RollbackException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import java.util.ArrayList;
import java.util.List;

@Test(groups = "functional")
public abstract class BaseInvalidationTest extends MultipleCacheManagersTest {
   protected boolean isSync;

   protected BaseInvalidationTest() {
      cleanup = CleanupPhase.AFTER_METHOD;
   }

   protected void createCacheManagers() throws Throwable {
      Configuration c = getDefaultClusteredConfig(isSync ? Configuration.CacheMode.INVALIDATION_SYNC : Configuration.CacheMode.INVALIDATION_ASYNC, false);
      c.setStateRetrievalTimeout(1000);
      c.setLockAcquisitionTimeout(500);
      createClusteredCaches(2, "invalidation", c);

      c = getDefaultClusteredConfig(isSync ? Configuration.CacheMode.INVALIDATION_SYNC : Configuration.CacheMode.INVALIDATION_ASYNC, true);
      c.setStateRetrievalTimeout(1000);
      c.setLockAcquisitionTimeout(500);
      defineConfigurationOnAllManagers("invalidationTx", c);
      waitForClusterToForm("invalidationTx");

   }

   public void testRemove() throws Exception {
      AdvancedCache cache1 = cache(0,"invalidation").getAdvancedCache();
      AdvancedCache cache2 = cache(1,"invalidation").getAdvancedCache();
      cache1.withFlags(CACHE_MODE_LOCAL).put("key", "value");
      assertEquals("value", cache1.get("key"));
      cache2.withFlags(CACHE_MODE_LOCAL).put("key", "value");
      assertEquals("value", cache2.get("key"));

      replListener(cache2).expectAny();
      assertEquals("value", cache1.remove("key"));
      replListener(cache2).waitForRpc();

      assertEquals(false, cache2.containsKey("key"));
   }

   public void testResurrectEntry() throws Exception {
      AdvancedCache cache1 = cache(0,"invalidation").getAdvancedCache();
      AdvancedCache cache2 = cache(1,"invalidation").getAdvancedCache();
      replListener(cache2).expect(InvalidateCommand.class);
      cache1.put("key", "value");
      replListener(cache2).waitForRpc();

      assertEquals("value", cache1.get("key"));
      assertEquals(null, cache2.get("key"));
      replListener(cache2).expect(InvalidateCommand.class);
      cache1.put("key", "newValue");
      replListener(cache2).waitForRpc();

      assertEquals("newValue", cache1.get("key"));
      assertEquals(null, cache2.get("key"));

      replListener(cache2).expect(InvalidateCommand.class);
      assertEquals("newValue", cache1.remove("key"));
      replListener(cache2).waitForRpc();

      assertEquals(null, cache1.get("key"));
      assertEquals(null, cache2.get("key"));

      // Restore locally
      replListener(cache2).expect(InvalidateCommand.class);
      cache1.put("key", "value");
      replListener(cache2).waitForRpc();

      assertEquals("value", cache1.get("key"));
      assertEquals(null, cache2.get("key"));

      replListener(cache1).expect(InvalidateCommand.class);
      cache2.put("key", "value2");
      replListener(cache1).waitForRpc();

      assertEquals("value2", cache2.get("key"));
      assertEquals(null, cache1.get("key"));
   }

   public void testDeleteNonExistentEntry() throws Exception {
      AdvancedCache cache1 = cache(0,"invalidationTx").getAdvancedCache();
      AdvancedCache cache2 = cache(1,"invalidationTx").getAdvancedCache();
      
      assertNull("Should be null", cache1.get("key"));
      assertNull("Should be null", cache2.get("key"));

      log.info("before...");
      replListener(cache2).expect(InvalidateCommand.class);
      cache1.put("key", "value");
      log.info("after...");
      replListener(cache2).waitForRpc();

      assertEquals("value", cache1.get("key"));
      assertNull("Should be null", cache2.get("key"));

      // OK, here's the real test
      TransactionManager tm = TestingUtil.getTransactionManager(cache2);
      replListener(cache1).expect(InvalidateCommand.class); // invalidates always happen outside of a tx
      tm.begin();
      // Remove an entry that doesn't exist in cache2
      cache2.remove("key");
      tm.commit();
      replListener(cache1).waitForRpc();

      assert cache1.get("key") == null;
      assert cache2.get("key") == null;
   }

   public void testTxSyncUnableToInvalidate() throws Exception {
      AdvancedCache cache1 = cache(0,"invalidationTx").getAdvancedCache();
      AdvancedCache cache2 = cache(1,"invalidationTx").getAdvancedCache();
      replListener(cache2).expect(InvalidateCommand.class);
      cache1.put("key", "value");
      replListener(cache2).waitForRpc();

      assertEquals("value", cache1.get("key"));
      assertNull(cache2.get("key"));

      // start a tx that cacahe1 will have to send out an evict ...
      TransactionManager mgr1 = TestingUtil.getTransactionManager(cache1);
      TransactionManager mgr2 = TestingUtil.getTransactionManager(cache2);

      mgr1.begin();
      cache1.put("key", "value2");
      Transaction tx1 = mgr1.suspend();
      mgr2.begin();
      cache2.put("key", "value3");
      Transaction tx2 = mgr2.suspend();
      mgr1.resume(tx1);
      // this oughtta fail
      try {
         replListener(cache2).expect(InvalidateCommand.class);
         mgr1.commit();
         replListener(cache2).waitForRpc();
      } catch (RollbackException roll) {
         assert isSync : "isSync should be true";
      }

      mgr2.resume(tx2);
      replListener(cache1).expect(InvalidateCommand.class);
      mgr2.commit();
      if (!isSync) replListener(cache1).waitForRpc();

      LockManager lm1 = TestingUtil.extractComponent(cache1, LockManager.class);
      LockManager lm2 = TestingUtil.extractComponent(cache2, LockManager.class);
      assert !lm1.isLocked("key");
      assert !lm2.isLocked("key");

      LockAssert.assertNoLocks(cache1);
      LockAssert.assertNoLocks(cache2);
   }

   public void testCacheMode() throws Exception {
      AdvancedCache cache1 = cache(0,"invalidation").getAdvancedCache();
      AdvancedCache cache2 = cache(1,"invalidation").getAdvancedCache();
      RpcManagerImpl rpcManager = (RpcManagerImpl) TestingUtil.extractComponent(cache1, RpcManager.class);
      Transport origTransport = TestingUtil.extractComponent(cache1, Transport.class);
      try {
         Transport mockTransport = createMock(Transport.class);
         rpcManager.setTransport(mockTransport);
         Address addressOne = createNiceMock(Address.class);
         Address addressTwo = createNiceMock(Address.class);
         List<Address> members = new ArrayList<Address>(2);
         members.add(addressOne);
         members.add(addressTwo);

         expect(mockTransport.getMembers()).andReturn(members).anyTimes();
         expect(mockTransport.getAddress()).andReturn(addressOne).anyTimes();
         expect(mockTransport.invokeRemotely((List<Address>) anyObject(), (CacheRpcCommand) anyObject(),
                                             eq(isSync ? ResponseMode.SYNCHRONOUS : ResponseMode.ASYNCHRONOUS_WITH_SYNC_MARSHALLING),
                                             anyLong(), anyBoolean(), (ResponseFilter) anyObject(), anyBoolean())).andReturn(null).anyTimes();
         replay(mockTransport);

         cache1.put("k", "v");
         verify(mockTransport);

      } finally {
         if (rpcManager != null) rpcManager.setTransport(origTransport);
      }
   }

   public void testPutIfAbsent() {
      AdvancedCache cache1 = cache(0,"invalidation").getAdvancedCache();
      AdvancedCache cache2 = cache(1,"invalidation").getAdvancedCache();
      assert null == cache2.withFlags(CACHE_MODE_LOCAL).put("key", "value");
      assert cache2.get("key").equals("value");
      assert cache1.get("key") == null;

      replListener(cache2).expect(InvalidateCommand.class);
      cache1.putIfAbsent("key", "value");
      replListener(cache2).waitForRpc();

      assert cache1.get("key").equals("value");
      assert cache2.get("key") == null;

      assert null == cache2.withFlags(CACHE_MODE_LOCAL).put("key", "value2");

      assert cache1.get("key").equals("value");
      assert cache2.get("key").equals("value2");

      cache1.putIfAbsent("key", "value3");

      assert cache1.get("key").equals("value");
      assert cache2.get("key").equals("value2"); // should not invalidate cache2!!
   }

   public void testRemoveIfPresent() {
      AdvancedCache cache1 = cache(0,"invalidation").getAdvancedCache();
      AdvancedCache cache2 = cache(1,"invalidation").getAdvancedCache();
      cache1.withFlags(CACHE_MODE_LOCAL).put("key", "value1");
      cache2.withFlags(CACHE_MODE_LOCAL).put("key", "value2");
      assert cache1.get("key").equals("value1");
      assert cache2.get("key").equals("value2");

      assert !cache1.remove("key", "value");

      assert cache1.get("key").equals("value1") : "Should not remove";
      assert cache2.get("key").equals("value2") : "Should not evict";

      replListener(cache2).expect(InvalidateCommand.class);
      cache1.remove("key", "value1");
      replListener(cache2).waitForRpc();

      assert cache1.get("key") == null;
      assert cache2.get("key") == null;
   }

   public void testClear() {
      AdvancedCache cache1 = cache(0,"invalidation").getAdvancedCache();
      AdvancedCache cache2 = cache(1,"invalidation").getAdvancedCache();
      cache1.withFlags(CACHE_MODE_LOCAL).put("key", "value1");
      cache2.withFlags(CACHE_MODE_LOCAL).put("key", "value2");
      assert cache1.get("key").equals("value1");
      assert cache2.get("key").equals("value2");

      replListener(cache2).expect(ClearCommand.class);
      cache1.clear();
      replListener(cache2).waitForRpc();

      assert cache1.get("key") == null;
      assert cache2.get("key") == null;
   }

   public void testReplace() {
      AdvancedCache cache1 = cache(0,"invalidation").getAdvancedCache();
      AdvancedCache cache2 = cache(1,"invalidation").getAdvancedCache();
      cache2.withFlags(CACHE_MODE_LOCAL).put("key", "value2");
      assert cache1.get("key") == null;
      assert cache2.get("key").equals("value2");

      assert null == cache1.replace("key", "value1"); // should do nothing since there is nothing to replace on cache1

      assert cache1.get("key") == null;
      assert cache2.get("key").equals("value2");

      assert null == cache1.withFlags(CACHE_MODE_LOCAL).put("key", "valueN");

      replListener(cache2).expect(InvalidateCommand.class);
      cache1.replace("key", "value1");
      replListener(cache2).waitForRpc();

      assert cache1.get("key").equals("value1");
      assert cache2.get("key") == null;
   }

   public void testReplaceWithOldVal() {
      AdvancedCache cache1 = cache(0,"invalidation").getAdvancedCache();
      AdvancedCache cache2 = cache(1,"invalidation").getAdvancedCache();
      cache2.withFlags(CACHE_MODE_LOCAL).put("key", "value2");
      assert cache1.get("key") == null;
      assert cache2.get("key").equals("value2");

      assert !cache1.replace("key", "valueOld", "value1"); // should do nothing since there is nothing to replace on cache1

      assert cache1.get("key") == null;
      assert cache2.get("key").equals("value2");

      assert null == cache1.withFlags(CACHE_MODE_LOCAL).put("key", "valueN");

      assert !cache1.replace("key", "valueOld", "value1"); // should do nothing since there is nothing to replace on cache1

      assert cache1.get("key").equals("valueN");
      assert cache2.get("key").equals("value2");

      replListener(cache2).expect(InvalidateCommand.class);
      assert cache1.replace("key", "valueN", "value1");
      replListener(cache2).waitForRpc();

      assert cache1.get("key").equals("value1");
      assert cache2.get("key") == null;
   }

   public void testLocalOnlyClear() {
      AdvancedCache cache1 = cache(0,"invalidation").getAdvancedCache();
      AdvancedCache cache2 = cache(1,"invalidation").getAdvancedCache();
      cache1.withFlags(CACHE_MODE_LOCAL).put("key", "value1");
      cache2.withFlags(CACHE_MODE_LOCAL).put("key", "value2");
      assert cache1.get("key").equals("value1");
      assert cache2.get("key").equals("value2");

      cache1.withFlags(CACHE_MODE_LOCAL).clear();

      assert cache1.get("key") == null;
      assert cache2.get("key") != null;
      assert cache2.get("key").equals("value2");
   }

   public void testPutForExternalRead() throws Exception {
      AdvancedCache cache1 = cache(0,"invalidation").getAdvancedCache();
      AdvancedCache cache2 = cache(1,"invalidation").getAdvancedCache();
      cache1.putForExternalRead("key", "value1");
      Thread.sleep(500); // sleep so that async invalidation (result of PFER) is propagated
      cache2.putForExternalRead("key", "value2");
      Thread.sleep(500); // sleep so that async invalidation (result of PFER) is propagated
      assert cache1.get("key") != null;
      assert cache1.get("key").equals("value1");
      assert cache2.get("key") != null;
      assert cache2.get("key").equals("value2");
   }
}
