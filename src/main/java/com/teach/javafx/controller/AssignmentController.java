package com.teach.javafx.controller;

import com.teach.javafx.controller.base.MessageDialog;
import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;
import com.teach.javafx.request.HttpRequestUtil;
import com.teach.javafx.request.OptionItem;
import com.teach.javafx.util.CommonMethod;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AssignmentController 作业管理控制类 对应 assignment-panel.fxml
 */
public class AssignmentController extends ToolController {
    @FXML
    private TextField numTitleTextField;
    @FXML
    private ComboBox<OptionItem> courseFilterComboBox;
    @FXML
    private TableView<Map> dataTableView;
    @FXML
    private TableColumn<Map, String> numColumn;
    @FXML
    private TableColumn<Map, String> titleColumn;
    @FXML
    private TableColumn<Map, String> courseColumn;
    @FXML
    private TableColumn<Map, String> publishTimeColumn;
    @FXML
    private TableColumn<Map, String> deadlineColumn;
    @FXML
    private TableColumn<Map, String> statusColumn;

    @FXML
    private TextField numField;
    @FXML
    private TextField titleField;
    @FXML
    private ComboBox<OptionItem> courseComboBox;
    @FXML
    private ComboBox<OptionItem> statusComboBox;
    @FXML
    private ComboBox<OptionItem> submissionTypeComboBox;
    @FXML
    private DatePicker publishTimePicker;
    @FXML
    private DatePicker deadlinePicker;
    @FXML
    private TextField totalScoreField;
    @FXML
    private TextArea contentField;
    @FXML
    private TextArea requirementsField;

    private Integer assignmentId = null;
    private ObservableList<Map> observableList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // 初始化表格列
        numColumn.setCellValueFactory(new MapValueFactory<>("num"));
        titleColumn.setCellValueFactory(new MapValueFactory<>("title"));
        courseColumn.setCellValueFactory(new MapValueFactory<>("courseName"));
        publishTimeColumn.setCellValueFactory(new MapValueFactory<>("publishTime"));
        deadlineColumn.setCellValueFactory(new MapValueFactory<>("deadline"));
        statusColumn.setCellValueFactory(new MapValueFactory<>("status"));

        dataTableView.setItems(observableList);

        // 监听表格选择变化
        dataTableView.getSelectionModel().getSelectedItems().addListener(
                (javafx.collections.ListChangeListener<Map>) change -> {
                    while (change.next()) {
                        if (change.wasAdded() && !change.getAddedSubList().isEmpty()) {
                            Map selectedItem = change.getAddedSubList().get(0);
                            if (selectedItem != null) {
                                assignmentId = CommonMethod.getInteger(selectedItem, "assignmentId");
                                showAssignmentDetails();
                            }
                        }
                    }
                });

