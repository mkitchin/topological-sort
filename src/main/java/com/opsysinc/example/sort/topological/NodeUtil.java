package com.opsysinc.example.sort.topological;

import java.util.*;
import java.util.logging.Logger;

/**
 * Node utilities.
 * <p>
 * Provides topological sort and supporting capabilities.
 * <p>
 * See NodeData for additional details.
 *
 * @author mkitchin
 */
public class NodeUtil {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(NodeUtil.class.getName());

    /**
     * Private ctor for util classes.
     */
    private NodeUtil() {

    }

    /**
     * Find (build a map of) nodes by type.
     * <p>
     * Supports findNodesSorted().
     *
     * @param input        Collection of nodes to examine.
     * @param target       Keys are node types, values are node id lists.
     * @param isClearFirst True to clear target collection first, false otherwise.
     * @return True if input valid and any types found, false otherwise.
     */
    private static boolean findNodeTypes(final Collection<NodeData> input,
                                         final Map<String, Collection<String>> target,
                                         final boolean isClearFirst) {

        DataUtil.checkNullObject(input, true);
        DataUtil.checkNullObject(target, true);

        boolean result = false;

        if (isClearFirst) {

            target.clear();
            result = true;
        }

        // iterate node data
        for (final NodeData nodeItem : input) {

            // iterate types
            for (final String typeItem : nodeItem.getNodeTypeSet()) {

                // normalize type and get node list
                Collection<String> nodeList = target.get(typeItem);

                if (nodeList == null) {

                    nodeList = new HashSet<>();

                    if (target.put(typeItem, nodeList) == null) {

                        result = true;
                    }
                }

                // add id to set
                nodeList.add(nodeItem.getNodeId());
            }
        }

        return result;
    }

