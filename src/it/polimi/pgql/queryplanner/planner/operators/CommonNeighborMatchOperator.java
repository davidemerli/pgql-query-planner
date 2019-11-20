package it.polimi.pgql.queryplanner.planner.operators;

import it.polimi.pgql.queryplanner.planner.QueryPlan;
import oracle.pgql.lang.ir.QueryVertex;

public class CommonNeighborMatchOperator extends QueryPlan {

    private QueryVertex srcVertex;
    private QueryVertex dstVertex;

    public CommonNeighborMatchOperator(QueryVertex vertexFromWhichWeCanExpand, QueryVertex vertex, boolean outgoing) {
        this.srcVertex = vertexFromWhichWeCanExpand;
        this.dstVertex = vertex;
    }

}
