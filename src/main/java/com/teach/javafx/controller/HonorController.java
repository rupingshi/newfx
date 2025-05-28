package com.teach.javafx.controller;

import com.teach.javafx.controller.base.MessageDialog;
import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;
import com.teach.javafx.request.HttpRequestUtil;
import com.teach.javafx.request.OptionItem;
import com.teach.javafx.util.CommonMethod;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HonorController 荣誉管理控制类 对应 honor-panel.fxml
 */
public class HonorController extends ToolController {
    @FXML
    private TextField numNameTextField;
    @FXML
    private ComboBox<OptionItem> honorTypeFilterComboBox;
    @FXML
    private ComboBox<OptionItem> honorLevelFilterComboBox;
    @FXML
    private ComboBox<OptionItem> statusFilterComboBox;
    @FXML
    private TableView<Map> dataTableView;
    @FXML
    private TableColumn<Map, String> numColumn;
    @FXML
    private TableColumn<Map, String> studentNameColumn;
    @FXML
    private TableColumn<Map, String> honorNameColumn;
    @FXML
    private TableColumn<Map, String> honorTypeColumn;
    @FXML
    private TableColumn<Map, String> honorLevelColumn;
    @FXML
    private TableColumn<Map, String> awardDateColumn;
    @FXML
    private TableColumn<Map, String> awardingOrganizationColumn;
    @FXML
    private TableColumn<Map, String> statusColumn;

    // 编辑表单控件
    @FXML
    private TextField numField;
    @FXML
    private ComboBox<OptionItem> studentComboBox;
    @FXML
    private TextField honorNameField;
    @FXML
    private ComboBox<OptionItem> honorTypeComboBox;
    @FXML
    private ComboBox<OptionItem> honorLevelComboBox;
    @FXML
    private ComboBox<OptionItem> statusComboBox;
    @FXML
    private TextField awardingOrganizationField;
    @FXML
    private DatePicker awardDatePicker;
    @FXML
    private TextField certificateNumberField;
    @FXML
    private TextArea descriptionField;

    private Integer honorId = null;
    private ObservableList<Map> observableList = FXCollections.observableArrayList();

    /**
     * 页面初始化
     */
    @FXML
    public void initialize() {
        // 初始化表格列
        numColumn.setCellValueFactory(new MapValueFactory<>("num"));
        studentNameColumn.setCellValueFactory(new MapValueFactory<>("studentName"));
        honorNameColumn.setCellValueFactory(new MapValueFactory<>("honorName"));
        honorTypeColumn.setCellValueFactory(new MapValueFactory<>("honorType"));
        honorLevelColumn.setCellValueFactory(new MapValueFactory<>("honorLevel"));
        awardDateColumn.setCellValueFactory(new MapValueFactory<>("awardDate"));
        awardingOrganizationColumn.setCellValueFactory(new MapValueFactory<>("awardingOrganization"));
        statusColumn.setCellValueFactory(new MapValueFactory<>("status"));

        dataTableView.setItems(observableList);

        // 监听表格选择变化
        dataTableView.getSelectionModel().getSelectedItems().addListener(
                (javafx.collections.ListChangeListener<Map>) change -> {
                    while (change.next()) {
                        if (change.wasAdded() && !change.getAddedSubList().isEmpty()) {
                            Map selectedItem = change.getAddedSubList().get(0);
                            if (selectedItem != null) {
                                honorId = CommonMethod.getInteger(selectedItem, "honorId");
                                showHonorDetails();
                            }
                        }
                    }
                });

        // 初始化下拉框
        initializeComboBoxes();
        loadStudentList();
        onQueryButtonClick();
    }

