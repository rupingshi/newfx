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
 * TeacherController 教师管理控制类 对应 teacher-panel.fxml
 */
public class TeacherController extends ToolController {

    @FXML
    private TableView<Map> dataTableView; // 教师信息表
    @FXML
    private TableColumn<Map, String> numColumn; // 教师编号列
    @FXML
    private TableColumn<Map, String> nameColumn; // 姓名列
    @FXML
    private TableColumn<Map, String> deptColumn; // 学院列
    @FXML
    private TableColumn<Map, String> genderColumn; // 性别列
    @FXML
    private TableColumn<Map, String> phoneColumn; // 电话列

    @FXML
    private TextField numField; // 教师编号输入域
    @FXML
    private TextField nameField; // 姓名输入域
    @FXML
    private TextField deptField; // 学院输入域
    @FXML
    private TextField cardField; // 身份证号输入域
    @FXML
    private ComboBox<OptionItem> genderComboBox; // 性别选择框
    @FXML
    private TextField birthdayField; // 生日输入域
    @FXML
    private TextField emailField; // 邮箱输入域
    @FXML
    private TextField phoneField; // 电话输入域
    @FXML
    private TextField addressField; // 地址输入域
    @FXML
    private TextArea introduceTextArea; // 个人简介输入域

    @FXML
    private TextField numNameTextField; // 查询 编号姓名输入域

    private Integer personId = null; // 当前编辑修改的教师的主键
    private ArrayList<Map> teacherList = new ArrayList<>(); // 教师信息列表数据
    private List<OptionItem> genderList; // 性别选择列表数据
    private ObservableList<Map> observableList = FXCollections.observableArrayList(); // TableView渲染列表

