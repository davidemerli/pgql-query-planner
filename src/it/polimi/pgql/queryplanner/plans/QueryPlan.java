package it.polimi.pgql.queryplanner.plans;

import oracle.pgql.lang.ir.QueryExpression;
import oracle.pgql.lang.ir.QueryVertex;

import java.util.Set;

public abstract class QueryPlan {

    private QueryVertex vertex;

    // Estimated cost of the operator
    private double cost;

    // list of filters we apply to the vertex
    private Set<QueryExpression> constraints;

    public QueryPlan parent, child;

}