    /**
     * 初始化下拉框
     */
    private void initializeComboBoxes() {
        // 荣誉类型下拉框
        List<OptionItem> honorTypeList = new ArrayList<>();
        honorTypeList.add(new OptionItem(1, "国家级", "国家级"));
        honorTypeList.add(new OptionItem(2, "省级", "省级"));
        honorTypeList.add(new OptionItem(3, "市级", "市级"));
        honorTypeList.add(new OptionItem(4, "校级", "校级"));
        honorTypeList.add(new OptionItem(5, "院级", "院级"));
        honorTypeComboBox.setItems(FXCollections.observableArrayList(honorTypeList));

        // 筛选用的荣誉类型（包含"全部"选项）
        List<OptionItem> filterHonorTypeList = new ArrayList<>();
        filterHonorTypeList.add(new OptionItem(0, "", "全部类型"));
        filterHonorTypeList.addAll(honorTypeList);
        honorTypeFilterComboBox.setItems(FXCollections.observableArrayList(filterHonorTypeList));

        // 荣誉等级下拉框
        List<OptionItem> honorLevelList = new ArrayList<>();
        honorLevelList.add(new OptionItem(1, "特等奖", "特等奖"));
        honorLevelList.add(new OptionItem(2, "一等奖", "一等奖"));
        honorLevelList.add(new OptionItem(3, "二等奖", "二等奖"));
        honorLevelList.add(new OptionItem(4, "三等奖", "三等奖"));
        honorLevelList.add(new OptionItem(5, "优秀奖", "优秀奖"));
        honorLevelComboBox.setItems(FXCollections.observableArrayList(honorLevelList));

        // 筛选用的荣誉等级（包含"全部"选项）
        List<OptionItem> filterHonorLevelList = new ArrayList<>();
        filterHonorLevelList.add(new OptionItem(0, "", "全部等级"));
        filterHonorLevelList.addAll(honorLevelList);
        honorLevelFilterComboBox.setItems(FXCollections.observableArrayList(filterHonorLevelList));

        // 状态下拉框
        List<OptionItem> statusList = new ArrayList<>();
        statusList.add(new OptionItem(1, "有效", "有效"));
        statusList.add(new OptionItem(2, "失效", "失效"));
        statusList.add(new OptionItem(3, "待审核", "待审核"));
        statusComboBox.setItems(FXCollections.observableArrayList(statusList));

        // 筛选用的状态（包含"全部"选项）
        List<OptionItem> filterStatusList = new ArrayList<>();
        filterStatusList.add(new OptionItem(0, "", "全部状态"));
        filterStatusList.addAll(statusList);
        statusFilterComboBox.setItems(FXCollections.observableArrayList(filterStatusList));
    }

    /**
     * 加载学生列表
     */
    private void loadStudentList() {
        DataRequest req = new DataRequest();
        DataResponse res = HttpRequestUtil.request("/api/honor/getStudentList", req);
        if (res != null && res.getCode() == 0) {
            List<Map<String, Object>> studentList = (List<Map<String, Object>>) res.getData();
            List<OptionItem> options = new ArrayList<>();

            for (Map<String, Object> student : studentList) {
                Integer studentId = CommonMethod.getInteger(student, "value");
                String name = CommonMethod.getString(student, "title");
                options.add(new OptionItem(studentId, studentId.toString(), name));
            }

            studentComboBox.setItems(FXCollections.observableArrayList(options));
        }
    }

    /**
     * 查询按钮点击事件
     */
    @FXML
    protected void onQueryButtonClick() {
        String numName = numNameTextField.getText();
        String honorType = "";
        String honorLevel = "";
        String status = "";

        if (honorTypeFilterComboBox.getSelectionModel().getSelectedItem() != null) {
            honorType = honorTypeFilterComboBox.getSelectionModel().getSelectedItem().getValue();
        }
        if (honorLevelFilterComboBox.getSelectionModel().getSelectedItem() != null) {
            honorLevel = honorLevelFilterComboBox.getSelectionModel().getSelectedItem().getValue();
        }
        if (statusFilterComboBox.getSelectionModel().getSelectedItem() != null) {
            status = statusFilterComboBox.getSelectionModel().getSelectedItem().getValue();
        }

        DataRequest req = new DataRequest();
        req.add("numName", numName);
        req.add("honorType", honorType);
        req.add("honorLevel", honorLevel);
        req.add("status", status);

        DataResponse res = HttpRequestUtil.request("/api/honor/getHonorList", req);
        if (res != null && res.getCode() == 0) {
            observableList.clear();
            List<Map<String, Object>> dataList = (List<Map<String, Object>>) res.getData();
            if (dataList != null && !dataList.isEmpty()) {
                observableList.addAll(dataList);
            }
        } else {
            MessageDialog.showDialog("查询失败: " + (res != null ? res.getMsg() : "网络错误"));
        }
    }

