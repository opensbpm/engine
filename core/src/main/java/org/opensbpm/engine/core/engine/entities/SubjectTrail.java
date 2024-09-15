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

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.opensbpm.engine.core.model.entities.State;
import org.opensbpm.engine.core.utils.entities.HasId;

@Entity(name = "subjecttrail")
public class SubjectTrail implements HasId, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject")
    private Subject subject;

    /* no backpointer from state; don't map 1:>10K */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "state")
    private State state;

    /* not every database stores millis in timestamp (MySQL); so use long and JVM time instead */
    @Column(name = "lastModified", nullable = false)
    private long lastModified;

    protected SubjectTrail() {
    }

    SubjectTrail(Subject subject, State state) {
        this.subject = subject;
        this.state = state;
        lastModified = System.currentTimeMillis();
    }

    @Override
    public Long getId() {
        return id;
    }

    public Subject getSubject() {
        return subject;
    }

    public State getState() {
        return state;
    }

    public long getLastModified() {
        return lastModified;
    }

    public LocalDateTime getLastModifiedDateTime() {
        return LocalDateTime.ofInstant(new Date(lastModified).toInstant(), ZoneId.systemDefault());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("subject", subject)
                .append("state", state)
                .append("lastModified", lastModified)
                .toString();
    }

}
