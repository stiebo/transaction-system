package dev.stiebo.app.views.dashboard;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.charts.model.style.SolidColor;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import dev.stiebo.app.data.TransactionLimit;
import dev.stiebo.app.dtos.DailyTransactionSummary;
import dev.stiebo.app.dtos.TransactionsByRegion;
import dev.stiebo.app.services.DashboardService;
import jakarta.annotation.security.PermitAll;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.time.ZoneId;
import java.util.List;

@PageTitle("Dashboard")
@Route("dashboard")
@Menu(order = 1, icon = LineAwesomeIconUrl.CHART_BAR_SOLID)
@PermitAll
public class DashboardView extends Composite<VerticalLayout> {

    private final DashboardService service;


    public DashboardView(DashboardService service) {
        this.service = service;

        VerticalLayout layout = getContent();
        layout.addClassNames(
                LumoUtility.Padding.Left.LARGE,
                LumoUtility.Padding.MEDIUM,
                LumoUtility.Gap.MEDIUM
        );
        layout.setSizeFull();

        HorizontalLayout keyMetrics = createKeyMetrics();
        keyMetrics.addClassNames(LumoUtility.Gap.MEDIUM, LumoUtility.AlignItems.CENTER);
        layout.add(keyMetrics);

        HorizontalLayout chartsLayout = new HorizontalLayout(createTransactionTrendsChart(), createRegionDistributionChart());
//        HorizontalLayout timelineLayout = new HorizontalLayout(createDailyTransactionSummaryChart());
        chartsLayout.setWidthFull();
        chartsLayout.addClassNames(LumoUtility.Gap.MEDIUM, LumoUtility.AlignItems.START);
        layout.add(chartsLayout);

    }

    private HorizontalLayout createKeyMetrics() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.addClassNames(LumoUtility.Gap.LARGE, LumoUtility.JustifyContent.CENTER);

        TransactionLimit limit = service.getLimit();
        Div allowedLimit = createMetricCard("Allowed Limit", limit.getMaxAllowed().toString());
        Div manualProcessingLimit = createMetricCard("Manual Processing Limit", limit.getMaxManual().toString());


        Long total = service.getTotalTransactions();
        Long reviewed = service.getReviewedTransactions();
        Div totalTransactions = createMetricCard("Total Transactions", total.toString());
        Div suspiciousTransactions = createMetricCard("Rejected Transactions",
                service.getRejectedTransactions().toString());
        Div manualReviews = createMetricCard("Manual Reviews", reviewed.toString());

