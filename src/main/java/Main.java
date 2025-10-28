import clustering.linkage.impls.AverageLinkageStrategy;
import clustering.linkage.interfaces.LinkageStrategy;
import clustering.models.Cluster;
import clustering.process.strategy.impls.DefaultClusteringAlgorithm;
import clustering.process.strategy.interfaces.ClusteringAlgorithm;
import graphs.*;
import parsers.Jdt;
import parsers.Spoon;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {

        Scanner sc = new Scanner(System.in);
        System.out.println("\nWelcome to the software comprehension application, you need to provide project path and jdk path ");

        // Hardcoded paths
        String projectPath = "C:\\Users\\harch\\Desktop\\Tp28Oct\\Library-Management-System-JAVA-master\\Project";
        String projectSourcePath = projectPath + "/src";
        String jrePath = "C:\\Progra~1\\Java\\jdk-17";

        System.out.println("\nMenu : ");
        System.out.println("1 : Response for questions using JDT.");
        System.out.println("2 : Response for questions using Spoon.");
        System.out.println("0 : Exit.");

        System.out.print("What do you choose : ");
        int input = sc.nextInt();

        while (input < 0 || input > 2) {
            System.out.print("Wrong input, please choose again : ");
            input = sc.nextInt();
        }

        if (input == 0) {
            System.exit(0);
        }

        // --- START OF CORRECTIONS ---
        // 1. Declare variables here, outside the if/else blocks, so their scope is the whole method.
        Graph graph = null;
        CouplingGraph couplingGraph = null;

        if (input == 1) {
            System.out.println("***** Call Graph using JDT *****");
            // 2. Assign the result to the 'graph' variable (don't re-declare it).
            graph = new JDTCallGraph(new Jdt(projectSourcePath, jrePath)).createCallGraph();
        } else if (input == 2) {
            System.out.println("----- Call Graph using Spoon -----");
            // 3. Assign the result to the same 'graph' variable.
            graph = new SpoonCallGraph(new Spoon(projectSourcePath, jrePath)).createCallGraph();
        }

        // 4. Now that we have a 'graph' object, we can process it.
        // This code is no longer duplicated inside each if/else block.
        if (graph != null && !graph.getClassesInvocations().isEmpty()) {
            System.out.println(graph.printInvocations());

            CouplingGraphTools couplingGraphTools = new CouplingGraphTools(graph);
            couplingGraphTools.calculateMetrics();
            couplingGraph = couplingGraphTools.getCouplingGraph(); // Assign to the couplingGraph variable.
            System.out.println(couplingGraph.printCouplingGraph());

            // --- DENDROGRAM LOGIC USING THE ANALYSIS RESULTS ---

            // 1. Get the list of class names from our analysis
            Set<String> classNamesSet = graph.getClassesInvocations().keySet();
            String[] names = classNamesSet.toArray(new String[0]);

            // 2. Generate the distance matrix from the coupling graph
            double[][] distancesMatrix = new double[names.length][names.length];
            for (int i = 0; i < names.length; i++) {
                for (int j = 0; j < names.length; j++) {
                    if (i == j) {
                        distancesMatrix[i][j] = 0;
                    } else {
                        // We use 1.0 - coupling because clustering works on distance (dissimilarity).
                        // High coupling (close to 1) means low distance (close to 0).
                        // Using a max to ensure distance is not negative if coupling is > 1 for any reason.
                        double distance = Math.max(0, 1.0 - couplingGraph.calculateDistance(names[i], names[j]));
                        distancesMatrix[i][j] = distance;
                        distancesMatrix[j][i] = distance; // Matrix is symmetric
                    }
                }
            }

            System.out.println("\n----- Hierarchical Clustering Input -----");
            System.out.println("Classes: " + Arrays.toString(names));
            System.out.println("Distance Matrix:");
            for (double[] row : distancesMatrix) {
                System.out.println(Arrays.toString(row));
            }

            LinkageStrategy strategy = new AverageLinkageStrategy();

            // 3. Create the Dendrogram with the REAL data from our analysis
            Frame f1 = new Dendrogram(createSampleCluster(strategy, names, distancesMatrix));
            f1.setTitle("Dendrogram of " + projectPath.substring(projectPath.lastIndexOf('\\') + 1));
            f1.setSize(800, 600); // Making it bigger to see all class names
            f1.setLocation(400, 200);
            f1.setVisible(true);
        } else {
            System.out.println("Analysis could not be completed or no classes were found.");
        }
    }

    private static Cluster createSampleCluster(LinkageStrategy strategy, String[] names, double[][] distancesMatrix ) {
        // Renaming this method to be more generic, as it's no longer just for a sample
        ClusteringAlgorithm alg = new DefaultClusteringAlgorithm();
        Cluster cluster = alg.executeClustering(distancesMatrix, names, strategy);
        System.out.println("\n----- Clustering Result -----");
        cluster.toConsole(0);
        // The selection_cluster part seems to be an extra algorithm for partitioning, let's keep it for now.
        // System.err.println(selection_cluster(cluster));
        return cluster;
    }


    private static java.util.List<Cluster> selection_cluster(Cluster dendgr) {
        List<Cluster> R = new ArrayList<>();
        Stack<Cluster> parcoursCluster = new Stack<>();
        parcoursCluster.push(dendgr);

        while (!parcoursCluster.isEmpty()) {
            Cluster parent = parcoursCluster.pop();
            if (parent.isLeaf() || parent.getChildren().size() < 2) {
                R.add(parent);
                continue;
            }

            Cluster cl1 = parent.getChildren().get(0);
            Cluster cl2 = parent.getChildren().get(1);

            if (S(parent) > avg(S(cl1), S(cl2))) {
                R.add(parent);
            } else {
                parcoursCluster.push(cl1);
                parcoursCluster.push(cl2);
            }
        }
        return R;
    }

    private static Double S(Cluster parent) {
        return parent.getDistanceValue() != null ? parent.getDistanceValue() : 0.0;
    }

    private static Double avg(double value1, double value2) {
        return (value1 + value2) / 2.0; // Corrected the avg formula
    }
}