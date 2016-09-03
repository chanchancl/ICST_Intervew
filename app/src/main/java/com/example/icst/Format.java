package com.example.icst;

import java.util.Date;
import java.util.Locale;

/**
 * Created by 大杨编 on 2016/8/26.
 */
public class Format {
    public static String Department(int department) {
        switch (department) {
            case 1:
                return "人力资源部";
            case 2:
                return "项目运营部";
            case 3:
                return "资讯传媒部";
            case 4:
                return "知识管理部";
            case 5:
                return "外联交流部";
            default:
                return "无部门";
        }
    }

    public static String Department(int department, String string) {
        switch (department) {
            case 1:
                return "人力";
            case 2:
                return "项目";
            case 3:
                return "资传";
            case 4:
                return "知管";
            case 5:
                return "外联";
            default:
                return string;
        }
    }

    public static int Department(String department) {
        switch (department) {
            case "人力资源部":
                return 1;
            case "项目运营部":
                return 2;
            case "资讯传媒部":
                return 3;
            case "知识管理部":
                return 4;
            case "外联交流部":
                return 5;
            default:
                return 0;
        }
    }

    public static String College(int college) {
        switch (college) {
            case 1:
                return "机械与汽车工程学院";
            case 2:
                return "建筑学院";
            case 3:
                return "土木与交通学院";
            case 4:
                return "电子与信息学院";
            case 5:
                return "材料科学与工程学院";
            case 6:
                return "化学与化工学院";
            case 7:
                return "轻工科学与工程学院";
            case 8:
                return "食品科学与工程学院";
            case 9:
                return "数学学院";
            case 10:
                return "物理与光电学院";
            case 11:
                return "经济与贸易学院";
            case 12:
                return "自动化科学与工程学院";
            case 13:
                return "计算机科学与工程学院";
            case 14:
                return "电力学院";
            case 15:
                return "生物科学与工程学院";
            case 16:
                return "环境与能源学院";
            case 17:
                return "软件学院";
            case 18:
                return "工商管理学院（创业教育学院）";
            case 19:
                return "公共管理学院";
            case 20:
                return "马克思主义学院";
            case 21:
                return "外国语学院";
            case 22:
                return "法学院（知识产权学院）";
            case 23:
                return "新闻与传播学院";
            case 24:
                return "艺术学院";
            case 25:
                return "体育学院";
            case 26:
                return "设计学院";
            case 27:
                return "医学院";
            case 28:
                return "国际教育学院";
            default:
                return "**学院**";
        }
    }

    public static int College(String college) {
        switch (college) {
            case "机械与汽车工程学院":
                return 1;
            case "建筑学院":
                return 2;
            case "土木与交通学院":
                return 3;
            case "电子与信息学院":
                return 4;
            case "材料科学与工程学院":
                return 5;
            case "化学与化工学院":
                return 6;
            case "轻工科学与工程学院":
                return 7;
            case "食品科学与工程学院":
                return 8;
            case "数学学院":
                return 9;
            case "物理与光电学院":
                return 10;
            case "经济与贸易学院":
                return 11;
            case "自动化科学与工程学院":
                return 12;
            case "计算机科学与工程学院":
                return 13;
            case "电力学院":
                return 14;
            case "生物科学与工程学院":
                return 15;
            case "环境与能源学院":
                return 16;
            case "软件学院":
                return 17;
            case "工商管理学院（创业教育学院）":
                return 18;
            case "公共管理学院":
                return 19;
            case "马克思主义学院":
                return 20;
            case "外国语学院":
                return 21;
            case "法学院（知识产权学院）":
                return 22;
            case "新闻与传播学院":
                return 23;
            case "艺术学院":
                return 24;
            case "体育学院":
                return 25;
            case "设计学院":
                return 26;
            case "医学院":
                return 27;
            case "国际教育学院":
                return 28;
            default:
                return 0;
        }
    }

    public static String Time(Date time) {
        return String.format(Locale.CHINA, "%1$tb%1$td日 %1$tH:%1$tM", time);
    }

    public static String Adjust(boolean adjust) {
        if (adjust) return "服从调剂";
        else return "不服从调剂";
    }

    public static String State(int state) {
        switch (state) {
            case 0:
                return "尚未开始";
            case 1:
                return "正在签到";
            case 2:
                return "确认名单";
            default:
                return "出现错误";
        }
    }

    public static String[] Department(int wish1, int wish2, boolean adjust) {
        String[] stringList;
        if (adjust) {
            stringList = new String[]{
                    "-取消-",
                    "人力资源部",
                    "项目运营部",
                    "资讯传媒部",
                    "知识管理部",
                    "外联交流部"
            };
            stringList[wish1] += "(第一志愿)";
            stringList[wish2] += "(第二志愿)";
        } else {
            stringList = new String[]{
                    "取消",
                    Format.Department(wish1) + "(第一志愿)",
                    Format.Department(wish2) + "(第二志愿)"
            };
        }
        return stringList;
    }
}
