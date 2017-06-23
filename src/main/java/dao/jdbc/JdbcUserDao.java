package dao.jdbc;

import application.exception.InternalServerErrorException;
import dao.DaoManager;
import dao.TaskDao;
import dao.UserDao;
import dao.exception.*;
import entity.Task;
import entity.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.List;

public class JdbcUserDao implements UserDao {
    private static final String SELECT_USER_BY_LOGIN_SQL = "SELECT id, login, password, taskListVersion FROM users WHERE login=?";
    private static final String INSERT_USER_SQL = "INSERT INTO users (login, password) VALUES (?, ?)";
    private static final String UPDATE_USER_SQL = "UPDATE users SET password=?, taskListVersion=? WHERE id=?";
    private static final String DELETE_USER_SQL = "DELETE FROM users WHERE login=?";

    private static final Logger infoLogger = LogManager.getLogger("infoLogger");

    @Override
    public User createUser(User user) throws InternalServerErrorException, DaoException {
        try (Connection connection = ConnectionManager.getConnection()) {

            PreparedStatement selectUserPreparedStatement = connection.prepareStatement(SELECT_USER_BY_LOGIN_SQL);
            selectUserPreparedStatement.setString(1, user.getLogin());

            ResultSet resultSet = selectUserPreparedStatement.executeQuery();
            if (resultSet.next()) {
                return null;
            }

            PreparedStatement insertUserPreparedStatement = connection.prepareStatement(INSERT_USER_SQL);
            insertUserPreparedStatement.setString(1, user.getLogin());
            insertUserPreparedStatement.setString(2, user.getPassword());

            if (insertUserPreparedStatement.executeUpdate() != 1) {
                throw new DaoException("Can't save user to database");
            }

            resultSet = selectUserPreparedStatement.executeQuery();

            if (!resultSet.next()) {
                throw new DaoException("Can't get created user from database");
            }

            User createdUser = new User(resultSet.getLong("id"), resultSet.getString("login"), resultSet.getString("password"), resultSet.getLong("taskListVersion"));
            infoLogger.info("user "+ createdUser.getLogin() + " added to database");
            return createdUser;

        } catch (SQLException e) {
            throw new DaoException("Connection with database failed", e);
        }
    }

    @Override
    public User loginUser(User user) throws InternalServerErrorException, DaoException {
        User persistedUser;
        try (Connection connection = ConnectionManager.getConnection()) {

            PreparedStatement selectUserPreparedStatement = connection.prepareStatement(SELECT_USER_BY_LOGIN_SQL);
            selectUserPreparedStatement.setString(1, user.getLogin());

            ResultSet resultSet = selectUserPreparedStatement.executeQuery();
            if (!resultSet.next()) {
                return null;
            }

            String password = resultSet.getString("password");
            if (!user.getPassword().equals(password)) {
                return null;
            }

            persistedUser = new User(resultSet.getLong("id"), resultSet.getString("login"), password, resultSet.getLong("taskListVersion"));

            // Следующий блок предназначен для уменьшения нагрузки на сеть
            // посредством отправки клиенту ответа без списка задач
            // в случае если имеющаяся у пользователя локальная версия данных
            // совпадает с версией в базе данных. Блок может быть раскоментирован
            // как только на клиенте будет добавлен соответствующий функционал
//            if (persistedUser.getTaskListVersion() == user.getTaskListVersion()) {
//                return persistedUser;
//            }

        }catch (SQLException e) {
            throw new DaoException("Connection with database failed", e);
        }

        TaskDao taskDao = DaoManager.getTaskDao();
        List<Task> taskList = taskDao.getTaskListByUserId(persistedUser.getId());
        persistedUser.setTaskList(taskList);
        infoLogger.info("user "+ persistedUser.getLogin() + " logged in to database");
        return persistedUser;
    }

    @Override
    public User updateUser(User user) throws InternalServerErrorException, DaoException {
        if (user.getId() == -1) {
            throw new DaoException("User is not persisted in database");
        }

        TaskDao taskDao = DaoManager.getTaskDao();
        List<Task> updatedTaskList = taskDao.updateTaskList(user.getTaskList());

        try (Connection connection = ConnectionManager.getConnection()){
            PreparedStatement updateUserPreparedStatement = connection.prepareStatement(UPDATE_USER_SQL);
            updateUserPreparedStatement.setString(1, user.getPassword());
            updateUserPreparedStatement.setLong(2, user.getTaskListVersion());
            updateUserPreparedStatement.setLong(3, user.getId());

            if (updateUserPreparedStatement.executeUpdate() != 1) {
                throw new DaoException("User update failed");
            }

            user.setTaskList(updatedTaskList);

            infoLogger.info("user "+ user.getLogin() + " updated in database");

            // Следующие две строчки добавлены для упрощения вычислений на клиентской части
            // посредством отправления полной информации по пользователю
            // и могут быть удалены как только в клиент будут внесены необходимые изменения
            List<Task> taskList = taskDao.getTaskListByUserId(user.getId());
            user.setTaskList(taskList);

            return user;
        } catch (SQLException e) {
            throw new DaoException("Connection with database failed", e);
        }
    }

    @Override
    public boolean deleteUser(User user) throws InternalServerErrorException, DaoException {
        try (Connection connection = ConnectionManager.getConnection()) {
            PreparedStatement deleteUserPreparedStatement = connection.prepareStatement(DELETE_USER_SQL);
            deleteUserPreparedStatement.setString(1, user.getLogin());
            if(deleteUserPreparedStatement.executeUpdate() != 1 ) {
                return false;
            }

            infoLogger.info("user "+ user.getLogin() + " deleted from database");
            return true;
        } catch (SQLException e) {
            throw new DaoException("Connection with database failed", e);
        }
    }
}
