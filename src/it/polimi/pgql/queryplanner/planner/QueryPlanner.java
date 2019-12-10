package it.polimi.pgql.queryplanner.planner;

import it.polimi.pgql.queryplanner.planner.operators.CartesianProductOperator;
import it.polimi.pgql.queryplanner.planner.operators.NeighborMatchOperator;
import it.polimi.pgql.queryplanner.planner.operators.RootVertexMatchOperator;
import oracle.pgql.lang.ir.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class QueryPlanner {

    public QueryPlan customPlan(GraphQuery query) {
        GraphPattern graphPattern = query.getGraphPattern();

        CycleFinder finder = new CycleFinder(query);
        finder.printCycles();

        for (QueryVertex vertex : query.getGraphPattern().getVertices()) {
            List<QueryExpression.BinaryExpression> constraints = getVertexConstraints(vertex, graphPattern);


        }

        return null;
    }

    private List<QueryExpression.BinaryExpression> getVertexConstraints(QueryVertex qv, GraphPattern gp) {
        List<QueryExpression.BinaryExpression> list = new ArrayList<>();

        for (QueryExpression constraint : gp.getConstraints()) {
            if (constraint instanceof QueryExpression.BinaryExpression) {
                QueryExpression.BinaryExpression be = (QueryExpression.BinaryExpression) constraint;

                boolean first = be.getExp1() instanceof QueryExpression.PropertyAccess
                        && ((QueryExpression.PropertyAccess)be.getExp1()).getVariable().equals(qv);
                boolean second = be.getExp2() instanceof QueryExpression.PropertyAccess
                        && ((QueryExpression.PropertyAccess)be.getExp2()).getVariable().equals(qv);

                if (first || second) {
                    list.add(be);
                }
            }
        }

        return list;
    }

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
                previousPlan = new RootVertexMatchOperator(vertex);
                doneVertices.add(vertex);
                continue;
            } // Try to find a vertex that we already planned from which we can expand

            QueryVertex vertexFromWhichWeCanExpand = null;
            boolean outgoing = false;

            for (QueryVertex plannedVertex : graphQuery.getGraphPattern().getVertices()) {
                boolean found = false;

                for (VertexPairConnection connection : graphQuery.getGraphPattern().getConnections()) {
                    if (connection.getSrc() == plannedVertex && connection.getDst() == vertex) {
                        vertexFromWhichWeCanExpand = connection.getSrc();
                        outgoing = true;
                        found = true;
                        break;
                    }

                    if (connection.getDst() == plannedVertex && connection.getDst() == vertex) {
                        vertexFromWhichWeCanExpand = connection.getDst();
                        outgoing = false;
                        found = true;
                        break;
                    }
                }

                if (found) break;
            }

            QueryPlan vertexPlan;

            if (vertexFromWhichWeCanExpand != null) { // We can expand using a neighbor match
                vertexPlan = new NeighborMatchOperator(vertexFromWhichWeCanExpand, vertex, outgoing);
            } else { // We need to generate cartesian product
                vertexPlan = new CartesianProductOperator(previousPlan, new RootVertexMatchOperator(vertex));
            } // Do the correct linking

            vertexPlan.getChildren().add(previousPlan);
            previousPlan.setParent(vertexPlan); // Record plan and mark vertex as done
            previousPlan = vertexPlan;
            doneVertices.add(vertex);
        }

        return previousPlan;
    }
}