package comp3607_project.execution;

public record InvocationInfo(
        String project,
        String className,
        String objectName,
        String methodName,
        int parameterCount,
        ExecutionOutcome outcome,
        String resultType) {
}
