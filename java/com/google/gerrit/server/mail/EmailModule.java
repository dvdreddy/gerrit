// Copyright (C) 2012 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.gerrit.server.mail;

import com.google.gerrit.entities.Address;
import com.google.gerrit.entities.Change;
import com.google.gerrit.entities.Project;
import com.google.gerrit.entities.SubmitRequirement;
import com.google.gerrit.entities.SubmitRequirementResult;
import com.google.gerrit.extensions.client.ChangeKind;
import com.google.gerrit.extensions.config.FactoryModule;
import com.google.gerrit.server.IdentifiedUser;
import com.google.gerrit.server.account.AccountSshKey;
import com.google.gerrit.server.mail.send.AbandonedChangeEmailDecorator;
import com.google.gerrit.server.mail.send.AddKeyEmailDecoratorFactory;
import com.google.gerrit.server.mail.send.AttentionSetChangeEmailDecorator;
import com.google.gerrit.server.mail.send.AttentionSetChangeEmailDecorator.AttentionSetChange;
import com.google.gerrit.server.mail.send.ChangeEmailNew;
import com.google.gerrit.server.mail.send.ChangeEmailNewFactory;
import com.google.gerrit.server.mail.send.CommentChangeEmailDecorator;
import com.google.gerrit.server.mail.send.CommentChangeEmailDecoratorFactory;
import com.google.gerrit.server.mail.send.DeleteKeyEmailDecoratorFactory;
import com.google.gerrit.server.mail.send.DeleteReviewerChangeEmailDecorator;
import com.google.gerrit.server.mail.send.DeleteVoteChangeEmailDecorator;
import com.google.gerrit.server.mail.send.EmailArguments;
import com.google.gerrit.server.mail.send.HttpPasswordUpdateEmailDecoratorFactory;
import com.google.gerrit.server.mail.send.InboundEmailRejectionEmailDecorator;
import com.google.gerrit.server.mail.send.InboundEmailRejectionEmailDecorator.InboundEmailError;
import com.google.gerrit.server.mail.send.MergedChangeEmailDecoratorFactory;
import com.google.gerrit.server.mail.send.OutgoingEmailNew;
import com.google.gerrit.server.mail.send.OutgoingEmailNewFactory;
import com.google.gerrit.server.mail.send.RegisterNewEmailSender;
import com.google.gerrit.server.mail.send.ReplacePatchSetChangeEmailDecorator;
import com.google.gerrit.server.mail.send.ReplacePatchSetChangeEmailDecoratorFactory;
import com.google.gerrit.server.mail.send.RestoredChangeEmailDecorator;
import com.google.gerrit.server.mail.send.RevertedChangeEmailDecorator;
import com.google.gerrit.server.mail.send.StartReviewChangeEmailDecorator;
import com.google.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jgit.lib.ObjectId;

public class EmailModule extends FactoryModule {
  @Override
  protected void configure() {
    factory(RegisterNewEmailSender.Factory.class);
  }

  public static class AbandonedChangeEmailFactories {
    private final EmailArguments args;
    private final ChangeEmailNewFactory changeEmailFactory;
    private final OutgoingEmailNewFactory outgoingEmailFactory;
    private final AbandonedChangeEmailDecorator abandonedChangeEmailDecorator;

    @Inject
    AbandonedChangeEmailFactories(
        EmailArguments args,
        ChangeEmailNewFactory changeEmailFactory,
        OutgoingEmailNewFactory outgoingEmailFactory,
        AbandonedChangeEmailDecorator abandonedChangeEmailDecorator) {
      this.args = args;
      this.changeEmailFactory = changeEmailFactory;
      this.outgoingEmailFactory = outgoingEmailFactory;
      this.abandonedChangeEmailDecorator = abandonedChangeEmailDecorator;
    }

    public ChangeEmailNew createChangeEmail(Project.NameKey project, Change.Id changeId) {
      return changeEmailFactory.create(
          args.newChangeData(project, changeId), abandonedChangeEmailDecorator);
    }

