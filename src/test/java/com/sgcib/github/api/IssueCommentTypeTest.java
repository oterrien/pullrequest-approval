package com.sgcib.github.api;

import com.sgcib.github.api.service.Configuration;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

public class IssueCommentTypeTest {

    private Configuration configuration = new Configuration();

    @Before
    public void setUp() throws Exception {
        Field approvalComments = Configuration.class.getDeclaredField("approvalComments");
        approvalComments.setAccessible(true);
        approvalComments.set(configuration, "approved,ok,+1,+2");

        Field autoApprovalComments = Configuration.class.getDeclaredField("autoApprovalComments");
        autoApprovalComments.setAccessible(true);
        autoApprovalComments.set(configuration, "auto-approved");

        Field rejectionComments = Configuration.class.getDeclaredField("rejectionComments");
        rejectionComments.setAccessible(true);
        rejectionComments.set(configuration, "refused,ko,rejected,-1,-2");

        Field pendingComments = Configuration.class.getDeclaredField("pendingComments");
        pendingComments.setAccessible(true);
        pendingComments.set(configuration, "to be reviewed");

        Method setUpMethod = Configuration.class.getDeclaredMethod("setUp");
        setUpMethod.setAccessible(true);
        setUpMethod.invoke(configuration);

    }

    @Test
    public void an_approved_message_should_be_bound_with_APPROVEMENT() {

        for (String comment : configuration.getApprovalCommentsList()){
            assertThat(configuration.getType(comment)).isEqualTo(Configuration.IssueCommentType.APPROVEMENT);
            assertThat(configuration.getType(comment)).isNotEqualTo(Configuration.IssueCommentType.AUTO_APPROVEMENT);
            assertThat(configuration.getType(comment)).isNotEqualTo(Configuration.IssueCommentType.REJECTION);
            assertThat(configuration.getType(comment)).isNotEqualTo(Configuration.IssueCommentType.PENDING);
        }
    }

    @Test
    public void an_autoapproved_message_should_be_bound_with_AUTO_APPROVEMENT() {

        for (String comment : configuration.getAutoApprovalCommentsList()){
            assertThat(configuration.getType(comment)).isNotEqualTo(Configuration.IssueCommentType.APPROVEMENT);
            assertThat(configuration.getType(comment)).isEqualTo(Configuration.IssueCommentType.AUTO_APPROVEMENT);
            assertThat(configuration.getType(comment)).isNotEqualTo(Configuration.IssueCommentType.REJECTION);
            assertThat(configuration.getType(comment)).isNotEqualTo(Configuration.IssueCommentType.PENDING);
        }
    }

    @Test
    public void a_rejected_message_should_be_bound_with_REJECTION() {

        for (String comment : configuration.getRejectionCommentsList()){
            assertThat(configuration.getType(comment)).isNotEqualTo(Configuration.IssueCommentType.APPROVEMENT);
            assertThat(configuration.getType(comment)).isNotEqualTo(Configuration.IssueCommentType.AUTO_APPROVEMENT);
            assertThat(configuration.getType(comment)).isEqualTo(Configuration.IssueCommentType.REJECTION);
            assertThat(configuration.getType(comment)).isNotEqualTo(Configuration.IssueCommentType.PENDING);
        }
    }

    @Test
    public void a_pending_message_should_be_bound_with_PENDING() {

        for (String comment : configuration.getPendingCommentsList()){
            assertThat(configuration.getType(comment)).isNotEqualTo(Configuration.IssueCommentType.APPROVEMENT);
            assertThat(configuration.getType(comment)).isNotEqualTo(Configuration.IssueCommentType.AUTO_APPROVEMENT);
            assertThat(configuration.getType(comment)).isNotEqualTo(Configuration.IssueCommentType.REJECTION);
            assertThat(configuration.getType(comment)).isEqualTo(Configuration.IssueCommentType.PENDING);
        }
    }

}
