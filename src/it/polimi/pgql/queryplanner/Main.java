package it.polimi.pgql.queryplanner;

import it.polimi.pgql.queryplanner.planner.QueryPlanner;
import oracle.pgql.lang.Pgql;
import oracle.pgql.lang.PgqlException;
import oracle.pgql.lang.PgqlResult;
import oracle.pgql.lang.ir.GraphQuery;

public class Main {

    /**
     * Statistics used only refer to the letter of the vertex
     * higher in alphabetic order means more selective
     *
     * TODO: add real statistic handling providing a graph
     */
    public static void main(String[] args) throws PgqlException {
        QueryPlanner planner = new QueryPlanner();

        try (Pgql pgql = new Pgql()) {
            //Gets the parsed PGQL result from which we extrapolate the GraphQuery

            PgqlResult result = pgql.parse("SELECT * FROM example MATCH " +
                    "(n)->(b)->(c)->(d)->(o)->(f)->(g)->(h)->(i)->(j)->(k)->(l)->(m)->(n), " +
                    "(n)->(a)->(e)->(d), " +
                    "(e)->(p)->(h), " +
                    "(n)->(q)->(l), " +
                    "(p)->(q)," +
                    "(b)->(r)->(s)," +
                    "(t)->(u)," +
                    "(t)->(v)->(w)," +
                    "(m)->(x)," +
                    "(r)->(y)->(z)");

            //Contains the AST structure with the different operators to be executed on the Graph
            GraphQuery graphQuery = result.getGraphQuery();
            System.out.println(result.getErrorMessages());

            planner.customPlan(graphQuery).stream().forEach(System.out::println);
        }
    }
}