    /**
     * Find (build) node order maps.
     * <p>
     * Supports findNodesSorted().
     *
     * @param input         Collection of nodes to examine.
     * @param isReverseAlso True to add reverse mappings to each result, incorporating
     *                      before- to the after maps and after- to the before maps.
     * @param beforeNodes   Keys are id's in input, values are their before ranks with
     *                      respect to other nodes.
     * @param afterNodes    Keys are id's in input, values are their after ranks with
     *                      respect to other nodes.
     * @param isClearFirst  True to clear target collections first, false otherwise.
     * @return True if input valid and at least one order found, false
     * otherwise.
     */
    private static boolean findNodeOrders(final Collection<NodeData> input,
                                          final boolean isReverseAlso,
                                          final Map<String, Map<String, Long>> beforeNodes,
                                          final Map<String, Map<String, Long>> afterNodes,
                                          final boolean isClearFirst) {

        DataUtil.checkNullObject(input, true);
        DataUtil.checkNullObject(beforeNodes, true);
        DataUtil.checkNullObject(afterNodes, true);

        if (isClearFirst) {

            beforeNodes.clear();
            afterNodes.clear();
        }

        // index input
        final Map<String, Collection<String>> nodeTypes = new HashMap<>();
        NodeUtil.findNodeTypes(input, nodeTypes, false);

        final Map<String, Map<String, Long>> tempBeforeNodes = new HashMap<>();
        final Map<String, Map<String, Long>> tempAfterNodes = new HashMap<>();

        // iterate nodes
        for (final NodeData nodeItem : input) {

            // get/build "before" target map (nodes->ranks).
            Map<String, Long> beforeMap = tempBeforeNodes.get(nodeItem.getNodeId());

            if (beforeMap == null) {

                beforeMap = new HashMap<>();
                tempBeforeNodes.put(nodeItem.getNodeId(), beforeMap);
            }

            // iterate "before" types
            for (final Map.Entry<String, Long> beforeTypeEntry : nodeItem.getBeforeNodeTypeMap().entrySet()) {

                // get all nodes of this type
                final Collection<String> beforeTypeNodes = nodeTypes.get(beforeTypeEntry.getKey());

                if (beforeTypeNodes != null) {

                    // if we've gotten nodes of this type, merge the type-based
                    // rank to the output map (we merge because a given node may
                    // qualify under more than one type).
                    for (final String beforeNodeItem : beforeTypeNodes) {

                        NodeUtil.mergeNodeRanks(beforeNodeItem, beforeTypeEntry.getValue(), beforeMap);
                    }
                }
            }

            // get/build "after" target map (nodes->ranks).
            Map<String, Long> afterMap = tempAfterNodes.get(nodeItem.getNodeId());

            if (afterMap == null) {

                afterMap = new HashMap<>();
                tempAfterNodes.put(nodeItem.getNodeId(), afterMap);
            }

            // iterate "after" types
            for (final Map.Entry<String, Long> afterTypeEntry : nodeItem.getAfterNodeTypeMap().entrySet()) {

                // get all nodes of this type
                final Collection<String> afterTypeNodes = nodeTypes.get(afterTypeEntry.getKey());

                if (afterTypeNodes != null) {

                    // if we've gotten nodes of this type, merge the type-based
                    // rank to the output map (we merge because a given node may
                    // qualify under more than one type).
                    for (final String afterNodeItem : afterTypeNodes) {

                        NodeUtil.mergeNodeRanks(afterNodeItem, afterTypeEntry.getValue(), afterMap);
                    }
                }
            }
        }

        // if reverse mappings are called for, we reverse and or- the main maps
        // into the output (e.g., integrate x->y "before" ranks as y->x "after"
        // ranks).
        if (isReverseAlso) {

            final Map<String, Map<String, Long>> reverseBeforeNodes = new HashMap<>();
            final Map<String, Map<String, Long>> reverseAfterNodes = new HashMap<>();

            // build "before" reverse mapping of x->y as y->x (or:
            // before->after as after->before)
            for (final Map.Entry<String, Map<String, Long>> nodeItem : tempBeforeNodes.entrySet()) {

                // iterate each forward before/after mapping
                for (final Map.Entry<String, Long> rankItem : nodeItem.getValue().entrySet()) {

                    // get/build reverse mapping
                    Map<String, Long> afterMap = reverseAfterNodes.get(rankItem.getKey());

                    if (afterMap == null) {

                        afterMap = new HashMap<>();
                        reverseAfterNodes.put(rankItem.getKey(), afterMap);
                    }

                    // merge forward with reverse mapping
                    NodeUtil.mergeNodeRanks(nodeItem.getKey(), rankItem.getValue(), afterMap);
                }
            }

            // build a "after" reverse mapping of x->y as y->x (or:
            // after->before as before->after)
            for (final Map.Entry<String, Map<String, Long>> nodeItem : tempAfterNodes.entrySet()) {

                // iterate each forward after/before mapping
                for (final Map.Entry<String, Long> rankItem : nodeItem.getValue().entrySet()) {

                    // get/build reverse mapping
                    Map<String, Long> beforeMap = reverseBeforeNodes.get(rankItem.getKey());

                    if (beforeMap == null) {

                        beforeMap = new HashMap<>();
                        reverseBeforeNodes.put(rankItem.getKey(), beforeMap);
                    }

                    // merge forward with reverse mapping
                    NodeUtil.mergeNodeRanks(nodeItem.getKey(), rankItem.getValue(), beforeMap);
                }
            }

            // integrate "before" reverse mapping into output maps (we
            // don't do this above because it could create infinite loops).
            for (final Map.Entry<String, Map<String, Long>> nodeItem : reverseBeforeNodes.entrySet()) {

                // get/build "before" map
                Map<String, Long> beforeMap = tempBeforeNodes.get(nodeItem.getKey());

                if (beforeMap == null) {

                    beforeMap = new HashMap<>();
                    tempBeforeNodes.put(nodeItem.getKey(), beforeMap);
                }

                // add in new nodes
                for (final Map.Entry<String, Long> rankItem : nodeItem.getValue().entrySet()) {

                    NodeUtil.mergeNodeRanks(rankItem.getKey(), rankItem.getValue(), beforeMap);
                }
            }

            // integrate "after" reverse mapping into output maps (we don't
            // do this above because it could create infinite loops).
            for (final Map.Entry<String, Map<String, Long>> nodeItem : reverseAfterNodes.entrySet()) {

                // get/build after map
                Map<String, Long> afterMap = tempAfterNodes.get(nodeItem.getKey());

                if (afterMap == null) {

                    afterMap = new HashMap<>();
                    tempAfterNodes.put(nodeItem.getKey(), afterMap);
                }

                // add in new nodes
                for (final Map.Entry<String, Long> rankItem : nodeItem.getValue().entrySet()) {

                    NodeUtil.mergeNodeRanks(rankItem.getKey(), rankItem.getValue(), afterMap);
                }
            }
        }

        beforeNodes.putAll(tempBeforeNodes);
        afterNodes.putAll(tempAfterNodes);

        return (!tempBeforeNodes.isEmpty() ||
                !tempAfterNodes.isEmpty());
    }

