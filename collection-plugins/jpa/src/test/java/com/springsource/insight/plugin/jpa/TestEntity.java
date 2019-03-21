/**
 * Copyright (c) 2009-2011 VMware, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.springsource.insight.plugin.jpa;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity(name="TestEntity")
@NamedQueries({
    @NamedQuery(name="TestEntity.findEntitiesInRange",
                query="SELECT e FROM TestEntity e WHERE e.creationDate BETWEEN :startTime AND :endTime")
})
public class TestEntity implements Serializable, Cloneable, Comparable<TestEntity> {
    private static final long serialVersionUID = 592907608114565723L;

    private Date    creationDate;
    private Long    id;

    public TestEntity() {
        this(new Date(System.currentTimeMillis()));
    }

    public TestEntity(@SuppressWarnings("hiding") Date creationDate) {
        this.creationDate = creationDate;
    }

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name="id",nullable=false,unique=true)
    public Long getId() {
        return id;
    }

    public void setId(@SuppressWarnings("hiding") Long id) {
        this.id = id;
    }

    @Column(name="creationDate",nullable=false)
    @Temporal(TemporalType.TIMESTAMP)
    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(@SuppressWarnings("hiding") Date creationDate) {
        this.creationDate = creationDate;
    }

    public int compareTo(TestEntity o) {
        if (this == o)
            return 0;
        if (o == null)
            return -1;

        Date    thisDate=this.getCreationDate(), otherDate=o.getCreationDate();
        if (thisDate == null) {
            return (otherDate == null) ? 0 : (+1);
        } else if (otherDate == null) {
            return (-1);
        } else {
            return thisDate.compareTo(otherDate);
        }
    }

    @Override
    public int hashCode() {
        Date    d=getCreationDate();
        return (d == null) ? 0 : d.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;

        return compareTo((TestEntity) obj) == 0;
    }

    @Override
    public TestEntity clone() {
        try {
            TestEntity  cloned=getClass().cast(super.clone());
            Date        clonedDate=cloned.getCreationDate();
            if (clonedDate != null) {
                cloned.setCreationDate((Date) clonedDate.clone());
            }
            
            return cloned;
        } catch(CloneNotSupportedException e) {
            throw new IllegalStateException("Failed to clone " + toString() + ": " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return String.valueOf(getId()) + "@" + String.valueOf(getCreationDate());
    }
}
