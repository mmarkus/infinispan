package org.infinispan.interceptors;

import org.infinispan.commands.write.ClearCommand;
import org.infinispan.commands.write.PutKeyValueCommand;
import org.infinispan.commands.write.PutMapCommand;
import org.infinispan.commands.write.RemoveCommand;
import org.infinispan.context.InvocationContext;
import org.infinispan.interceptors.base.CommandInterceptor;
import org.infinispan.util.logging.Log;
import org.infinispan.util.logging.LogFactory;

public class GroupingInterceptor extends CommandInterceptor {

   private static Log log = LogFactory.getLog(GroupingInterceptor.class);

   @Override
   public Object visitPutKeyValueCommand(InvocationContext ctx, PutKeyValueCommand command) throws Throwable {
      log.fatal("GroupingInterceptor.put ...");

      return super.visitPutKeyValueCommand(ctx, command);    // TODO: Customise this generated block
   }

   @Override
   public Object visitRemoveCommand(InvocationContext ctx, RemoveCommand command) throws Throwable {
      return super.visitRemoveCommand(ctx, command);    // TODO: Customise this generated block
   }

   @Override
   public Object visitClearCommand(InvocationContext ctx, ClearCommand command) throws Throwable {
      return super.visitClearCommand(ctx, command);    // TODO: Customise this generated block
   }

   @Override
   public Object visitPutMapCommand(InvocationContext ctx, PutMapCommand command) throws Throwable {
      return super.visitPutMapCommand(ctx, command);    // TODO: Customise this generated block
   }
}
