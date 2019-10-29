package it.polimi.pgql.queryplanner;

import it.polimi.pgql.queryplanner.plans.CartesianProductPlan;
import it.polimi.pgql.queryplanner.plans.NeighborMatchPlan;
import it.polimi.pgql.queryplanner.plans.QueryPlan;
import it.polimi.pgql.queryplanner.plans.RootVertexMatchPlan;
import oracle.pgql.lang.ir.GraphQuery;
import oracle.pgql.lang.ir.QueryVertex;
import oracle.pgql.lang.ir.VertexPairConnection;

import java.util.HashSet;
import java.util.Set;

public class QueryPlanner {

    /**
     * Basic implementation of the plan generator from the slides
     *
     * @param graphQuery parsed from a PGQL formatted String
     * @return QueryPlan root
     */
    public QueryPlan generatePlan(GraphQuery graphQuery) {
        Set<QueryVertex> doneVertices = new HashSet<>();
        QueryPlan previousPlan = null;

        for (QueryVertex vertex : graphQuery.getGraphPattern().getVertices()) {
            if (doneVertices.contains(vertex)) {
                continue;
            }

            if (previousPlan == null) {
                previousPlan = new RootVertexMatchPlan(vertex);
                doneVertices.add(vertex);
                continue;
            } // Try to find a vertex that we already planned from which we can expand

            QueryVertex vertexFromWhichWeCanExpand = null;
            boolean outgoing = false;

            for (QueryVertex plannedVertex : graphQuery.getGraphPattern().getVertices()) {
                for (VertexPairConnection connection : graphQuery.getGraphPattern().getConnections()) {
                    if (connection.getSrc() == plannedVertex && connection.getDst() == vertex) {
                        vertexFromWhichWeCanExpand = connection.getSrc();
                        outgoing = true;
                        break;
                    }
                    if (connection.getDst() == plannedVertex && connection.getDst() == vertex) {
                        vertexFromWhichWeCanExpand = connection.getDst();
                        outgoing = false;
                        break;
                    }
                }
            }

            QueryPlan vertexPlan;

            if (vertexFromWhichWeCanExpand != null) { // We can expand using a neighbor match
                vertexPlan = new NeighborMatchPlan(vertexFromWhichWeCanExpand, vertex, outgoing);
            } else { // We need to generate cartesian product
                vertexPlan = new CartesianProductPlan(previousPlan, new RootVertexMatchPlan(vertex));
            } // Do the correct linking

            vertexPlan.child = previousPlan;
            previousPlan.parent = vertexPlan; // Record plan and mark vertex as done
            previousPlan = vertexPlan;
            doneVertices.add(vertex);
        }

        return previousPlan;
    }

    /**
     *
     * @param root Root of the AST tree of the query plan
     * @return the new PGQL formatted string to use for the actual query
     */
    public String getPGQLQueryStringFromPlan(QueryPlan root) {
        StringBuilder outputBuilder = new StringBuilder();

        while(root != null) {
            //TODO: actually add to the StringBuider the string equivalent of the query plan operator
            outputBuilder.append(/*things*/  " ");

            root = root.child;
        }

        //Reverses the tree since we need post-order traversal
        outputBuilder.reverse();

        return outputBuilder.toString();
    }
}