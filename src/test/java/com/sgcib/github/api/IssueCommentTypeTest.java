package com.sgcib.github.api;

import com.sgcib.github.api.component.IssueCommentConfiguration;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

public class IssueCommentTypeTest {

    private IssueCommentConfiguration configuration = new IssueCommentConfiguration();

    @Before
    public void setUp() throws Exception {
        Field approvalComments = IssueCommentConfiguration.class.getDeclaredField("approvalComments");
        approvalComments.setAccessible(true);
        approvalComments.set(configuration, "approved,ok,+1,+2");

        Field autoApprovalComments = IssueCommentConfiguration.class.getDeclaredField("autoApprovalComments");
        autoApprovalComments.setAccessible(true);
        autoApprovalComments.set(configuration, "auto-approved");

        Field rejectionComments = IssueCommentConfiguration.class.getDeclaredField("rejectionComments");
        rejectionComments.setAccessible(true);
        rejectionComments.set(configuration, "refused,ko,rejected,-1,-2");

        Field pendingComments = IssueCommentConfiguration.class.getDeclaredField("pendingComments");
        pendingComments.setAccessible(true);
        pendingComments.set(configuration, "to be reviewed");

        Method setUpMethod = IssueCommentConfiguration.class.getDeclaredMethod("setUp");
        setUpMethod.setAccessible(true);
        setUpMethod.invoke(configuration);

    }

    @Test
    public void an_approved_message_should_be_bound_with_APPROVEMENT() {

        for (String comment : configuration.getApprovalCommentsList()){
            assertThat(configuration.getType(comment)).isEqualTo(IssueCommentConfiguration.Type.APPROVEMENT);
            assertThat(configuration.getType(comment)).isNotEqualTo(IssueCommentConfiguration.Type.AUTO_APPROVEMENT);
            assertThat(configuration.getType(comment)).isNotEqualTo(IssueCommentConfiguration.Type.REJECTION);
            assertThat(configuration.getType(comment)).isNotEqualTo(IssueCommentConfiguration.Type.PENDING);
        }
    }

    @Test
    public void an_autoapproved_message_should_be_bound_with_AUTO_APPROVEMENT() {

        for (String comment : configuration.getAutoApprovalCommentsList()){
            assertThat(configuration.getType(comment)).isNotEqualTo(IssueCommentConfiguration.Type.APPROVEMENT);
            assertThat(configuration.getType(comment)).isEqualTo(IssueCommentConfiguration.Type.AUTO_APPROVEMENT);
            assertThat(configuration.getType(comment)).isNotEqualTo(IssueCommentConfiguration.Type.REJECTION);
            assertThat(configuration.getType(comment)).isNotEqualTo(IssueCommentConfiguration.Type.PENDING);
        }
    }

    @Test
    public void a_rejected_message_should_be_bound_with_REJECTION() {

        for (String comment : configuration.getRejectionCommentsList()){
            assertThat(configuration.getType(comment)).isNotEqualTo(IssueCommentConfiguration.Type.APPROVEMENT);
            assertThat(configuration.getType(comment)).isNotEqualTo(IssueCommentConfiguration.Type.AUTO_APPROVEMENT);
            assertThat(configuration.getType(comment)).isEqualTo(IssueCommentConfiguration.Type.REJECTION);
            assertThat(configuration.getType(comment)).isNotEqualTo(IssueCommentConfiguration.Type.PENDING);
        }
    }

    @Test
    public void a_pending_message_should_be_bound_with_PENDING() {

        for (String comment : configuration.getPendingCommentsList()){
            assertThat(configuration.getType(comment)).isNotEqualTo(IssueCommentConfiguration.Type.APPROVEMENT);
            assertThat(configuration.getType(comment)).isNotEqualTo(IssueCommentConfiguration.Type.AUTO_APPROVEMENT);
            assertThat(configuration.getType(comment)).isNotEqualTo(IssueCommentConfiguration.Type.REJECTION);
            assertThat(configuration.getType(comment)).isEqualTo(IssueCommentConfiguration.Type.PENDING);
        }
    }

}
