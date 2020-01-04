package it.polimi.pgql.queryplanner.planner.operators;

import it.polimi.pgql.queryplanner.planner.QueryPlan;
import oracle.pgql.lang.ir.QueryVertex;

public class EdgeMatchOperator extends QueryPlan {

    private QueryVertex srcVertex;
    private QueryVertex dstVertex;

    public EdgeMatchOperator(QueryVertex src, QueryVertex dst) {
        this.srcVertex = src;
        this.dstVertex = dst;
    }

    @Override
    public String toString() {
        return String.format("%s (src %s, dst %s)", this.getClass().getSimpleName(), srcVertex.getName(), dstVertex.getName());
    }

}
