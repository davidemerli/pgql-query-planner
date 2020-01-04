package it.polimi.pgql.queryplanner.planner.operators;

import it.polimi.pgql.queryplanner.planner.QueryPlan;
import oracle.pgql.lang.ir.QueryVertex;

public class ConstantVertexMatchOperator extends QueryPlan {

    private QueryVertex vertex;

    public ConstantVertexMatchOperator(QueryVertex vertex) {
        this.vertex = vertex;
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", this.getClass().getSimpleName(), vertex.getName());
    }
}
