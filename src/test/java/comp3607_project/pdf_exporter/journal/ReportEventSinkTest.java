package comp3607_project.pdf_exporter.journal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;

import comp3607_project.model.EventSink;
import comp3607_project.model.TelemetryEvent;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReportEventSinkTest {

    private static TelemetryEvent stub(long timestamp) {
        return new TelemetryEvent() {
            @Override
            public String getEventType() {
                return "TEST";
            }

            @Override
            public long getTimestamp() {
                return timestamp;
            }

            @Override
            public String getTarget() {
                return "t";
            }
        };
    }

    @Test
    void recordsEveryEventInTheJournal() {
        TelemetryJournal journal = new TelemetryJournal(0);
        ReportEventSink sink = new ReportEventSink(null, journal);
        TelemetryEvent a = stub(1_000);
        TelemetryEvent b = stub(2_000);

        sink.accept(a);
        sink.accept(b);

        assertEquals(List.of(a, b), journal.events());
    }

    @Test
    void alsoForwardsEachEventToTheDelegate() {
        TelemetryJournal journal = new TelemetryJournal(0);
        List<TelemetryEvent> forwarded = new ArrayList<>();
        ReportEventSink sink = new ReportEventSink(forwarded::add, journal);
        TelemetryEvent e = stub(1_000);

        sink.accept(e);

        assertEquals(List.of(e), forwarded);
        assertEquals(List.of(e), journal.events());
    }

    @Test
    void worksWithoutADelegate() {
        ReportEventSink sink = new ReportEventSink(null, new TelemetryJournal(0));

        assertDoesNotThrow(() -> sink.accept(stub(1_000)));
    }

    @Test
    void exposesItsJournal() {
        TelemetryJournal journal = new TelemetryJournal(0);

        assertSame(journal, new ReportEventSink(null, journal).journal());
    }

    @Test
    void canBeUsedAsAnEventSink() {
        TelemetryJournal journal = new TelemetryJournal(0);
        EventSink sink = new ReportEventSink(null, journal);

        sink.accept(stub(1_000));

        assertEquals(1, journal.events().size());
    }
}
