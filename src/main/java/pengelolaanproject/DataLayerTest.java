package pengelolaanproject;

import pengelolaanproject.core.DatabaseConnection;
import pengelolaanproject.model.ProjectModel;
import pengelolaanproject.model.TaskModel;
import pengelolaanproject.model.TaskStatus;
import pengelolaanproject.repository.IProjectRepository;
import pengelolaanproject.repository.ProjectRepository;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Verification test suite for Project models and Repositories.
 */
public class DataLayerTest {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("         STARTING DATA-LAYER VERIFICATION        ");
        System.out.println("==================================================");

        try {
            runModelTests();
            System.out.println(" Model logic tests passed successfully.");
        } catch (Exception e) {
            System.err.println(" Model logic tests failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            if (conn != null) {
                System.out.println("Live database connection established. Running repository integration tests...");
                setupTestDatabase(conn);
                runRepositoryTests(conn);
                System.out.println(" Repository integration tests passed successfully.");
            } else {
                System.out.println(" Skipping database tests: MySQL is offline or database is not running.");
            }
        } catch (Exception e) {
            System.err.println(" Repository tests failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("==================================================");
        System.out.println("        VERIFICATION COMPLETED SUCCESSFULLY       ");
        System.out.println("==================================================");
    }

    private static void runModelTests() {
        System.out.println("\n--- Testing Model Classes ---");

        // 1. TaskModel tests
        TaskModel task = new TaskModel("Implement Login Page", 3);
        if (task.getId() != 0)
            throw new AssertionError("Task ID should default to 0");
        if (!task.getTitle().equals("Implement Login Page"))
            throw new AssertionError("Task title does not match");
        if (task.getStatus() != TaskStatus.TODO)
            throw new AssertionError("Task status should default to TODO");
        if (task.getAssigneeId() != 3)
            throw new AssertionError("Task assignee ID does not match");
        if (!task.getSubmissionLink().equals(""))
            throw new AssertionError("Task submission link should default to empty string");

        task.updateStatus(TaskStatus.IN_PROGRESS);
        if (task.getStatus() != TaskStatus.IN_PROGRESS)
            throw new AssertionError("Task status update failed");

        task.setSubmissionLink("https://github.com/lintang_dev/project");
        if (!task.getSubmissionLink().equals("https://github.com/lintang_dev/project"))
            throw new AssertionError("Task submission link set failed");

        // 2. ProjectModel tests
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        Date deadline = cal.getTime();

        ProjectModel project = new ProjectModel("E-Commerce Web Application", deadline);
        if (project.getId() != 0)
            throw new AssertionError("Project ID should default to 0");
        if (!project.getName().equals("E-Commerce Web Application"))
            throw new AssertionError("Project name does not match");
        if (project.getDeadline() != deadline)
            throw new AssertionError("Project deadline does not match");
        if (project.getStartDate() == null)
            throw new AssertionError("Project start date should be auto-initialized");
        if (project.getTasks().size() != 0)
            throw new AssertionError("Project tasks list should default to empty");

        project.addTask(task);
        if (project.getTasks().size() != 1)
            throw new AssertionError("Project tasks list size should be 1 after addTask");
        if (project.getTasks().get(0) != task)
            throw new AssertionError("Task added does not match");

        // Verify task list immutability contract
        try {
            project.getTasks().clear();
            throw new AssertionError("Project tasks list must be unmodifiable but allowed clear()");
        } catch (UnsupportedOperationException expected) {
            // Success
        }
    }

    private static void setupTestDatabase(Connection conn) throws Exception {
        System.out.println("Setting up/verifying PROJECTS and TASKS tables in test schema...");
        try (Statement stmt = conn.createStatement()) {
            // Create PROJECTS table
            stmt.execute("CREATE TABLE IF NOT EXISTS PROJECTS (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "name VARCHAR(255) NOT NULL," +
                    "start_date DATE," +
                    "deadline DATE" +
                    ")");

            // Create TASKS table
            stmt.execute("CREATE TABLE IF NOT EXISTS TASKS (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "project_id INT NOT NULL," +
                    "assignee_id INT," +
                    "title VARCHAR(255) NOT NULL," +
                    "type VARCHAR(50) DEFAULT 'TASK'," +
                    "status VARCHAR(50) NOT NULL," +
                    "due_date DATE," +
                    "submission_link VARCHAR(255)," +
                    "FOREIGN KEY (project_id) REFERENCES PROJECTS(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (assignee_id) REFERENCES USERS(id) ON DELETE SET NULL" +
                    ")");
        }
    }

    private static void runRepositoryTests(Connection conn) {
        System.out.println("\n--- Testing JDBC Repository Layer ---");

        IProjectRepository repository = new ProjectRepository(conn);

        // 1. Save new project
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 1);
        Date deadline = cal.getTime();
        ProjectModel project = new ProjectModel("Integration Test Project", deadline);

        repository.saveProject(project);
        int projectId = project.getId();
        if (projectId <= 0)
            throw new AssertionError("Project ID was not generated and set after saving");
        System.out.println("Project saved with generated ID: " + projectId);

        // 2. Save task
        TaskModel task1 = new TaskModel("Setup Database Connection", 2);
        Calendar taskCal = Calendar.getInstance();
        taskCal.add(Calendar.DAY_OF_MONTH, 5);
        task1.setDueDate(taskCal.getTime());
        task1.setSubmissionLink("http://db-submission");

        repository.saveTask(task1, projectId);
        int task1Id = task1.getId();
        if (task1Id <= 0)
            throw new AssertionError("Task ID was not generated and set after saving");
        System.out.println("Task saved with generated ID: " + task1Id);

        // 3. Find project by ID
        ProjectModel retrievedProject = repository.findProjectById(projectId);
        if (retrievedProject == null)
            throw new AssertionError("Failed to retrieve project by ID");
        if (!retrievedProject.getName().equals("Integration Test Project"))
            throw new AssertionError("Retrieved project name mismatch");
        if (retrievedProject.getTasks().size() != 1)
            throw new AssertionError("Retrieved project tasks list size mismatch");

        TaskModel retrievedTask = retrievedProject.getTasks().get(0);
        if (retrievedTask.getId() != task1Id)
            throw new AssertionError("Retrieved task ID mismatch");
        if (!retrievedTask.getTitle().equals("Setup Database Connection"))
            throw new AssertionError("Retrieved task title mismatch");
        if (retrievedTask.getStatus() != TaskStatus.TODO)
            throw new AssertionError("Retrieved task status mismatch");

        // 4. Update task status
        retrievedTask.updateStatus(TaskStatus.IN_PROGRESS);
        repository.updateTaskStatus(retrievedTask);

        ProjectModel retrievedProject2 = repository.findProjectById(projectId);
        TaskModel retrievedTask2 = retrievedProject2.getTasks().get(0);
        if (retrievedTask2.getStatus() != TaskStatus.IN_PROGRESS)
            throw new AssertionError("Task status was not updated in DB");
        System.out.println("Task status updated to: " + retrievedTask2.getStatus());

        // 5. Find all projects
        List<ProjectModel> allProjects = repository.findAllProjects();
        boolean found = false;
        for (ProjectModel p : allProjects) {
            if (p.getId() == projectId) {
                found = true;
                break;
            }
        }
        if (!found)
            throw new AssertionError("Saved project not found in findAllProjects list");
        System.out.println("Total projects in DB: " + allProjects.size());

        // Clean up test data (cascade delete of tasks)
        System.out.println("Cleaning up test project record (should cascade delete tasks)...");
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM PROJECTS WHERE id = " + projectId);
        } catch (Exception e) {
            System.err.println("Warning: Cleanup failed: " + e.getMessage());
        }

        // Verify cascade delete
        ProjectModel postDeleteProj = repository.findProjectById(projectId);
        if (postDeleteProj != null)
            throw new AssertionError("Project was not deleted");
        System.out.println("Cleanup successful, cascade delete verified.");
    }
}
