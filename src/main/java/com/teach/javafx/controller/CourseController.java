package com.teach.javafx.controller;

import com.teach.javafx.controller.base.MessageDialog;
import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;
import com.teach.javafx.request.HttpRequestUtil;
import com.teach.javafx.request.OptionItem;
import com.teach.javafx.util.CommonMethod;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
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
 * CourseController 课程管理控制类 对应 course-panel.fxml
 */
public class CourseController extends ToolController {

    @FXML
    private TableView<Map> dataTableView; // 课程信息表
    @FXML
    private TableColumn<Map, String> numColumn; // 课程编号列
    @FXML
    private TableColumn<Map, String> nameColumn; // 课程名称列
    @FXML
    private TableColumn<Map, String> creditColumn; // 学分列
    @FXML
    private TableColumn<Map, String> preCourseColumn; // 前序课程列

    @FXML
    private TextField numField; // 课程编号输入域
    @FXML
    private TextField nameField; // 课程名称输入域
    @FXML
    private TextField creditField; // 学分输入域
    @FXML
    private TextField coursePathField; // 课程路径输入域
    @FXML
    private ComboBox<OptionItem> preCourseComboBox; // 前序课程选择框

    @FXML
    private TextField numNameTextField; // 查询 编号名称输入域

    private Integer courseId = null; // 当前编辑修改的课程的主键
    private ArrayList<Map> courseList = new ArrayList<>(); // 课程信息列表数据
    private List<OptionItem> preCourseList; // 前序课程选择列表数据
    private ObservableList<Map> observableList = FXCollections.observableArrayList(); // TableView渲染列表

    /**
     * 将课程数据集合设置到面板上显示
     */
    private void setTableViewData() {
        observableList.clear();
        for (int j = 0; j < courseList.size(); j++) {
            Map<String, Object> record = courseList.get(j);

            // 确保courseId字段存在
            if (record.containsKey("course_id") && !record.containsKey("courseId")) {
                record.put("courseId", record.get("course_id"));
            }

            observableList.addAll(FXCollections.observableArrayList(record));
        }
        dataTableView.setItems(observableList);
    }

    /**
     * 页面加载对象创建完成初始化方法
     */
    @FXML
    public void initialize() {
        // 初始化表格列
        numColumn.setCellValueFactory(new MapValueFactory<>("num"));
        nameColumn.setCellValueFactory(new MapValueFactory<>("name"));
        creditColumn.setCellValueFactory(new MapValueFactory<>("credit"));
        preCourseColumn.setCellValueFactory(new MapValueFactory<>("preCourse"));

        // 设置表格行选择监听器
        TableView.TableViewSelectionModel<Map> tsm = dataTableView.getSelectionModel();
        ObservableList<Integer> list = tsm.getSelectedIndices();
        list.addListener(this::onTableRowSelect);

        // 初始化前序课程下拉列表
        loadPreCourseList();

        // 初始化课程列表数据
        onQueryButtonClick();
    }

    /**
     * 加载前序课程列表
     */
    private void loadPreCourseList() {
        try {
            DataRequest req = new DataRequest();
            DataResponse res = HttpRequestUtil.request("/api/course/getAllCourses", req);

            preCourseList = new ArrayList<>();
            // 添加空选项
            preCourseList.add(new OptionItem(null, "", "无前序课程"));

            if (res != null && res.getCode() == 0) {
                List<Map> courses = (List<Map>) res.getData();
                if (courses != null) {
                    for (Map course : courses) {
                        String value = CommonMethod.getString(course, "value");
                        String title = CommonMethod.getString(course, "title");
                        Integer id = CommonMethod.getInteger(course, "value"); // 使用value作为id
                        preCourseList.add(new OptionItem(id, value, title));
                    }
                }
            }

            preCourseComboBox.getItems().clear();
            preCourseComboBox.getItems().addAll(preCourseList);
        } catch (Exception e) {
            MessageDialog.showDialog("加载前序课程列表失败: " + e.getMessage());
        }
    }

    /**
     * 清除课程表单中输入信息
     */
    public void clearPanel() {
        courseId = null;
        numField.setText("");
        nameField.setText("");
        creditField.setText("");
        coursePathField.setText("");
        preCourseComboBox.getSelectionModel().select(0); // 选择"无前序课程"
    }

