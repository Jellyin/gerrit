// Copyright (C) 2016 The Android Open Source Project
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

package com.google.gerrit.server.restapi.config;

import com.google.gerrit.common.data.ContributorAgreement;
import com.google.gerrit.common.data.GroupReference;
import com.google.gerrit.common.errors.NoSuchGroupException;
import com.google.gerrit.extensions.common.AgreementInfo;
import com.google.gerrit.server.CurrentUser;
import com.google.gerrit.server.IdentifiedUser;
import com.google.gerrit.server.account.GroupControl;
import com.google.gerrit.server.group.GroupResource;
import com.google.gerrit.server.restapi.group.GroupJson;
import com.google.gwtorm.server.OrmException;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgreementJson {
  private static final Logger log = LoggerFactory.getLogger(AgreementJson.class);

  private final Provider<CurrentUser> self;
  private final IdentifiedUser.GenericFactory identifiedUserFactory;
  private final GroupControl.GenericFactory genericGroupControlFactory;
  private final GroupJson groupJson;

  @Inject
  AgreementJson(
      Provider<CurrentUser> self,
      IdentifiedUser.GenericFactory identifiedUserFactory,
      GroupControl.GenericFactory genericGroupControlFactory,
      GroupJson groupJson) {
    this.self = self;
    this.identifiedUserFactory = identifiedUserFactory;
    this.genericGroupControlFactory = genericGroupControlFactory;
    this.groupJson = groupJson;
  }

  public AgreementInfo format(ContributorAgreement ca) {
    AgreementInfo info = new AgreementInfo();
    info.name = ca.getName();
    info.description = ca.getDescription();
    info.url = ca.getAgreementUrl();
    GroupReference autoVerifyGroup = ca.getAutoVerify();
    if (autoVerifyGroup != null && self.get().isIdentifiedUser()) {
      IdentifiedUser user = identifiedUserFactory.create(self.get().getAccountId());
      try {
        GroupControl gc = genericGroupControlFactory.controlFor(user, autoVerifyGroup.getUUID());
        GroupResource group = new GroupResource(gc);
        info.autoVerifyGroup = groupJson.format(group);
      } catch (NoSuchGroupException | OrmException e) {
        log.warn(
            "autoverify group \""
                + autoVerifyGroup.getName()
                + "\" does not exist, referenced in CLA \""
                + ca.getName()
                + "\"");
      }
    }
    return info;
  }
}
