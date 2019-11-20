package it.polimi.pgql.queryplanner.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GraphVertex {

    private final String name;
    private final List<String> properties = new ArrayList<>();

    public GraphVertex(String name, String... properties) {
        this.name = name;
        this.properties.addAll(Arrays.asList(properties));
    }

    public String getName() {
        return this.name;
    }

    public List<String> getProperties() {
        return this.properties;
    }
}
