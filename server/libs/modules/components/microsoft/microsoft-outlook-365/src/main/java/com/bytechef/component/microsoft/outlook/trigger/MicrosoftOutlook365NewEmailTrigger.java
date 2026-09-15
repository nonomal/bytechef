/*
 * Copyright 2025 ByteChef
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bytechef.component.microsoft.outlook.trigger;

import static com.bytechef.component.definition.ComponentDsl.trigger;
import static com.bytechef.component.microsoft.outlook.constant.MicrosoftOutlook365Constants.FORMAT_PROPERTY;

import com.bytechef.component.definition.ComponentDsl.ModifiableTriggerDefinition;
import com.bytechef.component.definition.Parameters;
import com.bytechef.component.definition.TriggerContext;
import com.bytechef.component.definition.TriggerDefinition.PollOutput;
import com.bytechef.component.definition.TriggerDefinition.TriggerType;
import com.bytechef.component.microsoft.outlook.util.MicrosoftOutlook365Utils;
import com.bytechef.microsoft.commons.MicrosoftUtils;

/**
 * @author Monika Kušter
 */
public class MicrosoftOutlook365NewEmailTrigger {

    public static final ModifiableTriggerDefinition TRIGGER_DEFINITION = trigger("newEmail")
        .title("New Email")
        .description("Triggers a new workflow run for each new email received in your Inbox.")
        .help("", "https://docs.bytechef.io/reference/components/microsoft-outlook-365_v1#new-email")
        .type(TriggerType.POLLING)
        .properties(FORMAT_PROPERTY)
        .output(MicrosoftOutlook365Utils::getMessageOutput)
        .poll(MicrosoftOutlook365NewEmailTrigger::poll)
        .processErrorResponse(MicrosoftUtils::processErrorResponse);

    private MicrosoftOutlook365NewEmailTrigger() {
    }

    protected static PollOutput poll(
        Parameters inputParameters, Parameters connectionParameters, Parameters closureParameters,
        TriggerContext context) {

        return MicrosoftOutlook365Utils.getPollOutput(inputParameters, closureParameters, context);
    }
}
