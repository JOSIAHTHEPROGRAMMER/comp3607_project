package comp3607_project.pdf_exporter.pdf;

import comp3607_project.pdf_exporter.charts.ChartRenderer;
import comp3607_project.pdf_exporter.format.DurationFormatter;

import java.util.ArrayList;
import java.util.List;

public final class ReportSectionFactory {

    private ReportSectionFactory() {}

    public static List<ReportSection> create(DurationFormatter formatter,
                                             ChartRenderer pie,
                                             ChartRenderer histogram,
                                             ChartRenderer line) {
        List<ReportSection> sections = new ArrayList<>();
        sections.add(new SummarySection(formatter));
        sections.add(new ChartsSection(pie, histogram));
        sections.add(new QualityTableSection(line));
        sections.add(new TimelineSection());
        return sections;
    }
}