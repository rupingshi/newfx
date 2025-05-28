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
<<<<<<< Updated upstream
=======

>>>>>>> Stashed changes
    @FXML
    private ComboBox<OptionItem> studentComboBox;
    private List<OptionItem> studentList;
    @FXML
    private ComboBox<OptionItem> activityComboBox; // 修正拼写
    private List<OptionItem> activityList;
    @FXML
<<<<<<< Updated upstream
=======
    private TextField hoursField;
    @FXML
>>>>>>> Stashed changes
    private TextField roleField;
    private VolunteerTableController volunteerTableController= null;
    private Integer volunteerId= null;

    @FXML
    public void initialize() {
    }

    @FXML
    public void okButtonClick(){
        Map<String,Object> data = new HashMap<>();
        OptionItem op;
<<<<<<< Updated upstream
=======
        //获取被选中的学生
>>>>>>> Stashed changes
        op = studentComboBox.getSelectionModel().getSelectedItem();
        if(op != null) {
            data.put("personId",Integer.parseInt(op.getValue()));
        }
<<<<<<< Updated upstream
=======
        //获取被选中的志愿活动
>>>>>>> Stashed changes
        op = activityComboBox.getSelectionModel().getSelectedItem();
        if(op != null) {
            data.put("activityId", Integer.parseInt(op.getValue())); // 修正为 activityId
        }
        data.put("volunteerId",volunteerId);
        data.put("role",roleField.getText());
<<<<<<< Updated upstream
        volunteerTableController.doClose("ok",data);
    }
=======
        data.put("hours",hoursField.getText());
        volunteerTableController.doClose("ok",data);
//        System.out.println("准备保存的时长数据: " + hoursField.getText());
    }

>>>>>>> Stashed changes
    @FXML
    public void cancelButtonClick(){
        volunteerTableController.doClose("cancel",null);
    }

    public void setVolunteerTableController(VolunteerTableController volunteerTableController) {
        this.volunteerTableController = volunteerTableController;
    }
    public void init(){
<<<<<<< Updated upstream
        studentList =volunteerTableController.getStudentList();
=======
        studentList = volunteerTableController.getStudentList();
>>>>>>> Stashed changes
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
        }else {
            volunteerId = CommonMethod.getInteger(data,"volunteerId");
            studentComboBox.getSelectionModel().select(CommonMethod.getOptionItemIndexByValue(studentList, CommonMethod.getString(data, "personId")));
            activityComboBox.getSelectionModel().select(CommonMethod.getOptionItemIndexByValue(activityList, CommonMethod.getString(data, "activityId")));
            studentComboBox.setDisable(true);
            activityComboBox.setDisable(true);
            roleField.setText(CommonMethod.getString(data, "role"));
<<<<<<< Updated upstream
=======
            hoursField.setText(CommonMethod.getString(data,"hours"));
>>>>>>> Stashed changes
        }
    }
}
