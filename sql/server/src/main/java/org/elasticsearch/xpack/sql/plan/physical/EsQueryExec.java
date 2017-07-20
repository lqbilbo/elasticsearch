/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.sql.plan.physical;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.xpack.sql.execution.search.Scroller;
import org.elasticsearch.xpack.sql.expression.Attribute;
import org.elasticsearch.xpack.sql.querydsl.container.QueryContainer;
import org.elasticsearch.xpack.sql.session.RowSetCursor;
import org.elasticsearch.xpack.sql.session.Rows;
import org.elasticsearch.xpack.sql.session.SqlSession;
import org.elasticsearch.xpack.sql.tree.Location;

import java.util.List;
import java.util.Objects;

public class EsQueryExec extends LeafExec {

    private final String index, type;
    private final List<Attribute> output;

    private final QueryContainer queryContainer;

    public EsQueryExec(Location location, String index, String type, List<Attribute> output, QueryContainer queryContainer) {
        super(location);
        this.index = index;
        this.type = type;
        this.output = output;
        this.queryContainer = queryContainer;
    }

    public EsQueryExec with(QueryContainer queryContainer) {
        return new EsQueryExec(location(), index, type, output, queryContainer);
    }

    public String index() {
        return index;
    }
    
    public String type() {
        return type;
    }

    public QueryContainer queryContainer() {
        return queryContainer;
    }

    @Override
    public List<Attribute> output() {
        return output;
    }
    
    @Override
    public void execute(SqlSession session, ActionListener<RowSetCursor> listener) {
        Scroller scroller = new Scroller(session.client(), session.settings());
        scroller.scroll(Rows.schema(output), queryContainer, index, type, listener);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, type, queryContainer, output);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        EsQueryExec other = (EsQueryExec) obj;
        return Objects.equals(index, other.index)
                && Objects.equals(type, other.type)
                && Objects.equals(queryContainer, other.queryContainer)
                && Objects.equals(output, other.output);
    }

    @Override
    public String nodeString() {
        return nodeName() + "[" + index + "/" + type + "," + queryContainer + "]";
    }
}