        initializeComboBoxes();
        loadCourseList();
        onQueryButtonClick();
    }

    private void initializeComboBoxes() {
        // 作业状态下拉框
        List<OptionItem> statusList = new ArrayList<>();
        statusList.add(new OptionItem(1, "草稿", "草稿"));
        statusList.add(new OptionItem(2, "已发布", "已发布"));
        statusList.add(new OptionItem(3, "已截止", "已截止"));
        statusComboBox.setItems(FXCollections.observableArrayList(statusList));

        // 提交方式下拉框
        List<OptionItem> submissionTypeList = new ArrayList<>();
        submissionTypeList.add(new OptionItem(1, "在线提交", "在线提交"));
        submissionTypeList.add(new OptionItem(2, "文件上传", "文件上传"));
        submissionTypeList.add(new OptionItem(3, "邮件提交", "邮件提交"));
        submissionTypeComboBox.setItems(FXCollections.observableArrayList(submissionTypeList));
    }

    private void loadCourseList() {
        DataRequest req = new DataRequest();
        DataResponse res = HttpRequestUtil.request("/api/score/getCourseItemOptionList", req);

        // 添加空值检查和处理
        if (res != null && res.getCode() == 0) {
            List<Map<String, Object>> courseList = (List<Map<String, Object>>) res.getData();
            List<OptionItem> options = new ArrayList<>();
            options.add(new OptionItem(0, "", "全部课程")); // 添加默认选项

            // 检查courseList是否为null
            if (courseList != null) {
                for (Map<String, Object> course : courseList) {
                    Integer courseId = CommonMethod.getInteger(course, "value");
                    String title = CommonMethod.getString(course, "title");
                    if (courseId != null && title != null) {
                        options.add(new OptionItem(courseId, courseId.toString(), title));
                    }
                }
            } else {
                // 可以添加日志或默认课程选项
                System.out.println("警告: 获取的课程列表为空");
                // 添加一些默认课程选项示例（实际项目中可以从配置读取或使用缓存）
                options.add(new OptionItem(1, "1", "默认课程1"));
                options.add(new OptionItem(2, "2", "默认课程2"));
            }

            // 更新UI控件
            Platform.runLater(() -> {
                courseFilterComboBox.setItems(FXCollections.observableArrayList(options));
                // 排除"全部课程"选项
                courseComboBox.setItems(FXCollections.observableArrayList(
                        options.subList(1, options.size())));
            });
        } else {
            String errorMsg = res != null ? res.getMsg() : "无法连接到服务器";
            System.out.println("获取课程列表失败: " + errorMsg);
            MessageDialog.showDialog("获取课程列表失败: " + errorMsg);

            // 设置空列表避免NPE
            Platform.runLater(() -> {
                courseFilterComboBox.setItems(FXCollections.observableArrayList(
                        new OptionItem(0, "", "全部课程")));
                courseComboBox.setItems(FXCollections.observableArrayList());
            });
        }
    }

    @FXML
    protected void onQueryButtonClick() {
        try {
            String numTitle = numTitleTextField.getText();
            Integer courseId = null;

            // 安全获取课程ID
            if (courseFilterComboBox.getSelectionModel().getSelectedItem() != null) {
                try {
                    courseId = Integer.parseInt(courseFilterComboBox.getSelectionModel().getSelectedItem().getValue());
                } catch (NumberFormatException e) {
                    System.err.println("课程ID格式错误");
                }
            }

            DataRequest req = new DataRequest();
            req.add("numTitle", numTitle != null ? numTitle : "");

            // 只有当courseId有效时才添加
            if (courseId != null && courseId > 0) {
                req.add("courseId", courseId);
            }

            // 打印调试信息
            System.out.println("发送请求参数: " + req.getData());

            DataResponse res = HttpRequestUtil.request("/api/homework/getHomeworkList", req);

            // 打印响应信息
            System.out.println("收到响应: " + (res != null ? res.toString() : "null"));

            if (res != null && res.getCode() == 0) {
                observableList.clear();
                List<Map<String, Object>> dataList = (List<Map<String, Object>>) res.getData();

                if (dataList != null) {
                    System.out.println("获取到作业数量: " + dataList.size());
                    observableList.addAll(FXCollections.observableArrayList(dataList));
                } else {
                    System.out.println("警告: 作业列表为空");
                    MessageDialog.showDialog("没有找到符合条件的作业");
                }
            } else {
                String errorMsg = res != null ? res.getMsg() : "无法连接到服务器";
                System.err.println("查询失败: " + errorMsg);
                MessageDialog.showDialog("查询失败: " + errorMsg);
            }
        } catch (Exception e) {
            System.err.println("查询异常: " + e.getMessage());
            e.printStackTrace();
            MessageDialog.showDialog("查询时发生异常: " + e.getMessage());
        }
    }

    @FXML
    protected void onAddButtonClick() {
        assignmentId = null;
        clearFormFields();
    }

    @FXML
    protected void onSaveButtonClick() {
        String num = numField.getText().trim();
        String title = titleField.getText().trim();

        if (num.isEmpty()) {
            MessageDialog.showDialog("作业编号不能为空！");
            return;
        }
        if (title.isEmpty()) {
            MessageDialog.showDialog("作业标题不能为空！");
            return;
        }

        Map<String, Object> form = new HashMap<>();
        form.put("num", num);
        form.put("title", title);
        if (courseComboBox.getSelectionModel().getSelectedItem() != null) {
            form.put("courseId", Integer.parseInt(courseComboBox.getSelectionModel().getSelectedItem().getValue()));
        }
        form.put("publishTime",
                publishTimePicker.getValue() != null ? publishTimePicker.getValue().toString() + " 00:00:00" : "");
        form.put("deadline",
                deadlinePicker.getValue() != null ? deadlinePicker.getValue().toString() + " 23:59:59" : "");
        form.put("content", contentField.getText().trim());
        form.put("requirements", requirementsField.getText().trim());
        form.put("status",
                statusComboBox.getSelectionModel().getSelectedItem() != null
                        ? statusComboBox.getSelectionModel().getSelectedItem().getValue()
                        : "已发布");
        form.put("submissionType",
                submissionTypeComboBox.getSelectionModel().getSelectedItem() != null
                        ? submissionTypeComboBox.getSelectionModel().getSelectedItem().getValue()
                        : "在线提交");

        String totalScoreText = totalScoreField.getText().trim();
        if (!totalScoreText.isEmpty()) {
            try {
                form.put("totalScore", Integer.parseInt(totalScoreText));
            } catch (NumberFormatException e) {
                MessageDialog.showDialog("总分必须是数字！");
                return;
            }
        }

        DataRequest req = new DataRequest();
        req.add("assignmentId", assignmentId);
        req.add("form", form);

        DataResponse res = HttpRequestUtil.request("/api/homework/homeworkSave", req);
        if (res != null && res.getCode() == 0) {
            MessageDialog.showDialog("保存成功！");
            onQueryButtonClick();
            if (assignmentId == null) {
                clearFormFields();
            }
        } else {
            MessageDialog.showDialog("保存失败: " + (res != null ? res.getMsg() : "网络错误"));
        }
    }

    @FXML
    protected void onDeleteButtonClick() {
        if (assignmentId == null) {
            MessageDialog.showDialog("请先选择要删除的作业！");
            return;
        }

        int choice = MessageDialog.choiceDialog("确定要删除所选作业吗？");
        if (choice != MessageDialog.CHOICE_YES) {
            return;
        }

        DataRequest req = new DataRequest();
        req.add("assignmentId", assignmentId);

        DataResponse res = HttpRequestUtil.request("/api/homework/homeworkDelete", req);
        if (res != null && res.getCode() == 0) {
            MessageDialog.showDialog("删除成功！");
            assignmentId = null;
            clearFormFields();
            onQueryButtonClick();
        } else {
            MessageDialog.showDialog("删除失败: " + (res != null ? res.getMsg() : "网络错误"));
        }
    }

    @FXML
    protected void onExportButtonClick() {
        String numTitle = numTitleTextField.getText();

        DataRequest req = new DataRequest();
        req.add("numTitle", numTitle);

        byte[] bytes = HttpRequestUtil.requestByteData("/api/homework/getAssignmentListExcel", req);
        if (bytes != null) {
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("保存Excel文件");
                fileChooser.setInitialFileName("作业列表.xlsx");
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

    private void clearFormFields() {
        numField.clear();
        titleField.clear();
        courseComboBox.getSelectionModel().clearSelection();
        statusComboBox.getSelectionModel().clearSelection();
        submissionTypeComboBox.getSelectionModel().clearSelection();
        publishTimePicker.setValue(null);
        deadlinePicker.setValue(null);
        totalScoreField.clear();
        contentField.clear();
        requirementsField.clear();
    }

    /**
     * 显示作业详细信息
     */
    private void showAssignmentDetails() {
        if (assignmentId == null)
            return;

        DataRequest req = new DataRequest();
        req.add("assignmentId", assignmentId);

        DataResponse res = HttpRequestUtil.request("/api/homework/getHomeworkInfo", req);
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
        titleField.setText(CommonMethod.getString(data, "title"));

        // 设置课程
        Integer courseId = CommonMethod.getInteger(data, "courseId");
        if (courseId != null) {
            courseComboBox.getItems().forEach(item -> {
                if (item.getValue().equals(courseId.toString())) {
                    courseComboBox.getSelectionModel().select(item);
                }
            });
        }

        // 设置时间字段
        String publishTime = CommonMethod.getString(data, "publishTime");
        if (!publishTime.isEmpty()) {
            try {
                publishTimePicker.setValue(java.time.LocalDate.parse(publishTime.substring(0, 10)));
            } catch (Exception ignored) {
            }
        }

        String deadline = CommonMethod.getString(data, "deadline");
        if (!deadline.isEmpty()) {
            try {
                deadlinePicker.setValue(java.time.LocalDate.parse(deadline.substring(0, 10)));
            } catch (Exception ignored) {
            }
        }

        // 设置总分，处理Double类型
        Object totalScoreObj = data.get("totalScore");
        if (totalScoreObj != null) {
            if (totalScoreObj instanceof Double) {
                totalScoreField.setText(String.valueOf(((Double) totalScoreObj).intValue()));
            } else {
                totalScoreField.setText(totalScoreObj.toString());
            }
        } else {
            totalScoreField.setText("");
        }

        contentField.setText(CommonMethod.getString(data, "content"));
        requirementsField.setText(CommonMethod.getString(data, "requirements"));

        // 设置状态
        String status = CommonMethod.getString(data, "status");
        statusComboBox.getItems().forEach(item -> {
            if (item.getValue().equals(status)) {
                statusComboBox.getSelectionModel().select(item);
            }
        });

        // 设置提交方式
        String submissionType = CommonMethod.getString(data, "submissionType");
        submissionTypeComboBox.getItems().forEach(item -> {
            if (item.getValue().equals(submissionType)) {
                submissionTypeComboBox.getSelectionModel().select(item);
            }
        });
    }
}