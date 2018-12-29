package com.htfeng.sparkproject.dao;

import com.htfeng.sparkproject.domain.Task;

/**
 * 任务管理DAO接口
 * @author htfeng
 *
 */
public interface ITaskDAO {
    
    /**
     * 根据主键查询任务
     * @param taskid 主键
     * @return 任务
     */
    Task findById(long taskid);
}
