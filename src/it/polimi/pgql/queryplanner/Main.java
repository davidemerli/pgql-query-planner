package it.polimi.pgql.queryplanner;

import it.polimi.pgql.queryplanner.graph.Graph;
import it.polimi.pgql.queryplanner.graph.GraphVertex;
import it.polimi.pgql.queryplanner.planner.QueryPlan;
import it.polimi.pgql.queryplanner.planner.QueryPlanner;
import oracle.pgql.lang.Pgql;
import oracle.pgql.lang.PgqlException;
import oracle.pgql.lang.PgqlResult;
import oracle.pgql.lang.ir.GraphQuery;
import oracle.pgx.api.*;

import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public class Main {

    public static void main(String[] args) throws PgqlException, ExecutionException, InterruptedException {
        QueryPlanner planner = new QueryPlanner();

        try (Pgql pgql = new Pgql()) {
            //Gets the parsed PGQL result from which we extrapolate the GraphQuery
//            PgqlResult result = pgql.parse("SELECT a,b FROM g MATCH (a) -> (b) -> (d) -> (a) -> (c) -> (e) -> (f) -> (g) -> (c) (c) -> (a) WHERE a.ID = 0");
            PgqlResult result = pgql.parse("SELECT * FROM example MATCH (a)->(b)->(c)->(d)->(e)->(f)->(g)->(h)->(i)->(j)->(k)->(l)->(m)->(a), (a)->(n)->(o)->(d) (o)->(p)->(h), (n)->(q)->(l), (p)->(q)");

            //Contains the AST structure with the different operators to be executed on the Graph
            GraphQuery graphQuery = result.getGraphQuery();

            System.out.println(result.isQueryValid());

            QueryPlan plan = planner.customPlan(graphQuery);

            System.out.println(plan);
            //TODO: get a new string from the plan tree
        }

//        testPGX();
    }

    private static void testPGX() throws PgqlException, ExecutionException, InterruptedException {
        String example = "SELECT n.name AS from, m.name AS to, n.\"percentuale di figaggine\" AS sottaceti FROM test MATCH (n:Person) -[e:\"IS ENEMY OF\"]- (m:Person) WHERE n.\"percentuale di figaggine\" >= m.\"percentuale di figaggine\"";

        Pgql pgql = new Pgql();

        //Creates the PGX session
        PgxSession session = makePGXSession("test_abc");

        System.out.println("Loading graph...");

        //Creates and uploads to the server the test graph we are making
        PgxGraph g = GraphMaker.getMoviesDatabase(session);

//        System.out.println("Graph loaded. (with name '" + session.getGraphs().entrySet().stream().findAny().get().getKey() + "')");

        System.out.println("Graph loaded. (with name '" + session.getGraphs().entrySet().stream().findAny().get().getKey() + "')");
        
        do {
            System.out.println("Insert PGQL query (write 'quit' to exit): ");

            //Scanning System.in for new PGQL queries to perform on our test graph
            Scanner scanner = new Scanner(System.in);
            String query = scanner.nextLine();

            //Exits when requested
            if (query.equals("quit")) break;

            try {
                //Parse the query string so we at least know if its syntax is correct
                PgqlResult res = pgql.parse(query);

                if (res.isQueryValid()) {
                    //Performs the query and prints the result table
                    PgqlResultSet r = session.queryPgql(query);
                    r.print();
                } else {
                    System.out.println(res.getErrorMessages());
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        } while (true);

        //Destroys the graph and the session
        g.destroy();
        session.destroy();
    }

    private static PgxGraph testGraph(PgxSession session) throws ExecutionException, InterruptedException {
        return GraphMaker.getRandomEnemyGraph(
                session,
                "test",
                new String[]{"Kien", "Dario", "Davide", "Max", "Ari", "Leti", "Dani", "Gio", "Stef", "Giammi", "Luca", "Paso", "Pit"},
                69);
    }

    private static PgxSession makePGXSession(String name) throws ExecutionException, InterruptedException {
        ServerInstance si = Pgx.getInstance("http://localhost:7007");
        return si.createSession(name);
    }
}
