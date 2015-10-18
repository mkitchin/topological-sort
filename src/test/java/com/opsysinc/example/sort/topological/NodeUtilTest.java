package com.opsysinc.example.sort.topological;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Note utilities test.
 * <p>
 * Created by Michael J. Kitchin on 10/17/2015.
 */
public class NodeUtilTest {

    /**
     * Test a valid, diamond topology, e.g.:
     * <p>
     * <pre>
     *     2
     *   /  \
     *  1    4
     *  \   /
     *    3
     * </pre>
     * ...with "simple" results, not returning specific ranks.
     */
    @Test
    public void testSimpleGoodSort() {

        final List<NodeData> inputList = new ArrayList<>();

        final NodeData firstNode = new NodeData("Node1");
        inputList.add(firstNode);

        firstNode.getNodeTypeSet().addAll(
                Arrays.asList("foo", "bar"));
        firstNode.getAfterNodeTypeMap().putAll(
                Collections.singletonMap("burp", 1L));
        firstNode.getBeforeNodeTypeMap().putAll(
                Collections.singletonMap("derp", 1L));

        final NodeData secondNode = new NodeData("Node2");
        inputList.add(secondNode);

        secondNode.getNodeTypeSet().addAll(
                Arrays.asList("itty", "bitty"));
        secondNode.getAfterNodeTypeMap().putAll(
                Collections.singletonMap("foo", 1L));

        final NodeData thirdNode = new NodeData("Node3");
        inputList.add(thirdNode);

        thirdNode.getNodeTypeSet().addAll(
                Arrays.asList("zip", "bitty"));
        thirdNode.getAfterNodeTypeMap().putAll(
                Collections.singletonMap("bar", 1L));
        thirdNode.getBeforeNodeTypeMap().putAll(
                Collections.singletonMap("derp", 1L));

        final NodeData fourthNode = new NodeData("Node4");
        inputList.add(fourthNode);

        fourthNode.getNodeTypeSet().addAll(
                Arrays.asList("derp", "itty"));
        fourthNode.getAfterNodeTypeMap().putAll(
                Collections.singletonMap("bitty", 1L));
        fourthNode.getBeforeNodeTypeMap().putAll(
                Collections.singletonMap("boop", 1L));

        final List<NodeData> outputList = new ArrayList<>();
        NodeUtil.findNodesSorted(0L, inputList, outputList, false);

        final String outputListText = outputList.toString();
        assertEquals(outputListText, "[Node1, Node3, Node2, Node4]");

        System.out.println("testSimpleGoodSort() = " + outputListText);
    }

    /**
     * Test a valid, diamond topology, e.g.:
     * <p>
     * <pre>
     *     2
     *   /  \
     *  1    4
     *  \   /
     *    3
     * </pre>
     * ...with "full" results, returning specific ranks.
     */
    @Test
    public void testFullGoodSort() {

        final List<NodeData> inputList = new ArrayList<>();

        final NodeData firstNode = new NodeData("Node1");
        inputList.add(firstNode);

        firstNode.getNodeTypeSet().addAll(
                Arrays.asList("foo", "bar"));
        firstNode.getAfterNodeTypeMap().putAll(
                Collections.singletonMap("burp", 1L));
        firstNode.getBeforeNodeTypeMap().putAll(
                Collections.singletonMap("derp", 1L));

        final NodeData secondNode = new NodeData("Node2");
        inputList.add(secondNode);

        secondNode.getNodeTypeSet().addAll(
                Arrays.asList("itty", "bitty"));
        secondNode.getAfterNodeTypeMap().putAll(
                Collections.singletonMap("foo", 1L));

        final NodeData thirdNode = new NodeData("Node3");
        inputList.add(thirdNode);

        thirdNode.getNodeTypeSet().addAll(
                Arrays.asList("zip", "bitty"));
        thirdNode.getAfterNodeTypeMap().putAll(
                Collections.singletonMap("bar", 1L));
        thirdNode.getBeforeNodeTypeMap().putAll(
                Collections.singletonMap("derp", 1L));

        final NodeData fourthNode = new NodeData("Node4");
        inputList.add(fourthNode);

        fourthNode.getNodeTypeSet().addAll(
                Arrays.asList("derp", "itty"));
        fourthNode.getAfterNodeTypeMap().putAll(
                Collections.singletonMap("bitty", 1L));
        fourthNode.getBeforeNodeTypeMap().putAll(
                Collections.singletonMap("boop", 1L));

        final Map<Long, Collection<NodeData>> outputMap = new TreeMap<>();
        NodeUtil.findNodesSorted(0L, inputList, outputMap, false);

        final String outputMapText = outputMap.toString();
        assertEquals(outputMapText, "{0=[Node1], 1=[Node3, Node2], 2=[Node4]}");

        System.out.println("testFullGoodSort() = " + outputMapText);
    }

    /**
     * Test a cyclic, diamond topology, e.g.:
     * <p>
     * <pre>
     *     2
     *   /  \
     *  1 -> 4
     *  \   /
     *    3
     * </pre>
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBadSort() {

        final List<NodeData> inputList = new ArrayList<>();

        final NodeData firstNode = new NodeData("Node1");
        inputList.add(firstNode);

        firstNode.getNodeTypeSet().addAll(
                Arrays.asList("foo", "bar"));
        firstNode.getAfterNodeTypeMap().putAll(
                Collections.singletonMap("itty", 1L));
        firstNode.getBeforeNodeTypeMap().putAll(
                Collections.singletonMap("derp", 1L));

        final NodeData secondNode = new NodeData("Node2");
        inputList.add(secondNode);

        secondNode.getNodeTypeSet().addAll(
                Arrays.asList("itty", "bitty"));
        secondNode.getAfterNodeTypeMap().putAll(
                Collections.singletonMap("foo", 1L));

        final NodeData thirdNode = new NodeData("Node3");
        inputList.add(thirdNode);

        thirdNode.getNodeTypeSet().addAll(
                Arrays.asList("zip", "bitty"));
        thirdNode.getAfterNodeTypeMap().putAll(
                Collections.singletonMap("derp", 1L));
        thirdNode.getBeforeNodeTypeMap().putAll(
                Collections.singletonMap("derp", 1L));

        final NodeData fourthNode = new NodeData("Node4");
        inputList.add(fourthNode);

        fourthNode.getNodeTypeSet().addAll(
                Arrays.asList("derp", "itty"));
        fourthNode.getAfterNodeTypeMap().putAll(
                Collections.singletonMap("bitty", 1L));
        fourthNode.getBeforeNodeTypeMap().putAll(
                Collections.singletonMap("boop", 1L));

        final List<NodeData> outputList = new ArrayList<>();
        NodeUtil.findNodesSorted(0L, inputList, outputList, false);

        fail("testBadSort() should throw IllegalArgumentException.");
    }
}