        layout.add(allowedLimit, manualProcessingLimit, totalTransactions, suspiciousTransactions, manualReviews);
        return layout;
    }

    private Div createMetricCard(String title, String value) {
        Div container = new Div();
        container.addClassNames(
                LumoUtility.Padding.MEDIUM,
                LumoUtility.Background.CONTRAST_10,
                LumoUtility.Display.FLEX,
                LumoUtility.FlexDirection.COLUMN,
                LumoUtility.AlignItems.CENTER,
                LumoUtility.JustifyContent.CENTER,
                LumoUtility.BorderRadius.MEDIUM
        );

        H2 metricTitle = new H2(title);
        metricTitle.addClassNames(
                LumoUtility.Margin.NONE,
                LumoUtility.FontSize.SMALL,
                LumoUtility.TextColor.SECONDARY
        );

        Paragraph metricValue = new Paragraph(value);
        metricValue.addClassNames(
                LumoUtility.Margin.NONE,
                LumoUtility.FontSize.XXLARGE,
                LumoUtility.FontWeight.BOLD,
                LumoUtility.TextColor.BODY
        );

        container.add(metricTitle, metricValue);
        return container;
    }

    private Chart createDailyTransactionSummaryChart() {
        final Chart chart = new Chart();
        chart.setHeight("450px");
        chart.setWidth("100%");
        chart.setTimeline(true);

        Configuration configuration = chart.getConfiguration();
        configuration.getTitle().setText("Transaction Trends");

        PlotLine plotLine = new PlotLine();
        plotLine.setValue(2);
        plotLine.setWidth(2);
        plotLine.setColor(SolidColor.SILVER);

        Tooltip tooltip = new Tooltip();
        tooltip.setPointFormat("<span style=\"color:{series.color}\">{series.name}</span>: <b>{point.y}</b><br/>");
        tooltip.setValueDecimals(2);
        configuration.setTooltip(tooltip);

        List<DailyTransactionSummary> dataSeries = service.listDailyTransactionSummary();

        DataSeries allowedSeries = new DataSeries();
        allowedSeries.setName("ALLOWED");
        DataSeries manualSeries = new DataSeries();
        manualSeries.setName("MANUAL_PROCESSING");
        DataSeries prohibitedSeries = new DataSeries();
        prohibitedSeries.setName("PROHIBITED");

        for (DailyTransactionSummary dailyData : dataSeries) {
            DataSeriesItem item = new DataSeriesItem();
            item.setX(dailyData.date().atZone(ZoneId.systemDefault()).toInstant());
            item.setY(dailyData.count());
            switch (dailyData.type()) {
                case ALLOWED -> allowedSeries.add(item);
                case MANUAL_PROCESSING -> manualSeries.add(item);
                case PROHIBITED -> prohibitedSeries.add(item);
            }
        }

        configuration.setSeries(allowedSeries, manualSeries, prohibitedSeries);

        RangeSelector rangeSelector = new RangeSelector();
        rangeSelector.setSelected(4);
        configuration.setRangeSelector(rangeSelector);

        chart.drawChart();
        return chart;
    }

    private Chart createTransactionTrendsChart() {
        Chart chart = new Chart(ChartType.LINE);
        Configuration config = chart.getConfiguration();
        config.setTitle("Transaction Trends (Sample data)");

        XAxis xAxis = new XAxis();
        xAxis.setCategories("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun");
        config.addxAxis(xAxis);

        YAxis yAxis = new YAxis();
        yAxis.setTitle("Count");
        config.addyAxis(yAxis);

        DataSeries allowedSeries = new DataSeries("Allowed");
        allowedSeries.add(new DataSeriesItem("Monday", 50));
        allowedSeries.add(new DataSeriesItem("Tuesday", 70));
        allowedSeries.add(new DataSeriesItem("Wednesday", 90));
        allowedSeries.add(new DataSeriesItem("Thursday", 120));
        allowedSeries.add(new DataSeriesItem("Friday", 150));
        allowedSeries.add(new DataSeriesItem("Saturday", 200));
        allowedSeries.add(new DataSeriesItem("Sunday", 250));

        DataSeries manualSeries = new DataSeries("Manual");
        manualSeries.add(new DataSeriesItem("Monday", 30));
        manualSeries.add(new DataSeriesItem("Tuesday", 40));
        manualSeries.add(new DataSeriesItem("Wednesday", 20));
        manualSeries.add(new DataSeriesItem("Thursday", 60));
        manualSeries.add(new DataSeriesItem("Friday", 40));
        manualSeries.add(new DataSeriesItem("Saturday", 30));
        manualSeries.add(new DataSeriesItem("Sunday", 25));

        DataSeries prohibitedSeries = new DataSeries("Prohibited");
        prohibitedSeries.add(new DataSeriesItem("Monday", 20));
        prohibitedSeries.add(new DataSeriesItem("Tuesday", 10));
        prohibitedSeries.add(new DataSeriesItem("Wednesday", 30));
        prohibitedSeries.add(new DataSeriesItem("Thursday", 10));
        prohibitedSeries.add(new DataSeriesItem("Friday", 20));
        prohibitedSeries.add(new DataSeriesItem("Saturday", 10));
        prohibitedSeries.add(new DataSeriesItem("Sunday", 5));

        config.addSeries(allowedSeries);
        config.addSeries(manualSeries);
        config.addSeries(prohibitedSeries);

//        chart.setWidthFull();
//        chart.setHeight("64%");
        chart.addClassNames("w-full", "h-64");
        return chart;
    }

    private Chart createRegionDistributionChart() {
        Chart chart = new Chart(ChartType.PIE);

        Configuration configuration = chart.getConfiguration();
        configuration.setTitle("Region Distribution");

        PlotOptionsPie plotOptions = new PlotOptionsPie();
        configuration.setPlotOptions(plotOptions);

        List<TransactionsByRegion> transactionsByRegions = service.listTransactionsByRegion();
        DataSeries series = new DataSeries();
        transactionsByRegions
                .forEach(item ->
                        series.add(new DataSeriesItem(item.region().name(), item.transactions())));

        Tooltip tooltip = new Tooltip();
        tooltip.setValueDecimals(0);
        tooltip.setPointFormat("{point.y} transactions");
        configuration.setTooltip(tooltip);
        configuration.setSeries(series);

//        chart.setWidthFull();
//        chart.setHeight("64%");
        chart.addClassNames("w-full", "h-64");
        return chart;
    }

}