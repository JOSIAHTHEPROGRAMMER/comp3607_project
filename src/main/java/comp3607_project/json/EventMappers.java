package comp3607_project.json;

import java.util.List;

public final class EventMappers {
    private EventMappers() {
    }

    public static List<EventMapper> defaults() {
        return List.of(new CompileEventMapper(), new ExecutionEventMapper(),
                new LifecycleEventMapper(), new InteractionEventMapper());
    }
}