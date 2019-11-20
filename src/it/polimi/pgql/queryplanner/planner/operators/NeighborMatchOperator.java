package it.polimi.pgql.queryplanner.planner.operators;

import it.polimi.pgql.queryplanner.planner.QueryPlan;
import oracle.pgql.lang.ir.QueryVertex;

public class NeighborMatchOperator extends QueryPlan {

    private QueryVertex srcVertex;
    private QueryVertex dstVertex;
    private boolean outgoing; // true if we match from src to dst, false if we match from dst to src.

    public NeighborMatchOperator(QueryVertex vertexFromWhichWeCanExpand, QueryVertex vertex, boolean outgoing) {
        this.srcVertex = vertexFromWhichWeCanExpand;
        this.dstVertex = vertex;
        this.outgoing = outgoing;
    }
}
