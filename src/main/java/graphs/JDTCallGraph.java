package graphs;

import parsers.Jdt;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import visitors.jdt.ClassVisitor;
import visitors.jdt.MethodInvocationVisitor;
import visitors.jdt.MethodProcessor;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class JDTCallGraph {

    private final Jdt parser;
    private final Set<String> classes = new HashSet<>();
    private final Graph graph = new Graph();
    private final GraphTools graphTools = new GraphTools();

    public JDTCallGraph(Jdt parser) {
        this.parser = parser;
    }

    private void setClasses() throws IOException {
        ClassVisitor classVisitor = new ClassVisitor();
        for (CompilationUnit cu : parser.parseProject()) {
            cu.accept(classVisitor);
        }

        classes.addAll(
                classVisitor
                        .getClasses()
                        .stream()
                        // THIS IS THE CORRECTED LINE - GETS FULLY QUALIFIED NAMES
                        .map(TypeDeclaration::resolveBinding)
                        .map(typeBinding -> typeBinding.getQualifiedName())
                        .collect(Collectors.toList())
        );

        System.out.println("\nClasses : " + classes);
    }

    public Graph createCallGraph() {
        try {
            setClasses();
            for (CompilationUnit cu : parser.parseProject()) {
                MethodProcessor methodProcessor = new MethodProcessor();
                methodProcessor.processMethods(cu);

                for (MethodDeclaration method : methodProcessor.getMethods()) {
                    String className = graphTools.getClassName(method);
                    if (className == null || className.isEmpty()) {
                        continue;
                    }

                    Map<String, Map<String, String>> methodsInvocations = graph.getClassesInvocations().computeIfAbsent(className, k -> new HashMap<>());
                    Map<String, String> tmpMap = new HashMap<>();

                    MethodInvocationVisitor methodInvocationVisitor = new MethodInvocationVisitor();
                    method.accept(methodInvocationVisitor);

                    if (methodInvocationVisitor.getMethods().size() > 0) {
                        for (MethodInvocation methodInvocation : methodInvocationVisitor.getMethods()) {
                            String classOfInvocationedMethod = graphTools.getClassOfInvocationedMethod(methodInvocation);

                            // The critical check that now works correctly
                            if (!classOfInvocationedMethod.equals(className) && (classes.contains(classOfInvocationedMethod))) {
                                tmpMap.put(graphTools.getMethodInvocationName(methodInvocation), classOfInvocationedMethod);
                            }
                        }
                        if (!tmpMap.isEmpty()) {
                            methodsInvocations.put(graphTools.getMethodNameAndParams(method), tmpMap);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return graph;
    }
}