    public OutgoingEmailNew createEmail(ChangeEmailNew changeEmail) {
      return outgoingEmailFactory.create("abandon", changeEmail);
    }
  }

  public static class AttentionSetChangeEmailFactories {
    private final EmailArguments args;
    private final ChangeEmailNewFactory changeEmailFactory;
    private final OutgoingEmailNewFactory outgoingEmailFactory;

    @Inject
    AttentionSetChangeEmailFactories(
        EmailArguments args,
        ChangeEmailNewFactory changeEmailFactory,
        OutgoingEmailNewFactory outgoingEmailFactory) {
      this.args = args;
      this.changeEmailFactory = changeEmailFactory;
      this.outgoingEmailFactory = outgoingEmailFactory;
    }

    public AttentionSetChangeEmailDecorator createAttentionSetChangeEmail() {
      return new AttentionSetChangeEmailDecorator();
    }

    public ChangeEmailNew createChangeEmail(
        Project.NameKey project,
        Change.Id changeId,
        AttentionSetChangeEmailDecorator attentionSetChangeEmailDecorator) {
      return changeEmailFactory.create(
          args.newChangeData(project, changeId), attentionSetChangeEmailDecorator);
    }

    public OutgoingEmailNew createEmail(
        AttentionSetChange attentionSetChange, ChangeEmailNew changeEmail) {
      if (attentionSetChange.equals(AttentionSetChange.USER_ADDED)) {
        return outgoingEmailFactory.create("addToAttentionSet", changeEmail);
      } else {
        return outgoingEmailFactory.create("removeFromAttentionSet", changeEmail);
      }
    }
  }

  public static class CommentChangeEmailFactories {
    private final EmailArguments args;
    private final CommentChangeEmailDecoratorFactory commentChangeEmailFactory;
    private final ChangeEmailNewFactory changeEmailFactory;
    private final OutgoingEmailNewFactory outgoingEmailFactory;

    @Inject
    CommentChangeEmailFactories(
        EmailArguments args,
        CommentChangeEmailDecoratorFactory commentChangeEmailFactory,
        ChangeEmailNewFactory changeEmailFactory,
        OutgoingEmailNewFactory outgoingEmailFactory) {
      this.args = args;
      this.commentChangeEmailFactory = commentChangeEmailFactory;
      this.changeEmailFactory = changeEmailFactory;
      this.outgoingEmailFactory = outgoingEmailFactory;
    }

    public CommentChangeEmailDecorator createCommentChangeEmail(
        Project.NameKey project,
        Change.Id changeId,
        ObjectId preUpdateMetaId,
        Map<SubmitRequirement, SubmitRequirementResult> postUpdateSubmitRequirementResults) {
      return commentChangeEmailFactory.create(
          project, changeId, preUpdateMetaId, postUpdateSubmitRequirementResults);
    }

    public ChangeEmailNew createChangeEmail(
        Project.NameKey project,
        Change.Id changeId,
        CommentChangeEmailDecorator commentChangeEmailDecorator) {
      return changeEmailFactory.create(
          args.newChangeData(project, changeId), commentChangeEmailDecorator);
    }

    public OutgoingEmailNew createEmail(ChangeEmailNew changeEmail) {
      return outgoingEmailFactory.create("comment", changeEmail);
    }
  }

  public static class DeleteReviewerChangeEmailFactories {
    private final EmailArguments args;
    private final ChangeEmailNewFactory changeEmailFactory;
    private final OutgoingEmailNewFactory outgoingEmailFactory;

    @Inject
    DeleteReviewerChangeEmailFactories(
        EmailArguments args,
        ChangeEmailNewFactory changeEmailFactory,
        OutgoingEmailNewFactory outgoingEmailFactory) {
      this.args = args;
      this.changeEmailFactory = changeEmailFactory;
      this.outgoingEmailFactory = outgoingEmailFactory;
    }

    public DeleteReviewerChangeEmailDecorator createDeleteReviewerChangeEmail() {
      return new DeleteReviewerChangeEmailDecorator();
    }

