package it.polimi.pgql.queryplanner.planner;

import oracle.pgql.lang.ir.QueryExpression;
import oracle.pgql.lang.ir.QueryVertex;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class QueryPlan {

    // Estimated cost of the operator
    protected double cost;

    // list of filters we apply to the vertex
    protected Set<QueryExpression> constraints;

    private QueryPlan parent;
    private List<QueryPlan> children = new ArrayList<>();

    public QueryPlan getParent() {
        return parent;
    }

    public void setParent(QueryPlan parent) {
        this.parent = parent;
    }

    public List<QueryPlan> getChildren() {
        return children;
    }

    public double getCost() {
        return cost;
    }
}
