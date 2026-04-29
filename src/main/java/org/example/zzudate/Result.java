package org.example.zzudate;
import lombok.Data;

@Data
public class Result<T> {
    private int code;//状态码
    private String message;//提示信息
    private T data;//返回数据
    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
    //成功（带数据）
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }
    //成功（不带数据）
    public static <T> Result<T> success(String message) {return new Result<>(200,message,null);}
    //成功（自定义提示）
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }
    //失败
    public static <T> Result<T> error(String message) {
        return new Result<>(400, message, null);
    }
}