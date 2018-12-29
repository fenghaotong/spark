package com.htfeng.sparkproject.test;

import com.htfeng.sparkproject.dao.ITaskDAO;
import com.htfeng.sparkproject.dao.impl.DAOFactory;
import com.htfeng.sparkproject.domain.Task;

/**
 * 任务管理DAO测试类
 * @author htfeng
 *
 */
public class TaskDAOTest {
    public static void main(String[] args) {
        ITaskDAO taskDAO = DAOFactory.getTaskDAO();
        Task task = taskDAO.findById(1);
        System.out.println(task.getTaskName());  
    }
}
