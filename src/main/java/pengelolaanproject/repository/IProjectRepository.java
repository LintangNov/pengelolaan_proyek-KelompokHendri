package pengelolaanproject.repository;

import pengelolaanproject.model.ProjectModel;
import pengelolaanproject.model.TaskModel;
import java.util.List;

/**
 * Interface defining the project and task repository data contract.
 * Follows the Dependency Inversion Principle.
 */
public interface IProjectRepository {

    /**
     * Saves a project. If the project's ID is 0, it inserts a new project
     * and assigns it the auto-generated database ID. If the ID is greater than 0,
     * it updates the existing project record.
     *
     * @param project the project model to save
     */
    void saveProject(ProjectModel project);

    /**
     * Finds a project by its database ID, including all its associated tasks.
     *
     * @param id the project ID
     * @return the populated ProjectModel, or null if not found
     */
    ProjectModel findProjectById(int id);

    /**
     * Retrieves all projects from the database, populating each project
     * with its corresponding list of tasks.
     *
     * @return a list of all project models
     */
    List<ProjectModel> findAllProjects();

    /**
     * Updates the status of a specific task.
     *
     * @param task the task model whose status should be updated
     */
    void updateTaskStatus(TaskModel task);

    /**
     * Saves a task associated with a specific project ID. If the task's ID is 0,
     * it inserts a new task and assigns it the auto-generated database ID.
     * If the ID is greater than 0, it updates the existing task record.
     *
     * @param task      the task model to save
     * @param projectId the ID of the project the task belongs to
     */
    void saveTask(TaskModel task, int projectId);
}
