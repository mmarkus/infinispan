package org.infinispan.distribution.group;

import java.util.Set;

/**
 * Control's key grouping.
 * 
 * @author Pete Muir
 *
 */
public interface GroupManager {
	
	/**
	 * Get the group for a given key
	 * 
	 * @param key the key for which to get the group
	 * @return the group, or null if no group is defined for the key
	 */
	String getGroup(Object key);

   <G, KG> Set<KG> getKeysInGroup(G group);
}
