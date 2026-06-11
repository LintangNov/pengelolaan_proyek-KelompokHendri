package pengelolaanproject.repository;

import pengelolaanproject.model.ProjectModel;
import pengelolaanproject.model.TaskModel;
import pengelolaanproject.model.TaskStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Concrete implementation of IProjectRepository using raw JDBC.
 * Follows SOLID principles by implementing the IProjectRepository contract.
 */
public class ProjectRepository implements IProjectRepository {
    private final Connection connection;

    /**
     * Dependency injection of database connection.
     *
     * @param connection database connection to use for queries
     */
    public ProjectRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void saveProject(ProjectModel project) {
        if (project == null) {
            return;
        }
        if (project.getId() == 0) {
            String query = "INSERT INTO PROJECTS (name, start_date, deadline) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, project.getName());
                if (project.getStartDate() == null) {
                    stmt.setNull(2, java.sql.Types.DATE);
                } else {
                    stmt.setDate(2, new java.sql.Date(project.getStartDate().getTime()));
                }
                if (project.getDeadline() == null) {
                    stmt.setNull(3, java.sql.Types.DATE);
                } else {
                    stmt.setDate(3, new java.sql.Date(project.getDeadline().getTime()));
                }

                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        project.setId(rs.getInt(1));
                    }
                }
            } catch (SQLException e) {
                System.err.println("SQL Exception in saveProject (insert): " + e.getMessage());
            }
        } else {
            String query = "UPDATE PROJECTS SET name = ?, start_date = ?, deadline = ? WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, project.getName());
                if (project.getStartDate() == null) {
                    stmt.setNull(2, java.sql.Types.DATE);
                } else {
                    stmt.setDate(2, new java.sql.Date(project.getStartDate().getTime()));
                }
                if (project.getDeadline() == null) {
                    stmt.setNull(3, java.sql.Types.DATE);
                } else {
                    stmt.setDate(3, new java.sql.Date(project.getDeadline().getTime()));
                }
                stmt.setInt(4, project.getId());

                stmt.executeUpdate();
            } catch (SQLException e) {
                System.err.println("SQL Exception in saveProject (update): " + e.getMessage());
            }
        }
    }

    @Override
    public ProjectModel findProjectById(int id) {
        String query = "SELECT id, name, start_date, deadline FROM PROJECTS WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("name");
                    Date startDate = rs.getDate("start_date");
                    Date deadline = rs.getDate("deadline");
                    ProjectModel project = new ProjectModel(id, name, startDate, deadline);
                    loadTasksForProject(project);
                    return project;
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Exception in findProjectById: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<ProjectModel> findAllProjects() {
        List<ProjectModel> projects = new ArrayList<>();
        String query = "SELECT id, name, start_date, deadline FROM PROJECTS";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                Date startDate = rs.getDate("start_date");
                Date deadline = rs.getDate("deadline");
                ProjectModel project = new ProjectModel(id, name, startDate, deadline);
                loadTasksForProject(project);
                projects.add(project);
            }
        } catch (SQLException e) {
            System.err.println("SQL Exception in findAllProjects: " + e.getMessage());
        }
        return projects;
    }

    @Override
    public void updateTaskStatus(TaskModel task) {
        if (task == null || task.getId() <= 0) {
            return;
        }
        String query = "UPDATE TASKS SET status = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, task.getStatus() != null ? task.getStatus().name() : TaskStatus.TODO.name());
            stmt.setInt(2, task.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("SQL Exception in updateTaskStatus: " + e.getMessage());
        }
    }

    @Override
    public void saveTask(TaskModel task, int projectId) {
        if (task == null) {
            return;
        }
        if (task.getId() == 0) {
            String query = "INSERT INTO TASKS (project_id, assignee_id, title, type, status, due_date, submission_link, description, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, projectId);
                if (task.getAssigneeId() <= 0) {
                    stmt.setNull(2, java.sql.Types.INTEGER);
                } else {
                    stmt.setInt(2, task.getAssigneeId());
                }
                stmt.setString(3, task.getTitle());
                stmt.setString(4, "TASK"); // Default type value
                stmt.setString(5, task.getStatus() != null ? task.getStatus().name() : TaskStatus.TODO.name());
                if (task.getDueDate() == null) {
                    stmt.setNull(6, java.sql.Types.DATE);
                } else {
                    stmt.setDate(6, new java.sql.Date(task.getDueDate().getTime()));
                }
                stmt.setString(7, task.getSubmissionLink());
                stmt.setString(8, task.getDescription());
                stmt.setString(9, task.getNotes());

                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        task.setId(rs.getInt(1));
                    }
                }
            } catch (SQLException e) {
                System.err.println("SQL Exception in saveTask (insert): " + e.getMessage());
            }
        } else {
            String query = "UPDATE TASKS SET project_id = ?, assignee_id = ?, title = ?, status = ?, due_date = ?, submission_link = ?, description = ?, notes = ? WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, projectId);
                if (task.getAssigneeId() <= 0) {
                    stmt.setNull(2, java.sql.Types.INTEGER);
                } else {
                    stmt.setInt(2, task.getAssigneeId());
                }
                stmt.setString(3, task.getTitle());
                stmt.setString(4, task.getStatus() != null ? task.getStatus().name() : TaskStatus.TODO.name());
                if (task.getDueDate() == null) {
                    stmt.setNull(5, java.sql.Types.DATE);
                } else {
                    stmt.setDate(5, new java.sql.Date(task.getDueDate().getTime()));
                }
                stmt.setString(6, task.getSubmissionLink());
                stmt.setString(7, task.getDescription());
                stmt.setString(8, task.getNotes());
                stmt.setInt(9, task.getId());

                stmt.executeUpdate();
            } catch (SQLException e) {
                System.err.println("SQL Exception in saveTask (update): " + e.getMessage());
            }
        }
    }

    /**
     * Helper to load all tasks belonging to a project and add them to the project model.
     *
     * @param project the project model to load tasks for
     */
    private void loadTasksForProject(ProjectModel project) {
        String query = "SELECT id, title, status, submission_link, assignee_id, due_date, description, notes FROM TASKS WHERE project_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, project.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String title = rs.getString("title");
                    String statusStr = rs.getString("status");
                    TaskStatus status = TaskStatus.fromString(statusStr);
                    String submissionLink = rs.getString("submission_link");
                    int assigneeId = rs.getInt("assignee_id");
                    Date dueDate = rs.getDate("due_date");
                    String description = rs.getString("description");
                    String notes = rs.getString("notes");

                    TaskModel task = new TaskModel(id, title, status, submissionLink, assigneeId, dueDate, description, notes);
                    project.addTask(task);
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Exception in loadTasksForProject: " + e.getMessage());
        }
    }
}
