package it.polimi.pgql.queryplanner.statistics;

public enum StructureType {
    CLIQUE(0.8f),
    CYCLE(0.5f),
    BRANCHING_NODE(0.3f),
    LINE(0);

    private float baseValue;

    StructureType(float baseValue) {
        this.baseValue = baseValue;
    }

    public float getBaseValue() {
        return baseValue;
    }
}
