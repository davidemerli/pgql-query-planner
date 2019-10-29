package it.polimi.pgql.queryplanner.plans;

import oracle.pgql.lang.ir.QueryVertex;

public class NeighborMatchPlan extends QueryPlan {

    private QueryVertex srcVertex;
    private QueryVertex dstVertex;
    private boolean outgoing; // true if we match from src to dst, false if we match from dst to src.

    public NeighborMatchPlan(QueryVertex vertexFromWhichWeCanExpand, QueryVertex vertex, boolean outgoing) {

    }
}
