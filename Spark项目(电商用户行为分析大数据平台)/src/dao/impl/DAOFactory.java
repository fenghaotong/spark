package com.htfeng.sparkproject.dao.impl;

import com.htfeng.sparkproject.dao.ITaskDAO;

/**
 * DAO工厂类
 * @author htfeng
 *
 */
public class DAOFactory {
    public static ITaskDAO getTaskDAO() {
        return new TaskDAOImpl();
    }
}