    /**
     * 切换课程信息
     */
    protected void changeCourseInfo() {
        Map<String, Object> form = dataTableView.getSelectionModel().getSelectedItem();
        if (form == null) {
            clearPanel();
            return;
        }

        courseId = CommonMethod.getInteger(form, "courseId");
        DataRequest req = new DataRequest();
        req.add("courseId", courseId);
        DataResponse res = HttpRequestUtil.request("/api/course/getCourseInfo", req);

        if (res == null) {
            MessageDialog.showDialog("无法连接到服务器，请检查网络连接");
            return;
        }

        if (res.getCode() != 0) {
            MessageDialog.showDialog(res.getMsg());
            return;
        }

        form = (Map) res.getData();
        numField.setText(CommonMethod.getString(form, "num"));
        nameField.setText(CommonMethod.getString(form, "name"));

        // 确保学分显示为整数，避免小数点问题
        Object creditObj = form.get("credit");
        if (creditObj != null) {
            if (creditObj instanceof Integer) {
                creditField.setText(creditObj.toString());
            } else if (creditObj instanceof String) {
                try {
                    // 如果是字符串，尝试转换为整数再显示
                    double creditValue = Double.parseDouble((String) creditObj);
                    creditField.setText(String.valueOf((int) creditValue));
                } catch (NumberFormatException e) {
                    creditField.setText((String) creditObj);
                }
            } else {
                creditField.setText(creditObj.toString());
            }
        } else {
            creditField.setText("");
        }

        coursePathField.setText(CommonMethod.getString(form, "coursePath"));

        // 设置前序课程选择
        Integer preCourseId = CommonMethod.getInteger(form, "preCourseId");
        if (preCourseId != null) {
            preCourseComboBox.getSelectionModel().select(
                    CommonMethod.getOptionItemIndexByValue(preCourseList, preCourseId.toString()));
        } else {
            preCourseComboBox.getSelectionModel().select(0);
        }
    }

    /**
     * 点击课程列表的某一行，切换课程的编辑信息
     */
    public void onTableRowSelect(ListChangeListener.Change<? extends Integer> change) {
        changeCourseInfo();
    }

    /**
     * 点击查询按钮，查询课程列表
     */
    @FXML
    protected void onQueryButtonClick() {
        String numName = numNameTextField.getText();
        DataRequest req = new DataRequest();
        req.add("numName", numName);
        DataResponse res = HttpRequestUtil.request("/api/course/getCourseList", req);

        if (res != null && res.getCode() == 0) {
            courseList = (ArrayList<Map>) res.getData();
            setTableViewData();
        } else {
            String errorMsg = (res != null) ? res.getMsg() : "无法连接到服务器";
            MessageDialog.showDialog("查询课程列表失败: " + errorMsg);
        }
    }

    /**
     * 刷新课程列表
     */
    public void refreshCourseList() {
        numNameTextField.setText(""); // 清空搜索条件
        onQueryButtonClick();
    }

    /**
     * 添加新课程
     */
    @FXML
    protected void onAddButtonClick() {
        clearPanel();
        MessageDialog.showDialog("请填写课程信息，然后点击保存按钮");
    }

    /**
     * 点击删除按钮 删除当前编辑的课程
     */
    @FXML
    protected void onDeleteButtonClick() {
        Map form = dataTableView.getSelectionModel().getSelectedItem();
        if (form == null) {
            MessageDialog.showDialog("没有选择，不能删除");
            return;
        }

        int ret = MessageDialog.choiceDialog("确认要删除课程「" + CommonMethod.getString(form, "name") + "」吗?");
        if (ret != MessageDialog.CHOICE_YES) {
            return;
        }

        courseId = CommonMethod.getInteger(form, "courseId");
        DataRequest req = new DataRequest();
        req.add("courseId", courseId);
        DataResponse res = HttpRequestUtil.request("/api/course/courseDelete", req);

        if (res != null) {
            if (res.getCode() == 0) {
                MessageDialog.showDialog("删除成功！");
                onQueryButtonClick();
                clearPanel();
            } else {
                MessageDialog.showDialog(res.getMsg());
            }
        } else {
            MessageDialog.showDialog("无法连接到服务器，请检查网络连接");
        }
    }

