package visualize;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.ui.UIUtils;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import parser.TraceFormat;
import parser.TraceReader;
import parser.scarab.ScarabTraceReader;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class HitsDistributionGraph extends JFrame {

    public HitsDistributionGraph() {
        super();
    }

    private void generateGraph(TraceReader reader) throws IOException {
        final XYSeries series = new XYSeries("Count per Frequency");
        final XYSeries numHitsTotalSeries = new XYSeries("Total hits on the cache per frequency of hits");

        HashMap<Long, Long> hitCount = new HashMap<>(100000);
        for (long event : reader.events().toArray()) {
            hitCount.put(event, (hitCount.getOrDefault(event, 0L) + 1));
        }

        HashMap<Long, Long> countPerFreq = new HashMap<>(10000);
        hitCount.forEach((key, value) -> countPerFreq.put(value, countPerFreq.getOrDefault(value, 0L) + 1));

        countPerFreq.forEach(series::add);
        countPerFreq.forEach((key, value) -> numHitsTotalSeries.add(key, Double.valueOf(Math.max((key-1)*value, 0.001))));

        System.out.println(series.getItems());

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);
        dataset.addSeries(numHitsTotalSeries);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Hits ", "# of hits", "# of objects",
                dataset, PlotOrientation.VERTICAL,
                true, true, false);

        // Make the count axis logarithmic
        chart.getXYPlot().setRangeAxis(new LogarithmicAxis("# of objects"));
        Boolean log = true;
        if (log) {
            chart.getXYPlot().setDomainAxis(new LogarithmicAxis("# of hits"));
        } else {
            NumberAxis xAxis = (NumberAxis) chart.getXYPlot().getDomainAxis();
            xAxis.setTickUnit(new NumberTickUnit((int) series.getMaxX() / 10)); // We just want 10 ticks on the axis
        }

        int width = 640;    /* Width of the image */
        int height = 480;   /* Height of the image */
        File BarChart = new File("BarChart.jpeg");
        ChartPanel panel = new ChartPanel(chart);
        setContentPane(panel);
//        try {
//            ChartUtils.saveChartAsJPEG(BarChart, barChart, width, height);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }


    public static void main(String[] args) {
        HitsDistributionGraph hitsGraph = new HitsDistributionGraph();

        ScarabTraceReader reader = new ScarabTraceReader("/home/lfdversluis/Documents/vu/projects/caffeine/simulator/src/main/resources/com/github/benmanes/caffeine/cache/simulator/parser/scarab/scarab-recs.trace.xz");
        try {
            hitsGraph.generateGraph(reader);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        hitsGraph.pack();
        UIUtils.centerFrameOnScreen(hitsGraph);
        hitsGraph.setVisible(true);
    }

}
