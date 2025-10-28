package graphs;

import org.eclipse.jdt.core.dom.*;

public class GraphTools {

    public GraphTools() { }

    // This version is known to work correctly for JDT
    public String getClassName(MethodDeclaration methodDeclaration) {
        IMethodBinding resolveBinding = methodDeclaration.resolveBinding();
        if (resolveBinding != null && resolveBinding.getDeclaringClass() != null) {
            // Reverted to getQualifiedName for consistency
            return resolveBinding.getDeclaringClass().getQualifiedName();
        }
        return "";
    }

    public String getMethodNameAndParams(MethodDeclaration method) {
        return method.getName().getFullyQualifiedName() + method.parameters();
    }

    public String getClassOfInvocationedMethod(MethodInvocation methodInvocation) {
        Expression expression = methodInvocation.getExpression();
        if (expression != null) {
            ITypeBinding typeBinding = expression.resolveTypeBinding();
            if (typeBinding != null) {
                // For JDT, getting the qualified name from the TypeBinding is correct.
                return typeBinding.getQualifiedName();
            }
        } else {
            IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
            if (methodBinding != null && methodBinding.getDeclaringClass() != null) {
                return methodBinding.getDeclaringClass().getQualifiedName();
            }
        }
        return "";
    }

    public String getMethodInvocationName(MethodInvocation methodInvocation) {
        return methodInvocation.getName().getFullyQualifiedName();
    }
}