package comp3607_project.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.List;
import org.junit.jupiter.api.Test;

class EventMappersTest {

    @Test
    void defaultsContainOneMapperForEachKindOfEvent() {
        List<EventMapper> mappers = EventMappers.defaults();

        assertEquals(4, mappers.size());
        assertInstanceOf(CompileEventMapper.class, mappers.get(0));
        assertInstanceOf(ExecutionEventMapper.class, mappers.get(1));
        assertInstanceOf(LifecycleEventMapper.class, mappers.get(2));
        assertInstanceOf(InteractionEventMapper.class, mappers.get(3));
    }

    @Test
    void eachCallReturnsAFreshSetOfMappers() {
        List<EventMapper> first = EventMappers.defaults();
        List<EventMapper> second = EventMappers.defaults();

        assertEquals(first.size(), second.size());
        for (int i = 0; i < first.size(); i++) {
            assertEquals(first.get(i).getClass(), second.get(i).getClass());
        }
    }
}
