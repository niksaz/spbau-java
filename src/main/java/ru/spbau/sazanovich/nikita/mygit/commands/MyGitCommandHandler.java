package ru.spbau.sazanovich.nikita.mygit.commands;

import org.jetbrains.annotations.NotNull;
import ru.spbau.sazanovich.nikita.mygit.MyGitAlreadyInitializedException;
import ru.spbau.sazanovich.nikita.mygit.MyGitIllegalArgumentException;
import ru.spbau.sazanovich.nikita.mygit.MyGitMissingPrerequisitesException;
import ru.spbau.sazanovich.nikita.mygit.MyGitStateException;
import ru.spbau.sazanovich.nikita.mygit.objects.Branch;
import ru.spbau.sazanovich.nikita.mygit.objects.CommitLog;
import ru.spbau.sazanovich.nikita.mygit.objects.FileDifference;
import ru.spbau.sazanovich.nikita.mygit.objects.HeadStatus;
import ru.spbau.sazanovich.nikita.mygit.utils.SHA1Hasher;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Class which should be instantiated by a user to interact with the library. Handles command and delegating internal
 * representation changes to {@link InternalStateAccessor}.
 */
public class MyGitCommandHandler {

    @NotNull
    private final InternalStateAccessor internalStateAccessor;

    /**
     * Initialize MyGit repository in a given directory.
     *
     * @param directory an absolute path to a directory to initialize MyGit in
     * @throws MyGitIllegalArgumentException    if the directory path is not absolute
     * @throws MyGitAlreadyInitializedException if the directory already contains .mygit file
     * @throws MyGitStateException              if an internal error occurs during operations
     * @throws IOException                      if an error occurs during working with a filesystem
     */
    public static void init(@NotNull Path directory)
            throws MyGitIllegalArgumentException, MyGitAlreadyInitializedException, MyGitStateException, IOException {
        new InitCommand(directory).perform();
    }

    /**
     * Constructs a handler in a given directory.
     *
     * @param currentDirectory a path to the current directory for a handler
     * @throws MyGitIllegalArgumentException if the directory path is not absolute
     * @throws MyGitStateException           if the directory (or any of the parent directories) is not a MyGit repository
     */
    public MyGitCommandHandler(@NotNull Path currentDirectory) throws MyGitIllegalArgumentException, MyGitStateException {
        internalStateAccessor = new InternalStateAccessor(currentDirectory, new SHA1Hasher());
    }

    /**
     * Gets differences between current MyGit repository's HEAD state and the filesystem state.
     *
     * @return list of differences between MyGit repository's HEAD state and the filesystem state
     * @throws MyGitStateException if an internal error occurs during operations
     * @throws IOException         if an error occurs during working with a filesystem
     */
    @NotNull
    public List<FileDifference> getHeadDifferences() throws MyGitStateException, IOException {
        return new StatusCommand(internalStateAccessor).perform();
    }

    /**
     * Adds a path to the current index.
     *
     * @param path a path to add to the index
     * @throws MyGitIllegalArgumentException if the path is incorrect or located outside MyGit repository
     * @throws MyGitStateException           if an internal error occurs during operations
     * @throws IOException                   if an error occurs during working with a filesystem
     */
    public void stagePath(@NotNull String path)
            throws MyGitIllegalArgumentException, MyGitStateException, IOException {
        new StageCommand(path, internalStateAccessor).perform();
    }

    /**
     * Remove a path from the current index.
     *
     * @param path a path to remove from the index
     * @throws MyGitIllegalArgumentException if the path is incorrect or located outside MyGit repository
     * @throws MyGitStateException           if an internal error occurs during operations
     * @throws IOException                   if an error occurs during working with a filesystem
     */
    public void unstagePath(@NotNull String path)
            throws MyGitStateException, MyGitIllegalArgumentException, IOException {
        new UnstageCommand(path, internalStateAccessor).perform();
    }

    /**
     * Removes all paths from the current index.
     *
     * @throws MyGitStateException if an internal error occurs during operations
     * @throws IOException         if an error occurs during working with a filesystem
     */
    public void unstageAllPaths() throws MyGitStateException, IOException {
        new UnstageAllCommand(internalStateAccessor).perform();
    }

    /**
     * Resets file's state to the corresponding HEAD's one. Given file should be present in the filesystem.
     *
     * @param path a file's path to reset
     * @throws MyGitIllegalArgumentException if the file is not present either in the filesystem or MyGit's HEAD
     * @throws MyGitStateException           if an internal error occurs during operations
     * @throws IOException                   if an error occurs during working with a filesystem
     */
    public void resetPath(@NotNull String path) throws MyGitIllegalArgumentException, MyGitStateException, IOException {
        new ResetCommand(path, internalStateAccessor).perform();
    }

