package dao.jdbc;

import application.ApplicationContext;
import application.exception.InternalServerErrorException;
import dao.DaoManager;
import dao.TaskDao;
import dao.UserDao;
import dao.exception.DaoException;
import entity.Task;
import entity.User;
import org.junit.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JdbcTaskDaoTest {

    static UserDao userDao;
    static TaskDao taskDao;
    User testUser;
    List<Task> testTaskList;

    @BeforeClass
    public static void setUpClass() throws InternalServerErrorException {
        ApplicationContext.init();
        userDao = DaoManager.getUserDao();
        taskDao = DaoManager.getTaskDao();
    }

    @Before
    public void setUp() throws InternalServerErrorException, DaoException, InterruptedException {
        testUser = new User("testUser1", "testUser1");
        testUser = userDao.createUser(testUser);

        testTaskList = new ArrayList<>();

        Task task1 = new Task(testUser.getId(), "task1", LocalDateTime.now(), Duration.ofDays(10), null, "comment 1");
        Thread.sleep(1);
        Task task2 = new Task(testUser.getId(), "task2", LocalDateTime.now(), Duration.ofDays(12), null, "comment 2");
        Thread.sleep(1);
        Task task3 = new Task(testUser.getId(), "task3", LocalDateTime.now(), Duration.ofDays(4), task2, "comment 3");
        Thread.sleep(1);
        Task task4 = new Task(testUser.getId(), "task4", LocalDateTime.now(), Duration.ofDays(8), task2, "comment 4");

        testTaskList.add(task1);
        testTaskList.add(task2);

        testUser.setTaskList(testTaskList);

        userDao.updateUser(testUser);
    }

    @After
    public void cleanUp() throws InternalServerErrorException, DaoException {
        userDao.deleteUser(testUser);
    }

    @Test
    public void testGetTaskListByUserId() throws InternalServerErrorException, DaoException, InterruptedException {

        List<Task> taskList = taskDao.getTaskListByUserId(testUser.getId());

        Assert.assertEquals(testTaskList.size(), taskList.size());
        Assert.assertEquals(testTaskList, taskList);
        Assert.assertEquals(testTaskList.get(1).getSubTaskList(), taskList.get(1).getSubTaskList());

    }

    @Test
    public void testUpdateTaskList() throws InternalServerErrorException, DaoException, InterruptedException {

        Task updatedTask = testTaskList.get(0);
        updatedTask.setElapsedTaskDuration(updatedTask.getElapsedTaskDuration().plus(Duration.ofHours(1)));
        updatedTask.setChanged(true);
        testTaskList.get(1).setForDeletion(true);
        testTaskList.get(1).setChanged(true);

        Task task5 = new Task(testUser.getId(), "task5", LocalDateTime.now(), Duration.ofDays(9), null, "comment 5");
        Thread.sleep(1);
        Task task6 = new Task(testUser.getId(), "task6", LocalDateTime.now(), Duration.ofDays(3), task5, "comment 6");

        testTaskList.add(task5);

        List<Task> taskList = taskDao.updateTaskList(testTaskList);

        testTaskList.remove(1);

        Assert.assertEquals(1, taskList.size());
        Assert.assertNotEquals(-1, taskList.get(0).getId());
        Assert.assertEquals(task5, taskList.get(0));
        Assert.assertEquals(1, taskList.get(0).getSubTaskList().size());
        Assert.assertEquals(task6, taskList.get(0).getSubTaskList().get(0));

        taskList = taskDao.getTaskListByUserId(testUser.getId());

        Assert.assertEquals(testTaskList.size(), taskList.size());
        Assert.assertEquals(testTaskList, taskList);
        Assert.assertEquals(testTaskList.get(1).getSubTaskList(), taskList.get(1).getSubTaskList());
        Assert.assertEquals(updatedTask.getElapsedTaskDuration(), taskList.get(0).getElapsedTaskDuration());

    }
}