    /**
     * Merge a rank pair with a map of same, max'ing the value with that in the
     * map or adding it.
     * <p>
     * Supports findNodesSorted().
     *
     * @param rankKey   Rank key.
     * @param rankValue Rank value.
     * @param target    Target map.
     * @return True if the map size changed (i.e., the key was new).
     */
    private static boolean mergeNodeRanks(final String rankKey,
                                          final long rankValue,
                                          final Map<String, Long> target) {

        DataUtil.checkEmptyString(rankKey, true);
        DataUtil.checkNullObject(target, true);

        boolean result = false;

        // fetch previous (existing) rank
        final Long prevRank = target.get(rankKey);

        if (prevRank == null) {

            // if no previous rank, put current
            result = true;
            target.put(rankKey, rankValue);

        } else {

            // if previous rank, max with current
            target.put(rankKey, Math.max(rankValue, prevRank));
        }

        return result;
    }

    /**
     * Find (build a list of) nodes and ranks sorted by dependency.
     * <p>
     * Leverages (a) node (as in DAG) types, and (b) node before/after type sets to order nodes in "ranks" (orders).
     * Typically, ranks are purely ordinal, as in 0 (first), 1 (second), etc. If this is the case, baseRank and all
     * integers in results are just a traversal order.
     * <p>
     * E.g., if you're just trying to figure out what order to install software dependencies based solely on (a)
     * types and (b) before/after relationships, order is all that matters and ranks can all be (intervals of) 1.
     * <p>
     * E.g., if you're trying to start a bunch of processes with (a) types, (b) before/after relationships and
     * (c) integral delays, ranks may be used to represent those delays, because the end goal is an ordered,
     * cumulative timeline.
     *
     * @param baseRank     Starting rank, offsetting node-to-node ranks (used for layered runs; good default=0L).
     * @param input        Collection of nodes to examine.
     * @param target       Target for sorted nodes.
     * @param isClearFirst True to clear target collections first, false otherwise.
     * @return True if input valid and nodes found, false otherwise.
     * @throws IllegalArgumentException thrown if cycle detected.
     */
    public static boolean findNodesSorted(final long baseRank,
                                          final Collection<NodeData> input,
                                          final Collection<NodeData> target,
                                          final boolean isClearFirst) {

        DataUtil.checkNullObject(input, true);
        DataUtil.checkNullObject(target, true);

        boolean result = false;

        if (isClearFirst) {

            target.clear();
            result = true;
        }

        final Map<Long, Collection<NodeData>> tempMap = new TreeMap<>();
        NodeUtil.findNodesSorted(baseRank, input, tempMap, false);

        for (final Collection<NodeData> orderItem : tempMap.values()) {

            for (final NodeData nodeItem : orderItem) {

                if (target.add(nodeItem)) {

                    result = true;
                }
            }
        }

        return result;
    }

