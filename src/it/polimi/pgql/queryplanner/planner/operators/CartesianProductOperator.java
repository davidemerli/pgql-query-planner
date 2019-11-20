package it.polimi.pgql.queryplanner.planner.operators;


import it.polimi.pgql.queryplanner.planner.QueryPlan;

public class CartesianProductOperator extends QueryPlan {

    //TODO: Check if 'Operator' really means a QueryPlan object
    private QueryPlan leftOperator;
    private QueryPlan rightOperator;

    public CartesianProductOperator(QueryPlan previousPlan, RootVertexMatchOperator rootVertexMatchPlan) {
        this.leftOperator = previousPlan;
        this.rightOperator = rootVertexMatchPlan;
    }
}
