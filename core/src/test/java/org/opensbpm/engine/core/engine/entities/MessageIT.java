/*******************************************************************************
 * Copyright (C) 2020 Stefan Sedelmaier
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.opensbpm.engine.core.engine.entities;

import org.junit.Test;
import org.opensbpm.engine.core.junit.EntityDataTestCase;
import org.opensbpm.engine.core.model.entities.ModelVersion;
import org.opensbpm.engine.core.model.entities.ObjectModel;
import org.opensbpm.engine.core.model.entities.ProcessModel;
import org.opensbpm.engine.core.model.entities.SendState;
import org.opensbpm.engine.core.model.entities.SubjectModel;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;

public class MessageIT extends EntityDataTestCase {

    @Test
    public void testInsert() {
        //given
        ProcessModel processModel = new ProcessModel("Process", new ModelVersion(0, 0));
        ObjectModel objectModel = processModel.addObjectModel("Business Object");

        SubjectModel senderSubjectModel = processModel.addServiceSubjectModel("Sender");
        SubjectModel receiverSubjectModel = processModel.addServiceSubjectModel("Receiver");

        SendState sendState = senderSubjectModel.addSendState("Send", receiverSubjectModel, objectModel);
        processModel = entityManager.persist(processModel);

        ProcessInstance processInstance = persistedProcessInstance(processModel);

        //Subject senderSubject = new ServiceSubject(processInstance, senderSubjectModel);
        Subject receiverSubject = new ServiceSubject(processInstance, receiverSubjectModel);

        ObjectInstance objectInstance = new ObjectInstance(objectModel, processInstance);
        Message message = receiverSubject.addMessage(objectModel, senderSubjectModel);

        //when
        receiverSubject = entityManager.persistAndFlush(receiverSubject);

        //then
        Message result = entityManager.find(Message.class, message.getId());
        assertThat(result.getId(), is(notNullValue()));
        assertThat(receiverSubject.getUnconsumedMessages(objectModel), hasSize(1));
    }

    @Test
    public void testUpdateConsumed() {
        //given
        ProcessModel processModel = new ProcessModel("Process", new ModelVersion(0, 0));
        ObjectModel objectModel = processModel.addObjectModel("Business Object");

        SubjectModel senderSubjectModel = processModel.addServiceSubjectModel("Sender");
        SubjectModel receiverSubjectModel = processModel.addServiceSubjectModel("Receiver");

        SendState sendState = senderSubjectModel.addSendState("Send", receiverSubjectModel, objectModel);
        processModel = entityManager.persist(processModel);

        ProcessInstance processInstance = persistedProcessInstance(processModel);

        //Subject senderSubject = new ServiceSubject(processInstance, senderSubjectModel);
        Subject receiverSubject = new ServiceSubject(processInstance, receiverSubjectModel);

        ObjectInstance objectInstance = new ObjectInstance(objectModel, processInstance);
        Message message = receiverSubject.addMessage(objectModel, senderSubjectModel);
        receiverSubject = entityManager.persistAndFlush(receiverSubject);

        //when
        message.setConsumed(true);
        receiverSubject = entityManager.persistAndFlush(receiverSubject);

        //then
        Message result = entityManager.find(Message.class, message.getId());
        assertThat(result.getId(), is(notNullValue()));
        assertThat(result.isConsumed(), is(true));
        assertThat(receiverSubject.getUnconsumedMessages(objectModel), is(empty()));
    }

}
