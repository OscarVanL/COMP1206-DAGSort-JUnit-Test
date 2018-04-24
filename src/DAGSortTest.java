import org.junit.Test;
import org.omg.CORBA.DynAnyPackage.Invalid;

import java.util.Arrays;

import static org.junit.Assert.*;

public class DAGSortTest {

    /**
     * Passes null instead of a 2D array representing a DAG. The Sort should throw a NullPointerException.
     */
    @Test(expected = NullPointerException.class)
    public void testNullDetection() throws NullPointerException {
        try {
            DAGSort.sortDAG(null);
        } catch (CycleDetectedException | InvalidNodeException e) {
            fail("Unexpected other exception thrown");
        }
    }

    /**
     * Passes an empty 2D array, the sort should return an empty array, but not crash or throw any exceptions.
     */
    @Test
    public void testEmptyGraph() {
        int[][] edges = {};
        int[] output = {};

        try {
            output = DAGSort.sortDAG(edges);
        } catch (CycleDetectedException | InvalidNodeException e) {
            fail("Unexpected other exception thrown");
        } catch (NullPointerException e) {
            fail("Incorrectly threw NullPointerException for empty graph");
        }

        assertEquals("Does not return an empty array for an empty graph", Arrays.toString(new int[][]{}), Arrays.toString(output));
    }

    /**
     * Tests the DAGSort with a singleton. For any valid topological sort this will result in the same expected output.
     */
    @Test
    public void testSingleton() {
        int[][] edges = { {} };
        int[] output = {};

        try {
            output = DAGSort.sortDAG(edges);
        }  catch (CycleDetectedException | InvalidNodeException e) {
            fail("Unexpected other exception thrown");
        } catch (NullPointerException e) {
            fail("Incorrectly threw NullPointerException for graph with single node");
        }

        assertEquals("Does not return correct result from graph with single node", Arrays.toString(new int[]{0}), Arrays.toString(output));
    }

    /**
     * The number of nodes in the DAG 2D array and the nodes in the output topological ordering must match.
     */
    @Test
    public void testNodesCountMatch() {
        //We sneakily add in a couple of extra nodes that aren't connected (6 and 7), but these should still end up in the result.
        int[][] edges = { {3}, {3}, {}, {2}, {3}, {3}, {}, {}};
        int[] result = {};

        try {
            result = DAGSort.sortDAG(edges);
        } catch (CycleDetectedException | InvalidNodeException e) {
            fail("Unexpected other exception thrown");
        }

        assertEquals("Expected the number of elements in edges and results to be the same", edges.length, result.length);
    }

    /**
     * Tests for valid topological ordering from the DAGSort. https://en.wikipedia.org/wiki/Topological_sorting
     * This is that for the output ordering of vertices, for every directed edge on the graph uv from vertex u to vertex v, u comes before v in the ordering.
     * Example Input: { {3}, {3}, {}, {2, 5}, {3}, {} } must result in a linear output where 4, 1, 0 come before 3 and 3 comes before 2 and 5.
     * Example Output: {0, 1, 4, 3, 5, 2} is a valid topological ordering.
     */
    @Test
    public void testTopologicallyOrdered() {
        int[][] edges = { {3}, {3}, {}, {2, 5}, {3}, {}};
        int[] topologicalOutput = {};

        try {
            topologicalOutput = DAGSort.sortDAG(edges);
        } catch (CycleDetectedException | InvalidNodeException e) {
            fail("Unexpected other exception thrown");
        }

        Integer nodeU = null;
        Integer nodeV = null;

        //We check each node in the topological output to ensure that the ordering of nodes is valid.
        for (int nodeToCheck = 0; nodeToCheck < topologicalOutput.length; nodeToCheck++) {
            for (int u=0; u < edges[nodeToCheck].length; u++) { //Enables us to iterate through each sub-node of the node to check
                for (int v=0; v < topologicalOutput.length; v++) { //Enables us to iterate through every node in the output topological ordering
                    //First we identify what index in the topological ordering our nodeToCheck is in.
                    if (nodeToCheck == topologicalOutput[u]) {
                        nodeU = v;
                    }
                    //Then we find the index of the topological ordering 'v' vertex (any sub-node of our nodeToCheck)
                    if (edges[nodeToCheck].length > 0) {
                        if (edges[nodeToCheck][u] == topologicalOutput[v]) {
                            nodeV = v;
                        }
                    }
                    //We've found two valid u and v nodes, now to check if they actually break the rules
                    if (nodeU != null && nodeV != null) {
                        if (nodeU > nodeV) {
                            fail("Output is not in a valid topological ordering");
                        }
                        //If we don't fail, these two u and v nodes need to be reset as they conform to the rules of topological ordering!
                        nodeU = null;
                        nodeV = null;
                    }
                }
            }
        }
    }

