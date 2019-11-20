package it.polimi.pgql.queryplanner.planner.operators;

import it.polimi.pgql.queryplanner.planner.QueryPlan;
import oracle.pgql.lang.ir.QueryVertex;

public class RootVertexMatchOperator extends QueryPlan {

    private QueryVertex vertex;

    public RootVertexMatchOperator(QueryVertex vertex) {
        this.vertex = vertex;
    }
}
