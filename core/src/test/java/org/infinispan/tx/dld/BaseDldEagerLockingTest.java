package org.infinispan.tx.dld;

import org.infinispan.test.PerCacheExecutorThread;
import org.infinispan.test.TestingUtil;
import org.infinispan.util.concurrent.locks.DeadlockDetectingLockManager;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;

import static org.testng.Assert.assertEquals;

/**
 * // TODO: Document this
 *
 * @author Mircea.Markus@jboss.com
 * @since 4.2
 */
public abstract class BaseDldEagerLockingTest extends BaseDldTest {
   protected PerCacheExecutorThread ex0;
   protected PerCacheExecutorThread ex1;
   protected DeadlockDetectingLockManager lm0;
   protected DeadlockDetectingLockManager lm1;

   @BeforeMethod
   public void beforeMethod() {
      ex0 = new PerCacheExecutorThread(cache(0), 0);
      ex1 = new PerCacheExecutorThread(cache(1), 1);

      lm0 = (DeadlockDetectingLockManager) TestingUtil.extractLockManager(cache(0));
      lm0.setExposeJmxStats(true);
      lm1 = (DeadlockDetectingLockManager) TestingUtil.extractLockManager(cache(1));
      lm1.setExposeJmxStats(true);
   }

   @AfterMethod
   public void afterMethod() {
      ex0.stopThread();
      ex1.stopThread();
   }

   protected void testSymmetricDld(Object k0, Object k1) throws SystemException {

      long start = System.currentTimeMillis();

      log.trace("Here is where the test starts");

      ex0.execute(PerCacheExecutorThread.Operations.BEGGIN_TX);
      ex1.execute(PerCacheExecutorThread.Operations.BEGGIN_TX);


      ex0.setKeyValue(k0, "v0_0");
      assertEquals(ex0.execute(PerCacheExecutorThread.Operations.PUT_KEY_VALUE), PerCacheExecutorThread.OperationsResult.PUT_KEY_VALUE_OK);
      ex1.setKeyValue(k1, "v1_1");
      assertEquals(ex1.execute(PerCacheExecutorThread.Operations.PUT_KEY_VALUE), PerCacheExecutorThread.OperationsResult.PUT_KEY_VALUE_OK);

      assert lm0.isLocked(k0);
//      assert lm0.isLocked(k1);
//      assert lm1.isLocked(k0);
      assert lm1.isLocked(k1);

      log.trace("After first set of puts");

      ex0.clearResponse();
      ex1.clearResponse();

      log.info("Here is where DLD happens");

      ex1.setKeyValue(k0, "v0_1");
      ex1.executeNoResponse(PerCacheExecutorThread.Operations.PUT_KEY_VALUE);
      ex0.setKeyValue(k1, "v1_0");
      ex0.executeNoResponse(PerCacheExecutorThread.Operations.PUT_KEY_VALUE);
      ex0.waitForResponse();
      ex1.waitForResponse();

      boolean b1 = ex0.lastResponse() instanceof Exception;
      boolean b2 = ex1.lastResponse() instanceof Exception;
      log.info("b1:", b1);
      log.info("b2:", b2);
      assert xor(b1, b2) : "Both are " + (b1 || b2);

      assert xor(ex0.getOngoingTransaction().getStatus() == Status.STATUS_MARKED_ROLLBACK,
                 ex1.getOngoingTransaction().getStatus() == Status.STATUS_MARKED_ROLLBACK);

      Object txOutcome1 = ex0.execute(PerCacheExecutorThread.Operations.COMMIT_TX);
      Object txOutcome2 = ex1.execute(PerCacheExecutorThread.Operations.COMMIT_TX);
      assert xor(txOutcome1 == PerCacheExecutorThread.OperationsResult.COMMIT_TX_OK, txOutcome2 == PerCacheExecutorThread.OperationsResult.COMMIT_TX_OK);
      assert xor(txOutcome1 instanceof RollbackException, txOutcome2 instanceof RollbackException);

      assert cache(0).get(k0) != null;
      assert cache(0).get(k1) != null;
      assert cache(1).get(k0) != null;
      assert cache(1).get(k1) != null;

      long totalDeadlocks = lm0.getTotalNumberOfDetectedDeadlocks() + lm1.getTotalNumberOfDetectedDeadlocks();
      assert totalDeadlocks == 1 : "Expected 1 but received " + totalDeadlocks;

      System.out.println("Test took " + (System.currentTimeMillis() - start) + " millis.");
   }


}