    /**
     * Tests for instances where we refer to a node that does not exist (we have put node 0 having a path to node 1, but node 1 is not defined.
     * It should throw a InvalidNodeException if working as intended.
     */
    @Test(expected = InvalidNodeException.class)
    public void testReferralToInvalidNode() throws InvalidNodeException {
        int[][] invalidNodeEdge = { {1} };

        try {
            DAGSort.sortDAG(invalidNodeEdge);
        } catch (CycleDetectedException e) {
            fail("Unexpected other exception thrown");
        }
    }

    /*
     * Tests an instance where a node 0 of the graph has loads of children nodes (1, 2, 3, 4, 5, 6, ... are all child nodes)
     * Our test simply ensures that there are no exceptions thrown incorrectly, and that node 0 appears before any other node.
     */
    @Test
    public void testManyChildren() {
        //A graph with a single node 0 with 25 connected child nodes
        int[][] edges = { {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 23, 24, 25}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {} };
        int[] output = {};

        try {
            output = DAGSort.sortDAG(edges);
        } catch (CycleDetectedException | InvalidNodeException e) {
            fail("Unexpected other exception thrown");
        }

        //The 0th element of the output should be 0 because "for every directed edge on the graph uv from vertex u to vertex v, u comes before v in the ordering."
        assertEquals("Ordering of output topological ordering is wrong.", 0, output[0]);
    }

    /**
     * Passes a graph containing cycles to the DAGSort, it should throw a CycleDetectedException if working as intended.
     */
    @Test(expected = CycleDetectedException.class)
    public void testCycles() throws CycleDetectedException {
        //A graph containing a cycle
        int[][] edgesWithCycle = { {1}, {2}, {3, 4}, {1}, {} };

        try {
            DAGSort.sortDAG(edgesWithCycle);
        } catch (InvalidNodeException e) {
            fail("Unexpected other exception thrown");
        }
    }

    //Passes a graph containing a loop to the DAGSort, this is where a node maps to itself. It should throw a CycleDetectedException.

    /**
     * Passes a graph containing a loop (a vertex with a path to itself) to the DAGSort, it should throw a CycleDetectedException if working as intended.
     */
    @Test(expected = CycleDetectedException.class)
    public void testLoopCycles() throws CycleDetectedException{
        //A graph containing a cycle
        int[][] edgesWithLoop = { {3}, {3}, {}, {2, 3, 5}, {3}, {}};

        try {
            DAGSort.sortDAG(edgesWithLoop);
        } catch (InvalidNodeException e) {
            fail("Unexpected other exception thrown");
        }
    }

    //Tests a graph containing a negative node, it should throw an InvalidNodeException exception if functioning properly.
    @Test(expected = InvalidNodeException.class)
    public void testNegativeNodes() throws InvalidNodeException {
        //A graph mapping to a negative node
        int[][] edgesWithLoop = { {3}, {3}, {}, {2, 5}, {3}, {-1}};

        try {
            DAGSort.sortDAG(edgesWithLoop);
        } catch (CycleDetectedException e) {
            fail("Unexpected other exception thrown");
        }
    }

}