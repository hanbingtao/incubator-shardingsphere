/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.sharding.rewrite.token.generator.impl;

import lombok.Setter;
import org.apache.shardingsphere.core.rule.DataNode;
import org.apache.shardingsphere.sharding.rewrite.aware.ShardingRouteContextAware;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.impl.ShardingInsertValue;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.impl.ShardingInsertValuesToken;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingCondition;
import org.apache.shardingsphere.sharding.route.engine.context.ShardingRouteContext;
import org.apache.shardingsphere.sql.parser.binder.segment.insert.values.InsertValueContext;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.underlying.rewrite.sql.token.generator.OptionalSQLTokenGenerator;
import org.apache.shardingsphere.underlying.rewrite.sql.token.pojo.generic.InsertValuesToken;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Insert values token generator for sharding.
 */
@Setter
public final class ShardingInsertValuesTokenGenerator implements OptionalSQLTokenGenerator<InsertStatementContext>, ShardingRouteContextAware {
    
    private ShardingRouteContext shardingRouteContext;
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof InsertStatementContext && !(((InsertStatementContext) sqlStatementContext).getSqlStatement()).getValues().isEmpty();
    }
    
    @Override
    public InsertValuesToken generateSQLToken(final InsertStatementContext insertStatementContext) {
        Collection<InsertValuesSegment> insertValuesSegments = (insertStatementContext.getSqlStatement()).getValues();
        InsertValuesToken result = new ShardingInsertValuesToken(getStartIndex(insertValuesSegments), getStopIndex(insertValuesSegments));
        Iterator<ShardingCondition> shardingConditionIterator = null == shardingRouteContext || shardingRouteContext.getShardingConditions().getConditions().isEmpty()
                ? null : shardingRouteContext.getShardingConditions().getConditions().iterator();
        for (InsertValueContext each : insertStatementContext.getInsertValueContexts()) {
            List<ExpressionSegment> expressionSegments = each.getValueExpressions();
            Collection<DataNode> dataNodes = null == shardingConditionIterator ? Collections.emptyList() : shardingConditionIterator.next().getDataNodes();
            result.getInsertValues().add(new ShardingInsertValue(expressionSegments, dataNodes));
        }
        return result;
    }
    
    private int getStartIndex(final Collection<InsertValuesSegment> segments) {
        int result = segments.iterator().next().getStartIndex();
        for (InsertValuesSegment each : segments) {
            result = result > each.getStartIndex() ? each.getStartIndex() : result;
        }
        return result;
    }
    
    private int getStopIndex(final Collection<InsertValuesSegment> segments) {
        int result = segments.iterator().next().getStopIndex();
        for (InsertValuesSegment each : segments) {
            result = result < each.getStopIndex() ? each.getStopIndex() : result;
        }
        return result;
    }
}
