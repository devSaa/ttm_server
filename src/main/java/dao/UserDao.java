package dao;

import application.exception.InternalServerErrorException;
import dao.exception.*;
import entity.User;

public interface UserDao {
    /**
     * Creates a new user in database.
     *
     * @param   user user to create.
     * @return  created user (with id from database) or null if user already exists.
     * @throws  InternalServerErrorException if internal server error occurred.
     * @throws  DaoException if error occurred in process of interaction with database.
     */
    User createUser(User user) throws InternalServerErrorException, DaoException;

    /**
     * Authorize user in system and retrieve user data from database.
     *
     * @param   user user with login and password for authorization.
     * @return  persisted user from database or null if wrong login or password specified.
     * @throws  InternalServerErrorException if internal server error occurred.
     * @throws  DaoException if error occurred in process of interaction with database.
     */
    User loginUser(User user) throws InternalServerErrorException, DaoException;

    /**
     * Update data in database for specified user. Returns user with list of tasks which didn't have ids with ids assigned by database.
     *
     * @param   user user with data for update.
     * @return  user with updated data (with ids from database).
     * @throws  InternalServerErrorException if internal server error occurred.
     * @throws  DaoException if error occurred in process of interaction with database.
     */
    User updateUser(User user) throws InternalServerErrorException, DaoException;


    /**
     * Delete user from database.
     *
     * @param   user user for deletion.
     * @return  true - if deletion successful or false - if deletion failed.
     * @throws  InternalServerErrorException if internal server error occurred.
     * @throws  DaoException if error occurred in process of interaction with database.
     */
    boolean deleteUser(User user) throws InternalServerErrorException, DaoException;

}
