package it.polimi.pgql.queryplanner;

import it.polimi.pgql.queryplanner.plans.QueryPlan;
import oracle.pgql.lang.Pgql;
import oracle.pgql.lang.PgqlException;
import oracle.pgql.lang.PgqlResult;
import oracle.pgql.lang.ir.GraphQuery;

public class Main {

    public static void main(String[] args) throws PgqlException {
        QueryPlanner planner = new QueryPlanner();

        try(Pgql pgql = new Pgql()) {
            //Gets the parsed PGQL result from which we extrapolate the GraphQuery
            PgqlResult result = pgql.parse("SELECT n FROM g MATCH (n:Person) -[e:likes]-> (m:Person) WHERE n.name = 'Dave'");
            //Contains the AST structure with the different operators to be executed on the Graph
            GraphQuery graphQuery = result.getGraphQuery();

            QueryPlan plan = planner.generatePlan(graphQuery);

//            System.out.println(planner.getPGQLQueryStringFromPlan(plan));

            //TODO: get a new string from the plan tree

            //TODO: execute the plan
        }
    }

}
