package com.chen.Sign.common;

import lombok.Data;

/*
 * 用于返回json数据
 * */
@SuppressWarnings({"all"})
@Data
public class Result {
    private static final int SUCCESS = 1;
    private static final int FAILED = 0;
    private int code;
    private String message;
    private Object data;


    public Result(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }


    public Result() {
    }

    public static Result loginok(Object data) {
        return new Result(SUCCESS, "登录成功", data);
    }

    public static Result loginno(Object data) {
        return new Result(FAILED, "登录失败", data);
    }

    public static Result registerok(Object data) {
        return new Result(SUCCESS, "注册成功", data);
    }

    public static Result registerno(Object data) {
        return new Result(FAILED, "注册失败", data);
    }

    public static Result codeok(Object data) {
        return new Result(SUCCESS, "验证码发送成功", data);
    }

    public static Result codeno(Object data) {
        return new Result(FAILED, "验证码发送失败", data);
    }

    public static Result checkcodeno(Object data) {
        return new Result(3, "验证码错误", data);
    }

    public static Result insertcodeno(Object data) {
        return new Result(FAILED, "验证码保存至数据库失败", data);
    }

    public static Result insertContentok(Object data) {
        return new Result(SUCCESS, "学习记录保存成功", data);
    }

    public static Result insertContentno(Object data) {
        return new Result(SUCCESS, "学习记录保存失败", data);
    }

    public static Result selectByNameok(Object data) {
        return new Result(SUCCESS, "查找数据成功", data);
    }

    public static Result registerusernamecheckno(Object data) {
        return new Result(2, "用户名已存在", data);
    }

    public static Result forgotusernamenoexist(Object data) {
        return new Result(2, "用户名不存在", data);
    }


    public static Result logoutok(Object data) {
        return new Result(SUCCESS, "登出成功", data);
    }

    public static Result emailnomatch(Object data) {
        return new Result(2, "邮箱错误", data);
    }

    public static Result updatepasswordok(Object data) {
        return new Result(SUCCESS, "密码重置成功", data);
    }

    public static Result updatepasswordno(Object data) {
        return new Result(FAILED, "密码重置失败", data);
    }


    public static Result insertBlogok(Object data) {
        return new Result(SUCCESS, "博客添加成功", data);
    }

    public static Result insertBlogno(Object data) {
        return new Result(FAILED, "博客添加失败", data);
    }

    public static Result seeblogok(Object data) {
        return new Result(SUCCESS, "查看博客成功", data);
    }

    public static Result mailok(Object data) {
        return new Result(SUCCESS, "发送邮件成功", data);
    }

    public static Result mailno(Object data) {
        return new Result(SUCCESS, "发送邮件失败", data);
    }

    public static Result allinformationok(Object data) {
        return new Result(SUCCESS, "个人信息返回成功", data);
    }

    public static Result editinformationok(Object data) {
        return new Result(SUCCESS, "编辑个人信息成功", data);
    }

    public static Result editinformationno(Object data) {
        return new Result(FAILED, "系统出错了", data);
    }

    public static Result recorddataok(Object data) {
        return new Result(SUCCESS, "返回信息成功", data);
    }

    public static Result insertProfile_photook(Object data) {
        return new Result(SUCCESS, "头像上传成功", data);
    }

    public static Result insertProfile_photono(Object data) {
        return new Result(FAILED, "头像上传失败", data);
    }

    public static Result Profile_photonoempty(Object data) {
        return new Result(3, "头像为空", data);
    }

    public static Result getImageok(Object data) {
        return new Result(SUCCESS, "返回图片Base64成功", data);
    }

    public static Result addtagok(Object data) {
        return new Result(SUCCESS, "添加标签成功", data);
    }

    public static Result addtagno(Object data) {
        return new Result(FAILED, "添加标签失败", data);
    }

    public static Result modifyok(Object data) {
        return new Result(SUCCESS,"修改博客成功",data);
    }

    public static Result deleteok(Object data) {
        return new Result(SUCCESS,"删除博客成功",data);
    }

    public static Result insertblogimagesok(Object data) {
        return new Result(SUCCESS, "图片上传成功", data);
    }

    public static Result insertblogimagesno(Object data) {
        return new Result(FAILED, "图片上传失败", data);
    }

    public static Result blogimagesempty(Object data) {
        return new Result(3, "图片为空", data);
    }
}