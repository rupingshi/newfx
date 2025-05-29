package com.teach.javafx.controller;

import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.OptionItem;
import com.teach.javafx.util.CommonMethod;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 考勤编辑对话框控制器
 */
/**MessageController 登录交互控制类 对应 base/message-dialog.fxml
 *  @FXML  属性 对应fxml文件中的
 *  @FXML 方法 对应于fxml文件中的 on***Click的属性
 */

public class AttendanceEditController {
    @FXML
    private ComboBox<OptionItem> studentComboBox;
    private List<OptionItem> studentList; // 下拉学生列表

    @FXML
    private TextField dateField;

    @FXML
    private ComboBox<OptionItem> statusComboBox; // 考勤状态下拉框
    private List<OptionItem> statusList;

    @FXML
    private TextField remarkField;

    private AttendanceTableController attendanceTableController = null;
    private Integer attendanceId = null;

    @FXML
    public void initialize() {
        // 初始化方法
    }



    @FXML
    public void okButtonClick() {
        Map<String,Object> data = new HashMap<>();

        //获取被选中该的学生
        OptionItem op;
        op = studentComboBox.getSelectionModel().getSelectedItem();
        if(op != null) {
            data.put("personId",Integer.parseInt(op.getValue()));
        }

        // 获取选中的状态
        op = statusComboBox.getSelectionModel().getSelectedItem();
        if(op != null) {
            data.put("status", op.getValue().toString()); // 直接使用字符串值
        }

        data.put("attendanceId", attendanceId);
        data.put("date", dateField.getText());
        data.put("remark", remarkField.getText());

        attendanceTableController.doClose("ok", data);
    }

    @FXML
    public void cancelButtonClick() {
        attendanceTableController.doClose("cancel", null);
    }

    public void setAttendanceTableController(AttendanceTableController attendanceTableController) {
        this.attendanceTableController = attendanceTableController;
    }

    public void init() {
        if (attendanceTableController == null) {
            System.out.println("错误：attendanceTableController未设置");
            return;
        }
        // 从控制器类直接获取列表
        studentList = AttendanceTableController.getStudentList();
        statusList = AttendanceTableController.getStatusList();

        // 检查列表是否为空
        if(studentList == null || studentList.isEmpty()) {
            System.out.println("警告：学生列表为空");
            studentList = new ArrayList<>(); // 确保不为null
        }
        if(statusList == null || statusList.isEmpty()) {
            System.out.println("警告：状态列表为空");
            statusList = new ArrayList<>(); // 确保不为null
        }

        // 添加列表项
        studentComboBox.getItems().clear();
        studentComboBox.getItems().addAll(studentList);

        statusComboBox.getItems().clear();
        statusComboBox.getItems().addAll(statusList);
    }

    public void showDialog(Map data) {
            // 确保列表已初始化
            if (studentList == null || statusList == null) {
                init();
            }

        if(data == null) {
            attendanceId = null;
            studentComboBox.getSelectionModel().select(-1);
            statusComboBox.getSelectionModel().select(-1);
            studentComboBox.setDisable(false);
            dateField.setText("");
            remarkField.setText("");
        } else {
            attendanceId = CommonMethod.getInteger(data, "attendanceId");
            studentComboBox.getSelectionModel().select(CommonMethod.getOptionItemIndexByValue(studentList, CommonMethod.getString(data, "personId")));
            statusComboBox.getSelectionModel().select(CommonMethod.getOptionItemIndexByValue(statusList, CommonMethod.getString(data, "statusId")));
            studentComboBox.setDisable(true);
            dateField.setText(CommonMethod.getString(data, "dateNum"));
            remarkField.setText(CommonMethod.getString(data, "remark"));
        }
    }
}