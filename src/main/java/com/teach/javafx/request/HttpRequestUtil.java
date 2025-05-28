package com.teach.javafx.request;

import com.teach.javafx.AppStore;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.URI;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Path;
import java.util.Map;

/**
 * HttpRequestUtil 后台请求实例程序，主要实践向后台发送请求的方法
 *  static boolean isLocal 业务处理程序实现方式 false java-server实现 前端程序通过下面的方法把数据发送后台程序，后台返回前端需要的数据，true 本地方式 业务处理 在SQLiteJDBC 实现
 *  String serverUrl = "http://localhost:9090" 后台服务的机器地址和端口号
 */
public class HttpRequestUtil {
    private static final Gson gson = new Gson();
    private static final HttpClient client = HttpClient.newHttpClient();
    public static String serverUrl = "http://localhost:22223";
//    public static String serverUrl = "http://202.194.7.29:22222";

    /**
     *  应用关闭是需要做关闭处理
     */
    public static void close(){
    }

    /**
     * String login(LoginRequest request)  用户登录请求实现
     * @param request  username 登录账号 password 登录密码
     * @return  返回null 登录成功 AppStore注册登录账号信息 非空，登录错误信息
     */

    public static String login(LoginRequest request){
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/auth/login"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(request)))
                .headers("Content-Type", "application/json")
                .build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("response.statusCode===="+response.statusCode());
            if (response.statusCode() == 200) {
                JwtResponse jwt = gson.fromJson(response.body(), JwtResponse.class);
                AppStore.setJwt(jwt);
                return null;
            } else if (response.statusCode() == 401) {
                return "用户名或密码不存在！";
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return "登录失败";
    }

    /**
     * DataResponse request(String url,DataRequest request) 一般数据请求业务的实现
     * @param url  Web请求的Url 对用后的 RequestMapping
     * @param request 请求参数对象
     * @return DataResponse 返回后台返回数据
     */
    public static DataResponse request(String url, DataRequest request){
        try {
            request.add("username", AppStore.getJwt().getUsername());
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(serverUrl + url))
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(request)))
                    .headers("Content-Type", "application/json")
                    .headers("Authorization", "Bearer " + AppStore.getJwt().getToken())
                    .timeout(java.time.Duration.ofSeconds(10)) // 设置10秒超时
                    .build();

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(java.time.Duration.ofSeconds(5)) // 设置连接超时
                    .build();

