
# COMP1206-DAGSort-JUnit-Test
A JUnit Test created to test for a valid DAG graph sorting algorithm.

Can be ran using the following command:
`java -cp .:junit.jar:hamcrest-core.jar org.junit.runner.JUnitCore DAGSortTest`

This does not test for specific outputs, rather ones that conform to the general rules for a topological ordering, this is because there are multiple correct topological orderings for a given DAG graph. (https://en.wikipedia.org/wiki/Topological_sorting). It ensures that the DAGSort algorithm correctly throws exceptions when non-valid input edges/graphs are given.
