package visualize;

import com.google.common.base.Joiner;
import org.apache.commons.io.FilenameUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.ui.UIUtils;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import parser.BinaryTraceReader;
import parser.TraceReader;
import parser.scarab.ScarabTraceReader;

import javax.swing.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeSet;

public class HitsOverTimePerFrequencyHeatmap extends JFrame {

    public HitsOverTimePerFrequencyHeatmap() {
        super();
    }

    private void generateGraph(TraceReader reader) throws IOException {
        final XYSeries series = new XYSeries("Count per Frequency");

        HashMap<Long, Long> keyToHits = new HashMap<>(10000);
        HashMap<Long, TreeSet<Long>> accessesPerKey = new HashMap<>(10000);
        long counter = 0;
        for (long event : reader.events().toArray()) {
            keyToHits.put(event, (keyToHits.getOrDefault(event, 0L) + 1));
            if (!accessesPerKey.containsKey(event)) {
                accessesPerKey.put(event, new TreeSet<>());
            }
            accessesPerKey.get(event).add(counter);
            counter++;
        }

        accessesPerKey.forEach((key, set) -> {
            if (set.size() > 1) {
                long freq = keyToHits.get(key);
                long start = -1;
                for (long element : set) {
                    if (start == -1) {
                        start = element;
                    } else {
                        series.add(element - start, freq);
                        start = element;
                    }
                }
            }
        });

//        System.out.println(series.getItems());

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);

//        System.out.println(series.getItems());

        JFreeChart chart = ChartFactory.createScatterPlot(
                "Accesses of other objects between hits per frequency", "# other accesses between hits", "Total hits of the object on the cache",
                dataset, PlotOrientation.VERTICAL,
                true, true, false);

        // Make the count axis logarithmic
        chart.getXYPlot().setRangeAxis(new LogarithmicAxis("Total hits of the object on the cache"));
        Boolean log = true;
        if (log) {
            chart.getXYPlot().setDomainAxis(new LogarithmicAxis("# other accesses between hits"));
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
        HitsOverTimePerFrequencyHeatmap timeBetweenAccessesPerHitsGraph = new HitsOverTimePerFrequencyHeatmap();

        String[] scarabTraces = new String[]{
                "/home/lfdversluis/Documents/vu/projects/caffeine/simulator/src/main/resources/com/github/benmanes/caffeine/cache/simulator/parser/scarab/prods.trace.20160808T073231Z.xz",
                "/home/lfdversluis/Documents/vu/projects/caffeine/simulator/src/main/resources/com/github/benmanes/caffeine/cache/simulator/parser/scarab/recs.trace.20160808T073231Z.xz",
                "/home/lfdversluis/Documents/vu/projects/caffeine/simulator/src/main/resources/com/github/benmanes/caffeine/cache/simulator/parser/scarab/scarab-recs.trace.xz"
        };

        for(String path : scarabTraces) {
            BinaryTraceReader reader = new ScarabTraceReader(path);

            StringBuilder sb = new StringBuilder();
            try {
                for (long l : reader.events().toArray()) {
                    sb.append(l).append(",");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


            BufferedWriter writer;
            try {

                String fileNameWithOutExt = FilenameUtils.removeExtension(FilenameUtils.getName(path));
                writer = new BufferedWriter(new FileWriter(fileNameWithOutExt+".csv"));
                writer.write(sb.substring(0, sb.length() - 1));
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
//            try {
//                timeBetweenAccessesPerHitsGraph.generateGraph(reader);
//            } catch (IOException ex) {
//                ex.printStackTrace();
//            }
        }

//        timeBetweenAccessesPerHitsGraph.pack();
//        UIUtils.centerFrameOnScreen(timeBetweenAccessesPerHitsGraph);
//        timeBetweenAccessesPerHitsGraph.setVisible(true);
    }
}
