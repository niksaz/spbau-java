package ru.spbau.sazanovich.nikita.mygit.commands;

import org.junit.Test;
import ru.spbau.sazanovich.nikita.mygit.MyGitIllegalArgumentException;
import ru.spbau.sazanovich.nikita.mygit.objects.Branch;
import ru.spbau.sazanovich.nikita.mygit.objects.HeadStatus;

import static org.mockito.Mockito.*;

public class BranchDeleteCommandTest extends MockedInternalStateAccessorTest {

    @Test
    public void performNormal() throws Exception {
        final BranchDeleteCommand command = spy(new BranchDeleteCommand("test", accessor));
        doReturn(true).when(command).doesBranchExists("test");
        doReturn(new HeadStatus(Branch.TYPE, "master")).when(command).getHeadStatus();
        command.perform();
        verify(accessor, times(1)).deleteBranch("test");
    }
    
    @Test(expected = MyGitIllegalArgumentException.class)
    public void performNonExistent() throws Exception {
        final BranchDeleteCommand command = spy(new BranchDeleteCommand("test", accessor));
        doReturn(false).when(command).doesBranchExists("test");
        doReturn(new HeadStatus(Branch.TYPE, "master")).when(command).getHeadStatus();
        command.perform();
    }

    @Test(expected = MyGitIllegalArgumentException.class)
    public void performDeleteWhileCheckedOut() throws Exception {
        final BranchDeleteCommand command = spy(new BranchDeleteCommand("master", accessor));
        doReturn(true).when(command).doesBranchExists("master");
        doReturn(new HeadStatus(Branch.TYPE, "master")).when(command).getHeadStatus();
        command.perform();
    }
}