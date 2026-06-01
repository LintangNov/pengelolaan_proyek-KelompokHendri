package pengelolaanproject.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Model class representing a Project in the project management system.
 */
public class ProjectModel {
    private int id;
    private String name;
    private Date startDate;
    private Date deadline;
    private List<TaskModel> tasks;

    /**
     * Standard constructor requested by specification.
     * Initializes startDate to the current date and tasks as an empty ArrayList.
     *
     * @param name     the project name
     * @param deadline the project deadline
     */
    public ProjectModel(String name, Date deadline) {
        this.name = name;
        this.deadline = deadline;
        this.startDate = new Date();
        this.tasks = new ArrayList<>();
    }

    /**
     * Overloaded constructor for database mapping.
     *
     * @param id        the project ID
     * @param name      the project name
     * @param startDate the project start date
     * @param deadline  the project deadline
     */
    public ProjectModel(int id, String name, Date startDate, Date deadline) {
        this.id = id;
        this.name = name;
        this.startDate = startDate;
        this.deadline = deadline;
        this.tasks = new ArrayList<>();
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getDeadline() {
        return deadline;
    }

    /**
     * Returns an unmodifiable view of the tasks list.
     *
     * @return unmodifiable list of tasks
     */
    public List<TaskModel> getTasks() {
        return Collections.unmodifiableList(tasks);
    }

    // Setters and mutation methods
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    /**
     * Adds a task to the project's task list.
     *
     * @param task task to be added
     */
    public void addTask(TaskModel task) {
        if (task != null) {
            this.tasks.add(task);
        }
    }

    @Override
    public String toString() {
        return "ProjectModel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", startDate=" + startDate +
                ", deadline=" + deadline +
                ", tasksCount=" + tasks.size() +
                '}';
    }
}
