package pengelolaanproject.model;

import java.util.Date;

/**
 * Model class representing a Task in the project management system.
 */
public class TaskModel {
    private int id;
    private String title;
    private TaskStatus status;
    private String submissionLink;
    private int assigneeId;
    private Date dueDate;

    /**
     * Standard constructor requested by specification.
     * Initializes status to TODO by default.
     *
     * @param title      the task title
     * @param assigneeId the ID of the assigned user
     */
    public TaskModel(String title, int assigneeId) {
        this.title = title;
        this.assigneeId = assigneeId;
        this.status = TaskStatus.TODO;
        this.submissionLink = "";
        this.dueDate = null;
    }

    /**
     * Overloaded constructor for database mapping and general initialization.
     *
     * @param id             the task ID
     * @param title          the task title
     * @param status         the task status
     * @param submissionLink the submission link
     * @param assigneeId     the ID of the assigned user
     * @param dueDate        the due date
     */
    public TaskModel(int id, String title, TaskStatus status, String submissionLink, int assigneeId, Date dueDate) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.submissionLink = submissionLink;
        this.assigneeId = assigneeId;
        this.dueDate = dueDate;
    }

    // Getters for all fields
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public String getSubmissionLink() {
        return submissionLink;
    }

    public int getAssigneeId() {
        return assigneeId;
    }

    public Date getDueDate() {
        return dueDate;
    }

    // Setters and state update methods
    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void updateStatus(TaskStatus newStatus) {
        this.status = newStatus;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public void setSubmissionLink(String submissionLink) {
        this.submissionLink = submissionLink;
    }

    public void setAssigneeId(int assigneeId) {
        this.assigneeId = assigneeId;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    @Override
    public String toString() {
        return "TaskModel{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", status=" + status +
                ", submissionLink='" + submissionLink + '\'' +
                ", assigneeId=" + assigneeId +
                ", dueDate=" + dueDate +
                '}';
    }
}
