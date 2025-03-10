/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.druid.client.selector;

import com.google.common.collect.Iterables;
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import org.apache.druid.client.QueryableDruidServer;
import org.apache.druid.query.Query;
import org.apache.druid.timeline.DataSegment;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 */
public abstract class AbstractTierSelectorStrategy implements TierSelectorStrategy
{
  private final ServerSelectorStrategy serverSelectorStrategy;

  public AbstractTierSelectorStrategy(ServerSelectorStrategy serverSelectorStrategy)
  {
    this.serverSelectorStrategy = serverSelectorStrategy;
  }
  
  @Nullable
  @Override
  public <T> QueryableDruidServer pick(
      Query<T> query,
      Int2ObjectRBTreeMap<Set<QueryableDruidServer>> prioritizedServers,
      DataSegment segment
  )
  {
    return Iterables.getOnlyElement(pick(query, prioritizedServers, segment, 1), null);
  }

  @Override
  public <T> List<QueryableDruidServer> pick(
      Query<T> query,
      Int2ObjectRBTreeMap<Set<QueryableDruidServer>> prioritizedServers,
      DataSegment segment,
      int numServersToPick
  )
  {
    List<QueryableDruidServer> result = new ArrayList<>(numServersToPick);
    for (Set<QueryableDruidServer> priorityServers : prioritizedServers.values()) {
      result.addAll(serverSelectorStrategy.pick(query, priorityServers, segment, numServersToPick - result.size()));
      if (result.size() == numServersToPick) {
        break;
      }
    }
    return result;
  }
}
