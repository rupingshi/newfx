package com.teach.javafx.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.teach.javafx.MainApplication;
import com.teach.javafx.controller.base.MessageDialog;
import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;
import com.teach.javafx.request.HttpRequestUtil;
import com.teach.javafx.request.OptionItem;
import com.teach.javafx.util.CommonMethod;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.MapValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;


public class VolunteerTableController {
    @FXML
    private TableView<Map> dataTableView;
    @FXML
    private TableColumn<Map,String> studentNumColumn;
    @FXML
    private TableColumn<Map,String> studentNameColumn;
    @FXML
    private TableColumn<Map,String> classNameColumn;
    @FXML
    private TableColumn<Map,String> activityNumColumn;
    @FXML
    private TableColumn<Map,String> activityNameColumn;
    @FXML
    private TableColumn<Map,String> hoursColumn;
    @FXML
    private TableColumn<Map,String> roleColumn;
    @FXML
    private TableColumn<Map, Button> editColumn;

    private ArrayList<Map> volunteerList = new ArrayList();  // 志愿活动信息列表数据
    private ObservableList<Map> observableList= FXCollections.observableArrayList();  // TableView渲染列表

    @FXML
    private ComboBox<OptionItem> studentComboBox;
    private List<OptionItem> studentList;
    @FXML
    private ComboBox<OptionItem> activityComboBox;
    private List<OptionItem> activityList;

    private VolunteerEditController volunteerEditController = null;
    private Stage stage = null;

    public List<OptionItem> getStudentList() {
        return studentList;
    }
    public List<OptionItem> getActivityList() {
        return activityList;
    }

    @FXML
    private void onQueryButtonClick(){
        Integer personId = 0;
        Integer activityId = 0;
        OptionItem op;
        op = studentComboBox.getSelectionModel().getSelectedItem();
        if(op != null)
            personId = Integer.parseInt(op.getValue());
        op = activityComboBox.getSelectionModel().getSelectedItem();
        if(op != null)
            activityId = Integer.parseInt(op.getValue());
        DataResponse res;
        DataRequest req =new DataRequest();
        req.add("personId",personId);
        req.add("activityId",activityId);
        res = HttpRequestUtil.request("/api/volunteer/getVolunteerList",req); //从后台获取所有志愿活动信息列表集合
        if(res != null && res.getCode()== 0) {
            volunteerList = (ArrayList<Map>)res.getData();
        }
        setTableViewData();
    }

    private void setTableViewData() {
        observableList.clear();
        Map map;
        Button editButton;
        for (int j = 0; j < volunteerList.size(); j++) {
            map = volunteerList.get(j);
            editButton = new Button("编辑");
            editButton.setId("edit"+j);
            editButton.setOnAction(e->{
                editItem(((Button)e.getSource()).getId());
            });
            map.put("edit",editButton);
            observableList.addAll(FXCollections.observableArrayList(map));
        }
        dataTableView.setItems(observableList);
    }

    public void editItem(String name) {
        if (name == null)
            return;
        int j = Integer.parseInt(name.substring(4));
        Map data = volunteerList.get(j);
        initDialog();
        volunteerEditController.showDialog(data);
        MainApplication.setCanClose(false);
        stage.showAndWait();
    }

    @FXML
    public void initialize() {
        DataRequest req =new DataRequest();
        studentList = HttpRequestUtil.requestOptionItemList("/api/volunteer/getStudentItemOptionList",req); //从后台获取所有学生信息列表集合
        activityList = HttpRequestUtil.requestOptionItemList("/api/volunteer/getActivityItemOptionList",req); //从后台获取所有活动信息列表集合

        studentNumColumn.setCellValueFactory(new MapValueFactory<>("studentNum"));  //设置列值工程属性
        studentNameColumn.setCellValueFactory(new MapValueFactory<>("studentName"));
        classNameColumn.setCellValueFactory(new MapValueFactory<>("className"));
        activityNumColumn.setCellValueFactory(new MapValueFactory<>("activityNum"));
        activityNameColumn.setCellValueFactory(new MapValueFactory<>("activityName"));
        hoursColumn.setCellValueFactory(new MapValueFactory<>("hours"));
        roleColumn.setCellValueFactory(new MapValueFactory<>("role"));
        editColumn.setCellValueFactory(new MapValueFactory<>("edit"));

<<<<<<< Updated upstream
          OptionItem item = new OptionItem(null,"0","请选择");
=======
        OptionItem item = new OptionItem(null,"0","请选择");
>>>>>>> Stashed changes
        studentComboBox.getItems().addAll(item);
        studentComboBox.getItems().addAll(studentList);
        activityComboBox.getItems().addAll(item);
        activityComboBox.getItems().addAll(activityList);
        dataTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        onQueryButtonClick();
    }

