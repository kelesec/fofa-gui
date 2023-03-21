package club.keleya.controller;

import club.keleya.crawl.CheckAlive;
import club.keleya.crawl.Requests;
import club.keleya.data.Data;
import club.keleya.data.Export;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.Comparator;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainController {

    @FXML
    private TextField query;

    @FXML
    private TextField token;

    @FXML
    private CheckBox checkAlive;

    @FXML
    private TextField threads;

    @FXML
    private TextField fromPage;

    @FXML
    private TextField toPage;

    @FXML
    private TableView<Data> tableView;

    @FXML
    private TableColumn<Data, Integer> idColumn;

    @FXML
    private TableColumn<Data, String> urlColumn;

    @FXML
    private TableColumn<Data, String> hostColumn;

    @FXML
    private TableColumn<Data, Integer> portColumn;

    @FXML
    private TableColumn<Data, String> titleColumn;

    @FXML
    private TableColumn<Data, String> serverColumn;

    @FXML
    private TableColumn<Data, Boolean> aliveColumn;

    private final ObservableList<Data> datas = FXCollections.observableArrayList();

    private ExecutorService executorService;

    void alert(String title, String header, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.show();
    }

    @FXML
    void about(ActionEvent event) {
        String info = "学什么用什么，项目的更多信息可以在Github中找到:" +
                "\n" +
                "https://github.com/kelesec/fofa-gui" +
                "\n\n" +
                "注意: 项目仅限于学习使用，切勿用于非法途径";
        alert("About", "fofa-gui v1.0", info, Alert.AlertType.WARNING);
    }

    @FXML
    void bonusScene() {
        System.out.println("hello");
        alert("彩蛋", "恭喜~", "恭喜你触发彩蛋，可以考虑vme50", Alert.AlertType.INFORMATION);
    }

    @FXML
    void saveCSV(ActionEvent event) {
        HashSet<Data> set = new HashSet<>(datas);
        CountDownLatch count = new CountDownLatch(1);

        // 开启一个新的线程导出文件
        new Thread(() -> {
            Export.exportAsCSV(set);
            count.countDown();
        }).start();

        try {
            count.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        alert("Succeed!", "Succeed", "文件导出成功！", Alert.AlertType.INFORMATION);
    }

    @FXML
    void saveHosts(ActionEvent event) {
        HashSet<Data> set = new HashSet<>(datas);
        CountDownLatch count = new CountDownLatch(1);

        new Thread(() -> {
            Export.exportHostAsText(set);
            count.countDown();
        }).start();

        try {
            count.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        alert("Succeed!", "Succeed", "文件导出成功！", Alert.AlertType.INFORMATION);
    }

    @FXML
    void saveUrls(ActionEvent event) {
        HashSet<Data> set = new HashSet<>(datas);
        CountDownLatch count = new CountDownLatch(1);

        new Thread(() -> {
            Export.exportUrlAsText(set);
            count.countDown();
        }).start();

        try {
            count.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        alert("Succeed!", "Succeed", "文件导出成功！", Alert.AlertType.INFORMATION);
    }

    @FXML
    void startQuery(ActionEvent event) {
        datas.clear();
        String _query;
        String _token = token.getText();
        int _fromPage = 1;
        int _toPage = -1;
        int _threads = -1;

        if (query.getText().isEmpty()) {
            alert("Error", "Error", "必须提供查询语法", Alert.AlertType.ERROR);
            return;
        } else {
            _query = query.getText();
        }

        // 没有设置Token，只查询第一页
        if (_token.isEmpty()) {
            int final_threads = _threads;
            new Thread(() -> {
                HashSet<Data> dataSet = new Requests().query(_query, "", 1, 1, final_threads);

                if (checkAlive.isSelected()) {
                    CheckAlive.check(dataSet);
                }

                datas.addAll(dataSet);
            }).start();
            return;
        }

        // 设置Token，查询多页
        if (!fromPage.getText().isEmpty()) {
            try {
                int start = Integer.parseInt(fromPage.getText());
                _fromPage = start > 0 ? start: 1;
            } catch (NumberFormatException e) {
                //
            }
        }

        if (!toPage.getText().isEmpty()) {
            try {
                int end = Integer.parseInt(toPage.getText());
                _toPage = end > 0 ? end : 1;
            } catch (NumberFormatException e) {
                //
            }

            if (_fromPage > _toPage) {
                _toPage = _fromPage;
            }
        }

        // 获取线程
        if (!threads.getText().isEmpty()) {
            try {
                int max = Integer.parseInt(threads.getText());
                _threads = max > 0 ? max: -1;
            } catch (NumberFormatException e) {
                //
            }
        }

        executorService = Executors.newFixedThreadPool(1);
        int final_fromPage = _fromPage;
        int final_toPage = _toPage;
        int final_threads1 = _threads;
        executorService.submit(() -> {
            HashSet<Data> dataSet = new Requests().query(_query, _token, final_fromPage, final_toPage, final_threads1);

            if (checkAlive.isSelected()) {
                CheckAlive.check(dataSet);
            }

            datas.addAll(dataSet);
        });
        executorService.shutdown();
    }

    @FXML
    void stopQuery(ActionEvent event) {
        if (executorService != null) {
            Requests.shutdownThreadPool();
            executorService.shutdownNow();
        }
    }

    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        urlColumn.setCellValueFactory(new PropertyValueFactory<>("url"));
        hostColumn.setCellValueFactory(new PropertyValueFactory<>("host"));
        portColumn.setCellValueFactory(new PropertyValueFactory<>("port"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        serverColumn.setCellValueFactory(new PropertyValueFactory<>("server"));
        aliveColumn.setCellValueFactory(new PropertyValueFactory<>("isAlive"));

        SortedList<Data> sortedDatas = datas.sorted(Comparator.comparing(Data::getId));
        tableView.setItems(sortedDatas);
    }
}
