package org.infinispan.commands.remote;

import org.infinispan.context.InvocationContext;
import org.infinispan.distribution.group.GroupManager;
import org.infinispan.util.logging.Log;
import org.infinispan.util.logging.LogFactory;

import java.util.Collections;

public class GetKeysInGroup extends BaseRpcCommand {

   public static final byte COMMAND_ID = 40;

   private static Log log = LogFactory.getLog(GetKeysInGroup.class);

   private Object group;
   private GroupManager groupManager;

   private GetKeysInGroup() {
      super(null);
   }

   public GetKeysInGroup(String ownerCacheName) {
      super(ownerCacheName);
   }

   public GetKeysInGroup(String ownerCacheName, Object group) {
      super(ownerCacheName);
      this.group = group;
   }

   public void init(GroupManager groupManager) {
      this.groupManager = groupManager;
   }

   @Override
   public Object perform(InvocationContext ctx) throws Throwable {
      log.fatalf("GetKeysOnGroup invoked for %s", group);
      return Collections.emptySet();
   }

   @Override
   public byte getCommandId() {
      return COMMAND_ID;
   }

   @Override
   public Object[] getParameters() {
      return new Object[]{groupManager};
   }

   @Override
   public void setParameters(int commandId, Object[] parameters) {
      this.groupManager = groupManager;
   }

   @Override
   public boolean isReturnValueExpected() {
      return true;
   }
}
