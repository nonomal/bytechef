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

package com.bytechef.component.sendgrid.action;

import static com.bytechef.component.definition.ComponentDsl.ModifiableActionDefinition;
import static com.bytechef.component.definition.ComponentDsl.action;
import static com.bytechef.component.definition.ComponentDsl.array;
import static com.bytechef.component.definition.ComponentDsl.fileEntry;
import static com.bytechef.component.definition.ComponentDsl.option;
import static com.bytechef.component.definition.ComponentDsl.string;
import static com.bytechef.component.sendgrid.constant.SendgridConstants.ATTACHMENTS;
import static com.bytechef.component.sendgrid.constant.SendgridConstants.CC;
import static com.bytechef.component.sendgrid.constant.SendgridConstants.FROM;
import static com.bytechef.component.sendgrid.constant.SendgridConstants.SUBJECT;
import static com.bytechef.component.sendgrid.constant.SendgridConstants.TEXT;
import static com.bytechef.component.sendgrid.constant.SendgridConstants.TO;
import static com.bytechef.component.sendgrid.constant.SendgridConstants.TYPE;
import static com.bytechef.component.sendgrid.util.SendgridUtils.convertToEmailList;
import static com.bytechef.component.sendgrid.util.SendgridUtils.getAllAttachments;
import static com.bytechef.component.sendgrid.util.SendgridUtils.sendEmail;

import com.bytechef.component.definition.Context;
import com.bytechef.component.definition.FileEntry;
import com.bytechef.component.definition.Parameters;
import com.bytechef.component.definition.Property;
import com.bytechef.component.definition.Property.ControlType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Marko Krišković
 * @author Luka Ljubić
 */
public final class SendgridSendEmailAction {

    public static final ModifiableActionDefinition ACTION_DEFINITION = action("sendEmail")
        .title("Send Email")
        .description("Sends an email.")
        .properties(
            string(FROM)
                .label("From")
                .description("Email address from which you want to send.")
                .maxLength(320)
                .required(true),
            array(TO)
                .label("To")
                .items(string().controlType(ControlType.EMAIL))
                .description("Email addresses which you want to send to.")
                .required(true),
            array(CC)
                .label("CC")
                .description("Email address which receives a copy.")
                .items(string().controlType(ControlType.EMAIL))
                .maxItems(1000)
                .required(false)
                .advancedOption(true),
            string(SUBJECT)
                .label("Subject")
                .description("Subject of your email.")
                .minLength(1)
                .maxLength(998)
                .required(true),
            string(TEXT)
                .label("Message Body")
                .description("The message you want to send.")
                .minLength(1)
                .controlType(Property.ControlType.RICH_TEXT)
                .required(true),
            string(TYPE)
                .label("Message Type")
                .description("Message type for your content.")
                .options(
                    option("Plain text", "text/plain"),
                    option("HTML", "text/html"))
                .defaultValue("text/plain")
                .required(true),
            array(ATTACHMENTS)
                .label("Attachments")
                .description("A list of attachments you want to include with the email.")
                .items(fileEntry())
                .required(false))
        .help("", "https://docs.bytechef.io/reference/components/sendgrid_v1#send-email")
        .perform(SendgridSendEmailAction::perform);

    private SendgridSendEmailAction() {
    }

    public static Object perform(Parameters inputParameters, Parameters connectionParameters, Context context) {
        List<FileEntry> attachmentFiles = inputParameters.getList(ATTACHMENTS, FileEntry.class);

        List<Map<String, Object>> allAttachments = getAllAttachments(attachmentFiles, context);

        List<Map<String, String>> toList = convertToEmailList(inputParameters.getRequiredList(TO, String.class));
        List<Map<String, String>> ccList = convertToEmailList(inputParameters.getList(CC, String.class, List.of()));

        Map<String, List<Map<String, String>>> itemMap = new HashMap<>();

        itemMap.put(TO, toList);

        if (!ccList.isEmpty()) {
            itemMap.put(CC, ccList);
        }

        List<Map<?, ?>> personalization = new ArrayList<>(List.of(itemMap));

        Map<String, Object> body = new HashMap<>();

        body.put("personalizations", personalization);
        body.put(FROM, Map.of("email", inputParameters.getRequiredString(FROM)));
        body.put(SUBJECT, inputParameters.getRequiredString(SUBJECT));
        body.put(
            "content", List.of(
                Map.of(
                    TYPE, inputParameters.getRequiredString(TYPE),
                    "value", inputParameters.getRequiredString(TEXT))));

        if (!allAttachments.isEmpty()) {
            body.put(ATTACHMENTS, allAttachments);
        }

        return sendEmail(context, body);
    }
}
