package com.teach.javafx.controller;

<<<<<<< Updated upstream

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
=======
import com.teach.javafx.controller.base.LocalDateStringConverter;
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
>>>>>>> Stashed changes
import java.util.List;
import java.util.Map;

/**
<<<<<<< Updated upstream
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
=======
 * ActivityController 活动管理控制类 对应 activity-panel.fxml
 */
public class ActivityController extends ToolController {

    @FXML
    private TableView<Map> dataTableView; // 活动信息表
    @FXML
    private TableColumn<Map, String> numColumn; // 活动编号列
    @FXML
    private TableColumn<Map, String> nameColumn; // 活动名称列
    @FXML
    private TableColumn<Map, String> timeColumn; // 活动时间列
    @FXML
    private TableColumn<Map, String> durationColumn; // 时长列
    @FXML
    private TableColumn<Map, String> preActivityColumn; // 前序活动列

    @FXML
    private TextField numField; // 活动编号输入域
    @FXML
    private TextField nameField; // 活动名称输入域
    @FXML
    private TextField timeField; // 活动时间输入域
    @FXML
    private TextField durationField; // 时长输入域
    @FXML
    private TextField activityPathField; // 活动路径输入域
    @FXML
    private ComboBox<OptionItem> preActivityComboBox; // 前序活动选择框

    @FXML
    private TextField numNameTextField; // 查询 编号名称输入域

    private Integer activityId = null; // 当前编辑修改的活动的主键
    private ArrayList<Map> activityList = new ArrayList<>(); // 活动信息列表数据
    private List<OptionItem> preActivityList; // 前序活动选择列表数据
    private ObservableList<Map> observableList = FXCollections.observableArrayList(); // TableView渲染列表

    /**
     * 将活动数据集合设置到面板上显示
     */
    private void setTableViewData() {
        observableList.clear();
        for (int j = 0; j < activityList.size(); j++) {
            Map<String, Object> record = activityList.get(j);

            // 确保activityId字段存在
            if (record.containsKey("activity_id") && !record.containsKey("activityId")) {
                record.put("activityId", record.get("activity_id"));
            }

            observableList.addAll(FXCollections.observableArrayList(record));
>>>>>>> Stashed changes
        }
        dataTableView.setItems(observableList);
    }

<<<<<<< Updated upstream
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
=======
    /**
     * 页面加载对象创建完成初始化方法
     */
    @FXML
    public void initialize() {
        // 初始化表格列
        numColumn.setCellValueFactory(new MapValueFactory<>("num"));
        nameColumn.setCellValueFactory(new MapValueFactory<>("name"));
        timeColumn.setCellValueFactory(new MapValueFactory<>("time"));
        durationColumn.setCellValueFactory(new MapValueFactory<>("duration"));
        preActivityColumn.setCellValueFactory(new MapValueFactory<>("preActivity"));

        // 设置表格行选择监听器
        TableView.TableViewSelectionModel<Map> tsm = dataTableView.getSelectionModel();
        ObservableList<Integer> list = tsm.getSelectedIndices();
        list.addListener(this::onTableRowSelect);

        // 初始化前序活动下拉列表
        loadPreActivityList();

        // 初始化活动列表数据
        onQueryButtonClick();
    }

    /**
     * 加载前序活动列表
     */
    private void loadPreActivityList() {
        try {
            DataRequest req = new DataRequest();
            DataResponse res = HttpRequestUtil.request("/api/activity/getAllActivities", req);

            preActivityList = new ArrayList<>();
            // 添加空选项
            preActivityList.add(new OptionItem(null, "", "无前序活动"));

            if (res != null && res.getCode() == 0) {
                List<Map> activities = (List<Map>) res.getData();
                if (activities != null) {
                    for (Map activity : activities) {
                        String value = CommonMethod.getString(activity, "value");
                        String title = CommonMethod.getString(activity, "title");
                        Integer id = CommonMethod.getInteger(activity, "value"); // 使用value作为id
                        preActivityList.add(new OptionItem(id, value, title));
                    }
                }
            }

            preActivityComboBox.getItems().clear();
            preActivityComboBox.getItems().addAll(preActivityList);
        } catch (Exception e) {
            MessageDialog.showDialog("加载前序活动列表失败: " + e.getMessage());
        }
    }

    /**
     * 清除活动表单中输入信息
     */
    public void clearPanel() {
        activityId = null;
        numField.setText("");
        nameField.setText("");
        timeField.setText("");
        durationField.setText("");
        activityPathField.setText("");
        preActivityComboBox.getSelectionModel().select(0); // 选择"无前序活动"
    }

    /**
     * 切换活动信息
     */
    protected void changeActivityInfo() {
        Map<String, Object> form = dataTableView.getSelectionModel().getSelectedItem();
        if (form == null) {
            clearPanel();
            return;
        }

        activityId = CommonMethod.getInteger(form, "activityId");
        DataRequest req = new DataRequest();
        req.add("activityId", activityId);
        DataResponse res = HttpRequestUtil.request("/api/activity/getActivityInfo", req);

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
        timeField.setText(CommonMethod.getString(form, "time"));

        // 确保时长显示为整数，避免小数点问题
        Object durationObj = form.get("duration");
        if (durationObj != null) {
            if (durationObj instanceof Integer) {
                durationField.setText(durationObj.toString());
            } else if (durationObj instanceof String) {
                try {
                    // 如果是字符串，尝试转换为整数再显示
                    double durationValue = Double.parseDouble((String) durationObj);
                    durationField.setText(String.valueOf((int) durationValue));
                } catch (NumberFormatException e) {
                    durationField.setText((String) durationObj);
                }
            } else {
                durationField.setText(durationObj.toString());
            }
        } else {
            durationField.setText("");
        }

        activityPathField.setText(CommonMethod.getString(form, "activityPath"));

        // 设置前序活动选择
        Integer preActivityId = CommonMethod.getInteger(form, "preActivityId");
        if (preActivityId != null) {
            preActivityComboBox.getSelectionModel().select(
                    CommonMethod.getOptionItemIndexByValue(preActivityList, preActivityId.toString()));
        } else {
            preActivityComboBox.getSelectionModel().select(0);
        }
    }

