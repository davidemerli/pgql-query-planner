package it.polimi.pgql.queryplanner.plans;


public class CartesianProductPlan extends QueryPlan {

    //TODO: Check if 'Operator' really means a QueryPlan object
    private QueryPlan leftOperator;
    private QueryPlan rightOperator;

    public CartesianProductPlan(QueryPlan previousPlan, RootVertexMatchPlan rootVertexMatchPlan) {

    }
}