    public ChangeEmailNew createChangeEmail(
        Project.NameKey project,
        Change.Id changeId,
        DeleteReviewerChangeEmailDecorator deleteReviewerChangeEmailDecorator) {
      return changeEmailFactory.create(
          args.newChangeData(project, changeId), deleteReviewerChangeEmailDecorator);
    }

    public OutgoingEmailNew createEmail(ChangeEmailNew changeEmail) {
      return outgoingEmailFactory.create("deleteReviewer", changeEmail);
    }
  }

  public static class DeleteVoteChangeEmailFactories {
    private final EmailArguments args;
    private final ChangeEmailNewFactory changeEmailFactory;
    private final OutgoingEmailNewFactory outgoingEmailFactory;
    private final DeleteVoteChangeEmailDecorator deleteVoteChangeEmailDecorator;

    @Inject
    DeleteVoteChangeEmailFactories(
        EmailArguments args,
        ChangeEmailNewFactory changeEmailFactory,
        OutgoingEmailNewFactory outgoingEmailFactory,
        DeleteVoteChangeEmailDecorator deleteVoteChangeEmailDecorator) {
      this.args = args;
      this.changeEmailFactory = changeEmailFactory;
      this.outgoingEmailFactory = outgoingEmailFactory;
      this.deleteVoteChangeEmailDecorator = deleteVoteChangeEmailDecorator;
    }

    public ChangeEmailNew createChangeEmail(Project.NameKey project, Change.Id changeId) {
      return changeEmailFactory.create(
          args.newChangeData(project, changeId), deleteVoteChangeEmailDecorator);
    }

    public OutgoingEmailNew createEmail(ChangeEmailNew changeEmail) {
      return outgoingEmailFactory.create("deleteVote", changeEmail);
    }
  }

  public static class MergedChangeEmailFactories {
    private final EmailArguments args;
    private final MergedChangeEmailDecoratorFactory mergedChangeEmailDecoratorFactory;
    private final ChangeEmailNewFactory changeEmailFactory;
    private final OutgoingEmailNewFactory outgoingEmailFactory;

    @Inject
    MergedChangeEmailFactories(
        EmailArguments args,
        MergedChangeEmailDecoratorFactory mergedChangeEmailDecoratorFactory,
        ChangeEmailNewFactory changeEmailFactory,
        OutgoingEmailNewFactory outgoingEmailFactory) {
      this.args = args;
      this.mergedChangeEmailDecoratorFactory = mergedChangeEmailDecoratorFactory;
      this.changeEmailFactory = changeEmailFactory;
      this.outgoingEmailFactory = outgoingEmailFactory;
    }

    public ChangeEmailNew createChangeEmail(
        Project.NameKey project, Change.Id changeId, Optional<String> stickyApprovalDiff) {
      return changeEmailFactory.create(
          args.newChangeData(project, changeId),
          mergedChangeEmailDecoratorFactory.create(stickyApprovalDiff));
    }

    public OutgoingEmailNew createEmail(ChangeEmailNew changeEmail) {
      return outgoingEmailFactory.create("merged", changeEmail);
    }
  }

  public static class ReplacePatchSetChangeEmailFactories {
    private final EmailArguments args;
    private final ReplacePatchSetChangeEmailDecoratorFactory
        replacePatchSetChangeEmailDecoratorFactory;
    private final ChangeEmailNewFactory changeEmailFactory;
    private final OutgoingEmailNewFactory outgoingEmailFactory;

    @Inject
    ReplacePatchSetChangeEmailFactories(
        EmailArguments args,
        ReplacePatchSetChangeEmailDecoratorFactory replacePatchSetChangeEmailDecoratorFactory,
        ChangeEmailNewFactory changeEmailFactory,
        OutgoingEmailNewFactory outgoingEmailFactory) {
      this.args = args;
      this.replacePatchSetChangeEmailDecoratorFactory = replacePatchSetChangeEmailDecoratorFactory;
      this.changeEmailFactory = changeEmailFactory;
      this.outgoingEmailFactory = outgoingEmailFactory;
    }

