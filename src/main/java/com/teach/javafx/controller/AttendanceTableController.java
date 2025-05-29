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
import javafx.scene.control.TextField;
import javafx.scene.control.cell.MapValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AttendanceTableController {
    @FXML
    private TableView<Map> dataTableView;  // 数据显示表格
    @FXML
    private TableColumn<Map,String> studentNumColumn;  // 学号列
    @FXML
    private TableColumn<Map,String> studentNameColumn; // 学生名列
    @FXML
    private TableColumn<Map,String> classNameColumn;  // 班级名列
    @FXML
    private TableColumn<Map,String> dateColumn;       // 日期列
    @FXML
    private TableColumn<Map,String> statusColumn;    // 状态列
    @FXML
    private TableColumn<Map,String> remarkColumn;        // 备注列
    @FXML
    private TableColumn<Map, Button> editColumn;       // 编辑按钮列

    private ArrayList<Map> attendanceList = new ArrayList<>();  // 考勤信息列表数据
    private ObservableList<Map> observableList = FXCollections.observableArrayList();  // TableView渲染列表

    @FXML
    private ComboBox<OptionItem> studentComboBox;
    private static List<OptionItem> studentList;        // 学生选项列表
    @FXML
    private ComboBox<OptionItem> statusComboBox;  // 状态筛选下拉框
    private static List<OptionItem> statusList;      // 状态选项列表
    @FXML
    private TextField classField; //班级输入框
    @FXML
    private TextField dateField; // 日期输入框

    private AttendanceEditController attendanceEditController = null;  // 编辑对话框控制器
    private Stage stage = null;  // 编辑对话框舞台

    /**
     /**
     * 获取学生列表
     * @return 学生列表
     */
    public static List<OptionItem> getStudentList() {
        if (studentList == null) {
            System.out.println("开始请求学生列表...");
            DataRequest req = new DataRequest();
            studentList = HttpRequestUtil.requestOptionItemList("/api/volunteer/getStudentItemOptionList", req);
            System.out.println("请求结果：" + (studentList != null ? "成功，获取到 " + studentList.size() + " 条记录" : "失败"));
            if(studentList == null) {
                studentList = new ArrayList<>();
            }
        }
        return studentList;
    }
    
    /**
     * 获取考勤状态列表
     * @return 考勤状态列表
     */
    public static List<OptionItem> getStatusList() {
        if (statusList == null) {
            // 从后台获取考勤状态列表
            System.out.println("开始请求考勤状态列表...");
            DataRequest req = new DataRequest();
            statusList = HttpRequestUtil.requestOptionItemList("/api/attendance/status-options", req);
            System.out.println("请求结果：" + (statusList != null ? "成功，获取到 " + statusList.size() + " 条记录" : "失败"));
            if (statusList == null) {
                statusList = new ArrayList<>();
                statusList.add(new OptionItem(null, "1", "出勤")); // PRESENT
                statusList.add(new OptionItem(null, "2", "缺勤")); // ABSENT
                statusList.add(new OptionItem(null, "3", "迟到")); // LATE
                statusList.add(new OptionItem(null, "4", "请假")); // LEAVE
                statusList.add(new OptionItem(null, "5", "早退")); // EARLY_LEAVE
            }
        }
        return statusList;
    }
    
    /**
     * 初始化下拉框
     */
    private void initComboBox() {
        // 初始化学生下拉框
        studentList = getStudentList();
        studentComboBox.getItems().clear(); // 清空现有项
        OptionItem defaultItem = new OptionItem(null,"0","请选择");
        studentComboBox.getItems().add(defaultItem);
        studentComboBox.getItems().addAll(studentList);
        studentComboBox.getSelectionModel().select(0); // 默认选择"请选择"

        // 初始化状态下拉框
        statusList = getStatusList();
        statusComboBox.getItems().clear(); // 清空现有项
        statusComboBox.getItems().add(defaultItem);
        statusComboBox.getItems().addAll(statusList);
        statusComboBox.getSelectionModel().select(0); // 默认选择"请选择"
    }
    
    // 查询按钮点击事件
    @FXML
    private void onQueryButtonClick() {
        // 获取选中的学生和状态ID
        Integer personId = 0;
        Integer statusId = 0;
        String date = "";
        String classId = "";

        if (dateField != null) {
            date = dateField.getText();
        }

        if(classField != null){
            classId = classField.getText();
        }

        if (studentComboBox != null && studentComboBox.getSelectionModel().getSelectedItem() != null) {
            OptionItem op = studentComboBox.getSelectionModel().getSelectedItem();
            if(op != null && !op.getValue().equals("0")) {
                personId = Integer.parseInt(op.getValue());
            }
        }

        if (statusComboBox != null && statusComboBox.getSelectionModel().getSelectedItem() != null) {
            OptionItem op = statusComboBox.getSelectionModel().getSelectedItem();
            if(op != null && !op.getValue().equals("0")) {
                statusId = Integer.parseInt(op.getValue());
            }
        }

        // 发送查询请求
        DataRequest req = new DataRequest();
        req.add("personId", personId);
//        req.add("statusId", statusId);
        req.add("classId", classId);
        req.add("date", date);
        //发送请求到后端
        DataResponse res = HttpRequestUtil.request("/api/attendance/status-options", req);
        //处理响应
        if(res != null && res.getCode()== 0) {
            attendanceList = (ArrayList<Map>)res.getData();
        }
        setTableViewData();
    }

    // 设置表格数据
    private void setTableViewData() {
        if (observableList == null) {
            observableList = FXCollections.observableArrayList();
        }

        observableList.clear();

        if (attendanceList == null || attendanceList.isEmpty()) {
            return;
        }

        for (int j = 0; j < attendanceList.size(); j++) {
            Map map = attendanceList.get(j);
            if (map != null) {
                Button editButton = new Button("编辑");
                editButton.setId("edit" + j);
                final int index = j;
                editButton.setOnAction(e -> {
                    editItem(index);
                });
                map.put("edit", editButton);
                observableList.add(map);
            }
        }

        if (dataTableView != null) {
            dataTableView.setItems(observableList);
        }
    }

    // 编辑项目
    public void editItem(int index) {
        if (index < 0 || index >= attendanceList.size()) return;
        Map data = attendanceList.get(index);
        initDialog();
        attendanceEditController.showDialog(data);
        MainApplication.setCanClose(false);
        stage.showAndWait();
    }

    @FXML
    public void initialize() {
        // 初始化 observableList，确保不为 null
        observableList = FXCollections.observableArrayList();

        // 初始化下拉框
        initComboBox();

        // 从后台获取学生列表
        System.out.println("开始请求学生列表...");
        DataRequest req = new DataRequest();
        studentList = HttpRequestUtil.requestOptionItemList("/api/volunteer/getStudentItemOptionList", req);
        System.out.println("请求结果：" + (studentList != null ? "成功，获取到 " + studentList.size() + " 条记录" : "失败"));
    
        // 如果获取失败，初始化为空列表而不是 null
        if (studentList == null) {
            studentList = new ArrayList<>();
            System.out.println("警告：无法获取学生列表，使用空列表代替");
        }

        // 创建考勤状态列表（基于枚举）
        statusList = new ArrayList<>();
        statusList.add(new OptionItem(null, "1", "出勤")); // PRESENT
        statusList.add(new OptionItem(null, "2", "缺勤")); // ABSENT
        statusList.add(new OptionItem(null, "3", "迟到")); // LATE
        statusList.add(new OptionItem(null, "4", "请假")); // LEAVE
        statusList.add(new OptionItem(null, "5", "早退")); // EARLY_LEAVE

        // 设置表格列与数据字段的映射
        studentNumColumn.setCellValueFactory(new MapValueFactory<>("studentNum"));
        studentNameColumn.setCellValueFactory(new MapValueFactory<>("studentName"));
        classNameColumn.setCellValueFactory(new MapValueFactory<>("className"));
        dateColumn.setCellValueFactory(new MapValueFactory<>("dateNum"));
        statusColumn.setCellValueFactory(new MapValueFactory<>("statusName"));
        remarkColumn.setCellValueFactory(new MapValueFactory<>("remark"));
        editColumn.setCellValueFactory(new MapValueFactory<>("edit"));

        // 初始化下拉框
        OptionItem item = new OptionItem(null,"0","请选择");
        studentComboBox.getItems().add(item);
        studentComboBox.getItems().addAll(studentList);
        statusComboBox.getItems().add(item);
        statusComboBox.getItems().addAll(statusList);
        
        dataTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        onQueryButtonClick();
    }

    /**
     * 刷新活动列表
     */
    public void refreshAttendanceList() {
        classField.setText(""); // 清空搜索条件
        dateField.setText("");
        onQueryButtonClick();
    }

    // 编辑按钮点击事件
    @FXML
    private void onEditButtonClick() {
        Map data = dataTableView.getSelectionModel().getSelectedItem();
        if(data == null) {
            MessageDialog.showDialog("没有选中记录，不能修改！");
            return;
        }
        initDialog();
        attendanceEditController.showDialog(data);
        MainApplication.setCanClose(false);
        stage.showAndWait();
    }

    // 添加按钮点击事件
    @FXML
    private void onAddButtonClick() {
        initDialog();
        attendanceEditController.showDialog(null);
        MainApplication.setCanClose(false);
        stage.showAndWait();
    }

    // 删除按钮点击事件
    @FXML
    private void onDeleteButtonClick() {
        Map<String,Object> form = dataTableView.getSelectionModel().getSelectedItem();
        if(form == null) {
            MessageDialog.showDialog("没有选择记录，不能删除");
            return;
        }

        int ret = MessageDialog.choiceDialog("确认要删除此考勤记录吗?");
        if(ret != MessageDialog.CHOICE_YES) {
            return;
        }

        Integer attendanceId = CommonMethod.getInteger(form, "attendanceId");
        DataRequest req = new DataRequest();
        req.add("attendanceId", attendanceId);
        DataResponse res = HttpRequestUtil.request("/api/attendance/delete/{attendanceId}", req);

        if(res.getCode() == 0) {
            MessageDialog.showDialog("删除成功！");
            onQueryButtonClick();
        } else {
            MessageDialog.showDialog(res.getMsg());
        }
    }

    // 初始化编辑对话框
    private void initDialog() {
        if(stage != null) return;
        FXMLLoader fxmlLoader;
        Scene scene = null;
        try {
            fxmlLoader = new FXMLLoader(MainApplication.class.getResource("attendance-edit-dialog.fxml"));
            scene = new Scene(fxmlLoader.load(), 300, 200);
            stage = new Stage();
            stage.initOwner(MainApplication.getMainStage());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setAlwaysOnTop(true);
            stage.setScene(scene);
            stage.setTitle("考勤记录编辑");
            stage.setOnCloseRequest(event -> {
                MainApplication.setCanClose(true);
            });
            attendanceEditController = fxmlLoader.getController();
            attendanceEditController.setAttendanceTableController(this);
            attendanceEditController.init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 关闭对话框并处理结果
    public void doClose(String cmd, Map<String, Object> data) {
        MainApplication.setCanClose(true);
        stage.close();
        if(!"ok".equals(cmd)) return;

        // 验证数据
        Integer personId = CommonMethod.getInteger(data, "personId");
        if(personId == null || personId == 0) {  // 允许0或null时提示
            MessageDialog.showDialog("没有选中学生，不能保存！");
            return;
        }

//        Integer statusId = CommonMethod.getInteger(data, "statusId");
//        if(statusId == null) {
//            MessageDialog.showDialog("没有选中考勤状态，不能保存！");
//            return;
//        }

        String date = CommonMethod.getString(data, "date");
        if(date == null || date.trim().isEmpty()) {
            MessageDialog.showDialog("日期不能为空！");
            return;
        }

        String status = CommonMethod.getString(data, "status");
        //Integer statusId = CommonMethod.getInteger(data, "statusId");
        //String statusEnum;
        /*switch(status) {
            case 1: status = "PRESENT"; break;
            case 2: status = "ABSENT"; break;
            case 3: status = "LATE"; break;
            case 4: status = "LEAVE"; break;
            case 5: status = "EARLY_LEAVE"; break;
            default:
                MessageDialog.showDialog("无效的考勤状态！");
                return;
        }*/


        // 保存数据
        DataRequest req = new DataRequest();
        //req.add("attendanceId", CommonMethod.getInteger(data, "attendanceId"));
        req.add("personId", personId);
        req.add("status", status);
        req.add("date", CommonMethod.getString(data, "date")); // 确保日期字段名正确
        req.add("remark", CommonMethod.getString(data, "remark"));

        System.out.println("准备保存的数据：" + req.getData());

        DataResponse res = HttpRequestUtil.request("/api/attendance/record", req);
        if(res != null && res.getCode() == 0) {
            MessageDialog.showDialog("保存成功！");
            onQueryButtonClick();
        } else {
            MessageDialog.showDialog(res != null ? res.getMsg() : "保存失败");
        }
    }
}