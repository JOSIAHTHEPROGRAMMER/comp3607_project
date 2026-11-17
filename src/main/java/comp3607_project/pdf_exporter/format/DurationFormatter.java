package comp3607_project.pdf_exporter.format;


@FunctionalInterface
public interface DurationFormatter {
    String format(long millis);
}