    public ReplacePatchSetChangeEmailDecorator createReplacePatchSetChangeEmail(
        Project.NameKey project,
        Change.Id changeId,
        ChangeKind changeKind,
        ObjectId preUpdateMetaId,
        Map<SubmitRequirement, SubmitRequirementResult> postUpdateSubmitRequirementResults) {
      return replacePatchSetChangeEmailDecoratorFactory.create(
          project, changeId, changeKind, preUpdateMetaId, postUpdateSubmitRequirementResults);
    }

    public ChangeEmailNew createChangeEmail(
        Project.NameKey project,
        Change.Id changeId,
        ReplacePatchSetChangeEmailDecorator replacePatchSetChangeEmailDecoratorFactory) {
      return changeEmailFactory.create(
          args.newChangeData(project, changeId), replacePatchSetChangeEmailDecoratorFactory);
    }

    public OutgoingEmailNew createEmail(ChangeEmailNew changeEmail) {
      return outgoingEmailFactory.create("newpatchset", changeEmail);
    }
  }

  public static class RestoredChangeEmailFactories {
    private final EmailArguments args;
    private final ChangeEmailNewFactory changeEmailFactory;
    private final OutgoingEmailNewFactory outgoingEmailFactory;
    private final RestoredChangeEmailDecorator restoredChangeEmailDecorator;

    @Inject
    RestoredChangeEmailFactories(
        EmailArguments args,
        ChangeEmailNewFactory changeEmailFactory,
        OutgoingEmailNewFactory outgoingEmailFactory,
        RestoredChangeEmailDecorator restoredChangeEmailDecorator) {
      this.args = args;
      this.changeEmailFactory = changeEmailFactory;
      this.outgoingEmailFactory = outgoingEmailFactory;
      this.restoredChangeEmailDecorator = restoredChangeEmailDecorator;
    }

    public ChangeEmailNew createChangeEmail(Project.NameKey project, Change.Id changeId) {
      return changeEmailFactory.create(
          args.newChangeData(project, changeId), restoredChangeEmailDecorator);
    }

    public OutgoingEmailNew createEmail(ChangeEmailNew changeEmail) {
      return outgoingEmailFactory.create("restore", changeEmail);
    }
  }

  public static class RevertedChangeEmailFactories {
    private final EmailArguments args;
    private final ChangeEmailNewFactory changeEmailFactory;
    private final OutgoingEmailNewFactory outgoingEmailFactory;
    private final RevertedChangeEmailDecorator revertedChangeEmailDecorator;

    @Inject
    RevertedChangeEmailFactories(
        EmailArguments args,
        ChangeEmailNewFactory changeEmailFactory,
        OutgoingEmailNewFactory outgoingEmailFactory,
        RevertedChangeEmailDecorator revertedChangeEmailDecorator) {
      this.args = args;
      this.changeEmailFactory = changeEmailFactory;
      this.outgoingEmailFactory = outgoingEmailFactory;
      this.revertedChangeEmailDecorator = revertedChangeEmailDecorator;
    }

    public ChangeEmailNew createChangeEmail(Project.NameKey project, Change.Id changeId) {
      return changeEmailFactory.create(
          args.newChangeData(project, changeId), revertedChangeEmailDecorator);
    }

    public OutgoingEmailNew createEmail(ChangeEmailNew changeEmail) {
      return outgoingEmailFactory.create("revert", changeEmail);
    }
  }

  public static class StartReviewChangeEmailFactories {
    private final EmailArguments args;
    private final ChangeEmailNewFactory changeEmailFactory;
    private final OutgoingEmailNewFactory outgoingEmailFactory;

    @Inject
    StartReviewChangeEmailFactories(
        EmailArguments args,
        ChangeEmailNewFactory changeEmailFactory,
        OutgoingEmailNewFactory outgoingEmailFactory) {
      this.args = args;
      this.changeEmailFactory = changeEmailFactory;
      this.outgoingEmailFactory = outgoingEmailFactory;
    }

    public StartReviewChangeEmailDecorator createStartReviewChangeEmail() {
      return new StartReviewChangeEmailDecorator();
    }