    /**
     * Find (build a list of) nodes and ranks sorted by dependency.
     * <p>
     * Leverages (a) node (as in DAG) types, and (b) node before/after type sets to order nodes in "ranks" (orders).
     * Typically, ranks are purely ordinal, as in 0 (first), 1 (second), etc. If this is the case, baseRank and all
     * integers in results are just a traversal order.
     * <p>
     * E.g., if you're just trying to figure out what order to install software dependencies based solely on (a)
     * types and (b) before/after relationships, order is all that matters and ranks can all be (intervals of) 1.
     * <p>
     * E.g., if you're trying to start a bunch of processes with (a) types, (b) before/after relationships and
     * (c) integral delays, ranks may be used to represent those delays, because the end goal is an ordered,
     * cumulative timeline.
     *
     * @param baseRank     Starting rank, offsetting node-to-node ranks (used for layered runs; good default=0L).
     * @param input        Collection of nodes to examine.
     * @param targetRanks  Target for sorted node id's/ranks.
     * @param isClearFirst True to clear target collections first, false otherwise.
     * @return True if input valid and nodes found, false otherwise.
     * @throws IllegalArgumentException thrown if cycle detected.
     */
    public static boolean findNodesSorted(final long baseRank,
                                          final Collection<NodeData> input,
                                          final Map<Long, Collection<NodeData>> targetRanks,
                                          final boolean isClearFirst)
            throws IllegalArgumentException {

        DataUtil.checkNullObject(input, true);
        DataUtil.checkNullObject(targetRanks, true);

        if (isClearFirst) {

            targetRanks.clear();
        }

        // index input
        final Map<String, NodeData> inputMap = new HashMap<>();

        for (final NodeData item : input) {

            inputMap.put(item.getNodeId(), item);
        }

        // before/after relationships (edges)
        final Map<String, Map<String, Long>> beforeNodes = new HashMap<>();
        final Map<String, Map<String, Long>> afterNodes = new HashMap<>();
        NodeUtil.findNodeOrders(input, true, beforeNodes, afterNodes, false);

        // nodes we're checking
        final Set<String> workNodes = new HashSet<>(inputMap.keySet());

        // tracks done nodes (traversal finished)
        final LinkedHashSet<String> doneNodes = new LinkedHashSet<>();

        // tracks in-progress nodes (traversal in progress)
        final LinkedHashSet<String> checkingNodes = new LinkedHashSet<>();

        // destination for sorted results
        final List<String> currSortedNodeIds = new ArrayList<>();
        final List<String> allSortedNodeIds = new ArrayList<>();

        int prevSize = workNodes.size();

        while (true) {

            // iterate with recursive method
            for (final String item : workNodes) {

                // clear working set.
                currSortedNodeIds.clear();

                // sort
                NodeUtil.sortNodeIds(item, afterNodes, doneNodes, checkingNodes, currSortedNodeIds);

                // add to aggregate set
                allSortedNodeIds.addAll(currSortedNodeIds);
            }

            // remove nodes we're done with
            workNodes.removeAll(doneNodes);
            workNodes.removeAll(checkingNodes);

            // if we've worked down the node list or the list didn't change, bail
            if (workNodes.isEmpty() ||
                    (workNodes.size() == prevSize)) {

                break;
            }

            prevSize = workNodes.size();
        }

        // if any nodes are left over (orphans), add to the beginning
        if (!workNodes.isEmpty()) {

            for (final String item : workNodes) {

                allSortedNodeIds.add(0, item);
            }
        }

        // build aggregate ranks from base rank + individual, node-to-node
        // ranks (viable after an order is established, above).
        final List<Long> sortedRanks = new ArrayList<>();

        // iterate sorted nodes
        for (int ctr1 = 0; ctr1 < allSortedNodeIds.size(); ctr1++) {

            // start with base rank for each node
            long currRank = baseRank;

            // get current node id and map of node-to-node ranks for that node id
            final String currNodeId = allSortedNodeIds.get(ctr1);
            final Map<String, Long> nodeRanks = afterNodes.get(currNodeId);

            // iterate sorted nodes preceding current (above) one (first one
            // only gets base rank)
            for (int ctr2 = 0; ctr2 < ctr1; ctr2++) {

                // get node id and cumulative rank for that, earlier node id
                final String prevNodeId = allSortedNodeIds.get(ctr2);
                final Long prevRank = sortedRanks.get(ctr2);

                // get any node-to-node rank between the current and
                // earlier node
                final Long nodeRank = nodeRanks.get(prevNodeId);

                if (nodeRank != null) {

                    // current rank equals earlier, cumulative rank +
                    // node-to-node rank, maxed with itself (running total)
                    currRank = Math.max(currRank, prevRank + nodeRank);
                }
            }

            // tack on to the results
            sortedRanks.add(currRank);
        }

        // dereference the found id's into node data.
        for (int ctr = 0;
             ctr < Math.min(allSortedNodeIds.size(), sortedRanks.size());
             ctr++) {

            final String nodeId = allSortedNodeIds.get(ctr);
            final long rank = sortedRanks.get(ctr);

            Collection<NodeData> sortedNodes = targetRanks.get(rank);

            if (sortedNodes == null) {

                sortedNodes = new ArrayList<>();
                targetRanks.put(rank, sortedNodes);
            }

            sortedNodes.add(inputMap.get(nodeId));
        }

        return !allSortedNodeIds.isEmpty();
    }

