package it.polimi.pgql.queryplanner.planner.operators;


import it.polimi.pgql.queryplanner.planner.QueryPlan;
import oracle.pgql.lang.ir.QueryVertex;

public class CartesianProductOperator extends QueryPlan {

    private QueryVertex srcVertex;
    private QueryVertex dstVertex;

    public CartesianProductOperator(QueryVertex vertexFromWhichWeCanExpand, QueryVertex vertex) {
        this.srcVertex = vertexFromWhichWeCanExpand;
        this.dstVertex = vertex;
    }

    @Override
    public String toString() {
        return String.format("%s (src %s, dst %s)", this.getClass().getSimpleName(), srcVertex.getName(), dstVertex.getName());
    }
}