    public ChangeEmailNew createChangeEmail(
        Project.NameKey project,
        Change.Id changeId,
        StartReviewChangeEmailDecorator startReviewChangeEmailDecorator) {
      return changeEmailFactory.create(
          args.newChangeData(project, changeId), startReviewChangeEmailDecorator);
    }

    public OutgoingEmailNew createEmail(ChangeEmailNew changeEmail) {
      return outgoingEmailFactory.create("newchange", changeEmail);
    }
  }

  public static class AddKeyEmailFactories {
    private final AddKeyEmailDecoratorFactory addKeyEmailDecoratorFactory;
    private final OutgoingEmailNewFactory outgoingEmailFactory;

    @Inject
    AddKeyEmailFactories(
        AddKeyEmailDecoratorFactory addKeyEmailDecoratorFactory,
        OutgoingEmailNewFactory outgoingEmailFactory) {
      this.addKeyEmailDecoratorFactory = addKeyEmailDecoratorFactory;
      this.outgoingEmailFactory = outgoingEmailFactory;
    }

    public OutgoingEmailNew createEmail(IdentifiedUser user, AccountSshKey sshKey) {
      return outgoingEmailFactory.create(
          "addkey", addKeyEmailDecoratorFactory.create(user, sshKey));
    }

    public OutgoingEmailNew createEmail(IdentifiedUser user, List<String> gpgKeys) {
      return outgoingEmailFactory.create(
          "addkey", addKeyEmailDecoratorFactory.create(user, gpgKeys));
    }
  }

  public static class DeleteKeyEmailFactories {
    private final DeleteKeyEmailDecoratorFactory deleteKeyEmailDecoratorFactory;
    private final OutgoingEmailNewFactory outgoingEmailFactory;

    @Inject
    DeleteKeyEmailFactories(
        DeleteKeyEmailDecoratorFactory deleteKeyEmailDecoratorFactory,
        OutgoingEmailNewFactory outgoingEmailFactory) {
      this.deleteKeyEmailDecoratorFactory = deleteKeyEmailDecoratorFactory;
      this.outgoingEmailFactory = outgoingEmailFactory;
    }

    public OutgoingEmailNew createEmail(IdentifiedUser user, AccountSshKey sshKey) {
      return outgoingEmailFactory.create(
          "deletekey", deleteKeyEmailDecoratorFactory.create(user, sshKey));
    }

    public OutgoingEmailNew createEmail(IdentifiedUser user, List<String> gpgKeyFingerprints) {
      return outgoingEmailFactory.create(
          "deletekey", deleteKeyEmailDecoratorFactory.create(user, gpgKeyFingerprints));
    }
  }

  public static class HttpPasswordUpdateEmailFactory {
    private final HttpPasswordUpdateEmailDecoratorFactory httpPasswordUpdateEmailDecoratorFactory;
    private final OutgoingEmailNewFactory outgoingEmailFactory;

    @Inject
    HttpPasswordUpdateEmailFactory(
        HttpPasswordUpdateEmailDecoratorFactory httpPasswordUpdateEmailDecoratorFactory,
        OutgoingEmailNewFactory outgoingEmailFactory) {
      this.httpPasswordUpdateEmailDecoratorFactory = httpPasswordUpdateEmailDecoratorFactory;
      this.outgoingEmailFactory = outgoingEmailFactory;
    }

    public OutgoingEmailNew createEmail(IdentifiedUser user, String operation) {
      return outgoingEmailFactory.create(
          "HttpPasswordUpdate", httpPasswordUpdateEmailDecoratorFactory.create(user, operation));
    }
  }

  public static class InboundEmailRejectionEmailFactory {
    private final OutgoingEmailNewFactory outgoingEmailFactory;

    @Inject
    InboundEmailRejectionEmailFactory(OutgoingEmailNewFactory outgoingEmailFactory) {
      this.outgoingEmailFactory = outgoingEmailFactory;
    }

    public OutgoingEmailNew createEmail(Address to, String threadId, InboundEmailError reason) {
      return outgoingEmailFactory.create(
          "error", new InboundEmailRejectionEmailDecorator(to, threadId, reason));
    }
  }
}