    /**
     * 新建按钮点击事件
     */
    @FXML
    protected void onAddButtonClick() {
        honorId = null;
        clearFormFields();
        // 设置默认编号
        generateDefaultNum();
    }

    /**
     * 保存按钮点击事件
     */
    @FXML
    protected void onSaveButtonClick() {
        String num = numField.getText().trim();
        String honorName = honorNameField.getText().trim();

        if (num.isEmpty()) {
            MessageDialog.showDialog("荣誉编号不能为空！");
            return;
        }
        if (honorName.isEmpty()) {
            MessageDialog.showDialog("荣誉名称不能为空！");
            return;
        }

        if (studentComboBox.getSelectionModel().getSelectedItem() == null) {
            MessageDialog.showDialog("请选择学生！");
            return;
        }

        Map<String, Object> form = new HashMap<>();
        form.put("num", num);
        form.put("studentId", Integer.parseInt(studentComboBox.getSelectionModel().getSelectedItem().getValue()));
        form.put("honorName", honorName);
        form.put("honorType",
                honorTypeComboBox.getSelectionModel().getSelectedItem() != null
                        ? honorTypeComboBox.getSelectionModel().getSelectedItem().getValue()
                        : "校级");
        form.put("honorLevel",
                honorLevelComboBox.getSelectionModel().getSelectedItem() != null
                        ? honorLevelComboBox.getSelectionModel().getSelectedItem().getValue()
                        : "优秀奖");
        form.put("awardingOrganization", awardingOrganizationField.getText().trim());
        form.put("awardDate",
                awardDatePicker.getValue() != null ? awardDatePicker.getValue().toString() : "");
        form.put("certificateNumber", certificateNumberField.getText().trim());
        form.put("description", descriptionField.getText().trim());
        form.put("status",
                statusComboBox.getSelectionModel().getSelectedItem() != null
                        ? statusComboBox.getSelectionModel().getSelectedItem().getValue()
                        : "有效");

        DataRequest req = new DataRequest();
        req.add("honorId", honorId);
        req.add("form", form);

        DataResponse res = HttpRequestUtil.request("/api/honor/honorEditSave", req);
        if (res != null && res.getCode() == 0) {
            MessageDialog.showDialog("保存成功！");
            onQueryButtonClick();
            if (honorId == null) {
                clearFormFields();
                generateDefaultNum();
            }
        } else {
            MessageDialog.showDialog("保存失败: " + (res != null ? res.getMsg() : "网络错误"));
        }
    }

    /**
     * 删除按钮点击事件
     */
    @FXML
    protected void onDeleteButtonClick() {
        if (honorId == null) {
            MessageDialog.showDialog("请先选择要删除的荣誉记录！");
            return;
        }

        int choice = MessageDialog.choiceDialog("确定要删除所选荣誉记录吗？");
        if (choice != MessageDialog.CHOICE_YES) {
            return;
        }

        DataRequest req = new DataRequest();
        req.add("honorId", honorId);

        DataResponse res = HttpRequestUtil.request("/api/honor/honorDelete", req);
        if (res != null && res.getCode() == 0) {
            MessageDialog.showDialog("删除成功！");
            honorId = null;
            clearFormFields();
            onQueryButtonClick();
        } else {
            MessageDialog.showDialog("删除失败: " + (res != null ? res.getMsg() : "网络错误"));
        }
    }

