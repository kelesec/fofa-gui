package club.keleya.data;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.*;
import java.util.*;
import java.util.stream.Stream;

public class Export {
    /**
     * 导出URL到 .txt 中
     * @param dataSet
     */
    public static void exportUrlAsText(HashSet<Data> dataSet) {
        String filename = UUID.randomUUID() + ".txt";
        File file = new File(filename);
        FileWriter writer;

        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            writer = new FileWriter(file);

            for (Data data : dataSet) {
                writer.write(data.getUrl() + "\r\n");
            }
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 导出Host到 .txt 中
     * @param dataSet
     */
    public static void exportHostAsText(HashSet<Data> dataSet) {
        String filename = UUID.randomUUID() + ".txt";
        File file = new File(filename);
        FileWriter writer;

        try {
            writer = new FileWriter(file);

            for (Data data : dataSet) {
                writer.write(data.getHost() + "\r\n");
            }
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将获取到的信息写入到CSV文件中
     * @param dataSet
     */
    public static void exportAsCSV(HashSet<Data> dataSet) {
        String filename = UUID.randomUUID() + ".csv";
        OutputStreamWriter writer;
        CSVFormat header = CSVFormat.DEFAULT.withHeader(
                "ID", "URL", "HOST", "PORT", "TITLE", "SERVER", "ALIVE");

        try {
            // 写数据
            writer = new OutputStreamWriter(new FileOutputStream(filename), "GBK");
            CSVPrinter csvPrinter = new CSVPrinter(writer, header);

            // 对序号简单排个序
            Stream<Data> sortedSet = dataSet.stream().sorted(Comparator.comparing(Data::getId));

            for (Iterator<Data> it = sortedSet.iterator(); it.hasNext(); ) {
                Data d = it.next();
                csvPrinter.printRecord(Arrays.asList(
                        d.getId(),
                        d.getUrl(),
                        d.getHost(),
                        d.getPort(),
                        d.getTitle(),
                        d.getServer(),
                        d.getIsAlive()
                ));
                csvPrinter.flush();
            }

            csvPrinter.close();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