    /**
     * 将教师数据集合设置到面板上显示
     */
    private void setTableViewData() {
        observableList.clear();
        for (int j = 0; j < teacherList.size(); j++) {
            Map<String, Object> record = teacherList.get(j);

            // 确保personId字段存在
            if (record.containsKey("person_id") && !record.containsKey("personId")) {
                record.put("personId", record.get("person_id"));
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
        deptColumn.setCellValueFactory(new MapValueFactory<>("dept"));
        genderColumn.setCellValueFactory(cell -> {
            Map<String, Object> item = cell.getValue();
            String gender = CommonMethod.getString(item, "gender");
            String genderText = "1".equals(gender) ? "男" : "2".equals(gender) ? "女" : "";
            return new javafx.beans.property.SimpleStringProperty(genderText);
        });
        phoneColumn.setCellValueFactory(new MapValueFactory<>("phone"));

        // 设置表格行选择监听器
        TableView.TableViewSelectionModel<Map> tsm = dataTableView.getSelectionModel();
        ObservableList<Integer> list = tsm.getSelectedIndices();
        list.addListener(this::onTableRowSelect);

        // 初始化性别下拉列表
        initGenderComboBox();

        // 初始化教师列表数据
        onQueryButtonClick();
    }

    /**
     * 初始化性别下拉列表
     */
    private void initGenderComboBox() {
        genderList = new ArrayList<>();
        genderList.add(new OptionItem(null, "", "请选择"));
        genderList.add(new OptionItem(1, "1", "男"));
        genderList.add(new OptionItem(2, "2", "女"));

        genderComboBox.getItems().clear();
        genderComboBox.getItems().addAll(genderList);
        genderComboBox.getSelectionModel().select(0); // 默认选择"请选择"
    }

    /**
     * 清除教师表单中输入信息
     */
    public void clearPanel() {
        personId = null;
        numField.setText("");
        nameField.setText("");
        deptField.setText("");
        cardField.setText("");
        genderComboBox.getSelectionModel().select(0);
        birthdayField.setText("");
        emailField.setText("");
        phoneField.setText("");
        addressField.setText("");
        introduceTextArea.setText("");
    }

    /**
     * 切换教师信息
     */
    protected void changeTeacherInfo() {
        Map<String, Object> form = dataTableView.getSelectionModel().getSelectedItem();
        if (form == null) {
            clearPanel();
            return;
        }

        personId = CommonMethod.getInteger(form, "personId");
        DataRequest req = new DataRequest();
        req.add("personId", personId);
        DataResponse res = HttpRequestUtil.request("/api/teacher/getTeacherInfo", req);

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
        deptField.setText(CommonMethod.getString(form, "dept"));
        cardField.setText(CommonMethod.getString(form, "card"));
        birthdayField.setText(CommonMethod.getString(form, "birthday"));
        emailField.setText(CommonMethod.getString(form, "email"));
        phoneField.setText(CommonMethod.getString(form, "phone"));
        addressField.setText(CommonMethod.getString(form, "address"));
        introduceTextArea.setText(CommonMethod.getString(form, "introduce"));

        // 设置性别选择
        String gender = CommonMethod.getString(form, "gender");
        if (gender != null && !gender.isEmpty()) {
            genderComboBox.getSelectionModel().select(
                    CommonMethod.getOptionItemIndexByValue(genderList, gender));
        } else {
            genderComboBox.getSelectionModel().select(0);
        }
    }

    /**
     * 点击教师列表的某一行，切换教师的编辑信息
     */
    public void onTableRowSelect(ListChangeListener.Change<? extends Integer> change) {
        changeTeacherInfo();
    }

    /**
     * 点击查询按钮，查询教师列表
     */
    @FXML
    protected void onQueryButtonClick() {
        String numName = numNameTextField.getText();
        DataRequest req = new DataRequest();
        req.add("numName", numName);
        DataResponse res = HttpRequestUtil.request("/api/teacher/getTeacherList", req);

        if (res != null && res.getCode() == 0) {
            teacherList = (ArrayList<Map>) res.getData();
            setTableViewData();
        } else {
            String errorMsg = (res != null) ? res.getMsg() : "无法连接到服务器";
            MessageDialog.showDialog("查询教师列表失败: " + errorMsg);
        }
    }

    /**
     * 刷新教师列表
     */
    public void refreshTeacherList() {
        numNameTextField.setText(""); // 清空搜索条件
        onQueryButtonClick();
    }

    /**
     * 添加新教师
     */
    @FXML
    protected void onAddButtonClick() {
        clearPanel();
        MessageDialog.showDialog("请填写教师信息，然后点击保存按钮");
    }

    /**
     * 点击删除按钮 删除当前编辑的教师
     */
    @FXML
    protected void onDeleteButtonClick() {
        Map form = dataTableView.getSelectionModel().getSelectedItem();
        if (form == null) {
            MessageDialog.showDialog("没有选择，不能删除");
            return;
        }

        int ret = MessageDialog.choiceDialog("确认要删除教师「" + CommonMethod.getString(form, "name") + "」吗?");
        if (ret != MessageDialog.CHOICE_YES) {
            return;
        }

        personId = CommonMethod.getInteger(form, "personId");
        DataRequest req = new DataRequest();
        req.add("personId", personId);
        DataResponse res = HttpRequestUtil.request("/api/teacher/teacherDelete", req);

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
     * 点击保存按钮，保存当前编辑的教师信息
     */
    @FXML
    protected void onSaveButtonClick() {
        // 获取表单数据
        String num = numField.getText();
        String name = nameField.getText();
        String dept = deptField.getText();
        String card = cardField.getText();
        String birthday = birthdayField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String address = addressField.getText();
        String introduce = introduceTextArea.getText();

        // 检查必填字段
        if (num == null || num.trim().isEmpty()) {
            MessageDialog.showDialog("教师编号不能为空");
            return;
        }
        if (name == null || name.trim().isEmpty()) {
            MessageDialog.showDialog("教师姓名不能为空");
            return;
        }
        if (dept == null || dept.trim().isEmpty()) {
            MessageDialog.showDialog("学院不能为空");
            return;
        }

        // 获取性别
        String gender = null;
        OptionItem selectedGender = genderComboBox.getSelectionModel().getSelectedItem();
        if (selectedGender != null && selectedGender.getValue() != null && !selectedGender.getValue().isEmpty()) {
            gender = selectedGender.getValue();
        }

        // 创建请求对象
        Map<String, Object> form = new HashMap<>();
        form.put("num", num.trim());
        form.put("name", name.trim());
        form.put("dept", dept.trim());
        form.put("card", card != null ? card.trim() : "");
        form.put("gender", gender);
        form.put("birthday", birthday != null ? birthday.trim() : "");
        form.put("email", email != null ? email.trim() : "");
        form.put("phone", phone != null ? phone.trim() : "");
        form.put("address", address != null ? address.trim() : "");
        form.put("introduce", introduce != null ? introduce.trim() : "");

        DataRequest req = new DataRequest();
        req.add("personId", personId);
        req.add("form", form);

        DataResponse res = HttpRequestUtil.request("/api/teacher/teacherEditSave", req);

        if (res != null && res.getCode() == 0) {
            personId = CommonMethod.getIntegerFromObject(res.getData());
            MessageDialog.showDialog("保存成功！");
            // 刷新教师列表
            onQueryButtonClick();
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
     * 导出教师信息表到Excel
     */
    public void doExport() {
        String numName = numNameTextField.getText();
        DataRequest req = new DataRequest();
        req.add("numName", numName);

        byte[] bytes = HttpRequestUtil.requestByteData("/api/teacher/getTeacherListExcel", req);
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
}