    /**
     * 导出Excel按钮点击事件
     */
    @FXML
    protected void onExportButtonClick() {
        String numName = numNameTextField.getText();

        DataRequest req = new DataRequest();
        req.add("numName", numName);

        byte[] bytes = HttpRequestUtil.requestByteData("/api/honor/getHonorListExcel", req);
        if (bytes != null) {
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("保存Excel文件");
                fileChooser.setInitialFileName("荣誉列表.xlsx");
                File file = fileChooser.showSaveDialog(dataTableView.getScene().getWindow());
                if (file != null) {
                    try (FileOutputStream out = new FileOutputStream(file)) {
                        out.write(bytes);
                        MessageDialog.showDialog("导出成功！");
                    }
                }
            } catch (Exception e) {
                MessageDialog.showDialog("导出失败: " + e.getMessage());
            }
        } else {
            MessageDialog.showDialog("导出失败！");
        }
    }

    /**
     * 显示荣誉详细信息
     */
    private void showHonorDetails() {
        if (honorId == null)
            return;

        DataRequest req = new DataRequest();
        req.add("honorId", honorId);

        DataResponse res = HttpRequestUtil.request("/api/honor/getHonorInfo", req);
        if (res != null && res.getCode() == 0) {
            Map<String, Object> data = (Map<String, Object>) res.getData();
            if (data != null) {
                fillFormFields(data);
            }
        }
    }

    /**
     * 填充表单字段
     */
    private void fillFormFields(Map<String, Object> data) {
        numField.setText(CommonMethod.getString(data, "num"));
        honorNameField.setText(CommonMethod.getString(data, "honorName"));

        // 设置学生
        Integer studentId = CommonMethod.getInteger(data, "studentId");
        if (studentId != null) {
            studentComboBox.getItems().forEach(item -> {
                if (item.getValue().equals(studentId.toString())) {
                    studentComboBox.getSelectionModel().select(item);
                }
            });
        }

        // 设置荣誉类型
        String honorType = CommonMethod.getString(data, "honorType");
        honorTypeComboBox.getItems().forEach(item -> {
            if (item.getValue().equals(honorType)) {
                honorTypeComboBox.getSelectionModel().select(item);
            }
        });

        // 设置荣誉等级
        String honorLevel = CommonMethod.getString(data, "honorLevel");
        honorLevelComboBox.getItems().forEach(item -> {
            if (item.getValue().equals(honorLevel)) {
                honorLevelComboBox.getSelectionModel().select(item);
            }
        });

        awardingOrganizationField.setText(CommonMethod.getString(data, "awardingOrganization"));

        // 设置获奖日期
        String awardDate = CommonMethod.getString(data, "awardDate");
        if (!awardDate.isEmpty()) {
            try {
                awardDatePicker.setValue(LocalDate.parse(awardDate));
            } catch (Exception ignored) {
            }
        }

        certificateNumberField.setText(CommonMethod.getString(data, "certificateNumber"));
        descriptionField.setText(CommonMethod.getString(data, "description"));

        // 设置状态
        String status = CommonMethod.getString(data, "status");
        statusComboBox.getItems().forEach(item -> {
            if (item.getValue().equals(status)) {
                statusComboBox.getSelectionModel().select(item);
            }
        });
    }

    /**
     * 清空表单字段
     */
    private void clearFormFields() {
        numField.clear();
        honorNameField.clear();
        studentComboBox.getSelectionModel().clearSelection();
        honorTypeComboBox.getSelectionModel().clearSelection();
        honorLevelComboBox.getSelectionModel().clearSelection();
        statusComboBox.getSelectionModel().clearSelection();
        awardingOrganizationField.clear();
        awardDatePicker.setValue(null);
        certificateNumberField.clear();
        descriptionField.clear();
    }

    /**
     * 生成默认编号
     */
    private void generateDefaultNum() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String defaultNum = "HONOR" + date + String.format("%03d", (int) (Math.random() * 1000));
        numField.setText(defaultNum);
    }
}