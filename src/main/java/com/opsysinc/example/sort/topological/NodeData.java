package com.opsysinc.example.sort.topological;

import java.util.*;

/**
 * Node data.
 * <p>
 * A "node" is something with (a) an "ID" unique among other nodes
 * in a set, (b) zero or more "types" that classify and (c) before/after
 * type sets, indicating which (other) types this node should appear
 * before/after in a given sort.
 * <p>
 * Types may be employed as/mixed with aliases (alternate node
 * ID's), per use case.
 * <p>
 * This could readily be re-done as an interface, abstract base, etc.
 * <p>
 * Created by Michael J. Kitchin on 10/08/2015.
 */
public class NodeData {

    /**
     * Node ID.
     */
    private final String nodeId;

    /**
     * Before types.
     */
    private Map<String, Long> beforeNodeTypeMap;

    /**
     * After types.
     */
    private Map<String, Long> afterNodeTypeMap;

    /**
     * Types.
     */
    private Set<String> nodeTypeSet;

    /**
     * Basic ctor.
     */
    public NodeData(final String nodeId) {

        this.nodeId = nodeId;
    }

    /**
     * Checks data.
     */
    private void checkData() {

        this.beforeNodeTypeMap = ((this.beforeNodeTypeMap == null)
                ? Collections.synchronizedMap(new TreeMap<>()) : this.beforeNodeTypeMap);
        this.afterNodeTypeMap = ((this.afterNodeTypeMap == null)
                ? Collections.synchronizedMap(new TreeMap<>()) : this.afterNodeTypeMap);
        this.nodeTypeSet = ((this.nodeTypeSet == null)
                ? Collections.synchronizedSet(new TreeSet<>()) : this.nodeTypeSet);
    }

    /**
     * Gets before type set (by ref).
     *
     * @return Before type set (by ref).
     */
    public Map<String, Long> getBeforeNodeTypeMap() {

        this.checkData();
        return this.beforeNodeTypeMap;
    }

    /**
     * Gets after type set (by ref).
     *
     * @return After type set (by ref).
     */
    public Map<String, Long> getAfterNodeTypeMap() {

        this.checkData();
        return this.afterNodeTypeMap;
    }

    /**
     * Gets type set (by ref).
     *
     * @return Type set (by ref).
     */
    public Set<String> getNodeTypeSet() {

        this.checkData();
        return this.nodeTypeSet;
    }

    /**
     * Gets node ID.
     *
     * @return Node ID.
     */
    public String getNodeId() {

        return this.nodeId;
    }

    @Override
    public String toString() {

        return this.nodeId;
    }
}