    private void initDialog() {
        if(stage!= null)
            return;
        FXMLLoader fxmlLoader ;
        Scene scene = null;
        try {
            fxmlLoader = new FXMLLoader(MainApplication.class.getResource("volunteer-edit-dialog.fxml"));
            scene = new Scene(fxmlLoader.load(), 260, 140);
            stage = new Stage();
            stage.initOwner(MainApplication.getMainStage());
            stage.initModality(Modality.NONE);
            stage.setAlwaysOnTop(true);
            stage.setScene(scene);
            stage.setTitle("志愿活动录入对话框！");
            stage.setOnCloseRequest(event ->{
                MainApplication.setCanClose(true);
            });
            volunteerEditController = (VolunteerEditController) fxmlLoader.getController();
            volunteerEditController.setVolunteerTableController(this);
            volunteerEditController.init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void doClose(String cmd, Map<String, Object> data) {
        MainApplication.setCanClose(true);
        stage.close();
        if(!"ok".equals(cmd))
            return;
        DataResponse res;
        Integer personId = CommonMethod.getInteger(data,"personId");
        if(personId == null) {
            MessageDialog.showDialog("没有选中学生不能添加保存！");
            return;
        }
        Integer activityId = CommonMethod.getInteger(data,"activityId");
        if(activityId == null) {
            MessageDialog.showDialog("没有选中活动不能添加保存！");
            return;
        }
        DataRequest req =new DataRequest();
        req.add("personId",personId);
        req.add("activityId",activityId);
        req.add("volunteerId",CommonMethod.getInteger(data,"volunteerId"));
        req.add("role",CommonMethod.getString(data,"role"));
<<<<<<< Updated upstream
=======
        req.add("hours",CommonMethod.getString(data,"hours"));
>>>>>>> Stashed changes
        res = HttpRequestUtil.request("/api/volunteer/volunteerSave",req); //保存志愿活动信息
        if(res != null && res.getCode()== 0) {
            onQueryButtonClick();
        }
<<<<<<< Updated upstream
=======
//        System.out.println("传递到后端的时长: " + data.get("hours"));
>>>>>>> Stashed changes
    }

    @FXML
    private void onAddButtonClick() {
        initDialog();
        volunteerEditController.showDialog(null);
        MainApplication.setCanClose(false);
        stage.showAndWait();
    }

    @FXML
    private void onEditButtonClick() {
        Map data = dataTableView.getSelectionModel().getSelectedItem();
        if(data == null) {
            MessageDialog.showDialog("没有选中，不能修改！");
            return;
        }
        initDialog();
        volunteerEditController.showDialog(data);
        MainApplication.setCanClose(false);
        stage.showAndWait();
    }

    @FXML
    private void onDeleteButtonClick() {
        Map<String,Object> form = dataTableView.getSelectionModel().getSelectedItem();
        if(form == null) {
            MessageDialog.showDialog("没有选择，不能删除");
            return;
        }
        int ret = MessageDialog.choiceDialog("确认要删除吗?");
        if(ret != MessageDialog.CHOICE_YES) {
            return;
        }
        Integer volunteerId = CommonMethod.getInteger(form,"volunteerId");
        DataRequest req = new DataRequest();
        req.add("volunteerId", volunteerId);
        DataResponse res = HttpRequestUtil.request("/api/volunteer/volunteerDelete",req);
        if(res.getCode() == 0) {
            onQueryButtonClick();
        }
        else {
            MessageDialog.showDialog(res.getMsg());
        }
    }
}
