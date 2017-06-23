package entity;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Task implements Serializable {

    public static final long serialVersionUID = 123L;

    private long id = -1; // -1 for not yet saved in database task
    private long userId;
    private String taskName;
    private long parentTaskId = -1; // -1 if no parent
    private Task parentTask;
    private List<Task> subTaskList = new ArrayList<>();
    private LocalDateTime creationTime;
    private LocalDateTime finishTime;
    private Duration suggestedTaskDuration;
    private Duration elapsedTaskDuration;
    private String comments;
    private boolean finished;

    /** Set true on any change to task. Indicate that task need to be updated in database */
    private boolean changed;

    /** Set true for task which need to be removed from database*/
    private boolean forDeletion;

    public Task() {
    }

    public Task(long id) {
        this.id = id;
    }

    public Task(String taskName, Duration suggestedTaskDuration, String comments) {
        this.taskName = taskName;
        this.suggestedTaskDuration = suggestedTaskDuration;
        this.comments = comments;
    }

    public Task(long userId, String taskName, LocalDateTime creationTime, Duration suggestedTaskDuration, Task parentTask, String comments) {
        // желательно сделать проверку на то, чтобы сумарная длительность подзадач не привышала длительность родительской задачи

        this.userId = userId;
        this.taskName = taskName;
        this.creationTime = creationTime;
        this.suggestedTaskDuration = suggestedTaskDuration;
        this.elapsedTaskDuration = Duration.ZERO;
        this.finished = false;
        this.changed = true;
        this.forDeletion = false;
        this.comments = comments;

        this.parentTask = parentTask;
        if (parentTask != null){
            this.parentTaskId = parentTask.getId();
            parentTask.getSubTaskList().add(this);
        }
    }

    public Task(long id, long userId, String taskName, long parentTaskId, LocalDateTime creationTime, LocalDateTime finishTime,Duration suggestedTaskDuration, Duration elapsedTaskDuration, boolean finished, String comments) {
        this.id = id;
        this.userId = userId;
        this.taskName = taskName;
        this.parentTaskId = parentTaskId;
        this.creationTime = creationTime;
        this.finishTime = finishTime;
        this.suggestedTaskDuration = suggestedTaskDuration;
        this.elapsedTaskDuration = elapsedTaskDuration;
        this.finished = finished;
        this.comments = comments;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public long getParentTaskId() {
        return parentTaskId;
    }

    public void setParentTaskId(long parentTaskId) {
        this.parentTaskId = parentTaskId;
    }

    public Task getParentTask() {
        return parentTask;
    }

    public void setParentTask(Task parentTask) {
        this.parentTask = parentTask;
    }

    public List<Task> getSubTaskList() {
        return subTaskList;
    }

    public void setSubTaskList(List<Task> subTaskList) {
        this.subTaskList = subTaskList;
    }

    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(LocalDateTime creationTime) {
        this.creationTime = creationTime;
    }

    public LocalDateTime getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(LocalDateTime finishTime) {
        this.finishTime = finishTime;
    }

    public Duration getSuggestedTaskDuration() {
        return suggestedTaskDuration;
    }

    public void setSuggestedTaskDuration(Duration suggestedTaskDuration) {
        this.suggestedTaskDuration = suggestedTaskDuration;
    }

    public Duration getElapsedTaskDuration() {
        return elapsedTaskDuration;
    }

    public void setElapsedTaskDuration(Duration elapsedTaskDuration) {
        this.elapsedTaskDuration = elapsedTaskDuration;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    public boolean isForDeletion() {
        return forDeletion;
    }

    public void setForDeletion(boolean forDeletion) {
        this.forDeletion = forDeletion;
    }

    public void addSubTask(Task task) {
        this.subTaskList.add(task);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Task task = (Task) o;

        if (userId != task.userId) return false;
        return creationTime.equals(task.creationTime);

    }

    @Override
    public int hashCode() {
        int result = (int) (userId ^ (userId >>> 32));
        result = 31 * result + creationTime.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", userId=" + userId +
                ", taskName='" + taskName + '\'' +
                ", parentTaskId=" + parentTaskId +
                ", creationTime=" + creationTime +
                ", suggestedTaskDuration=" + suggestedTaskDuration +
                ", elapsedTaskDuration=" + elapsedTaskDuration +
                ", finished=" + finished +
                ", subTasks=" + subTaskList +
                '}';
    }
}
