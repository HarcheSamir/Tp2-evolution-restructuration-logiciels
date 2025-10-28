package graphs;

import parsers.Spoon;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtTypeInformation;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import visitors.spoon.ClassCollector;
import visitors.spoon.InterfaceCollector;
import visitors.spoon.MethodCollector;
import visitors.spoon.MethodInvocationsCollector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


public class SpoonCallGraph {

    private final Spoon parser;
    private CtModel ctModel;
    private final Set<String> classes = new HashSet<>();
    private final Graph graph = new Graph();
    // We no longer need GraphTools for the Spoon path.

    public SpoonCallGraph(Spoon parser) {
        this.parser = parser;
        setCtModel(parser);
    }

    public void setCtModel(Spoon parser) {
        this.ctModel = parser.createFAMIXModel();
    }

    public void setClasses(ClassCollector classCollector,
                           InterfaceCollector interfaceCollector) {

        Set<String> tempClasses = classCollector.getClasses().stream().map(CtTypeInformation::getQualifiedName).collect(Collectors.toSet());
        // Use the fully qualified names
        classes.addAll(tempClasses);

        System.out.println("\nClasses : " + classes);
    }

    public Graph createCallGraph() {
        ClassCollector classCollector = new ClassCollector(ctModel);
        InterfaceCollector interfaceCollector = new InterfaceCollector(ctModel);
        setClasses(classCollector, interfaceCollector);

        MethodCollector methodCollector = MethodCollector.getInstance();
        MethodInvocationsCollector methodInvocationsCollector = MethodInvocationsCollector.getInstance();

        for (CtClass<?> ctClass : classCollector.getClasses()) {
            Map<String, Map<String, String>> methodsInvocations = new HashMap<>();

            for (CtMethod<?> ctMethod : methodCollector.getMethodsOfClass(ctClass)) {
                Map<String, String> tmpMap = new HashMap<>();
                if (methodInvocationsCollector.getMethodsInvocation(ctMethod).size() > 0) {
                    for (CtInvocation<?> ctInvocation : methodInvocationsCollector.getMethodsInvocation(ctMethod)) {
                        String classOfInvocationedMethod = getClassOfInvocationedMethod(ctInvocation);

                        // Filter out calls to self and calls to classes not in our project scope
                        if (!classOfInvocationedMethod.equals(ctClass.getQualifiedName()) && (classes.contains(classOfInvocationedMethod))) {
                            tmpMap.put(getMethodInvocationName(ctInvocation), classOfInvocationedMethod);
                        }
                    }
                    if (!tmpMap.isEmpty()) {
                        methodsInvocations.put(ctMethod.getSimpleName() + ctMethod.getParameters(), tmpMap);
                    }
                }
            }
            if (!methodsInvocations.isEmpty()) {
                graph.getClassesInvocations().put(ctClass.getQualifiedName(), methodsInvocations);
            }
        }
        return graph;
    }

    // --- SPOON-SPECIFIC HELPER METHODS MOVED HERE ---

    private String getMethodInvocationName(CtInvocation<?> methodInvocation) {
        CtExecutableReference<?> executable = methodInvocation.getExecutable();
        if (executable != null) {
            return executable.getSimpleName();
        }
        return "unknown_method";
    }

    private String getClassOfInvocationedMethod(CtInvocation<?> ctInvocation) {
        CtExecutableReference<?> executable = ctInvocation.getExecutable();
        if (executable == null) {
            return "unknown_class";
        }

        CtTypeReference<?> declaringType = executable.getDeclaringType();
        if (declaringType != null) {
            return declaringType.getQualifiedName();
        }

        return "unknown_class";
    }
}