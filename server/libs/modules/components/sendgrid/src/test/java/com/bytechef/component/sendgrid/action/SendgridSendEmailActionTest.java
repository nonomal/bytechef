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

import static com.bytechef.component.sendgrid.constant.SendgridConstants.ATTACHMENTS;
import static com.bytechef.component.sendgrid.constant.SendgridConstants.CC;
import static com.bytechef.component.sendgrid.constant.SendgridConstants.FROM;
import static com.bytechef.component.sendgrid.constant.SendgridConstants.SUBJECT;
import static com.bytechef.component.sendgrid.constant.SendgridConstants.TEXT;
import static com.bytechef.component.sendgrid.constant.SendgridConstants.TO;
import static com.bytechef.component.sendgrid.constant.SendgridConstants.TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.mockStatic;

import com.bytechef.component.definition.Context;
import com.bytechef.component.definition.FileEntry;
import com.bytechef.component.definition.Parameters;
import com.bytechef.component.sendgrid.util.SendgridUtils;
import com.bytechef.component.test.definition.MockParametersFactory;
import com.bytechef.component.test.definition.extension.MockContextSetupExtension;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

/**
 * @author Luka Ljubić
 */
@ExtendWith(MockContextSetupExtension.class)
class SendgridSendEmailActionTest {

    private final Parameters mockedParameters = MockParametersFactory.create(
        Map.of(
            FROM, "emailFrom@example.com",
            TO, List.of("to@example.com"),
            CC, List.of("cc@example.com"),
            SUBJECT, "testSubject",
            TEXT, "testText",
            TYPE, "text/plain",
            ATTACHMENTS, new ArrayList<>()));
    @SuppressWarnings("unchecked")
    private final ArgumentCaptor<List<FileEntry>> fileArgumentCaptor = forClass(List.class);
    private final ArgumentCaptor<Context> contextArgumentCaptor = forClass(Context.class);
    @SuppressWarnings("unchecked")
    private final ArgumentCaptor<Map<String, Object>> mapArgumentCaptor = forClass(Map.class);

    @Test
    void testPerform(Context mockedContext) {

        List<Map<String, String>> expectedTo = List.of(Map.of("email", "to@example.com"));
        List<Map<String, String>> expectedCc = List.of(Map.of("email", "cc@example.com"));

        try (MockedStatic<SendgridUtils> sendgridUtilsMockedStatic = mockStatic(SendgridUtils.class)) {

            sendgridUtilsMockedStatic
                .when(() -> SendgridUtils.getAllAttachments(
                    fileArgumentCaptor.capture(), contextArgumentCaptor.capture()))
                .thenReturn(List.of());

            sendgridUtilsMockedStatic
                .when(() -> SendgridUtils.convertToEmailList(List.of("to@example.com")))
                .thenReturn(expectedTo);

            sendgridUtilsMockedStatic
                .when(() -> SendgridUtils.convertToEmailList(List.of("cc@example.com")))
                .thenReturn(expectedCc);

            sendgridUtilsMockedStatic
                .when(() -> SendgridUtils.sendEmail(
                    contextArgumentCaptor.capture(), mapArgumentCaptor.capture()))
                .thenReturn(null);

            Object result = SendgridSendEmailAction.perform(mockedParameters, null, mockedContext);

            assertNull(result);

            assertEquals(mockedContext, contextArgumentCaptor.getAllValues()
                .getFirst());

            Map<String, Object> body = mapArgumentCaptor.getValue();
            assertEquals(List.of(Map.of(TO, expectedTo, CC, expectedCc)), body.get("personalizations"));
            assertEquals(Map.of("email", "emailFrom@example.com"), body.get(FROM));
            assertEquals("testSubject", body.get(SUBJECT));
            assertEquals(List.of(Map.of(TYPE, "text/plain", "value", "testText")), body.get("content"));
        }
    }
}