    /**
     * Removes all files which are untracked by MyGit.
     *
     * @throws MyGitStateException if an internal error occurs during operations
     * @throws IOException         if an error occurs during working with a filesystem
     */
    public void clean() throws MyGitStateException, IOException {
        new CleanCommand(internalStateAccessor).perform();
    }

    /**
     * Gets the current HEAD state.
     *
     * @return a head state
     * @throws MyGitStateException if an internal error occurs during operations
     * @throws IOException         if an error occurs during working with a filesystem
     */
    @NotNull
    public HeadStatus getHeadStatus() throws MyGitStateException, IOException {
        return new HeadStatusCommand(internalStateAccessor).perform();
    }

    /**
     * Gets the logs from commit's which are reachable from the HEAD's commit.
     *
     * @return list of commit's logs
     * @throws MyGitStateException if an internal error occurs during operations
     * @throws IOException         if an error occurs during working with a filesystem
     */
    @NotNull
    public List<CommitLog> getCommitsLogsHistory() throws MyGitStateException, IOException {
        return new LogCommand(internalStateAccessor).perform();
    }

    /**
     * Checkouts a revision and moves HEAD there.
     *
     * Replaces all files which differs from the HEAD's ones if such exist. Index should be empty before checking out.
     *
     * @param revisionName a name of revision
     * @throws MyGitMissingPrerequisitesException if the index is not empty
     * @throws MyGitIllegalArgumentException      if the revision does not exist
     * @throws MyGitStateException                if an internal error occurs during operations
     * @throws IOException                        if an error occurs during working with a filesystem
     */
    public void checkout(@NotNull String revisionName)
            throws MyGitStateException, MyGitMissingPrerequisitesException, MyGitIllegalArgumentException, IOException {
        new CheckoutCommand(revisionName, internalStateAccessor).perform();
    }

    /**
     * Perform a merge operation of HEAD and given branch.
     *
     * Chooses files based on their last modification's date -- older have a priority. Index should be empty before
     * merging and HEAD should not be in detached state.
     *
     * @param branch branch with which to merge HEAD
     * @throws MyGitMissingPrerequisitesException if the index is not empty or currently in a detached HEAD state
     * @throws MyGitIllegalArgumentException      if there is no such branch or a user tries to merge branch with itself
     * @throws MyGitStateException                if an internal error occurs during operations
     * @throws IOException                        if an error occurs during working with a filesystem
     */
    public void mergeHeadWithBranch(@NotNull String branch)
            throws MyGitMissingPrerequisitesException, MyGitStateException, IOException, MyGitIllegalArgumentException {
        new MergeCommand(branch, internalStateAccessor).perform();
    }

    /**
     * Gets list of branches in MyGit repository.
     *
     * @return list of branches
     * @throws MyGitStateException if an internal error occurs during operations
     * @throws IOException         if an error occurs during working with a filesystem
     */
    @NotNull
    public List<Branch> listBranches() throws MyGitStateException, IOException {
        return new ListBranchesCommand(internalStateAccessor).perform();
    }

    /**
     * Creates a new branch with the given name.
     *
     * @param branchName branch name for a new branch
     * @throws MyGitIllegalArgumentException if the branch with the name already exists
     * @throws MyGitStateException           if an internal error occurs during operations
     * @throws IOException                   if an error occurs during working with a filesystem
     */
    public void createBranch(@NotNull String branchName)
            throws MyGitStateException, IOException, MyGitIllegalArgumentException {
        new BranchCreateCommand(branchName, internalStateAccessor).perform();
    }

    /**
     * Deletes the branch with the given name.
     *
     * @param branchName the name of the branch to delete
     * @throws MyGitIllegalArgumentException if there is no a branch with this name or
     *                                       this branch is currently checked out
     * @throws MyGitStateException           if an internal error occurs during operations
     * @throws IOException                   if an error occurs during working with a filesystem
     */
    public void deleteBranch(@NotNull String branchName)
            throws MyGitIllegalArgumentException, IOException, MyGitStateException {
        new BranchDeleteCommand(branchName, internalStateAccessor).perform();
    }

    /**
     * Commits all indexed files and moves head to the newly created commit.
     *
     * @param message commit's message
     * @throws MyGitStateException if an internal error occurs during operations
     * @throws IOException         if an error occurs during working with a filesystem
     */
    public void commitWithMessage(@NotNull String message) throws MyGitStateException, IOException {
        new CommitCommand(message, internalStateAccessor).perform();
    }
}
