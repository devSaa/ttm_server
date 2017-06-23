package dao;

import application.ApplicationContext;
import application.exception.PropertyNotFoundException;
import application.exception.InternalServerErrorException;

public class DaoManager {
    /**
     * Returns currently using in system implementation for UserDao.
     *
     * @return UserDao implementation.
     * @throws InternalServerErrorException if internal server error occurred in process.
     */
    public static UserDao getUserDao() throws InternalServerErrorException {
        return (UserDao) getDao("UserDao");
    }

    /**
     * Returns currently using in system implementation for TaskDao.
     *
     * @return TaskDao implementation.
     * @throws InternalServerErrorException if internal server error occurred in process.
     */
    public static TaskDao getTaskDao() throws InternalServerErrorException {
        return (TaskDao) getDao("TaskDao");
    }

    private static Object getDao(String daoName) throws InternalServerErrorException {
        try {
            String daoClassName = ApplicationContext.getProperty(daoName);
            return Class.forName(daoClassName).newInstance();
        } catch (InstantiationException | IllegalAccessException | PropertyNotFoundException | ClassNotFoundException e) {
            throw new InternalServerErrorException("Error occurred in process of creation dao object", e);
        }
    }
}
