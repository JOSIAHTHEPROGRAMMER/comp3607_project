package comp3607_project.pdf_exporter.format;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HumanReadableDurationFormatterTest {

    private final HumanReadableDurationFormatter formatter = new HumanReadableDurationFormatter();

    @Test
    void zeroIsZeroSeconds() {
        assertEquals("0s", formatter.format(0));
    }

    @Test
    void underAMinuteShowsOnlySeconds() {
        assertEquals("45s", formatter.format(45_000));
    }

    @Test
    void millisecondsAreTruncated() {
        assertEquals("0s", formatter.format(999));
        assertEquals("1s", formatter.format(1_999));
    }

    @Test
    void exactMinuteShowsPaddedSeconds() {
        assertEquals("1m 00s", formatter.format(60_000));
    }

    @Test
    void minutesAndSeconds() {
        assertEquals("2m 05s", formatter.format(125_000));
    }

    @Test
    void exactHourShowsPaddedMinutesAndSeconds() {
        assertEquals("1h 00m 00s", formatter.format(3_600_000));
    }

    @Test
    void hoursMinutesAndSeconds() {
        assertEquals("1h 02m 03s", formatter.format(3_723_000));
    }

    @Test
    void hoursKeepCountingPastADay() {
        assertEquals("25h 01m 01s", formatter.format(90_061_000));
    }

    @Test
    void worksThroughTheDurationFormatterInterface() {
        DurationFormatter asInterface = formatter;

        assertEquals("1s", asInterface.format(1_000));
    }
}
