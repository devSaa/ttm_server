package controller;

import application.exception.InternalServerErrorException;
import dao.DaoManager;
import dao.UserDao;
import dao.exception.DaoException;
import entity.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UserController {

    private static final Logger infoLogger = LogManager.getLogger("infoLogger");

    public static User create(User user) throws InternalServerErrorException, DaoException {
        UserDao userDao = DaoManager.getUserDao();
        user = userDao.createUser(user);
        if (user != null)
            infoLogger.info(user.getLogin() + " - create successful");
        return user;
    }

    public static User login(User user) throws InternalServerErrorException, DaoException {
        UserDao userDao = DaoManager.getUserDao();
        user = userDao.loginUser(user);
        if (user != null)
            infoLogger.info(user.getLogin() + " - login successful");
        return user;

    }

    public static User update(User user) throws InternalServerErrorException, DaoException {
        UserDao userDao = DaoManager.getUserDao();
        user = userDao.updateUser(user);
        if (user != null)
            infoLogger.info(user.getLogin() + " - update successful");
        return user;

    }
}
