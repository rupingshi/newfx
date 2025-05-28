package com.teach.javafx.controller;

import com.teach.javafx.request.OptionItem;
import com.teach.javafx.util.CommonMethod;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.util.*;

/**
 * MessageController 登录交互控制类 对应 base/message-dialog.fxml
 *  @FXML  属性 对应fxml文件中的
 *  @FXML 方法 对应于fxml文件中的 on***Click的属性
 */


public class VolunteerEditController {
    @FXML
    private ComboBox<OptionItem> studentComboBox;
    private List<OptionItem> studentList;
    @FXML
    private ComboBox<OptionItem> activityComboBox; // 修正拼写
    private List<OptionItem> activityList;
    @FXML
    private TextField roleField;
    @FXML
    private TextField hoursField;
    private VolunteerTableController volunteerTableController= null;
    private Integer volunteerId= null;

    @FXML
    public void initialize() {
    }

    @FXML
    public void okButtonClick(){
        Map<String,Object> data = new HashMap<>();
        OptionItem op;
        op = studentComboBox.getSelectionModel().getSelectedItem();
        if(op != null) {
            data.put("personId",Integer.parseInt(op.getValue()));
        }
        op = activityComboBox.getSelectionModel().getSelectedItem();
        if(op != null) {
            data.put("activityId", Integer.parseInt(op.getValue())); // 修正为 activityId
        }
        data.put("volunteerId",volunteerId);
        data.put("role",roleField.getText());
        data.put("hours", hoursField.getText());  // 添加时长数据
        volunteerTableController.doClose("ok",data);
    }

    @FXML
    public void cancelButtonClick(){
        volunteerTableController.doClose("cancel",null);
    }

    public void setVolunteerTableController(VolunteerTableController volunteerTableController) {
        this.volunteerTableController = volunteerTableController;
    }
    public void init(){
        studentList =volunteerTableController.getStudentList();
        activityList = volunteerTableController.getActivityList();
        studentComboBox.getItems().addAll(studentList );
        activityComboBox.getItems().addAll(activityList);
    }
    public void showDialog(Map data){
        if(data == null) {
            volunteerId = null;
            studentComboBox.getSelectionModel().select(-1);
            activityComboBox.getSelectionModel().select(-1);
            studentComboBox.setDisable(false);
            activityComboBox.setDisable(false);
            roleField.setText("");
            hoursField.setText("");
        }else {
            volunteerId = CommonMethod.getInteger(data,"volunteerId");
            studentComboBox.getSelectionModel().select(CommonMethod.getOptionItemIndexByValue(studentList, CommonMethod.getString(data, "personId")));
            activityComboBox.getSelectionModel().select(CommonMethod.getOptionItemIndexByValue(activityList, CommonMethod.getString(data, "activityId")));
            studentComboBox.setDisable(true);
            activityComboBox.setDisable(true);
            roleField.setText(CommonMethod.getString(data, "role"));
            hoursField.setText(CommonMethod.getString(data, "hours"));
        }
    }
}
