package dao.jdbc;

import application.ApplicationContext;
import application.exception.InternalServerErrorException;
import dao.DaoManager;
import dao.UserDao;
import dao.exception.DaoException;
import entity.User;
import org.junit.*;

public class JdbcUserDaoTest{

    static UserDao userDao;
    User testUser;

    @BeforeClass
    public static void setUpClass() throws InternalServerErrorException {
        ApplicationContext.init();
        userDao = DaoManager.getUserDao();
    }

    @Before
    public void setUp() throws InternalServerErrorException {
        testUser = new User("testUser1", "testUser1");
    }

    @After
    public void cleanUp() throws InternalServerErrorException, DaoException {
        userDao.deleteUser(testUser);
    }


    @Test
    public void testCreateUser() throws Exception {
        User createdUser = userDao.createUser(testUser);
        Assert.assertEquals(testUser, createdUser);

        createdUser = userDao.createUser(testUser);
        Assert.assertEquals(null, createdUser);
    }

    @Test
    public void testLoginUser() throws Exception {
        User user = userDao.loginUser(testUser);
        Assert.assertEquals(null, user);

        userDao.createUser(testUser);
        user = userDao.loginUser(testUser);
        Assert.assertEquals(testUser, user);

        User wrongUser = new User("testUser1", "testUser2");
        user = userDao.loginUser(wrongUser);
        Assert.assertEquals(null, user);

    }

    @Test
    public void testUpdateUser() throws Exception {
        testUser = userDao.createUser(testUser);
        testUser.setTaskListVersion(1);
        userDao.updateUser(testUser);
        User user = userDao.loginUser(testUser);
        Assert.assertEquals(1, user.getTaskListVersion());
    }

}
