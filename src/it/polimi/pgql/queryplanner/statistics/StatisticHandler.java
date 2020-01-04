package it.polimi.pgql.queryplanner.statistics;

import it.polimi.pgql.queryplanner.planner.QueryPlanner;
import oracle.pgql.lang.ir.GraphPattern;
import oracle.pgql.lang.ir.QueryExpression;
import oracle.pgql.lang.ir.QueryVertex;

import java.util.List;

/**
 * Statistics used only refer to the letter of the vertex
 * higher in alphabetic order means more selective
 *
 * TODO: add real statistic handling providing a graph
 */
public class StatisticHandler {


    public StatisticHandler(/*WHERE TO TAKE THE INFORMATION FROM*/) {

    }

    /**
     * @param vertexConstraints constraints applied on the vertex
     * @return a float in range (0, 1)
     */
    public float getVertexSelectivity(QueryVertex v, List<QueryExpression.BinaryExpression> vertexConstraints) {
//        System.out.println(v.getName());
        return (float) v.getName().toCharArray()[0] / 26;
    }

    public float getStructureSelectivity(List<QueryVertex> struct, StructureType type, GraphPattern gp) {
        return type.getBaseValue()
                + 1 - (struct.stream()
                .map(v -> getVertexSelectivity(v, QueryPlanner.getVertexConstraints(v, gp)))
                .reduce((prod, el) -> prod *= (1 - el)).orElse(0f))
                + struct.size();
    }


}
