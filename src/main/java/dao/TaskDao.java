package dao;

import application.exception.InternalServerErrorException;
import dao.exception.DaoException;
import entity.Task;

import java.util.List;

public interface TaskDao {
    /**
     * Returns list of tasks for specified user in hierarchical representation
     *
     * @param   userId id of user whose tasks needed to retrieve.
     * @return  list of tasks.
     * @throws  InternalServerErrorException if internal server error occurred.
     * @throws  DaoException if error occurred in process of interaction with database.
     */
    List<Task> getTaskListByUserId(long userId) throws InternalServerErrorException, DaoException;

    /**
     * Update specified list of tasks in database and returns list with tasks which didn't have ids with ids assigned by database.
     *
     * @param   taskList list of tasks for update.
     * @return  list of inserted tasks.
     * @throws  InternalServerErrorException if internal server error occurred.
     * @throws  DaoException if error occurred in process of interaction with database.
     */
    List<Task> updateTaskList(List<Task> taskList) throws InternalServerErrorException, DaoException;
}