    /**
     * 点击保存按钮，保存当前编辑的课程信息
     */
    @FXML
    protected void onSaveButtonClick() {
        // 获取表单数据
        String num = numField.getText();
        String name = nameField.getText();
        String credit = creditField.getText();
        String coursePath = coursePathField.getText();

        // 检查必填字段
        if (num == null || num.trim().isEmpty()) {
            MessageDialog.showDialog("课程编号不能为空");
            return;
        }
        if (name == null || name.trim().isEmpty()) {
            MessageDialog.showDialog("课程名称不能为空");
            return;
        }

        // 验证学分字段
        Integer creditValue = null;
        if (credit != null && !credit.trim().isEmpty()) {
            try {
                creditValue = Integer.parseInt(credit.trim());
                if (creditValue < 0) {
                    MessageDialog.showDialog("学分不能为负数");
                    return;
                }
                if (creditValue > 20) {
                    MessageDialog.showDialog("学分不能超过20");
                    return;
                }
            } catch (NumberFormatException e) {
                MessageDialog.showDialog("学分必须是有效的整数");
                return;
            }
        } else {
            MessageDialog.showDialog("学分不能为空");
            return;
        }

        // 获取前序课程ID
        Integer preCourseId = null;
        OptionItem selectedPreCourse = preCourseComboBox.getSelectionModel().getSelectedItem();
        if (selectedPreCourse != null && selectedPreCourse.getId() != null) {
            preCourseId = selectedPreCourse.getId();
        }

        // 创建请求对象
        Map<String, Object> form = new HashMap<>();
        form.put("num", num.trim());
        form.put("name", name.trim());
        form.put("credit", creditValue);
        form.put("coursePath", coursePath != null ? coursePath.trim() : "");
        form.put("preCourseId", preCourseId);

        DataRequest req = new DataRequest();
        req.add("courseId", courseId);
        req.add("form", form);

        DataResponse res = HttpRequestUtil.request("/api/course/courseEditSave", req);

        if (res != null && res.getCode() == 0) {
            courseId = CommonMethod.getIntegerFromObject(res.getData());
            MessageDialog.showDialog("保存成功！");
            // 刷新课程列表
            onQueryButtonClick();
            // 重新加载前序课程列表（如果添加了新课程）
            loadPreCourseList();
        } else {
            String errorMsg = (res != null) ? res.getMsg() : "无法连接到服务器，请检查网络连接";
            MessageDialog.showDialog("保存失败: " + errorMsg);
        }
    }

    /**
     * 重写 ToolController 中的方法
     */
    public void doNew() {
        onAddButtonClick();
    }

    public void doSave() {
        onSaveButtonClick();
    }

    public void doDelete() {
        onDeleteButtonClick();
    }

    /**
     * 导出课程信息表到Excel
     */
    public void doExport() {
        String numName = numNameTextField.getText();
        DataRequest req = new DataRequest();
        req.add("numName", numName);

        byte[] bytes = HttpRequestUtil.requestByteData("/api/course/getCourseListExcel", req);
        if (bytes != null) {
            try {
                FileChooser fileDialog = new FileChooser();
                fileDialog.setTitle("选择保存的文件");
                fileDialog.setInitialDirectory(new File(System.getProperty("user.home")));
                fileDialog.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("XLSX 文件", "*.xlsx"));
                File file = fileDialog.showSaveDialog(null);

                if (file != null) {
                    FileOutputStream out = new FileOutputStream(file);
                    out.write(bytes);
                    out.close();
                    MessageDialog.showDialog("导出成功！文件保存在: " + file.getPath());
                }
            } catch (Exception e) {
                MessageDialog.showDialog("导出失败: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            MessageDialog.showDialog("导出失败: 无法获取数据");
        }
    }

    /**
     * 刷新前序课程列表
     */
    @FXML
    protected void onRefreshPreCourseButtonClick() {
        loadPreCourseList();
        MessageDialog.showDialog("前序课程列表已刷新");
    }
}
