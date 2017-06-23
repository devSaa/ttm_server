package entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {

    public static final long serialVersionUID = 124L;

    private long id = -1; // -1 for not yet saved in database user
    private String login;
    private String password;

    /** Contains user's root tasks */
    private List<Task> taskList = new ArrayList<>();;

    /** Version changes on every successful update to database */
    private long taskListVersion;

    public User() {
    }

    public User(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public User(long id, String login, String password, long taskListVersion) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.taskListVersion = taskListVersion;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Task> getTaskList() {
        return taskList;
    }

    public void setTaskList(List<Task> taskList) {
        this.taskList = taskList;
    }

    public long getTaskListVersion() {
        return taskListVersion;
    }

    public void setTaskListVersion(long taskListVersion) {
        this.taskListVersion = taskListVersion;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", login='" + login + '\'' +
                ", taskList=" + taskList +
                ", taskListVersion=" + taskListVersion +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (login != null ? !login.equals(user.login) : user.login != null) return false;
        return !(password != null ? !password.equals(user.password) : user.password != null);

    }

    @Override
    public int hashCode() {
        int result = login != null ? login.hashCode() : 0;
        result = 31 * result + (password != null ? password.hashCode() : 0);
        return result;
    }
}
