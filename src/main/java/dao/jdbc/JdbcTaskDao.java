package dao.jdbc;

import dao.TaskDao;
import application.exception.InternalServerErrorException;
import dao.exception.DaoException;
import entity.Task;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class JdbcTaskDao implements TaskDao {
    private static final String SELECT_TASKS_BY_USER_ID_SQL = "SELECT " +
            "id, taskName, parentTaskId, creationTime, finishTime, suggestedTaskDuration, elapsedTaskDuration, finished, comments " +
            "FROM tasks WHERE userId=?";

    private static final String INSERT_TASK_SQL = "INSERT INTO tasks" +
            "(userId, taskName, parentTaskId, creationTime, finishTime, suggestedTaskDuration, elapsedTaskDuration, finished, comments) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE_TASK_BY_ID_SQL = "UPDATE tasks SET " +
            "taskName=?, suggestedTaskDuration=?, elapsedTaskDuration=?, finishTime=?, finished=?, comments=? " +
            "WHERE id=?";

    private static final String DELETE_TASK_BY_ID_SQL = "DELETE FROM tasks WHERE id=?";

    private static final String SELECT_TASK_ID_BY_USER_ID_AND_CREATION_TIME_SQL = "SELECT id " +
            "FROM tasks WHERE userId=? AND creationTime=?";

    PreparedStatement selectTasksPreparedStatement;
    PreparedStatement insertTaskPreparedStatement;
    PreparedStatement updateTaskPreparedStatement;
    PreparedStatement deleteTaskPreparedStatement;
    PreparedStatement selectInsertedTaskPreparedStatement;

    List<Task> insertedTaskList;
    List<Long> alreadyInListTaskIds;

    @Override
    public List<Task> getTaskListByUserId(long userId) throws InternalServerErrorException, DaoException{
        try (Connection connection = ConnectionManager.getConnection()) {
            Map<Long, Task> idTaskMap = new HashMap<>();

            selectTasksPreparedStatement = connection.prepareStatement(SELECT_TASKS_BY_USER_ID_SQL);
            selectTasksPreparedStatement.setLong(1, userId);
            ResultSet resultSet = selectTasksPreparedStatement.executeQuery();

            // add all user's tasks to idTaskMap
            while (resultSet.next()) {
                long id = resultSet.getLong("id");
                String taskName = resultSet.getString("taskName");
                long parentTaskId = resultSet.getLong("parentTaskId");
                LocalDateTime creationTime = LocalDateTime.parse(resultSet.getString("creationTime"));

                String finishTimeString = resultSet.getString("finishTime");
                LocalDateTime finishTime = null;
                if (finishTimeString != null) {
                    finishTime = LocalDateTime.parse(resultSet.getString("finishTime"));
                }

                Duration suggestedTaskDuration = Duration.parse(resultSet.getString("suggestedTaskDuration"));
                Duration elapsedTaskDuration = Duration.parse(resultSet.getString("elapsedTaskDuration"));
                boolean finished = resultSet.getBoolean("finished");

                String comments = resultSet.getString("comments");

                Task task = new Task(id, userId, taskName, parentTaskId, creationTime, finishTime, suggestedTaskDuration, elapsedTaskDuration, finished, comments);
                idTaskMap.put(id, task);
            }

            // restore parentTask-subTask relations
            List<Task> taskList = new ArrayList<>();
            for (Task currentTask : idTaskMap.values()) {
                long parentTaskId;
                if ((parentTaskId = currentTask.getParentTaskId()) != -1) {
                    Task parentTask = idTaskMap.get(parentTaskId);
                    currentTask.setParentTask(parentTask);
                    parentTask.addSubTask(currentTask);
                } else {
                    taskList.add(currentTask);
                }
            }


            // Для уменьшения нагрузки на сервер сортировку задач целесообразно проводиться на клиенте.
            // Поэтому сортировку задач на сервере стоит рассматривать как временную меру
            // sort tasks by creation time
            sortTasksByCreationTime(taskList);

            return taskList;

        } catch (SQLException e) {
            throw new DaoException("Error occurred in process of retrieving task list", e);
        }
    }

    private void sortTasksByCreationTime(List<Task> taskList) {
        taskList.sort((o1, o2) -> o1.getCreationTime().compareTo(o2.getCreationTime()));

        for (Task subTask : taskList) {
            sortTasksByCreationTime(subTask.getSubTaskList());
        }
    }

    @Override
    public List<Task> updateTaskList(List<Task> taskList) throws InternalServerErrorException, DaoException{
        try (Connection connection = ConnectionManager.getConnection()) {
            deleteTaskPreparedStatement = connection.prepareStatement(DELETE_TASK_BY_ID_SQL);
            updateTaskPreparedStatement = connection.prepareStatement(UPDATE_TASK_BY_ID_SQL);
            insertTaskPreparedStatement = connection.prepareStatement(INSERT_TASK_SQL);
            selectInsertedTaskPreparedStatement = connection.prepareStatement(SELECT_TASK_ID_BY_USER_ID_AND_CREATION_TIME_SQL);

            insertedTaskList = new ArrayList<>();
            alreadyInListTaskIds = new ArrayList<>();

            processTaskList(taskList);

            return insertedTaskList;
        } catch (SQLException e) {
            throw new DaoException("TaskList Update failed", e);
        }
    }

    /**
     * Sequentially process tasks from list in attempt to save, change or delete task from database.
     *
     * @param   taskList task list for update.
     * @throws  SQLException if problems occurred during interaction with database.
     * @throws  DaoException if one of task update failed.
     */
    private void processTaskList(List<Task> taskList) throws SQLException, DaoException {
        for (Task currentTask : taskList) {
            processTask(currentTask);
        }
    }

    /**
     * Attempt to save, change or delete specified task from database.
     *
     * @param   task task for update.
     * @throws  SQLException if problems occurred during interaction with database.
     * @throws  DaoException if task update failed.
     */
    private void processTask(Task task) throws SQLException, DaoException {
        if (!task.isChanged()) {
            processTaskList(task.getSubTaskList());
            return;
        }

        if (task.isForDeletion()) {
            deleteTask(task);
            return;
        } else if (task.getId() != -1) {
            updateTask(task);
        } else {
            insertTask(task);
        }

        processTaskList(task.getSubTaskList());

    }

    private void insertTask(Task task) throws SQLException, DaoException {
        insertTaskPreparedStatement.setLong(1, task.getUserId());
        insertTaskPreparedStatement.setString(2, task.getTaskName());
        insertTaskPreparedStatement.setLong(3, task.getParentTaskId());
        insertTaskPreparedStatement.setString(4, task.getCreationTime().toString());

        String finishTimeString = null;
        if (task.getFinishTime() != null) {
            finishTimeString = task.getFinishTime().toString();
        }
        insertTaskPreparedStatement.setString(5, finishTimeString);
        insertTaskPreparedStatement.setString(6, task.getSuggestedTaskDuration().toString());
        insertTaskPreparedStatement.setString(7, task.getElapsedTaskDuration().toString());
        insertTaskPreparedStatement.setBoolean(8, task.isFinished());
        insertTaskPreparedStatement.setString(9, task.getComments());

        if (insertTaskPreparedStatement.executeUpdate() != 1) {
            throw new DaoException("Can't insert task. Update failed");
        }

        long taskId = getInsertedTaskId(task.getUserId(), task.getCreationTime());
        task.setId(taskId);

        if (!alreadyInListTaskIds.contains(task.getParentTaskId())) {
            insertedTaskList.add(task);
        }
        alreadyInListTaskIds.add(taskId);

        for (Task subTask : task.getSubTaskList()) {
            subTask.setParentTaskId(taskId);
        }

        task.setChanged(false);
    }

    private void updateTask(Task task) throws SQLException, DaoException {
        updateTaskPreparedStatement.setString(1, task.getTaskName());
        updateTaskPreparedStatement.setString(2, task.getSuggestedTaskDuration().toString());
        updateTaskPreparedStatement.setString(3, task.getElapsedTaskDuration().toString());

        String finishTimeString = null;
        if (task.getFinishTime() != null) {
            finishTimeString = task.getFinishTime().toString();
        }
        updateTaskPreparedStatement.setString(4, finishTimeString);

        updateTaskPreparedStatement.setBoolean(5, task.isFinished());
        updateTaskPreparedStatement.setString(6, task.getComments());
        updateTaskPreparedStatement.setLong(7, task.getId());

        if (updateTaskPreparedStatement.executeUpdate() != 1) {
            throw new DaoException("Can't update task. Update failed");
        }
        task.setChanged(false);
    }

    private void deleteTask(Task task) throws SQLException, DaoException {
        if (task.getId() == -1) {
            return;
        }
        deleteTaskPreparedStatement.setLong(1, task.getId());

        if (deleteTaskPreparedStatement.executeUpdate() != 1) {
            throw new DaoException("Can't delete task. Update failed");
        }
        task.setChanged(false);
    }

    /**
     * Retrieve task with specified parameters from database and return it's id.
     * Used for getting id for just inserted task.
     *
     * @param   userId task owner user's id.
     * @param   creationTime task's time of creation.
     * @return  task's id assigned by database.
     * @throws  SQLException if problems occurred during interaction with database.
     * @throws  DaoException if specified task not found in database.
     */
    private long getInsertedTaskId(long userId, LocalDateTime creationTime) throws SQLException, DaoException {
        PreparedStatement preparedStatement = selectInsertedTaskPreparedStatement;
        preparedStatement.setLong(1, userId);
        preparedStatement.setString(2, creationTime.toString());

        ResultSet resultSet = preparedStatement.executeQuery();
        if (!resultSet.next()) {
            throw new DaoException("Can't retrieve inserted task. Update failed");
        }

        return resultSet.getLong("id");
    }
}
