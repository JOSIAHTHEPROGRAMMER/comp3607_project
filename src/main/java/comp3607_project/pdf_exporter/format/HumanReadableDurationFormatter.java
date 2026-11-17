package comp3607_project.pdf_exporter.format;


public class HumanReadableDurationFormatter implements DurationFormatter {

    @Override
    public String format(long millis) {
        long totalSec = millis / 1000;
        long h = totalSec / 3600;
        long m = (totalSec % 3600) / 60;
        long s = totalSec % 60;
        if (h > 0) return String.format("%dh %02dm %02ds", h, m, s);
        if (m > 0) return String.format("%dm %02ds", m, s);
        return s + "s";
    }
}