    /**
     * 点击活动列表的某一行，切换活动的编辑信息
     */
    public void onTableRowSelect(ListChangeListener.Change<? extends Integer> change) {
        changeActivityInfo();
    }

    /**
     * 点击查询按钮，查询活动列表
     */
    @FXML
    protected void onQueryButtonClick() {
        String numName = numNameTextField.getText();
        DataRequest req = new DataRequest();
        req.add("numName", numName);
        DataResponse res = HttpRequestUtil.request("/api/activity/getActivityList", req);

        if (res != null && res.getCode() == 0) {
            activityList = (ArrayList<Map>) res.getData();
            setTableViewData();
        } else {
            String errorMsg = (res != null) ? res.getMsg() : "无法连接到服务器";
            MessageDialog.showDialog("查询活动列表失败: " + errorMsg);
        }
    }

    /**
     * 刷新活动列表
     */
    public void refreshActivityList() {
        numNameTextField.setText(""); // 清空搜索条件
        onQueryButtonClick();
    }

    /**
     * 添加新活动
     */
    @FXML
    protected void onAddButtonClick() {
        clearPanel();
        MessageDialog.showDialog("请填写活动信息，然后点击保存按钮");
    }

    /**
     * 点击删除按钮 删除当前编辑的活动
     */
    @FXML
    protected void onDeleteButtonClick() {
        Map form = dataTableView.getSelectionModel().getSelectedItem();
        if (form == null) {
            MessageDialog.showDialog("没有选择，不能删除");
            return;
        }

        int ret = MessageDialog.choiceDialog("确认要删除活动「" + CommonMethod.getString(form, "name") + "」吗?");
        if (ret != MessageDialog.CHOICE_YES) {
            return;
        }

        activityId = CommonMethod.getInteger(form, "activityId");
        DataRequest req = new DataRequest();
        req.add("activityId", activityId);
        DataResponse res = HttpRequestUtil.request("/api/activity/activityDelete", req);

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
     * 点击保存按钮，保存当前编辑的活动信息
     */
    @FXML
    protected void onSaveButtonClick() {
        // 获取表单数据
        String num = numField.getText();
        String name = nameField.getText();
        String time = timeField.getText();
        String duration = durationField.getText();
        String activityPath = activityPathField.getText();

        // 检查必填字段
        if (num == null || num.trim().isEmpty()) {
            MessageDialog.showDialog("活动编号不能为空");
            return;
        }
        if (name == null || name.trim().isEmpty()) {
            MessageDialog.showDialog("活动名称不能为空");
            return;
        }

        // 验证时长字段
        Integer durationValue = null;
        if (duration != null && !duration.trim().isEmpty()) {
            try {
                durationValue = Integer.parseInt(duration.trim());
                if (durationValue < 0) {
                    MessageDialog.showDialog("活动时长不能为负数");
                    return;
                }
            } catch (NumberFormatException e) {
                MessageDialog.showDialog("活动时长必须是有效的整数");
                return;
            }
        }

        // 获取前序活动ID
        Integer preActivityId = null;
        OptionItem selectedPreActivity = preActivityComboBox.getSelectionModel().getSelectedItem();
        if (selectedPreActivity != null && selectedPreActivity.getId() != null) {
            preActivityId = selectedPreActivity.getId();
        }

        // 创建请求对象
        Map<String, Object> form = new HashMap<>();
        form.put("num", num.trim());
        form.put("name", name.trim());
        form.put("time", time != null ? time.trim() : "");
        form.put("duration", durationValue);
        form.put("activityPath", activityPath != null ? activityPath.trim() : "");
        form.put("preActivityId", preActivityId);

        DataRequest req = new DataRequest();
        req.add("activityId", activityId);
        req.add("form", form);

        DataResponse res = HttpRequestUtil.request("/api/activity/activityEditSave", req);

        if (res != null && res.getCode() == 0) {
            activityId = CommonMethod.getIntegerFromObject(res.getData());
            MessageDialog.showDialog("保存成功！");
            // 刷新活动列表
            onQueryButtonClick();
            // 重新加载前序活动列表（如果添加了新活动）
            loadPreActivityList();
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
     * 导出活动信息表到Excel
     */
    public void doExport() {
        String numName = numNameTextField.getText();
        DataRequest req = new DataRequest();
        req.add("numName", numName);

        byte[] bytes = HttpRequestUtil.requestByteData("/api/activity/getActivityListExcel", req);
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
     * 刷新前序活动列表
     */
    @FXML
    protected void onRefreshPreActivityButtonClick() {
        loadPreActivityList();
        MessageDialog.showDialog("前序活动列表已刷新");
    }
>>>>>>> Stashed changes
}
