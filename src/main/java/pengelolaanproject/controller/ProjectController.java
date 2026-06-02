package pengelolaanproject.controller;

import pengelolaanproject.core.BaseController;
import pengelolaanproject.model.ProjectModel;
import pengelolaanproject.repository.IProjectRepository;
import pengelolaanproject.view.ProjectView;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

/**
 * Controller class managing the project creation flow.
 * Wires the ProjectView and handles validation, persistence, and dialog closing.
 */
public class ProjectController extends BaseController {
    private final ProjectView view;
    private final IProjectRepository repository;
    private final JDialog dialog;
    private final Runnable onProjectCreated;

    /**
     * Constructs the controller, wraps ProjectView in a modal dialog, and registers listener.
     *
     * @param repository       the project repository contract
     * @param onProjectCreated callback to execute on successful creation (e.g. to refresh parent view)
     */
    public ProjectController(IProjectRepository repository, Runnable onProjectCreated) {
        this.repository = repository;
        this.onProjectCreated = onProjectCreated;
        this.view = new ProjectView();

        // Wrap ProjectView in a modal JDialog
        this.dialog = new JDialog((JFrame) null, "Create New Project", true);
        this.dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.dialog.setContentPane(view);
        this.dialog.pack();
        this.dialog.setSize(480, 540);
        this.dialog.setLocationRelativeTo(null);
        this.dialog.setResizable(false);

        // Register form listener
        this.view.addCreateListener(new CreateButtonListener());
    }

    /**
     * Opens/displays the modal project creation dialog.
     */
    public void show() {
        this.view.clearForm();
        this.dialog.setVisible(true);
    }

    /**
     * Inner class representing the click listener for the project create action.
     */
    private class CreateButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String name = view.getProjectName();
            Date startDate = view.getStartDate();
            Date deadline = view.getDeadline();

            // Client-side validations
            if (name.isEmpty()) {
                view.showError("Project name cannot be empty.");
                return;
            }

            if (deadline == null) {
                // ProjectView's getDeadline() already checks format, but we handle empty/failure fallback here
                view.showError("A valid deadline date is required (YYYY-MM-DD).");
                return;
            }

            if (startDate != null && startDate.after(deadline)) {
                view.showError("Start date cannot be after deadline.");
                return;
            }

            view.showError(""); // Clear any error messages

            try {
                // Construct and save the model
                ProjectModel project = new ProjectModel(name, deadline);
                project.setStartDate(startDate);

                repository.saveProject(project);

                // Notify user
                JOptionPane.showMessageDialog(
                        dialog,
                        "Project \"" + name + "\" created successfully!",
                        "Project Created",
                        JOptionPane.INFORMATION_MESSAGE
                );

                // Clear and dispose form
                view.clearForm();
                dialog.dispose();

                // Refresh dashboard table
                if (onProjectCreated != null) {
                    onProjectCreated.run();
                }
            } catch (Exception ex) {
                view.showError("Failed to save project: " + ex.getMessage());
            }
        }
    }
}