    /**
     * Recursive node topological sort method (re: Wikipedia "topological sort"
     * pseudocode -- Cormen, Trajan, et al.).
     * <p>
     * Supports findNodesSorted().
     *
     * @param currNodeId    Node to traverse.
     * @param afterNodes    Map of nodes to other nodes they're supposed to follow (i.e.,
     *                      edges).
     * @param doneNodes     Set of nodes completely evaluated.
     * @param checkingNodes Set of nodes in evlauation.
     * @throws IllegalArgumentException Thrown if start order cycle detected.
     */
    private static void sortNodeIds(final String currNodeId,
                                    final Map<String, Map<String, Long>> afterNodes,
                                    final Set<String> doneNodes,
                                    final Set<String> checkingNodes,
                                    final Collection<String> sortedNodeIds)
            throws IllegalArgumentException {

        DataUtil.checkEmptyString(currNodeId, true);
        DataUtil.checkNullObject(afterNodes, true);
        DataUtil.checkNullObject(doneNodes, true);
        DataUtil.checkNullObject(checkingNodes, true);
        DataUtil.checkNullObject(sortedNodeIds, true);

        // if node is in the check set, we've hit a cycle (not a DAG)
        if (checkingNodes.contains(currNodeId)) {

            throw new IllegalArgumentException("node order cycle - " + checkingNodes + " <-> " + currNodeId);

            // if node is not done with, traverse
        } else if (!doneNodes.contains(currNodeId)) {

            // track to detect cycles
            checkingNodes.add(currNodeId);

            // traverse edges from this node to others ("after" relationships)
            final Map<String, Long> nodeList = afterNodes.get(currNodeId);

            if ((nodeList != null) && !nodeList.isEmpty()) {

                // iterate edges, recurse (depth-first search)
                for (final Map.Entry<String, Long> item : nodeList.entrySet()) {

                    NodeUtil.sortNodeIds(item.getKey(), afterNodes, doneNodes, checkingNodes, sortedNodeIds);
                }
            }

            // remove cycle check
            checkingNodes.remove(currNodeId);

            // mark as done
            doneNodes.add(currNodeId);

            // add to end of result (depth-first search, so by now deeper levels
            // have been added)
            sortedNodeIds.add(currNodeId);
        }
    }
}