            try {
                HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                System.out.println("url=" + url + "    response.statusCode=" + response.statusCode());
                if (response.statusCode() == 200) {
                    return gson.fromJson(response.body(), DataResponse.class);
                } else if (response.statusCode() == 500) {
                    // 服务器内部错误
                    DataResponse errorResponse = new DataResponse();
                    errorResponse.setCode(500);
                    errorResponse.setMsg("服务器内部错误，请联系管理员");
                    return errorResponse;
                } else if (response.statusCode() == 401) {
                    // 未授权
                    DataResponse errorResponse = new DataResponse();
                    errorResponse.setCode(401);
                    errorResponse.setMsg("会话已过期，请重新登录");
                    return errorResponse;
                }
            } catch (IOException e) {
                System.err.println("IO错误: " + e.getMessage());
                DataResponse errorResponse = new DataResponse();
                errorResponse.setCode(-1);
                errorResponse.setMsg("网络连接错误: " + e.getMessage());
                return errorResponse;
            } catch (InterruptedException e) {
                System.err.println("请求被中断: " + e.getMessage());
                Thread.currentThread().interrupt();
                DataResponse errorResponse = new DataResponse();
                errorResponse.setCode(-2);
                errorResponse.setMsg("请求被中断");
                return errorResponse;
            }
        } catch (Exception e) {
            System.err.println("请求发生异常: " + e.getMessage());
            e.printStackTrace();
            DataResponse errorResponse = new DataResponse();
            errorResponse.setCode(-3);
            errorResponse.setMsg("请求发生异常: " + e.getMessage());
            return errorResponse;
        }

        // 默认错误响应
        DataResponse errorResponse = new DataResponse();
        errorResponse.setCode(-999);
        errorResponse.setMsg("未知错误");
        return errorResponse;
    }

    /**
     *  MyTreeNode requestTreeNode(String url, DataRequest request) 获取树节点对象
     * @param url  Web请求的Url 对用后的 RequestMapping
     * @param request 请求参数对象
     * @return MyTreeNode 返回后台返回数据
     */
    public static MyTreeNode requestTreeNode(String url, DataRequest request){
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + url))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(request)))
                .headers("Content-Type", "application/json")
                .headers("Authorization", "Bearer "+AppStore.getJwt().getToken())
                .build();
        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpResponse<String>  response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() == 200) {
                return gson.fromJson(response.body(), MyTreeNode.class);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<MyTreeNode> requestTreeNodeList(String url, DataRequest request){
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + url))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(request)))
                .headers("Content-Type", "application/json")
                .headers("Authorization", "Bearer "+AppStore.getJwt().getToken())
                .build();
        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpResponse<String>  response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() == 200) {
                List<Map<String,Object>> list = gson.fromJson(response.body(),List.class);
                List<MyTreeNode> rList = new ArrayList<>();
                for (Map<String, Object> stringObjectMap : list) {
                    rList.add(new MyTreeNode(stringObjectMap));
                }
                return rList;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *  List<OptionItem> requestOptionItemList(String url, DataRequest request) 获取OptionItemList对象
     * @param url  Web请求的Url 对用后的 RequestMapping
     * @param request 请求参数对象
     * @return List<OptionItem> 返回后台返回数据
     */
    public static List<OptionItem> requestOptionItemList(String url, DataRequest request){
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + url))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(request)))
                .headers("Content-Type", "application/json")
                .headers("Authorization", "Bearer "+AppStore.getJwt().getToken())
                .build();
        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpResponse<String>  response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() == 200) {
                OptionItemList o = gson.fromJson(response.body(), OptionItemList.class);
                return o.getItemList();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *   List<OptionItem> getDictionaryOptionItemList(String code) 获取数据字典OptionItemList对象
     * @param code  数据字典类型吗
     * @param
     * @return List<OptionItem> 返回后台返回数据
     */
    public static  List<OptionItem> getDictionaryOptionItemList(String code) {
        DataRequest req = new DataRequest();
        req.add("code", code);
        return requestOptionItemList("/api/base/getDictionaryOptionItemList",req);
    }

    /**
     *  byte[] requestByteData(String url, DataRequest request) 获取byte[] 对象 下载数据文件等
     * @param url  Web请求的Url 对用后的 RequestMapping
     * @param request 请求参数对象
     * @return List<OptionItem> 返回后台返回数据
     */
    public static byte[] requestByteData(String url, DataRequest request){
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + url))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(request)))
                .headers("Content-Type", "application/json")
                .headers("Authorization", "Bearer "+AppStore.getJwt().getToken())
                .build();
        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpResponse<byte[]>  response = client.send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
            if(response.statusCode() == 200) {
                return response.body();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * DataResponse uploadFile(String fileName,String remoteFile) 上传数据文件
     * @param fileName  本地文件名
     * @param remoteFile 远程文件路径
     * @return 上传操作信息
     */
    public static DataResponse uploadFile(String uri,String fileName,String remoteFile)  {
        try {
            Path file = Path.of(fileName);
            HttpClient client = HttpClient.newBuilder().build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(serverUrl+uri+"?uploader=HttpTestApp&remoteFile="+remoteFile + "&fileName="
                            + file.getFileName()))
                    .POST(HttpRequest.BodyPublishers.ofFile(file))
                    .headers("Authorization", "Bearer " + AppStore.getJwt().getToken())
                    .build();
            HttpResponse<String>  response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() == 200) {
                return gson.fromJson(response.body(), DataResponse.class);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param url      Web请求的Url 对用后的 RequestMapping
     * @param fileName 本地文件名
     * @param paras    上传参数
     * @return 导入结果信息
     */
    public static DataResponse importData(String url, String fileName, String paras)  {
        try {
            Path file = Path.of(fileName);

            // 检查文件是否存在
            if (!file.toFile().exists()) {
                DataResponse errorResponse = new DataResponse();
                errorResponse.setCode(-1);
                errorResponse.setMsg("找不到指定文件：" + fileName);
                return errorResponse;
            }

            // 检查文件大小
            long fileSize = file.toFile().length();
            if (fileSize == 0) {
                DataResponse errorResponse = new DataResponse();
                errorResponse.setCode(-1);
                errorResponse.setMsg("文件为空，请选择有效的文件");
                return errorResponse;
            }

            if (fileSize > 50 * 1024 * 1024) { // 限制文件大小为50MB
                DataResponse errorResponse = new DataResponse();
                errorResponse.setCode(-1);
                errorResponse.setMsg("文件过大，请选择小于50MB的文件");
                return errorResponse;
            }

            // 对文件名进行URL编码，避免特殊字符导致的问题
            String encodedFileName = java.net.URLEncoder.encode(file.getFileName().toString(), "UTF-8")
                    .replace("+", "%20"); // 替换空格编码

            String urlStr = serverUrl + url + "?uploader=HttpTestApp&fileName=" + encodedFileName;
            if (paras != null && !paras.isEmpty()) {
                // 确保参数也被正确编码
                urlStr += "&" + paras;
            }

            System.out.println("上传文件到: " + urlStr);
            System.out.println("文件路径: " + fileName);
            System.out.println("文件大小: " + fileSize + " bytes");

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(java.time.Duration.ofSeconds(30)) // 增加连接超时时间
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlStr))
                    .POST(HttpRequest.BodyPublishers.ofFile(file))
                    .headers("Authorization", "Bearer " + AppStore.getJwt().getToken())
                    .headers("Content-Type", "application/octet-stream") // 明确设置内容类型
                    .timeout(java.time.Duration.ofMinutes(2)) // 设置较长的请求超时时间，以便处理大文件
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("服务器响应状态码: " + response.statusCode());
            System.out.println("服务器响应内容: " + response.body());

            if(response.statusCode() == 200) {
                return gson.fromJson(response.body(), DataResponse.class);
            } else {
                // 处理非200状态码
                DataResponse errorResponse = new DataResponse();
                errorResponse.setCode(response.statusCode());
                errorResponse.setMsg("服务器返回错误状态码：" + response.statusCode() + "\n" + response.body());
                return errorResponse;
            }
        } catch (java.nio.file.NoSuchFileException e) {
            DataResponse errorResponse = new DataResponse();
            errorResponse.setCode(-1);
            errorResponse.setMsg("找不到指定文件：" + fileName);
            return errorResponse;
        } catch (java.io.IOException e) {
            DataResponse errorResponse = new DataResponse();
            errorResponse.setCode(-2);
            errorResponse.setMsg("IO错误：" + e.getMessage());
            e.printStackTrace();
            return errorResponse;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            DataResponse errorResponse = new DataResponse();
            errorResponse.setCode(-3);
            errorResponse.setMsg("请求被中断");
            return errorResponse;
        } catch (Exception e) {
            DataResponse errorResponse = new DataResponse();
            errorResponse.setCode(-4);
            errorResponse.setMsg("导入文件时发生错误：" + e.getMessage());
            e.printStackTrace();
            return errorResponse;
        }
    }

}
