package it.polimi.pgql.queryplanner.planner.operators;

import it.polimi.pgql.queryplanner.planner.QueryPlan;
import oracle.pgql.lang.ir.QueryExpression;
import oracle.pgql.lang.ir.QueryVertex;

import java.util.Set;

public class ReachabilityOperator extends QueryPlan {

    private QueryVertex vertex;
    private double cost; // Estimated cost of the operator
    private Set<QueryExpression> constraints; // list of filters we apply to the vertex

}
