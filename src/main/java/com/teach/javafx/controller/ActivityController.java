package com.teach.javafx.controller;


import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.FlowPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * CourseController 登录交互控制类 对应 activity-panel.fxml
 *  @FXML  属性 对应fxml文件中的
 *  @FXML 方法 对应于fxml文件中的 on***Click的属性
 */

public class ActivityController {
    @FXML
    private TableView<Map<String, Object>> dataTableView;
    @FXML
    private TableColumn<Map,String> numColumn;
    @FXML
    private TableColumn<Map,String> nameColumn;
    @FXML
    private TableColumn<Map,String> timeColumn;
    @FXML
    private TableColumn<Map,String> durationColumn;
    @FXML
    private TableColumn<Map,String> preActivityColumn;
    @FXML
    private TableColumn<Map, FlowPane> operateColumn;

    private List<Map<String,Object>> activityList = new ArrayList<>();  // 学生信息列表数据
    private final ObservableList<Map<String,Object>> observableList= FXCollections.observableArrayList();  // TableView渲染列表

    @FXML
    private void onQueryButtonClick(){
        DataResponse res;
        DataRequest req =new DataRequest();
        res = HttpRequestUtil.request("/api/activity/getActivityList",req); //从后台获取所有学生信息列表集合
        if(res != null && res.getCode()== 0) {
            activityList = (List<Map<String, Object>>) res.getData();
        }
        setTableViewData();
    }

    private void setTableViewData() {
        observableList.clear();
        Map<String,Object> map;
        FlowPane flowPane;
        Button saveButton,deleteButton;
        for (int j = 0; j < activityList.size(); j++) {
            map = activityList.get(j);
            flowPane = new FlowPane();
            flowPane.setHgap(10);
            flowPane.setAlignment(Pos.CENTER);
            saveButton = new Button("修改保存");
            saveButton.setId("save"+j);
            saveButton.setOnAction(e->{
                saveItem(((Button)e.getSource()).getId());
            });
            deleteButton = new Button("删除");
            deleteButton.setId("delete"+j);
            deleteButton.setOnAction(e->{
                deleteItem(((Button)e.getSource()).getId());
            });
            flowPane.getChildren().addAll(saveButton,deleteButton);
            map.put("operate",flowPane);
            observableList.addAll(FXCollections.observableArrayList(map));
        }
        dataTableView.setItems(observableList);
    }

    public void saveItem(String name){
        if(name == null)
            return;
        int j = Integer.parseInt(name.substring(4));
        Map<String,Object> data = activityList.get(j);
        System.out.println(data);
    }
    public void deleteItem(String name){
        if(name == null)
            return;
        int j = Integer.parseInt(name.substring(5));
        Map<String,Object> data = activityList.get(j);
        System.out.println(data);
    }

    @FXML
    public void initialize() {
        numColumn.setCellValueFactory(new MapValueFactory<>("num"));
        numColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        numColumn.setOnEditCommit(event -> {
            Map<String,Object> map = event.getRowValue();
            map.put("num", event.getNewValue());
        });
        nameColumn.setCellValueFactory(new MapValueFactory<>("name"));
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nameColumn.setOnEditCommit(event -> {
            Map<String, Object> map = event.getRowValue();
            map.put("name", event.getNewValue());
        });
        timeColumn.setCellValueFactory(new MapValueFactory<>("time"));
        timeColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        timeColumn.setOnEditCommit(event -> {
            Map<String, Object> map = event.getRowValue();
            map.put("time", event.getNewValue());
        });
        durationColumn.setCellValueFactory(new MapValueFactory<>("duration"));
        durationColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        durationColumn.setOnEditCommit(event -> {
            Map<String, Object> map = event.getRowValue();
            map.put("duration", event.getNewValue());
        });
        preActivityColumn.setCellValueFactory(new MapValueFactory<>("preActivity"));
        preActivityColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        preActivityColumn.setOnEditCommit(event -> {
            Map<String, Object> map = event.getRowValue();
            map.put("preActivity", event.getNewValue());
        });
        operateColumn.setCellValueFactory(new MapValueFactory<>("operate"));
        dataTableView.setEditable(true);
        onQueryButtonClick();
    }
}
